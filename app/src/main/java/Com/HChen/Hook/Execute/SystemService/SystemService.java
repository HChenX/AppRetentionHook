package Com.HChen.Hook.Execute.SystemService;

import static Com.HChen.Hook.Name.SystemName.ActivityManagerConstants;
import static Com.HChen.Hook.Name.SystemName.ActivityManagerService;
import static Com.HChen.Hook.Name.SystemName.ActivityManagerShellCommand;
import static Com.HChen.Hook.Name.SystemName.LowMemDetector;
import static Com.HChen.Hook.Name.SystemName.OomAdjuster;
import static Com.HChen.Hook.Name.SystemName.PhantomProcessList;
import static Com.HChen.Hook.Name.SystemName.ProcessList;
import static Com.HChen.Hook.Name.SystemName.RecentTasks;
import static Com.HChen.Hook.Value.SystemValue.checkExcessivePowerUsage;
import static Com.HChen.Hook.Value.SystemValue.getDefaultMaxCachedProcesses;
import static Com.HChen.Hook.Value.SystemValue.isInVisibleRange;
import static Com.HChen.Hook.Value.SystemValue.killAllBackgroundProcesses;
import static Com.HChen.Hook.Value.SystemValue.killAppIfBgRestrictedAndCachedIdleLocked;
import static Com.HChen.Hook.Value.SystemValue.killPids;
import static Com.HChen.Hook.Value.SystemValue.killProcessesBelowAdj;
import static Com.HChen.Hook.Value.SystemValue.performIdleMaintenance;
import static Com.HChen.Hook.Value.SystemValue.run;
import static Com.HChen.Hook.Value.SystemValue.runKillAll;
import static Com.HChen.Hook.Value.SystemValue.setMemFactorOverride;
import static Com.HChen.Hook.Value.SystemValue.setOverrideMaxCachedProcesses;
import static Com.HChen.Hook.Value.SystemValue.shouldKillExcessiveProcesses;
import static Com.HChen.Hook.Value.SystemValue.trimInactiveRecentTasks;
import static Com.HChen.Hook.Value.SystemValue.trimPhantomProcessesIfNecessary;
import static Com.HChen.Hook.Value.SystemValue.updateAndTrimProcessLSP;
import static Com.HChen.Hook.Value.SystemValue.updateBackgroundRestrictedForUidPackageLocked;
import static Com.HChen.Hook.Value.SystemValue.updateKillBgRestrictedCachedIdle;

import android.content.Context;
import android.os.Handler;

