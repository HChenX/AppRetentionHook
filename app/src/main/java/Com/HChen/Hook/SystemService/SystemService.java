package Com.HChen.Hook.SystemService;

import android.content.Context;
import android.os.Handler;

import Com.HChen.Hook.HookMode.HookMode;
import de.robv.android.xposed.XC_MethodHook;

public class SystemService extends HookMode {
    @Override
    public void init() {
        /*关闭Cpu超时检查*/
        hookAllMethods("com.android.server.am.ActivityManagerService",
                "checkExcessivePowerUsage",
                new HookAction() {
                    @Override
                    protected void before(XC_MethodHook.MethodHookParam param) {
                        logSI("Hook checkExcessivePowerUsage");
                        param.setResult(null);
                    }
                }
        );//

        /*禁用killPids*/
        findAndHookMethod("com.android.server.am.ActivityManagerService",
                "killPids", int[].class, String.class, boolean.class,
                new HookAction() {
                    @Override
                    protected void before(XC_MethodHook.MethodHookParam param) {
                        logSI("Hook killPids");
                        param.setResult(true);
                    }
                }
        );

        /*禁止killProcessesBelowForeground*/
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
        findAndHookMethod("com.android.server.am.ActivityManagerService"
                , "killProcessesBelowAdj", int.class, String.class,
                new HookAction() {
                    @Override
                    protected void before(XC_MethodHook.MethodHookParam param) {
                        logSI("Hook killProcessesBelowAdj");
                        param.setResult(true);
                    }
                }
        );

        /*禁止清理空进程_待考虑*/
        findAndHookMethod("com.android.server.am.ActivityManagerService",
                "trimApplicationsLocked", boolean.class, String.class,
                new HookAction() {
                    @Override
                    protected void before(XC_MethodHook.MethodHookParam param) {
                        logSI("Hook trimApplicationsLocked");
                        param.setResult(null);
                    }
                }
        );

        /*禁止空闲清理*/
        findAndHookMethod("com.android.server.am.ActivityManagerService",
                "performIdleMaintenance", new HookAction() {
                    @Override
                    protected void before(XC_MethodHook.MethodHookParam param) {
                        logSI("Hook performIdleMaintenance");
                        param.setResult(null);
                    }
                }
        );

        /*关闭后台服务限制*/
        findAndHookMethod("com.android.server.am.ActivityManagerService",
                "getAppStartModeLOSP", int.class, String.class, int.class, int.class, boolean.class, boolean.class, boolean.class, String.class,
                new HookAction() {
                    @Override
                    protected void before(XC_MethodHook.MethodHookParam param) {
                        logSI("Hook getAppStartModeLOSP");
                        param.setResult(0);
                    }
                }
        );

        /*禁止停止后台空闲服务*/
        findAndHookMethod("com.android.server.am.ActiveServices",
                "stopInBackgroundLocked", int.class,
                new HookAction() {
                    @Override
                    protected void before(XC_MethodHook.MethodHookParam param) {
                        logSI("Hook stopInBackgroundLocked");
                        param.setResult(null);
                    }
                }
        );//

        /*禁止停止后台受限和缓存的app*/
        hookAllMethods("com.android.server.am.ProcessList",
                "killAppIfBgRestrictedAndCachedIdleLocked",
                new HookAction() {
                    @Override
                    protected void before(XC_MethodHook.MethodHookParam param) {
                        logSI("Hook killAppIfBgRestrictedAndCachedIdleLocked");
                        param.setResult(0L);
                    }
                }
        );

        /*禁止使用命令停止全部活动*/
        hookAllMethods("com.android.server.am.ActivityManagerShellCommand",
                "runKillAll",
                new HookAction() {
                    @Override
                    protected void before(XC_MethodHook.MethodHookParam param) {
                        logSI("Hook runKillAll");
                        param.setResult(null);
                    }
                }
        );//

        /*禁止检查是否应该停止活动*/
        hookAllMethods("com.android.server.am.OomAdjuster",
                "shouldKillExcessiveProcesses",
                new HookAction() {
                    @Override
                    protected void before(XC_MethodHook.MethodHookParam param) {
                        logSI("Hook shouldKillExcessiveProcesses");
                        param.setResult(false);
                    }
                }
        );//

        /*禁止修剪进程*/
        hookAllMethods("com.android.server.am.OomAdjuster",
                "updateAndTrimProcessLSP",
                new HookAction() {
                    @Override
                    protected void before(XC_MethodHook.MethodHookParam param) {
                        logSI("Hook updateAndTrimProcessLSP");
                        param.args[2] = 0;
                    }
                }
        );

        /*禁止修剪虚幻进程*/
        hookAllMethods("com.android.server.am.PhantomProcessList",
                "trimPhantomProcessesIfNecessary",
                new HookAction() {
                    @Override
                    protected void before(XC_MethodHook.MethodHookParam param) {
                        logSI("Hook trimPhantomProcessesIfNecessary");
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
        hookAllMethods("com.android.server.wm.RecentTasks",
                "trimInactiveRecentTasks",
                new HookAction() {
                    @Override
                    protected void before(XC_MethodHook.MethodHookParam param) {
                        logSI("Hook trimInactiveRecentTasks");
                        param.setResult(null);
                    }
                }
        );//

        /*设置最近任务可见*/
        hookAllMethods("com.android.server.wm.RecentTasks",
                "isInVisibleRange",
                new HookAction() {
                    @Override
                    protected void before(XC_MethodHook.MethodHookParam param) {
                        logSI("Hook isInVisibleRange");
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
        findAndHookConstructor("com.android.server.am.LowMemDetector",
                findClass("com.android.server.am.ActivityManagerService"),
                new HookAction() {
                    @Override
                    protected void after(XC_MethodHook.MethodHookParam param) {
                        logSI("Hook LowMemDetector mPressureState");
                        getDeclaredField(param, "mPressureState", 0);
                    }
                }
        );

        /*禁止更新压力*/
        findAndHookMethod("com.android.server.am.LowMemDetector$LowMemThread",
                "run",
                new HookAction() {
                    @Override
                    protected void before(XC_MethodHook.MethodHookParam param) {
                        logSI("Hook LowMemDetector$LowMemThread run");
                        param.setResult(null);
                    }
                }
        );

        /*设置最大进程限制*/
        findAndHookMethod("com.android.server.am.ActivityManagerConstants",
                "getDefaultMaxCachedProcesses",
                new HookAction() {
                    @Override
                    protected void after(XC_MethodHook.MethodHookParam param) {
                        logSI("Hook getDefaultMaxCachedProcesses");
                        param.setResult(Integer.MAX_VALUE);
                    }
                }
        );//

        /*阻止更新最大进程限制*/
        findAndHookMethod("com.android.server.am.ActivityManagerConstants",
                "updateMaxCachedProcesses",
                new HookAction() {
                    @Override
                    protected void before(XC_MethodHook.MethodHookParam param) {
                        logSI("Hook updateMaxCachedProcesses");
                        param.setResult(null);
                    }
                }
        );//

        /*设置最大幻影进程数量*/
        findAndHookConstructor("com.android.server.am.ActivityManagerConstants", Context.class,
                findClass("com.android.server.am.ActivityManagerService"), Handler.class,
                new HookAction() {
                    @Override
                    protected void after(XC_MethodHook.MethodHookParam param) {
                        setInt(param.thisObject, "MAX_PHANTOM_PROCESSES", Integer.MAX_VALUE);
                    }
                }
        );

        /*禁止kill受限的缓存*/
        findAndHookMethod("com.android.server.am.ActivityManagerConstants",
                "updateKillBgRestrictedCachedIdle",
                new HookAction() {
                    @Override
                    protected void after(XC_MethodHook.MethodHookParam param) {
                        logSI("Hook updateKillBgRestrictedCachedIdle");
                        setBoolean(param.thisObject, "mKillBgRestrictedAndCachedIdle", false);
                    }
                }
        );//

        /*谎称已经被杀*/
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

        /*拒绝重新设置压力等*/
        findAndHookMethod("com.android.server.am.AppProfiler"
                , "updateLowMemStateLSP", int.class, int.class, int.class,
                new HookAction() {
                    @Override
                    protected void before(XC_MethodHook.MethodHookParam param) {
                        logSI("Hook updateLowMemStateLSP");
                        param.setResult(false);

                    }
                }
        );//

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

        /*设置自定义最大缓存数量*/
        findAndHookMethod("com.android.server.am.ActivityManagerConstants",
                "setOverrideMaxCachedProcesses", int.class,
                new HookAction() {
                    @Override
                    protected void after(XC_MethodHook.MethodHookParam param) {
                        logSI("Hook setOverrideMaxCachedProcesses");
                        param.args[0] = Integer.MAX_VALUE;
                    }
                }
        );//
    }
}
