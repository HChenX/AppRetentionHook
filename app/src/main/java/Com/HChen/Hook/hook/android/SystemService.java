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
package Com.HChen.Hook.hook.android;

import static Com.HChen.Hook.param.classpath.SystemName.ActivityManagerConstants;
import static Com.HChen.Hook.param.classpath.SystemName.ActivityManagerService;
import static Com.HChen.Hook.param.classpath.SystemName.ActivityManagerServiceStub;
import static Com.HChen.Hook.param.classpath.SystemName.ActivityManagerShellCommand;
import static Com.HChen.Hook.param.classpath.SystemName.AmsExtImpl;
import static Com.HChen.Hook.param.classpath.SystemName.AppProfiler;
import static Com.HChen.Hook.param.classpath.SystemName.LowMemDetector;
import static Com.HChen.Hook.param.classpath.SystemName.OomAdjuster;
import static Com.HChen.Hook.param.classpath.SystemName.PhantomProcessList;
import static Com.HChen.Hook.param.classpath.SystemName.ProcessList$ImperceptibleKillRunner;
import static Com.HChen.Hook.param.classpath.SystemName.ProcessStatsService;
import static Com.HChen.Hook.param.classpath.SystemName.RecentTasks;
import static Com.HChen.Hook.param.name.SystemValue.checkExcessivePowerUsageLPr;
import static Com.HChen.Hook.param.name.SystemValue.doLowMemReportIfNeededLocked;
import static Com.HChen.Hook.param.name.SystemValue.getDefaultMaxCachedProcesses;
import static Com.HChen.Hook.param.name.SystemValue.getLastMemoryLevelLocked;
import static Com.HChen.Hook.param.name.SystemValue.getMemFactor;
import static Com.HChen.Hook.param.name.SystemValue.getMemFactorLocked;
import static Com.HChen.Hook.param.name.SystemValue.getOrCreatePhantomProcessIfNeededLocked;
import static Com.HChen.Hook.param.name.SystemValue.getOverrideMaxCachedProcesses;
import static Com.HChen.Hook.param.name.SystemValue.handleDeviceIdle;
import static Com.HChen.Hook.param.name.SystemValue.isAvailable;
import static Com.HChen.Hook.param.name.SystemValue.isInVisibleRange;
import static Com.HChen.Hook.param.name.SystemValue.isLastMemoryLevelNormal;
import static Com.HChen.Hook.param.name.SystemValue.killPids;
import static Com.HChen.Hook.param.name.SystemValue.killProcessesBelowAdj;
import static Com.HChen.Hook.param.name.SystemValue.onSystemReady;
import static Com.HChen.Hook.param.name.SystemValue.performIdleMaintenance;
import static Com.HChen.Hook.param.name.SystemValue.pruneStaleProcessesLocked;
import static Com.HChen.Hook.param.name.SystemValue.runKillAll;
import static Com.HChen.Hook.param.name.SystemValue.shouldKillExcessiveProcesses;
import static Com.HChen.Hook.param.name.SystemValue.trimInactiveRecentTasks;
import static Com.HChen.Hook.param.name.SystemValue.trimPhantomProcessesIfNecessary;
import static Com.HChen.Hook.param.name.SystemValue.updateAndTrimProcessLSP;
import static Com.HChen.Hook.param.name.SystemValue.updateMaxCachedProcesses;

import android.content.Context;
import android.os.Handler;
import android.provider.Settings;

import Com.HChen.Hook.mode.Hook;
import dalvik.system.PathClassLoader;

public class SystemService extends Hook {
    public static String name = "SystemService";

