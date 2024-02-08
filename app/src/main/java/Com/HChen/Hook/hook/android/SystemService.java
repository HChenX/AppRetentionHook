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
import static Com.HChen.Hook.param.classpath.SystemName.LowMemDetector;
import static Com.HChen.Hook.param.classpath.SystemName.OomAdjuster;
import static Com.HChen.Hook.param.classpath.SystemName.PhantomProcessList;
import static Com.HChen.Hook.param.classpath.SystemName.ProcessList;
import static Com.HChen.Hook.param.classpath.SystemName.ProcessList$ImperceptibleKillRunner;
import static Com.HChen.Hook.param.classpath.SystemName.ProcessRecord;
import static Com.HChen.Hook.param.classpath.SystemName.ProcessStateRecord;
import static Com.HChen.Hook.param.classpath.SystemName.ProcessStatsService;
import static Com.HChen.Hook.param.classpath.SystemName.RecentTasks;
import static Com.HChen.Hook.param.name.SystemValue.checkExcessivePowerUsageLPr;
import static Com.HChen.Hook.param.name.SystemValue.getDefaultMaxCachedProcesses;
import static Com.HChen.Hook.param.name.SystemValue.getMemFactor;
import static Com.HChen.Hook.param.name.SystemValue.getMemFactorLocked;
import static Com.HChen.Hook.param.name.SystemValue.handleDeviceIdle;
import static Com.HChen.Hook.param.name.SystemValue.isInVisibleRange;
import static Com.HChen.Hook.param.name.SystemValue.killAppIfBgRestrictedAndCachedIdleLocked;
import static Com.HChen.Hook.param.name.SystemValue.killLocked;
import static Com.HChen.Hook.param.name.SystemValue.killPids;
import static Com.HChen.Hook.param.name.SystemValue.killProcessesBelowAdj;
import static Com.HChen.Hook.param.name.SystemValue.performIdleMaintenance;
import static Com.HChen.Hook.param.name.SystemValue.pruneStaleProcessesLocked;
import static Com.HChen.Hook.param.name.SystemValue.runKillAll;
import static Com.HChen.Hook.param.name.SystemValue.setOverrideMaxCachedProcesses;
import static Com.HChen.Hook.param.name.SystemValue.shouldKillExcessiveProcesses;
import static Com.HChen.Hook.param.name.SystemValue.shouldNotKillOnBgRestrictedAndIdle;
import static Com.HChen.Hook.param.name.SystemValue.trimInactiveRecentTasks;
import static Com.HChen.Hook.param.name.SystemValue.trimPhantomProcessesIfNecessary;
import static Com.HChen.Hook.param.name.SystemValue.updateAndTrimProcessLSP;
import static Com.HChen.Hook.param.name.SystemValue.updateBackgroundRestrictedForUidPackageLocked;
import static Com.HChen.Hook.param.name.SystemValue.updateMaxCachedProcesses;

import android.content.Context;
import android.os.Handler;

import Com.HChen.Hook.mode.Hook;

public class SystemService extends Hook {
    public static String name = "SystemService";

