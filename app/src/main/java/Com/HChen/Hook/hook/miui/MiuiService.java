/*
 * This file is part of AppRetentionHook.

 * AppRetentionHook is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.

 * Author of this project: 焕晨HChen
 * You can reference the code of this project,
 * but as a project developer, I hope you can indicate it when referencing.

 * Copyright (C) 2023-2024 AppRetentionHook Contributions
 */
package Com.HChen.Hook.hook.miui;

import static Com.HChen.Hook.param.classpath.MiuiName.Build;
import static Com.HChen.Hook.param.classpath.MiuiName.CameraBooster;
import static Com.HChen.Hook.param.classpath.MiuiName.DeviceLevelUtils;
import static Com.HChen.Hook.param.classpath.MiuiName.GameMemoryCleaner;
import static Com.HChen.Hook.param.classpath.MiuiName.GameProcessCompactor;
import static Com.HChen.Hook.param.classpath.MiuiName.GameProcessKiller;
import static Com.HChen.Hook.param.classpath.MiuiName.MemoryStandardProcessControl;
import static Com.HChen.Hook.param.classpath.MiuiName.PeriodicCleanerService;
import static Com.HChen.Hook.param.classpath.MiuiName.PreloadAppControllerImpl;
import static Com.HChen.Hook.param.classpath.MiuiName.PressureStateSettings;
import static Com.HChen.Hook.param.classpath.MiuiName.ProcessKiller;
import static Com.HChen.Hook.param.classpath.MiuiName.ProcessKillerIdler;
import static Com.HChen.Hook.param.classpath.MiuiName.ProcessMemoryCleaner;
import static Com.HChen.Hook.param.classpath.MiuiName.ProcessPowerCleaner;
import static Com.HChen.Hook.param.classpath.MiuiName.ProcessUtils;
import static Com.HChen.Hook.param.classpath.MiuiName.ScoutDisplayMemoryManager;
import static Com.HChen.Hook.param.classpath.MiuiName.ScoutHelper;
import static Com.HChen.Hook.param.classpath.MiuiName.SystemPressureController;
import static Com.HChen.Hook.param.classpath.SystemName.ActivityManagerService;
import static Com.HChen.Hook.param.classpath.SystemName.ProcessRecord;
import static Com.HChen.Hook.param.name.MiuiValue.boostCameraIfNeeded;
import static Com.HChen.Hook.param.name.MiuiValue.cleanUpMemory;
import static Com.HChen.Hook.param.name.MiuiValue.doClean;
import static Com.HChen.Hook.param.name.MiuiValue.getDeviceLevelForRAM;
import static Com.HChen.Hook.param.name.MiuiValue.getGameOomEnable;
import static Com.HChen.Hook.param.name.MiuiValue.handleAutoLockOff;
import static Com.HChen.Hook.param.name.MiuiValue.handleKillAll;
import static Com.HChen.Hook.param.name.MiuiValue.handleKillApp;
import static Com.HChen.Hook.param.name.MiuiValue.handleScreenOff;
import static Com.HChen.Hook.param.name.MiuiValue.handleThermalKillProc;
import static Com.HChen.Hook.param.name.MiuiValue.init;
import static Com.HChen.Hook.param.name.MiuiValue.isEnableScoutMemory;
import static Com.HChen.Hook.param.name.MiuiValue.isLowMemory;
import static Com.HChen.Hook.param.name.MiuiValue.isMiuiLiteVersion;
import static Com.HChen.Hook.param.name.MiuiValue.killApplication;
import static Com.HChen.Hook.param.name.MiuiValue.killPackage;
import static Com.HChen.Hook.param.name.MiuiValue.killProcess;
import static Com.HChen.Hook.param.name.MiuiValue.killProcessByMinAdj;
import static Com.HChen.Hook.param.name.MiuiValue.nStartPressureMonitor;
import static Com.HChen.Hook.param.name.MiuiValue.onStartJob;
import static Com.HChen.Hook.param.name.MiuiValue.reclaimMemoryForGameIfNeed;
import static Com.HChen.Hook.param.name.MiuiValue.shouldSkip;
import static Com.HChen.Hook.param.name.MiuiValue.updateScreenState;

import android.app.job.JobParameters;
import android.content.Context;

import Com.HChen.Hook.mode.Hook;
import dalvik.system.PathClassLoader;
import de.robv.android.xposed.XC_MethodHook;

public class MiuiService extends Hook {

    public static String name = "MiuiService";

