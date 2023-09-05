package Com.HChen.Hook.SystemService;

import static Com.HChen.Hook.HookValue.SystemValue.*;
import static Com.HChen.Hook.HookName.SystemName.*;

import android.content.Context;
import android.os.Handler;

import Com.HChen.Hook.HookMode.HookMode;
import de.robv.android.xposed.XC_MethodHook;

public class SystemService extends HookMode {
    final String logI = "I";
    final String logW = "W";
    final String logE = "E";

    @Override
    public int smOr() {
        return 1;
    }

    @Override
    public void init() {
        /*关闭Cpu超时检查*/
        hookAllMethods(ActivityManagerService,
                checkExcessivePowerUsage,
                new HookAction() {
                    @Override
                    protected void before(XC_MethodHook.MethodHookParam param) {
                        setLog(1, logI, ActivityManagerService, checkExcessivePowerUsage);
                        param.setResult(null);
                    }
                }
        );//

        /*禁用killPids_根据adj计算最差类型pid并kill
        X此方法牵扯过多且不适合Hook。方法可用于处理表面内存不足的情况，但这是合理的X*/
        findAndHookMethod(ActivityManagerService,
                killPids, int[].class, String.class, boolean.class,
                new HookAction() {
                    @Override
                    protected void before(XC_MethodHook.MethodHookParam param) {
                        setLog(1, logI, ActivityManagerService, killPids);
                        param.setResult(true);
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
        findAndHookMethod(ActivityManagerService
                , killProcessesBelowAdj, int.class, String.class,
                new HookAction() {
                    @Override
                    protected void before(XC_MethodHook.MethodHookParam param) {
                        setLog(1, logI, ActivityManagerService, killProcessesBelowAdj);
                        param.setResult(true);
                    }
                }
        );

        /*禁止清理全部后台*/
        findAndHookMethod(ActivityManagerService,
                killAllBackgroundProcesses,
                new HookAction() {
                    @Override
                    protected void before(MethodHookParam param) {
                        setLog(1, logI, ActivityManagerService, killAllBackgroundProcesses);
                        param.setResult(null);
                    }
                }
        );

        /*禁止清理空进程_空进程不包含任何活动，清理是合理的*/
        /*findAndHookMethod("com.android.server.am.ActivityManagerService",
                "trimApplicationsLocked", boolean.class, String.class,
                new HookAction() {
                    @Override
                    protected void before(XC_MethodHook.MethodHookParam param) {
                        logSI("Hook trimApplicationsLocked");
                        param.setResult(null);
                    }
                }
        );*/

        /*禁止空闲清理*/
        findAndHookMethod(ActivityManagerService,
                performIdleMaintenance, new HookAction() {
                    @Override
                    protected void before(XC_MethodHook.MethodHookParam param) {
                        setLog(1, logI, ActivityManagerService, performIdleMaintenance);
                        param.setResult(null);
                    }
                }
        );

        /*关闭后台服务限制_可能导致后台异常*/
        /*findAndHookMethod(ActivityManagerService,
                getAppStartModeLOSP, int.class, String.class, int.class, int.class, boolean.class, boolean.class, boolean.class, String.class,
                new HookAction() {
                    @Override
                    protected void before(XC_MethodHook.MethodHookParam param) {
                        setLog(ActivityManagerService, getAppStartModeLOSP);
                        param.setResult(0);
                    }
                }
        );*/

        /*禁止停止后台空闲服务_与getAppStartModeLOSP的Hook重复_不适合存活的service应该杀掉*/
       /* findAndHookMethod("com.android.server.am.ActiveServices",
                "stopInBackgroundLocked", int.class,
                new HookAction() {
                    @Override
                    protected void before(XC_MethodHook.MethodHookParam param) {
                        logSI("Hook stopInBackgroundLocked");
                        param.setResult(null);
                    }
                }
        );//*/

        /*禁止停止后台受限和缓存的app*/
        hookAllMethods(ProcessList,
                killAppIfBgRestrictedAndCachedIdleLocked,
                new HookAction() {
                    @Override
                    protected void before(XC_MethodHook.MethodHookParam param) {
                        setLog(1, logI, ProcessList, killAppIfBgRestrictedAndCachedIdleLocked);
                        param.setResult(0L);
                    }
                }
        );

        /*去除受限限制*/
        findAndHookMethod(ProcessList,
                updateBackgroundRestrictedForUidPackageLocked,
                int.class, String.class, boolean.class,
                new HookAction() {
                    @Override
                    protected void before(MethodHookParam param) {
                        setLog(1, logI, ProcessList, updateBackgroundRestrictedForUidPackageLocked);
                        param.args[2] = false;
                    }
                }
        );

        /*禁止使用命令停止全部活动*/
        hookAllMethods(ActivityManagerShellCommand,
                runKillAll,
                new HookAction() {
                    @Override
                    protected void before(XC_MethodHook.MethodHookParam param) {
                        setLog(1, logI, ActivityManagerShellCommand, runKillAll);
                        param.setResult(null);
                    }
                }
        );//

        /*禁止检查是否应该停止活动*/
        hookAllMethods(OomAdjuster,
                shouldKillExcessiveProcesses,
                new HookAction() {
                    @Override
                    protected void before(XC_MethodHook.MethodHookParam param) {
                        setLog(1, logI, OomAdjuster, shouldKillExcessiveProcesses);
                        param.setResult(false);
                    }
                }
        );//

        /*禁止修剪进程*/
        hookAllMethods(OomAdjuster,
                updateAndTrimProcessLSP,
                new HookAction() {
                    @Override
                    protected void before(XC_MethodHook.MethodHookParam param) {
                        setLog(1, logI, OomAdjuster, updateAndTrimProcessLSP);
                        param.args[2] = 0;
                    }
                }
        );

        /*禁止修剪虚幻进程*/
        hookAllMethods(PhantomProcessList,
                trimPhantomProcessesIfNecessary,
                new HookAction() {
                    @Override
                    protected void before(XC_MethodHook.MethodHookParam param) {
                        setLog(1, logI, PhantomProcessList, trimPhantomProcessesIfNecessary);
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
                    protected void before(XC_MethodHook.MethodHookParam param) {
                        setLog(1, logI, RecentTasks, trimInactiveRecentTasks);
                        param.setResult(null);
                    }
                }
        );//

        /*设置最近任务可见*/
        hookAllMethods(RecentTasks,
                isInVisibleRange,
                new HookAction() {
                    @Override
                    protected void before(XC_MethodHook.MethodHookParam param) {
                        setLog(1, logI, RecentTasks, isInVisibleRange);
                        param.args[2] = 0;
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

        /*谎报内存压力*/
        findAndHookConstructor(LowMemDetector,
                findClass(ActivityManagerService),
                new HookAction() {
                    @Override
                    protected void after(XC_MethodHook.MethodHookParam param) {
                        setLog(1, logI, LowMemDetector, "mPressureState");
                        getDeclaredField(param, "mPressureState", 0);
                    }
                }
        );

        /*禁止更新压力*/
        findAndHookMethod(LowMemDetector + "$LowMemThread",
                run,
                new HookAction() {
                    @Override
                    protected void before(XC_MethodHook.MethodHookParam param) {
                        setLog(1, logI, LowMemDetector + "$LowMemThread", run);
                        param.setResult(null);
                    }
                }
        );

        /*设置最大进程限制*/
        findAndHookMethod(ActivityManagerConstants,
                getDefaultMaxCachedProcesses,
                new HookAction() {
                    @Override
                    protected void after(XC_MethodHook.MethodHookParam param) {
                        setLog(1, logI, ActivityManagerConstants, getDefaultMaxCachedProcesses);
                        param.setResult(Integer.MAX_VALUE);
                    }
                }
        );//

        /*阻止更新最大进程限制*/
        findAndHookMethod(ActivityManagerConstants,
                updateMaxCachedProcesses,
                new HookAction() {
                    @Override
                    protected void before(XC_MethodHook.MethodHookParam param) {
                        setLog(1, logI, ActivityManagerConstants, updateMaxCachedProcesses);
                        param.setResult(null);
                    }
                }
        );//

        /*设置最大幻影进程数量*/
        findAndHookConstructor(ActivityManagerConstants, Context.class,
                findClass(ActivityManagerService), Handler.class,
                new HookAction() {
                    @Override
                    protected void after(XC_MethodHook.MethodHookParam param) {
                        setLog(1, logI, ActivityManagerConstants, "MAX_PHANTOM_PROCESSES");
                        setInt(param.thisObject, "MAX_PHANTOM_PROCESSES", Integer.MAX_VALUE);
                    }
                }
        );

        /*禁止kill受限的缓存*/
        findAndHookMethod(ActivityManagerConstants,
                updateKillBgRestrictedCachedIdle,
                new HookAction() {
                    @Override
                    protected void after(XC_MethodHook.MethodHookParam param) {
                        setLog(1, logI, ActivityManagerConstants, updateKillBgRestrictedCachedIdle);
                        setBoolean(param.thisObject, "mKillBgRestrictedAndCachedIdle", false);
                    }
                }
        );//

        /*设置自定义最大缓存数量*/
        findAndHookMethod(ActivityManagerConstants,
                setOverrideMaxCachedProcesses, int.class,
                new HookAction() {
                    @Override
                    protected void after(XC_MethodHook.MethodHookParam param) {
                        setLog(1, logI, ActivityManagerConstants, setOverrideMaxCachedProcesses);
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

        /*调整内存因子*/
        findAndHookMethod(ActivityManagerService,
                setMemFactorOverride, int.class,
                new HookAction() {
                    @Override
                    protected void before(MethodHookParam param) {
                        setLog(1, logI, ActivityManagerService, setMemFactorOverride);
                        param.args[0] = 0;
                    }
                }
        );

        /*拒绝根据压力trim进程_我TM看不懂逻辑，什么内存因子修剪等级去死吧。
         * 看了最终调用，触发GC回收系统垃圾内存，所以可以保留。
         * */
       /* findAndHookMethod(AppProfiler,
                updateLowMemStateLSP, int.class, int.class, int.class,
                new HookAction() {
                    @Override
                    protected void before(XC_MethodHook.MethodHookParam param) {
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