import Com.HChen.Hook.Mode.HookMode;
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
        );

        /*禁用killPids_根据adj计算最差类型pid并kill
        X此方法牵扯过多且不适合Hook.方法可用于处理表面内存不足的情况,但这是合理的X*/
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

        /*禁止杀死adj低的进程*/
        findAndHookMethod(ActivityManagerService, killProcessesBelowAdj, int.class, String.class,
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

        /*禁止清理空进程_空进程不包含任何活动,清理是合理的*/
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
        );*/

        /*禁止停止后台受限和缓存的app*/
        hookAllMethods(ProcessList,
            killAppIfBgRestrictedAndCachedIdleLocked,
            new HookAction() {
                @Override
                protected void before(XC_MethodHook.MethodHookParam param) {
                    setLog(1, logI, ProcessList, killAppIfBgRestrictedAndCachedIdleLocked);
                    param.setResult(null);
                }
            }
        );

        /*禁止清理后台剩余任务*/
        findAndHookMethod(RecentTasks,
            trimInactiveRecentTasks,
            new HookAction() {
                @Override
                protected void before(XC_MethodHook.MethodHookParam param) {
                    setLog(1, logI, RecentTasks, trimInactiveRecentTasks);
                    param.setResult(null);
                }
            }
        );

        /*禁止清理虚幻进程*/
        findAndHookMethod(PhantomProcessList,
            trimPhantomProcessesIfNecessary,
            new HookAction() {
                @Override
                protected void before(XC_MethodHook.MethodHookParam param) {
                    setLog(1, logI, PhantomProcessList, trimPhantomProcessesIfNecessary);
                    param.setResult(null);
                }
            }
        );

        /*禁止杀掉所有后台应用*/
        findAndHookMethod(LowMemDetector,
            runKillAll,
            new HookAction() {
                @Override
                protected void before(XC_MethodHook.MethodHookParam param) {
                    setLog(1, logI, LowMemDetector, runKillAll);
                    param.setResult(null);
                }
            }
        );

        /*禁止更新后台限制状态*/
        findAndHookMethod(ActivityManagerService,
            updateBackgroundRestrictedForUidPackageLocked,
            int.class, int.class, int.class, int.class, int.class, boolean.class,
            new HookAction() {
                @Override
                protected void before(XC_MethodHook.MethodHookParam param) {
                    setLog(1, logI, ActivityManagerService, updateBackgroundRestrictedForUidPackageLocked);
                    param.setResult(null);
                }
            }
        );

        /*禁止杀死活跃进程_活跃进程不应该被杀死*/
        hookAllMethods(ActivityManagerShellCommand,
            run,
            new HookAction() {
                @Override
                protected void before(XC_MethodHook.MethodHookParam param) {
                    setLog(1, logI, ActivityManagerShellCommand, run);
                    param.setResult(null);
                }
            }
        );

        /*禁止杀掉优先级可见进程*/
        hookAllMethods(OomAdjuster,
            run,
            new HookAction() {
                @Override
                protected void before(XC_MethodHook.MethodHookParam param) {
                    if (!isInVisibleRange()) {
                        setLog(1, logI, OomAdjuster, run);
                        param.setResult(null);
                    }
                }
            }
        );

        /*禁止杀掉优先级可见进程*/
        hookAllMethods(ActivityManagerConstants,
            run,
            new HookAction() {
                @Override
                protected void before(XC_MethodHook.MethodHookParam param) {
                    if (!isInVisibleRange()) {
                        setLog(1, logI, ActivityManagerConstants, run);
                        param.setResult(null);
                    }
                }
            }
        );

        /*设置最大缓存进程数*/
        findAndHookMethod(ActivityManagerService,
            setOverrideMaxCachedProcesses,
            int.class, int.class,
            new HookAction() {
                @Override
                protected void before(XC_MethodHook.MethodHookParam param) {
                    setLog(1, logI, ActivityManagerService, setOverrideMaxCachedProcesses);
                    param.setResult(null);
                }
            }
        );

        /*设置默认最大缓存进程数*/
        findAndHookMethod(ActivityManagerService,
            getDefaultMaxCachedProcesses,
            new HookAction() {
                @Override
                protected void before(XC_MethodHook.MethodHookParam param) {
                    setLog(1, logI, ActivityManagerService, getDefaultMaxCachedProcesses);
                    param.setResult(150);
                }
            }
        );

        /*禁止清理任何多余的进程_多余进程不应该被杀死*/
        hookAllMethods(ActivityManagerService,
            shouldKillExcessiveProcesses,
            new HookAction() {
                @Override
                protected void before(XC_MethodHook.MethodHookParam param) {
                    if (!isInVisibleRange()) {
                        setLog(1, logI, ActivityManagerService, shouldKillExcessiveProcesses);
                        param.setResult(null);
                    }
                }
            }
        );

        /*禁止更新和清理进程_不适用于特殊情况,如需求X*/
        hookAllMethods(ActivityManagerService,
            updateAndTrimProcessLSP,
            new HookAction() {
                @Override
                protected void before(XC_MethodHook.MethodHookParam param) {
                    if (!isInVisibleRange()) {
                        setLog(1, logI, ActivityManagerService, updateAndTrimProcessLSP);
                        param.setResult(null);
                    }
                }
            }
        );
    }

    @Override
    public void Run(Context context) {
        logI("Hook Start!");
        Run(context, ActivityManagerService);
    }

    @Override
    public void Run(Handler handler, ClassLoader classLoader) {
        logI("Hook Start!");
        Run(handler, classLoader, ActivityManagerService);
    }
}