    @Override
    public void init() {
        PathClassLoader pathClassLoader = pathClassLoader("/system/framework/mediatek-services.jar",
            loadPackageParam.classLoader);
        if (pathClassLoader != null) {
            /*MTK快霸*/
            findAndHookMethod(AmsExtImpl, pathClassLoader,
                onSystemReady, Context.class,
                new HookAction(name) {
                    @Override
                    protected void before(MethodHookParam param) {
                        Context context = (Context) param.args[0];
                        Settings.System.putInt(context.getContentResolver(), "setting.duraspeed.enabled", 0);
                        Settings.Global.putInt(context.getContentResolver(), "setting.duraspeed.enabled", 0);
                    }
                }
            );

            findAndHookConstructor(AmsExtImpl, pathClassLoader,
                new HookAction(name) {
                    @Override
                    protected void after(MethodHookParam param) {
                        setBoolean(param.thisObject, "isDuraSpeedSupport", false);
                    }
                }
            );
        }

        /*设备空闲清理？*/
        findAndHookMethod(ProcessList$ImperceptibleKillRunner,
            handleDeviceIdle,
            new HookAction(name) {
                @Override
                protected void before(MethodHookParam param) {
                    param.setResult(null);
                }
            }
        );

        /*禁止清理过时虚幻进程*/
        findAndHookMethod(PhantomProcessList,
            pruneStaleProcessesLocked,
            new HookAction(name) {
                @Override
                protected void before(MethodHookParam param) {
                    param.setResult(null);
                }
            }
        );

        // 拒绝创建虚幻进程记录
        findAndHookMethod(PhantomProcessList,
            getOrCreatePhantomProcessIfNeededLocked,
            String.class, int.class, int.class, boolean.class,
            new HookAction(name) {
                @Override
                protected void before(MethodHookParam param) {
                    param.setResult(null);
                }
            }
        );

        /*禁止修剪虚幻进程*/
        hookAllMethods(PhantomProcessList,
            trimPhantomProcessesIfNecessary,
            new HookAction(name) {
                @Override
                protected void before(MethodHookParam param) {
                    param.setResult(null);
                }
            }
        );

        /*cpu超时false*/
        hookAllMethods(ActivityManagerService,
            checkExcessivePowerUsageLPr,
            new HookAction(name) {
                @Override
                protected void before(MethodHookParam param) {
                    param.setResult(false);
                }
            }
        );

        /*禁用killPids根据adj计算最差类型pid并kill
        此方法根据adj进行kill，kill的主要是缓存和adj500的进程，可以禁止它kill.*/
        findAndHookMethod(ActivityManagerService,
            killPids, int[].class, String.class, boolean.class,
            new HookAction(name) {
                @Override
                protected void before(MethodHookParam param) {
                    // if ("Free memory".equals(param.args[1])) {
                    param.setResult(true);
                    // }
                }
            }
        );

        /*禁止杀死adj低的进程*/
        findAndHookMethod(ActivityManagerService,
            killProcessesBelowAdj, int.class, String.class,
            new HookAction(name) {
                @Override
                protected void before(MethodHookParam param) {
                    param.setResult(true);
                }
            }
        );

        /*禁止空闲清理，空闲维护*/
        findAndHookMethod(ActivityManagerService,
            performIdleMaintenance, new HookAction(name) {
                @Override
                protected void before(MethodHookParam param) {
                    param.setResult(null);
                }
            }
        );

        /*报告内存压力低*/
        findAndHookMethod(LowMemDetector,
            getMemFactor, new HookAction(name) {
                @Override
                protected void before(MethodHookParam param) {
                    param.setResult(0);
                }
            }
        );

        // 强制使用本工具获取压力
        findAndHookMethod(LowMemDetector,
            isAvailable,
            new HookAction(name) {
                @Override
                protected void before(MethodHookParam param) {
                    param.setResult(true);
                }
            }
        );

        /*谎报内存压力正常*/
        findAndHookMethod(ProcessStatsService,
            getMemFactorLocked,
            new HookAction(name) {
                @Override
                protected void before(MethodHookParam param) {
                    param.setResult(0);
                }
            }
        );

        /*禁止报告低内存*/
        hookAllMethods(AppProfiler,
            doLowMemReportIfNeededLocked, new HookAction(name) {
                @Override
                protected void before(MethodHookParam param) {
                    param.setResult(null);
                }
            }
        );

        /*内存等级*/
        findAndHookMethod(AppProfiler,
            getLastMemoryLevelLocked,
            new HookAction(name) {
                @Override
                protected void before(MethodHookParam param) {
                    param.setResult(0);
                }
            }
        );

        // 内存正常
        findAndHookMethod(AppProfiler,
            isLastMemoryLevelNormal,
            new HookAction(name) {
                @Override
                protected void before(MethodHookParam param) {
                    param.setResult(true);
                }
            }
        );

        /*禁止使用命令停止全部活动*/
        hookAllMethods(ActivityManagerShellCommand,
            runKillAll,
            new HookAction(name) {
                @Override
                protected void before(MethodHookParam param) {
                    param.setResult(null);
                }
            }
        );

        /*禁止检查是否应该停止活动*/
        hookAllMethods(OomAdjuster,
            shouldKillExcessiveProcesses,
            new HookAction(name) {
                @Override
                protected void before(MethodHookParam param) {
                    param.setResult(false);
                }
            }
        );

        /*禁止修剪进程*/
        hookAllMethods(OomAdjuster,
            updateAndTrimProcessLSP,
            new HookAction(name) {
                @Override
                protected void before(MethodHookParam param) {
                    param.args[2] = 0L;
                }
            }
        );

        /*用来防止频繁log*/
        hookAllConstructors(OomAdjuster,
            new HookAction(name) {
                @Override
                protected void after(MethodHookParam param) {
                    setObject(param.thisObject, "mNextNoKillDebugMessageTime", Long.MAX_VALUE);
                }
            }
        );

        /*禁止修剪最近任务卡片*/
        hookAllMethods(RecentTasks,
            trimInactiveRecentTasks,
            new HookAction(name) {
                @Override
                protected void before(MethodHookParam param) {
                    param.setResult(null);
                }
            }
        );

        /*设置最近任务可见*/
        hookAllMethods(RecentTasks,
            isInVisibleRange,
            new HookAction(name) {
                @Override
                protected void before(MethodHookParam param) {
                    param.args[2] = 0;
                }
            }
        );

        try {
            /*findClassIfExists(ActivityManagerConstants).getDeclaredMethod(getDefaultMaxCachedProcesses);*/
            checkDeclaredMethod(ActivityManagerConstants, getDefaultMaxCachedProcesses);
            /*设置最大进程限制*/
            findAndHookMethod(ActivityManagerConstants,
                getDefaultMaxCachedProcesses,
                new HookAction(name) {
                    @Override
                    protected void before(MethodHookParam param) {
                        param.setResult(Integer.MAX_VALUE);
                    }
                }
            );
        } catch (NoSuchMethodException e) {
            /*安卓14*/
            findAndHookMethod(ActivityManagerServiceStub,
                getDefaultMaxCachedProcesses,
                new HookAction(name) {
                    @Override
                    protected void before(MethodHookParam param) {
                        param.setResult(Integer.MAX_VALUE);
                    }
                }
            );
        }

        /*阻止更新最大进程限制*/
        findAndHookMethod(ActivityManagerConstants,
            updateMaxCachedProcesses,
            new HookAction(name) {
                @Override
                protected void before(MethodHookParam param) {
                    param.setResult(null);
                }
            }
        );

        /*设置最大幻影进程数量*/
        /*禁止kill受限的缓存*/
        findAndHookConstructor(ActivityManagerConstants, Context.class,
            findClassIfExists(ActivityManagerService), Handler.class,
            new HookAction(name) {
                @Override
                protected void after(MethodHookParam param) {
                    setInt(param.thisObject, "MAX_PHANTOM_PROCESSES", Integer.MAX_VALUE);
                    // 清理后台受限且已经idle的内存是合理的
                    // setBoolean(param.thisObject, "mKillBgRestrictedAndCachedIdle", false);
                    /*似乎是高通的东西？*/
                    // setBoolean(param.thisObject, "USE_TRIM_SETTINGS", false);
                    // setBoolean(param.thisObject, "PROACTIVE_KILLS_ENABLED", false);
                }
            }
        );

        setStaticBoolean(findClassIfExists(ActivityManagerConstants), "PROACTIVE_KILLS_ENABLED", false);
        setStaticBoolean(findClassIfExists(ActivityManagerConstants), "USE_TRIM_SETTINGS", false);
        setStaticInt(findClassIfExists(ActivityManagerConstants), "DEFAULT_MAX_PHANTOM_PROCESSES", Integer.MAX_VALUE);

        /*Hook获取更合理*/
        findAndHookMethod(ActivityManagerConstants,
            getOverrideMaxCachedProcesses,
            new HookAction(name) {
                @Override
                protected void before(MethodHookParam param) {
                    param.setResult(Integer.MAX_VALUE);
                }
            }
        );
    }
}