    @Override
    public void init() {
        /*MiuiLite来自HyperCeiler*/
        PathClassLoader pathClassLoader = pathClassLoader("/system/framework/MiuiBooster.jar",
            loadPackageParam.classLoader);
        PathClassLoader pathClassLoader1 = pathClassLoader("/system_ext/framework/MiuiBooster.jar",
            loadPackageParam.classLoader);
        if (pathClassLoader != null) {
            hookDeviceLevelUtils(pathClassLoader);
        }
        if (pathClassLoader1 != null) {
            hookDeviceLevelUtils(pathClassLoader1);
        }

        findAndHookMethod(Build,
            isMiuiLiteVersion,
            new HookAction(name) {
                @Override
                protected void before(MethodHookParam param) {
                    param.setResult(false);
                }
            }
        );

        /*谎报内存等级*/
        findAndHookMethod(Build,
            getDeviceLevelForRAM, int.class,
            new HookAction(name) {
                @Override
                protected void before(MethodHookParam param) {
                    param.setResult(3);
                }
            }
        );

        // 谎称非低内存
        findAndHookMethod(ProcessUtils,
            isLowMemory,
            new HookAction(name) {
                @Override
                protected void before(MethodHookParam param) {
                    param.setResult(false);
                }
            }
        );

        /*设置禁止Scout功能*/
        findAndHookMethod(ScoutDisplayMemoryManager,
            isEnableScoutMemory, new HookAction(name) {
                @Override
                protected void before(MethodHookParam param) {
                    param.setResult(false);
                }
            }
        );

        /*关闭一堆Scout的功能*/
        setStaticBoolean(findClassIfExists(ScoutHelper), "ENABLED_SCOUT", false);
        setStaticBoolean(findClassIfExists(ScoutHelper), "ENABLED_SCOUT_DEBUG", false);
        setStaticBoolean(findClassIfExists(ScoutHelper), "BINDER_FULL_KILL_PROC", false);

        // 跳过游戏进程kill和压缩，待测试
        findAndHookMethod(GameProcessKiller,
            shouldSkip, ProcessRecord,
            new HookAction(name) {
                @Override
                protected void before(MethodHookParam param) {
                    param.setResult(true);
                }
            }
        );

        findAndHookMethod(GameProcessCompactor,
            shouldSkip, ProcessRecord,
            new HookAction(name) {
                @Override
                protected void before(MethodHookParam param) {
                    param.setResult(true);
                }
            }
        );

        /*禁止在开游戏时回收内存*/
        findAndHookMethod(GameMemoryCleaner,
            reclaimMemoryForGameIfNeed, String.class,
            new HookAction(name) {
                @Override
                protected void before(MethodHookParam param) {
                    param.setResult(null);
                }

            }
        );

        /*关闭内存回收功能，寄生于游戏清理*/
        setStaticBoolean(findClassIfExists(GameMemoryCleaner), "IS_MEMORY_CLEAN_ENABLED", false);

        /*禁用预加载APP，我对此功能存怀疑态度*/
        setStaticBoolean(findClassIfExists(PreloadAppControllerImpl), "ENABLE", false);

        /*findClassIfExists(PeriodicCleanerService).getDeclaredMethod(doClean, int.class, int.class, int.class, String.class);*/
        // checkDeclaredMethod(PeriodicCleanerService, doClean, int.class, int.class, int.class, String.class);
        if (findClassIfExists(PeriodicCleanerService) != null) {
            /*禁用PeriodicCleaner的clean*/
            findAndHookMethod(PeriodicCleanerService,
                doClean, int.class, int.class, int.class, String.class,
                new HookAction(name) {
                    @Override
                    protected void before(MethodHookParam param) {
                        param.setResult(null);
                    }
                }
            );

            /*禁止息屏清理后台*/
            findAndHookMethod(PeriodicCleanerService,
                handleScreenOff, new HookAction(name) {
                    @Override
                    protected void before(MethodHookParam param) {
                        param.setResult(null);
                    }
                }
            );

            /*禁用PeriodicCleaner*/
            findAndHookConstructor(PeriodicCleanerService,
                Context.class,
                new HookAction(name) {
                    @Override
                    protected void after(MethodHookParam param) {
                        setDeclaredField(param, "mEnable", false);
                    }
                }
            );
        } else {
            /*安卓14的东西*/
            findAndHookConstructor(MemoryStandardProcessControl,
                new HookAction(name) {
                    @Override
                    protected void after(MethodHookParam param) {
                        setBoolean(param.thisObject, "mEnable", false);
                    }
                }
            );

            /*虽然上面设置false下面就不会执行了，但是多层保障*/
            findAndHookMethod(MemoryStandardProcessControl,
                killProcess, boolean.class,
                new HookAction(name) {
                    @Override
                    protected void before(MethodHookParam param) {
                        param.setResult(null);
                    }
                }
            );
        }

        /*禁止清理内存*/
        findAndHookMethod(SystemPressureController,
            cleanUpMemory, long.class,
            new HookAction(name) {
                @Override
                protected void before(MethodHookParam param) {
                    param.setResult(null);
                }
            }
        );

        /*禁用小米回收和游戏OOM*/
        findAndHookMethod(SystemPressureController, init,
            Context.class, ActivityManagerService,
            new HookAction(name) {
                @Override
                protected void after(MethodHookParam param) {
                    // setBoolean(param.thisObject, "IS_ENABLE_RECLAIM", false);
                    setBoolean(param.thisObject, "mGameOomEnable", false);
                }
            }
        );
        setStaticBoolean(findClassIfExists(SystemPressureController), "IS_ENABLE_RECLAIM", false);

        findAndHookMethod(SystemPressureController,
            getGameOomEnable,
            new HookAction(name) {
                @Override
                protected void before(MethodHookParam param) {
                    param.setResult(false);
                }
            }
        );

        /*禁止启动内存压力检查工具*/
        hookAllMethods(SystemPressureController,
            nStartPressureMonitor,
            new HookAction(name) {
                @Override
                protected void before(MethodHookParam param) {
                    param.setResult(null);
                }
            }
        );

        /*禁止跟随屏幕关闭/启动内存压力检测工具*/
        findAndHookMethod(SystemPressureController,
            updateScreenState,
            boolean.class,
            new HookAction(name) {
                @Override
                protected void before(MethodHookParam param) {
                    param.setResult(null);
                }
            }
        );

        /*禁止启动定时任务ProcessKillerIdler*/
        findAndHookMethod(ProcessKillerIdler,
            onStartJob, JobParameters.class,
            new HookAction(name) {
                @Override
                protected void before(MethodHookParam param) {
                    param.setResult(false);
                }
            }
        );

        /*禁止息屏清理内存*/
        hookAllMethods(ProcessPowerCleaner,
            handleAutoLockOff,
            new HookAction(name) {
                @Override
                protected void before(MethodHookParam param) {
                    param.setResult(null);
                }
            }
        );

        /*禁止温度过高清理*/
        hookAllMethods(ProcessPowerCleaner,
            handleThermalKillProc,
            new HookAction(name) {
                @Override
                protected void before(MethodHookParam param) {
                    param.setResult(null);
                }
            }
        );

        /*禁止kill进程*/
        hookAllMethods(ProcessPowerCleaner,
            handleKillAll,
            new HookAction(name) {
                @Override
                protected void before(MethodHookParam param) {
                    param.setResult(null);
                }
            }
        );

        hookAllMethods(ProcessPowerCleaner,
            handleKillApp, new HookAction(name) {
                @Override
                protected void before(MethodHookParam param) {
                    param.setResult(null);
                }
            }
        );

        /*禁止ProcessMemoryCleaner的killPackage*/
        hookAllMethods(ProcessMemoryCleaner,
            killPackage, new HookAction(name) {
                @Override
                protected void before(MethodHookParam param) {
                    param.setResult(0L);
                }
            }
        );

        /*禁止kill，这个是最终的kill方法，专用于释放内存*/
        hookAllMethods(ProcessMemoryCleaner,
            killProcess,
            new HookAction(name) {
                @Override
                protected void before(MethodHookParam param) {
                    param.setResult(0L);
                }
            }
        );

        /*禁止ProcessMemoryCleaner的killProcessByMinAdj*/
        hookAllMethods(ProcessMemoryCleaner,
            killProcessByMinAdj, new HookAction(name) {
                @Override
                protected void before(MethodHookParam param) {
                    param.setResult(null);
                }
            }
        );

        /*谎称清理成功*/
        hookAllMethods(ProcessMemoryCleaner,
            cleanUpMemory,
            new HookAction(name) {
                @Override
                protected void before(MethodHookParam param) {
                    param.setResult(true);
                }
            }
        );

        try {
            checkDeclaredMethod(CameraBooster, boostCameraIfNeeded, long.class, boolean.class);
            /*findClassIfExists(CameraBooster).getDeclaredMethod(boostCameraIfNeeded, long.class, boolean.class);*/
            /*禁止相机kill*/
            findAndHookMethod(CameraBooster,
                boostCameraIfNeeded, long.class, boolean.class,
                new HookAction(name) {
                    @Override
                    protected void before(MethodHookParam param) {
                        param.setResult(null);
                    }
                }
            );
        } catch (NoSuchMethodException f) {
            /*安卓14的逻辑，相机kill的最终调用，当然还有进程检查也调用了*/
            hookAllMethods(ProcessKiller,
                killApplication,
                new HookAction(name) {
                    @Override
                    protected void before(MethodHookParam param) {
                        param.setResult(false);
                    }
                }
            );
        }

        /*禁用spc*/
        setStaticBoolean(findClassIfExists(PressureStateSettings), "PROCESS_CLEANER_ENABLED", false);
    }

    private void hookDeviceLevelUtils(PathClassLoader pathClassLoader) {
        findAndHookMethod(DeviceLevelUtils, pathClassLoader,
            isMiuiLiteVersion,
            new HookAction(name) {
                @Override
                protected void before(MethodHookParam param) {
                    param.setResult(false);
                }
            }
        );

        findAndHookMethod(DeviceLevelUtils, pathClassLoader,
            getDeviceLevelForRAM,
            new HookAction(name) {
                @Override
                protected void before(XC_MethodHook.MethodHookParam param) {
                    param.setResult(3);
                }
            }
        );
    }
}