    @Override
    public void init() {
        /*关闭Cpu超时检查*/
       /* hookAllMethods(ActivityManagerService,
            checkExcessivePowerUsage,
            new HookAction() {
                @Override
                protected void before(MethodHookParam param) {
                    param.setResult(null);
                }

            }
        );//*/

        /*防止kill冻结的应用*/
        /*findAndHookMethod(CachedAppOptimizer,
            getBinderFreezeInfo, int.class,
            new HookAction() {
                @Override
                protected void after(MethodHookParam param) {
                    int end = (int) param.getResult();
                    if ((end & 1) != 0) {
                        param.setResult(5);
                    }
                    logSI(getBinderFreezeInfo, "end: " + end + " in: " + param.args[0]);
                }
            }
        );*/

        /*findAndHookMethod(CachedAppOptimizer,
            unfreezeAppInternalLSP, new HookAction() {
                @Override
                protected void before(MethodHookParam param) {

                }
            }
        );*/

        /*设备空闲清理？*/
        findAndHookMethod(ProcessList$ImperceptibleKillRunner,
            handleDeviceIdle,
            new HookAction() {
                @Override
                protected void before(MethodHookParam param) {
                    param.setResult(null);
                }

                @Override
                public String hookLog() {
                    return name;
                }
            }
        );

        /*防止各种原因的kill*/
        findAndHookMethod(ProcessRecord, killLocked,
            String.class, String.class, int.class, int.class, boolean.class,
            new HookAction() {
                @Override
                public String hookLog() {
                    return name;
                }

                @Override
                protected void before(MethodHookParam param) {
                    boolean hook = false;
                    /*"isolated not needed"被隔离的进程只有变成空进程时才会被清理，我认为是合理的*/
                    switch ((String) param.args[0]) {
                        case "Unable to query binder frozen stats",
                            "Unable to unfreeze", "Unable to freeze binder interface",
                            "start timeout", "crash", "bg anr", "skip anr", "anr",
                            "swap low and too many cached" -> {
                            param.setResult(null);
                            hook = true;
                        }
                    }
                    /*"depends on provider",内容提供者被杀导致的调用方被杀，
                     * 这是软件作者的bug，不应该在用户端禁止。
                     * reason.contains("depends on provider")*/
                    String reason = (String) param.args[0];
                    if (reason.contains("scheduleCrash for")) {
                        param.setResult(null);
                        hook = true;
                    }
                    logSI(null, killLocked, "reason: " + param.args[0] + " description: " + param.args[1]
                        + " reasonCode: " + param.args[2]
                        + " subReason: " + param.args[3]
                        + " noisy: " + param.args[4] + " im hook? " + hook);

                }
            }
        );

        /*cpu超时false*/
        hookAllMethods(ActivityManagerService,
            checkExcessivePowerUsageLPr,
            new HookAction() {
                @Override
                public String hookLog() {
                    return name;
                }

                @Override
                protected void before(MethodHookParam param) {
                    param.setResult(false);
                    /*logSI(checkExcessivePowerUsageLPr, "uptimeSince: " + param.args[0]
                        + " doCpuKills: " + param.args[1]
                        + " cputimeUsed: " + param.args[2]
                        + " processName: " + param.args[3]
                        + " description: " + param.args[4]
                        + " cpuLimit: " + param.args[5]
                        + " app:" + param.args[6]);*/
                }
            }
        );

        /*禁止清理过时虚幻进程*/
        findAndHookMethod(PhantomProcessList,
            pruneStaleProcessesLocked,
            new HookAction() {
                @Override
                public String hookLog() {
                    return name;
                }

                @Override
                protected void before(MethodHookParam param) {
                    param.setResult(null);
                }
            }
        );

        /*禁用killPids_根据adj计算最差类型pid并kill
        此方法根据adj进行kill，kill的主要是缓存和adj500的进程，可以禁止它kill.
        WindowManagerService在处理窗口的过程中发生Out Of Memroy时，会调用reclaimSomeSurfaceMemoryLocked来回收某些Surface占用的内存，reclaimSomeSurfaceMemoryLocked的逻辑如下所示：
        (1).首先检查有没有泄漏的Surface，即那些Session已经不存在但是还没有销毁的Surface，以及那些宿主Activity已经不可见但是还没有销毁的Surface。如果存在的话，就将它们销毁即可，不用KillPids。
        (2).如果不存在没有泄漏的Surface，那么那些存在Surface的进程都有可能被杀掉，这是通过KillPids来实现的。
        X此方法牵扯过多且不适合Hook。方法可用于处理表面内存不足的情况，但这是合理的X*/
        findAndHookMethod(ActivityManagerService,
            killPids, int[].class, String.class, boolean.class,
            new HookAction() {
                @Override
                public String hookLog() {
                    return name;
                }

                @Override
                protected void before(MethodHookParam param) {
                    if (param.args[1] == "Free memory") {
                        param.setResult(true);
                    }
                    /*logSI(killPids, "pids: " + param.args[0]
                        + " pReason: " + param.args[1]
                        + " secure: " + param.args[2]);*/
                }
            }
        );

        /*禁止killProcessesBelowForeground_和下面重复*/
        /*findAndHookMethod("com.android.server.am.ActivityManagerService",
                "killProcessesBelowForeground", String.class,
                new HookAction() {
                    @Override
                    protected void before(MethodHookParam param) {
                        logSI("Hook killProcessesBelowForeground");
                        param.setResult(true);
                    }
                }
        );*/

        /*禁止杀死adj低的进程*/
        findAndHookMethod(ActivityManagerService,
            killProcessesBelowAdj, int.class, String.class,
            new HookAction() {
                @Override
                public String hookLog() {
                    return name;
                }

                @Override
                protected void before(MethodHookParam param) {
                    param.setResult(true);
                    /*logSI(killProcessesBelowAdj, "belowAdj: " + param.args[0]
                        + " reason: " + param.args[1]);*/
                }
            }
        );

        /*调整内存因子，一般来说0是无压力*//*
        findAndHookMethod(ActivityManagerService,
            setMemFactorOverride, int.class,
            new HookAction() {
                @Override
                protected void before(MethodHookParam param) {
                    setLog(1, logI, ActivityManagerService, setMemFactorOverride);
                    param.args[0] = 0;
                }
            }
        );*/

        /*禁止清理全部后台
         * 未经测试,暂时封存*/
        /*findAndHookMethod(ActivityManagerService,
            killAllBackgroundProcesses,
            new HookAction() {
                @Override
                protected void before(MethodHookParam param) {
                    param.setResult(null);
                }
            }
        );*/


        /*hookAllMethods(ActivityManagerService,
            killAllBackgroundProcessesExcept,
            new HookAction() {
                @Override
                protected void before(MethodHookParam param) {
                    setLog(1, logI, ActivityManagerService, killAllBackgroundProcessesExcept);
                    param.setResult(null);
                }
            }
        );*/

        /*禁止kill除条件之外的所有后台进程
         * 未经测试*//*
        findAndHookMethod(ActivityManagerService + "$LocalService",
            killAllBackgroundProcessesExcept, int.class, int.class, new HookAction() {
                @Override
                protected void before(MethodHookParam param) {
                    param.setResult(null);
                }
            }
        );*/

        /*禁止清理空进程_空进程不包含任何活动，清理是合理的,牵扯太多*/
        /*findAndHookMethod("com.android.server.am.ActivityManagerService",
                "trimApplicationsLocked", boolean.class, String.class,
                new HookAction() {
                    @Override
                    protected void before(MethodHookParam param) {
                        logSI("Hook trimApplicationsLocked");
                        param.setResult(null);
                    }
                }
        );*/

        /*禁止空闲清理，空闲维护*/
        findAndHookMethod(ActivityManagerService,
            performIdleMaintenance, new HookAction() {
                @Override
                public String hookLog() {
                    return name;
                }

                @Override
                protected void before(MethodHookParam param) {
                    param.setResult(null);
                }
            }
        );

        /*关闭后台服务限制_可能导致后台异常
         * 需要测试
         * 应该不需要hook*/
        /*findAndHookMethod(ActivityManagerService,
            getAppStartModeLOSP, int.class, String.class, int.class, int.class, boolean.class, boolean.class, boolean.class, String.class,
            new HookAction() {
                @Override
                protected void before(MethodHookParam param) {
                    param.setResult(0);
                }
            }
        );*/

        /*禁止停止后台空闲服务_与getAppStartModeLOSP的Hook重复_不适合存活的service应该杀掉*/
       /* findAndHookMethod("com.android.server.am.ActiveServices",
                "stopInBackgroundLocked", int.class,
                new HookAction() {
                    @Override
                    protected void before(MethodHookParam param) {
                        logSI("Hook stopInBackgroundLocked");
                        param.setResult(null);
                    }
                }
        );//*/

        /*调整内存因子，一般来说0是无压力
         * 调用好多*/
        /*findAndHookConstructor(AppProfiler,
            findClass(ActivityManagerService), Looper.class, findClass(LowMemDetector),
            new HookAction() {
                @Override
                protected void after(MethodHookParam param) {
                    getDeclaredField(param, "mMemFactorOverride", 0);
                }
            }
        );*/

        /*报告内存压力低*/
        findAndHookMethod(LowMemDetector,
            getMemFactor, new HookAction() {
                @Override
                public String hookLog() {
                    return name;
                }

                @Override
                protected void before(MethodHookParam param) {
                    param.setResult(0);
                }
            }
        );

        /*谎报内存压力无，待测试*/
        findAndHookMethod(ProcessStatsService,
            getMemFactorLocked,
            new HookAction() {
                @Override
                public String hookLog() {
                    return name;
                }

                @Override
                protected void before(MethodHookParam param) {
                    param.setResult(0);
                }
            }
        );

        /*禁止报告低内存*//*
        hookAllMethods(AppProfiler,
            doLowMemReportIfNeededLocked, new HookAction() {
                @Override
                protected void before(MethodHookParam param) {
                    param.setResult(null);
                    logSI(doLowMemReportIfNeededLocked, "dyingProc: " + param.args[0]);
                }
            }
        );*/

        /*禁止trim应用内存
         * 当应用处于不可见或不活跃状态时，释放不必要的内存，这是合理的*/
        /*hookAllMethods(AppProfiler,
            trimMemoryUiHiddenIfNecessaryLSP,
            new HookAction() {
                @Override
                protected void before(MethodHookParam param) {
                    param.setResult(null);
                }
            }
        );*/

        /*禁止停止后台受限和缓存的app
         * 是否允许在后台限制和闲置的情况下杀死应用程序进程*/
        /*hookAllMethods(ProcessList,
            killAppIfBgRestrictedAndCachedIdleLocked,
            new HookAction() {
                @Override
                protected void before(MethodHookParam param) {
//                    param.setResult(0L);
                    logSI(killAppIfBgRestrictedAndCachedIdleLocked, "app: " + param.args[0]
                        + " nowElapsed: " + param.args[1]);
                }
            }
        );*/

        /*设置不得清理*/
        findAndHookMethod(ProcessStateRecord,
            shouldNotKillOnBgRestrictedAndIdle, new HookAction() {
                @Override
                public String hookLog() {
                    return name;
                }

                @Override
                protected void before(MethodHookParam param) {
                    param.setResult(true);
                }
            }
        );

        /*去除受限限制
         * 目前待测试*/
        findAndHookMethod(ProcessList,
            updateBackgroundRestrictedForUidPackageLocked,
            int.class, String.class, boolean.class,
            new HookAction() {
                @Override
                public String hookLog() {
                    return name;
                }

                @Override
                protected void before(MethodHookParam param) {
                    /*logSI(updateBackgroundRestrictedForUidPackageLocked, "uid: " + param.args[0]
                        + " packageName: " + param.args[1]
                        + " restricted: " + param.args[2]);*/
                    param.args[2] = false;
                }
            }
        );

        /*禁止停止后台受限和缓存的app*/
        findAndHookMethod(ProcessList,
            killAppIfBgRestrictedAndCachedIdleLocked,
            ProcessRecord, long.class,
            new HookAction() {
                @Override
                protected void before(MethodHookParam param) {
                    param.setResult(0L);
                }

                @Override
                public String hookLog() {
                    return name;
                }
            }
        );

        /*禁止kill除条件之外的所有后台进程
         * 未经测试*/
        /*findAndHookMethod(ProcessList,
            killAllBackgroundProcessesExceptLSP, int.class, int.class,
            new HookAction() {
                @Override
                protected void before(MethodHookParam param) {
                    param.setResult(null);
                }
            }
        );*/

        /*禁止使用命令停止全部活动*/
        hookAllMethods(ActivityManagerShellCommand,
            runKillAll,
            new HookAction() {
                @Override
                public String hookLog() {
                    return name;
                }

                @Override
                protected void before(MethodHookParam param) {
                    param.setResult(null);
                    /*logSI(runKillAll, "pw: " + param.args[0]);*/
                }
            }
        );//

        /*禁止检查是否应该停止活动*/
        hookAllMethods(OomAdjuster,
            shouldKillExcessiveProcesses,
            new HookAction() {
                @Override
                public String hookLog() {
                    return name;
                }

                @Override
                protected void before(MethodHookParam param) {
                    param.setResult(false);
                    /*logSI(shouldKillExcessiveProcesses, "nowUptime: " + param.args[0]);*/
                }
            }
        );//

        /*禁止修剪进程*/
        hookAllMethods(OomAdjuster,
            updateAndTrimProcessLSP,
            new HookAction() {
                @Override
                public String hookLog() {
                    return name;
                }

                @Override
                protected void before(MethodHookParam param) {
                    /*logSI(updateAndTrimProcessLSP, "now: " + param.args[0] +
                        " nowElapsed: " + param.args[1] +
                        " oldTime: " + param.args[2] +
                        " activeUids: " + param.args[3]);*/
                    /*param.args[0] = 0L;
                    param.args[1] = 0L;*/
                    param.args[2] = 0L;
                }
            }
        );

        /*用来防止频繁log*/
        hookAllConstructors(OomAdjuster,
            new HookAction() {
                @Override
                public String hookLog() {
                    return name;
                }

                @Override
                protected void after(MethodHookParam param) {
                    setObject(param.thisObject, "mNextNoKillDebugMessageTime", Long.MAX_VALUE);
                }
            }
        );

        /*禁止修剪虚幻进程*/
        hookAllMethods(PhantomProcessList,
            trimPhantomProcessesIfNecessary,
            new HookAction() {
                @Override
                public String hookLog() {
                    return name;
                }

                @Override
                protected void before(MethodHookParam param) {
                    param.setResult(null);
                }
            }
        );//

        /*虚假设置adj数值，妈的法克太容易爆内存了*/
        /*findAndHookMethod("com.android.server.am.ProcessList",
                "setOomAdj", int.class, int.class, int.class,
                new HookAction() {
                    @Override
                    protected void before(MethodHookParam param) {
                        int pid = (int) param.args[0];
                        int uid = (int) param.args[1];
                        param.args[2] = -1000;
                        logSI("Hook setOomAdj set pid: " + pid + " uid: " + uid + " adj to: -1000");
                       *//* param.args[2] = 1001;
                        int amt = 1001;
                        ByteBuffer buffer = ByteBuffer.allocate(16);
                        buffer.putInt(1);
                        buffer.putInt(pid);
                        buffer.putInt(uid);
                        buffer.putInt(amt);
                        XposedHelpers.callStaticMethod(findClassIfExists("com.android.server.am.ProcessList"), "writeLmkd", buffer, null);
                        logSI("Hook setOomAdj set pid: " + pid + " uid: " + uid + " adj to: " + amt);
                        param.setResult(null);*//*

                    }
                }
        );*/

        /*禁止修剪最近任务卡片*/
        hookAllMethods(RecentTasks,
            trimInactiveRecentTasks,
            new HookAction() {
                @Override
                public String hookLog() {
                    return name;
                }

                @Override
                protected void before(MethodHookParam param) {
                    param.setResult(null);
                }
            }
        );//

        /*设置最近任务可见*/
        hookAllMethods(RecentTasks,
            isInVisibleRange,
            new HookAction() {
                @Override
                public String hookLog() {
                    return name;
                }

                @Override
                protected void before(MethodHookParam param) {
                    param.args[2] = 0;
                    /*logSI(isInVisibleRange, "task: " + param.args[0] +
                        " taskIndex: " + param.args[1] +
                        " numVisibleTasks: " + param.args[2] +
                        " skipExcludedCheck: " + param.args[3]);*/
                }
            }
        );//

       /* findAndHookMethod("com.android.server.am.LowMemDetector$LowMemThread",
                "run",
                new HookAction() {
                    @Override
                    protected void after(MethodHookParam param) {
                        setInt(param.thisObject, "newPressureState", 0);
                    }
                }
        );*/

        /*findAndHookMethod("com.android.server.am.LowMemDetector",
                "waitForPressure",
                new HookAction() {
                    @Override
                    protected void before(MethodHookParam param) {
                        logSI("Hook waitForPressure");
                        param.setResult(0);
                    }
                }
        );*/

        /*没必要了*/
        /*谎报内存压力
         * 导致频繁唤醒*//*
        findAndHookConstructor(LowMemDetector,
            findClass(ActivityManagerService),
            new HookAction() {
                @Override
                protected void after(MethodHookParam param) {
                    setLog(1, logI, LowMemDetector, "mPressureState");
                    getDeclaredField(param, "mPressureState", 0);
                }
            }
        );

        *//*禁止更新压力*//*
        findAndHookMethod(LowMemDetector + "$LowMemThread",
            run,
            new HookAction() {
                @Override
                protected void before(MethodHookParam param) {
                    setLog(1, logI, LowMemDetector + "$LowMemThread", run);
                    param.setResult(null);
                }
            }
        );*/

        try {
            /*findClassIfExists(ActivityManagerConstants).getDeclaredMethod(getDefaultMaxCachedProcesses);*/
            checkDeclaredMethod(ActivityManagerConstants, getDefaultMaxCachedProcesses);
            /*设置最大进程限制*/
            findAndHookMethod(ActivityManagerConstants,
                getDefaultMaxCachedProcesses,
                new HookAction() {
                    @Override
                    public String hookLog() {
                        return name;
                    }

                    @Override
                    protected void before(MethodHookParam param) {
                        param.setResult(Integer.MAX_VALUE);
                    }
                }
            );//
        } catch (NoSuchMethodException e) {
            /*安卓14*/
            findAndHookMethod(ActivityManagerServiceStub,
                getDefaultMaxCachedProcesses,
                new HookAction() {
                    @Override
                    public String hookLog() {
                        return name;
                    }

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
            new HookAction() {
                @Override
                public String hookLog() {
                    return name;
                }

                @Override
                protected void before(MethodHookParam param) {
                    param.setResult(null);
                }
            }
        );//

        /*设置最大幻影进程数量*/
        /*禁止kill受限的缓存*/
        findAndHookConstructor(ActivityManagerConstants, Context.class,
            findClassIfExists(ActivityManagerService), Handler.class,
            new HookAction() {
                @Override
                public String hookLog() {
                    return name;
                }

                @Override
                protected void after(MethodHookParam param) {
                    setInt(param.thisObject, "MAX_PHANTOM_PROCESSES", Integer.MAX_VALUE);
                    setBoolean(param.thisObject, "mKillBgRestrictedAndCachedIdle", false);
                    /*似乎是高通的东西？*/
                    setBoolean(param.thisObject, "USE_TRIM_SETTINGS", false);
                    setBoolean(param.thisObject, "PROACTIVE_KILLS_ENABLED", false);
                }
            }
        );

        /*禁止kill受限的缓存*/
        /*findAndHookMethod(ActivityManagerConstants,
            updateKillBgRestrictedCachedIdle,
            new HookAction() {
                @Override
                protected void after(MethodHookParam param) {
                }
            }
        );//*/

        /*设置自定义最大缓存数量*/
        findAndHookMethod(ActivityManagerConstants,
            setOverrideMaxCachedProcesses, int.class,
            new HookAction() {
                @Override
                public String hookLog() {
                    return name;
                }

                @Override
                protected void before(MethodHookParam param) {
                    param.args[0] = Integer.MAX_VALUE;
                }
            }
        );//

        /*谎称已经被杀_调用多，容易卡开机*/
       /* findAndHookMethod("com.android.server.am.ProcessRecord",
                "isKilledByAm",
                new HookAction() {
                    @Override
                    protected void after(MethodHookParam param) {
                        logSI("Hook isKilledByAm");
                        param.setResult(true);
                    }
                }
        );//*/

        /*锁定内存因子*/
       /* findAndHookConstructor(AppProfiler,
                findClass(ActivityManagerService), Looper.class, findClass(LowMemDetector),
                new HookAction() {
                    @Override
                    protected void after(MethodHookParam param) {
                        getDeclaredField(param, "mMemFactorOverride", -1);
                        getDeclaredField(param, "mLastMemoryLevel", 0);

                    }
                }
        );*/

        /*拒绝根据压力trim进程_我TM看不懂逻辑，什么内存因子修剪等级去死吧。
         * 看了最终调用，触发GC回收系统垃圾内存，所以可以保留。
         * */
       /* findAndHookMethod(AppProfiler,
                updateLowMemStateLSP, int.class, int.class, int.class,
                new HookAction() {
                    @Override
                    protected void before(MethodHookParam param) {
                        setLog(AppProfiler, updateLowMemStateLSP);
                        param.setResult(false);

                    }
                }
        );//*/

      /*  findAndHookMethod("com.android.server.am.ActivityManagerService",
                "setProcessMemoryTrimLevel", String.class, int.class, int.class,
                new HookAction() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) {
                        param.args[2] = 0;
                    }
                }
        );*/

        /*findAndHookConstructor("com.android.server.am.ProcessProfileRecord",
                findClass("com.android.server.am.ProcessRecord"),
                new HookAction() {
                    @Override
                    protected void after(MethodHookParam param) {
                        setObject(param.thisObject, "mTrimMemoryLevel", 5);
                    }
                });*/
    }
}
