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
package com.hchen.appretention.hook.miui;

import static com.hchen.appretention.param.classpath.Miui.Build;
import static com.hchen.appretention.param.classpath.Miui.CameraBooster;
import static com.hchen.appretention.param.classpath.Miui.DeviceLevelUtils;
import static com.hchen.appretention.param.classpath.Miui.GameMemoryCleaner;
import static com.hchen.appretention.param.classpath.Miui.GameProcessCompactor;
import static com.hchen.appretention.param.classpath.Miui.GameProcessKiller;
import static com.hchen.appretention.param.classpath.Miui.MemoryStandardProcessControl;
import static com.hchen.appretention.param.classpath.Miui.PeriodicCleanerService;
import static com.hchen.appretention.param.classpath.Miui.PreloadAppControllerImpl;
import static com.hchen.appretention.param.classpath.Miui.PressureStateSettings;
import static com.hchen.appretention.param.classpath.Miui.ProcessKiller;
import static com.hchen.appretention.param.classpath.Miui.ProcessKillerIdler;
import static com.hchen.appretention.param.classpath.Miui.ProcessMemoryCleaner;
import static com.hchen.appretention.param.classpath.Miui.ProcessPowerCleaner;
import static com.hchen.appretention.param.classpath.Miui.ProcessUtils;
import static com.hchen.appretention.param.classpath.Miui.ScoutDisplayMemoryManager;
import static com.hchen.appretention.param.classpath.Miui.ScoutHelper;
import static com.hchen.appretention.param.classpath.Miui.SystemPressureController;
import static com.hchen.appretention.param.classpath.System.ActivityManagerService;
import static com.hchen.appretention.param.classpath.System.ProcessRecord;
import static com.hchen.appretention.param.field.Miui.BINDER_FULL_KILL_PROC;
import static com.hchen.appretention.param.field.Miui.ENABLE;
import static com.hchen.appretention.param.field.Miui.ENABLED_SCOUT;
import static com.hchen.appretention.param.field.Miui.ENABLED_SCOUT_DEBUG;
import static com.hchen.appretention.param.field.Miui.IS_ENABLE_RECLAIM;
import static com.hchen.appretention.param.field.Miui.IS_MEMORY_CLEAN_ENABLED;
import static com.hchen.appretention.param.field.Miui.PROCESS_CLEANER_ENABLED;
import static com.hchen.appretention.param.field.Miui.mEnable;
import static com.hchen.appretention.param.field.Miui.mGameOomEnable;
import static com.hchen.appretention.param.method.Miui.boostCameraIfNeeded;
import static com.hchen.appretention.param.method.Miui.cleanUpMemory;
import static com.hchen.appretention.param.method.Miui.doClean;
import static com.hchen.appretention.param.method.Miui.getDeviceLevelForRAM;
import static com.hchen.appretention.param.method.Miui.getGameOomEnable;
import static com.hchen.appretention.param.method.Miui.handleAutoLockOff;
import static com.hchen.appretention.param.method.Miui.handleKillAll;
import static com.hchen.appretention.param.method.Miui.handleKillApp;
import static com.hchen.appretention.param.method.Miui.handleScreenOff;
import static com.hchen.appretention.param.method.Miui.handleThermalKillProc;
import static com.hchen.appretention.param.method.Miui.init;
import static com.hchen.appretention.param.method.Miui.isEnableScoutMemory;
import static com.hchen.appretention.param.method.Miui.isLowMemory;
import static com.hchen.appretention.param.method.Miui.isMiuiLiteVersion;
import static com.hchen.appretention.param.method.Miui.killApplication;
import static com.hchen.appretention.param.method.Miui.killPackage;
import static com.hchen.appretention.param.method.Miui.killProcess;
import static com.hchen.appretention.param.method.Miui.killProcessByMinAdj;
import static com.hchen.appretention.param.method.Miui.nStartPressureMonitor;
import static com.hchen.appretention.param.method.Miui.onStartJob;
import static com.hchen.appretention.param.method.Miui.reclaimMemoryForGameIfNeed;
import static com.hchen.appretention.param.method.Miui.shouldSkip;
import static com.hchen.appretention.param.method.Miui.updateScreenState;

import android.app.job.JobParameters;
import android.content.Context;

import com.hchen.appretention.mode.Hook;

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
        setStaticBoolean(findClassIfExists(ScoutHelper), ENABLED_SCOUT, false);
        setStaticBoolean(findClassIfExists(ScoutHelper), ENABLED_SCOUT_DEBUG, false);
        setStaticBoolean(findClassIfExists(ScoutHelper), BINDER_FULL_KILL_PROC, false);

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
        setStaticBoolean(findClassIfExists(GameMemoryCleaner), IS_MEMORY_CLEAN_ENABLED, false);

        /*禁用预加载APP，我对此功能存怀疑态度*/
        setStaticBoolean(findClassIfExists(PreloadAppControllerImpl), ENABLE, false);

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
                        setDeclaredField(param, mEnable, false);
                    }
                }
            );
        } else {
            /*安卓14的东西*/
            findAndHookConstructor(MemoryStandardProcessControl,
                new HookAction(name) {
                    @Override
                    protected void after(MethodHookParam param) {
                        setBoolean(param.thisObject, mEnable, false);
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
                    setBoolean(param.thisObject, mGameOomEnable, false);
                }
            }
        );
        setStaticBoolean(findClassIfExists(SystemPressureController), IS_ENABLE_RECLAIM, false);

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
        setStaticBoolean(findClassIfExists(PressureStateSettings), PROCESS_CLEANER_ENABLED, false);
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
