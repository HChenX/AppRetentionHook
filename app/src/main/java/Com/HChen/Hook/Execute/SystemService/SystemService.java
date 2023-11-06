package Com.HChen.Hook.Execute.SystemService;

import static Com.HChen.Hook.Name.SystemName.ActivityManagerConstants;
import static Com.HChen.Hook.Name.SystemName.ActivityManagerService;
import static Com.HChen.Hook.Name.SystemName.ActivityManagerShellCommand;
import static Com.HChen.Hook.Name.SystemName.AppProfiler;
import static Com.HChen.Hook.Name.SystemName.LowMemDetector;
import static Com.HChen.Hook.Name.SystemName.OomAdjuster;
import static Com.HChen.Hook.Name.SystemName.PhantomProcessList;
import static Com.HChen.Hook.Name.SystemName.ProcessList;
import static Com.HChen.Hook.Name.SystemName.RecentTasks;
import static Com.HChen.Hook.Value.SystemValue.checkExcessivePowerUsage;
import static Com.HChen.Hook.Value.SystemValue.doLowMemReportIfNeededLocked;
import static Com.HChen.Hook.Value.SystemValue.getDefaultMaxCachedProcesses;
import static Com.HChen.Hook.Value.SystemValue.isInVisibleRange;
import static Com.HChen.Hook.Value.SystemValue.killAllBackgroundProcesses;
import static Com.HChen.Hook.Value.SystemValue.killAppIfBgRestrictedAndCachedIdleLocked;
import static Com.HChen.Hook.Value.SystemValue.killPids;
import static Com.HChen.Hook.Value.SystemValue.killProcessesBelowAdj;
import static Com.HChen.Hook.Value.SystemValue.performIdleMaintenance;
import static Com.HChen.Hook.Value.SystemValue.runKillAll;
import static Com.HChen.Hook.Value.SystemValue.setOverrideMaxCachedProcesses;
import static Com.HChen.Hook.Value.SystemValue.shouldKillExcessiveProcesses;
import static Com.HChen.Hook.Value.SystemValue.trimInactiveRecentTasks;
import static Com.HChen.Hook.Value.SystemValue.trimPhantomProcessesIfNecessary;
import static Com.HChen.Hook.Value.SystemValue.updateAndTrimProcessLSP;
import static Com.HChen.Hook.Value.SystemValue.updateBackgroundRestrictedForUidPackageLocked;
import static Com.HChen.Hook.Value.SystemValue.updateKillBgRestrictedCachedIdle;
import static Com.HChen.Hook.Value.SystemValue.updateMaxCachedProcesses;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import Com.HChen.Hook.Mode.HookMode;

public class SystemService extends HookMode {
    @Override
    public void init() {
        /*关闭Cpu超时检查*/
        hookAllMethods(ActivityManagerService,
            checkExcessivePowerUsage,
            new HookAction() {
                @Override
                protected void before(MethodHookParam param) {
                    super.before(param);
                    param.setResult(null);
                }

            }
        );//


        /*禁用killPids_根据adj计算最差类型pid并kill
        此方法根据adj进行kill，kill的主要是缓存和adj500的进程，可以禁止它kill
        X此方法牵扯过多且不适合Hook。方法可用于处理表面内存不足的情况，但这是合理的X*/
        findAndHookMethod(ActivityManagerService,
            killPids, int[].class, String.class, boolean.class,
            new HookAction() {
                @Override
                protected void before(MethodHookParam param) {
                    super.before(param);
                    param.setResult(true);
                    logSI(killPids, "pids: " + param.args[0]
                        + " pReason: " + param.args[1]
                        + " secure: " + param.args[2]);
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
                protected void before(MethodHookParam param) {
                    super.before(param);
                    param.setResult(true);
                    logSI(killProcessesBelowAdj, "belowAdj: " + param.args[0]
                        + " reason: " + param.args[1]);
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
         * 未经测试*/
        findAndHookMethod(ActivityManagerService,
            killAllBackgroundProcesses,
            new HookAction() {
                @Override
                protected void before(MethodHookParam param) {
                    super.before(param);
                    param.setResult(null);
                }
            }
        );


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
                protected void before(MethodHookParam param) {
                    super.before(param);
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
                    super.before(param);
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

        /*调整内存因子，一般来说0是无压力*/
        findAndHookConstructor(AppProfiler,
            findClass(ActivityManagerService), Looper.class, findClass(LowMemDetector),
            new HookAction() {
                @Override
                protected void after(MethodHookParam param) {
                    super.before(param);
                    getDeclaredField(param, "mMemFactorOverride", 0);
                }
            }
        );

        /*禁止报告低内存*/
        hookAllMethods(AppProfiler,
            doLowMemReportIfNeededLocked, new HookAction() {
                @Override
                protected void before(MethodHookParam param) {
                    super.before(param);
                    param.setResult(null);
                    logSI(doLowMemReportIfNeededLocked, "dyingProc: " + param.args[0]);
                }
            }
        );

        /*禁止trim应用内存
         * 当应用处于不可见或不活跃状态时，释放不必要的内存，这是合理的*/
        /*hookAllMethods(AppProfiler,
            trimMemoryUiHiddenIfNecessaryLSP,
            new HookAction() {
                @Override
                protected void before(MethodHookParam param) {
                    super.before(param);
                    param.setResult(null);
                }
            }
        );*/

        /*禁止停止后台受限和缓存的app
         * 是否允许在后台限制和闲置的情况下杀死应用程序进程*/
        hookAllMethods(ProcessList,
            killAppIfBgRestrictedAndCachedIdleLocked,
            new HookAction() {
                @Override
                protected void before(MethodHookParam param) {
                    super.before(param);
                    param.setResult(0L);
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
                protected void before(MethodHookParam param) {
                    super.before(param);
                    param.args[2] = false;
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
                    super.before(param);
                    param.setResult(null);
                }
            }
        );*/

        /*禁止使用命令停止全部活动*/
        hookAllMethods(ActivityManagerShellCommand,
            runKillAll,
            new HookAction() {
                @Override
                protected void before(MethodHookParam param) {
                    super.before(param);
                    param.setResult(null);
                }
            }
        );//

        /*禁止检查是否应该停止活动*/
        hookAllMethods(OomAdjuster,
            shouldKillExcessiveProcesses,
            new HookAction() {
                @Override
                protected void before(MethodHookParam param) {
                    super.before(param);
                    param.setResult(false);
                    logSI(shouldKillExcessiveProcesses, "nowUptime: " + param.args[0]);
                }
            }
        );//

        /*禁止修剪进程*/
        hookAllMethods(OomAdjuster,
            updateAndTrimProcessLSP,
            new HookAction() {
                @Override
                protected void before(MethodHookParam param) {
                    super.before(param);
                    param.args[2] = 0;
                    logSI(updateAndTrimProcessLSP, "now: " + param.args[0] +
                        " nowElapsed: " + param.args[1] +
                        " oldTime: " + param.args[2] +
                        " activeUids: " + param.args[3]);
                }
            }
        );

        /*禁止修剪虚幻进程*/
        hookAllMethods(PhantomProcessList,
            trimPhantomProcessesIfNecessary,
            new HookAction() {
                @Override
                protected void before(MethodHookParam param) {
                    super.before(param);
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
                protected void before(MethodHookParam param) {
                    super.before(param);
                    param.setResult(null);
                }
            }
        );//

        /*设置最近任务可见*/
        hookAllMethods(RecentTasks,
            isInVisibleRange,
            new HookAction() {
                @Override
                protected void before(MethodHookParam param) {
                    super.before(param);
                    param.args[2] = 0;
                    logSI(isInVisibleRange, "task: " + param.args[0] +
                        " taskIndex: " + param.args[1] +
                        " numVisibleTasks: " + param.args[2] +
                        " skipExcludedCheck: " + param.args[3]);
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

        /*设置最大进程限制*/
        findAndHookMethod(ActivityManagerConstants,
            getDefaultMaxCachedProcesses,
            new HookAction() {
                @Override
                protected void after(MethodHookParam param) {
                    super.after(param);
                    param.setResult(Integer.MAX_VALUE);
                }
            }
        );//

        /*阻止更新最大进程限制*/
        findAndHookMethod(ActivityManagerConstants,
            updateMaxCachedProcesses,
            new HookAction() {
                @Override
                protected void before(MethodHookParam param) {
                    super.before(param);
                    param.setResult(null);
                }
            }
        );//

        /*设置最大幻影进程数量*/
        findAndHookConstructor(ActivityManagerConstants, Context.class,
            findClass(ActivityManagerService), Handler.class,
            new HookAction() {
                @Override
                protected void after(MethodHookParam param) {
                    super.after(param);
                    setInt(param.thisObject, "MAX_PHANTOM_PROCESSES", Integer.MAX_VALUE);
                }
            }
        );

        /*禁止kill受限的缓存*/
        findAndHookMethod(ActivityManagerConstants,
            updateKillBgRestrictedCachedIdle,
            new HookAction() {
                @Override
                protected void after(MethodHookParam param) {
                    super.after(param);
                    setBoolean(param.thisObject, "mKillBgRestrictedAndCachedIdle", false);
                }
            }
        );//

        /*设置自定义最大缓存数量*/
        findAndHookMethod(ActivityManagerConstants,
            setOverrideMaxCachedProcesses, int.class,
            new HookAction() {
                @Override
                protected void after(MethodHookParam param) {
                    super.after(param);
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
