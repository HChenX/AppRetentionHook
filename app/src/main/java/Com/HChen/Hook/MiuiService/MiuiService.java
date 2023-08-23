package Com.HChen.Hook.MiuiService;

import android.app.job.JobParameters;
import android.content.Context;
import android.os.Message;

import java.io.PrintWriter;

import Com.HChen.Hook.HookMode.HookMode;
import de.robv.android.xposed.XC_MethodHook;

public class MiuiService extends HookMode {
    @Override
    public void init() {
        /*设置禁止Scout功能*/
        findAndHookConstructor("com.miui.server.stability.ScoutDisplayMemoryManager",
                new HookAction() {
                    @Override
                    protected void after(XC_MethodHook.MethodHookParam param) {
                        logSI("Hook ScoutDisplayMemoryManager");
                        getDeclaredField(param, "ENABLE_SCOUT_MEMORY_MONITOR", false);
                        getDeclaredField(param, "SCOUT_MEMORY_DISABLE_KGSL", false);
                    }
                }
        );//

        /*关闭Scout的一个功能*/
        findAndHookMethod("com.miui.server.stability.ScoutDisplayMemoryManager",
                "isEnableResumeFeature", new HookAction() {
                    @Override
                    protected void before(XC_MethodHook.MethodHookParam param) {
                        logSI("Hook isEnableResumeFeature");
                        param.setResult(false);
                    }
                }
        );//

        /*关闭一堆Scout的功能*/
        findAndHookConstructor("com.android.server.ScoutHelper", new HookAction() {
                    @Override
                    protected void after(XC_MethodHook.MethodHookParam param) {
                        logSI("Hook ScoutHelper");
                        setBoolean(param.thisObject, "ENABLED_SCOUT", false);
                        setBoolean(param.thisObject, "ENABLED_SCOUT_DEBUG", false);
                        setBoolean(param.thisObject, "BINDER_FULL_KILL_PROC", false);
                        setBoolean(param.thisObject, "PANIC_D_THREAD", false);
                        setBoolean(param.thisObject, "SYSRQ_ANR_D_THREAD", false);
                        setBoolean(param.thisObject, "PANIC_ANR_D_THREAD", false);
                        setBoolean(param.thisObject, "SCOUT_BINDER_GKI", false);
                        setBoolean(param.thisObject, "DISABLE_AOSP_ANR_TRACE_POLICY", false);
                    }
                }
        );//

        /*禁止在开游戏时回收内存*/
        findAndHookMethod("com.miui.server.migard.memory.GameMemoryCleaner",
                "reclaimMemoryForGameIfNeed", String.class,
                new HookAction() {
                    @Override
                    protected void before(XC_MethodHook.MethodHookParam param) {
                        logSI("Hook reclaimMemoryForGameIfNeed");
                        param.setResult(null);
                    }

                }
        );//

        /*禁用PeriodicCleaner的clean*/
        findAndHookMethod("com.android.server.am.PeriodicCleanerService",
                "doClean", int.class, int.class, int.class, String.class,
                new HookAction() {
                    @Override
                    protected void before(XC_MethodHook.MethodHookParam param) {
                        logSI("Hook doClean");
                        param.setResult(null);
                    }
                }
        );//

        /*禁用PeriodicCleaner的响应*/
        findAndHookMethod("com.android.server.am.PeriodicCleanerService$MyHandler",
                "handleMessage",
                Message.class,
                new HookAction() {
                    @Override
                    protected void before(XC_MethodHook.MethodHookParam param) {
                        logSI("Hook PeriodicCleanerService$MyHandler handleMessage");
                        param.setResult(null);
                    }
                }
        );

        /*禁用PeriodicCleaner清理*/
        findAndHookMethod("com.android.server.am.PeriodicCleanerService$PeriodicShellCmd",
                "runClean", PrintWriter.class,
                new HookAction() {
                    @Override
                    protected void before(XC_MethodHook.MethodHookParam param) {
                        logSI("Hook runClean");
                        param.setResult(null);
                    }
                }
        );

        /*禁用PeriodicCleaner*/
        findAndHookConstructor("com.android.server.am.PeriodicCleanerService",
                Context.class,
                new HookAction() {
                    @Override
                    protected void after(XC_MethodHook.MethodHookParam param) {
                        logSI("Hook PeriodicCleanerService");
                        getDeclaredField(param, "mEnable", false);
                    }
                }
        );//

        /*禁止清理内存*/
        findAndHookMethod("com.android.server.am.SystemPressureController",
                "cleanUpMemory", long.class,
                new HookAction() {
                    @Override
                    protected void before(XC_MethodHook.MethodHookParam param) {
                        logSI("Hook SystemPressureController cleanUpMemory");
                        param.setResult(null);
                    }
                }
        );//

        /*禁止启动内存压力检查工具*/
        hookAllMethods("com.android.server.am.SystemPressureController",
                "nStartPressureMonitor",
                new HookAction() {
                    @Override
                    protected void before(XC_MethodHook.MethodHookParam param) {
                        logSI("Hook nStartPressureMonitor");
                        param.setResult(null);
                    }
                }
        );//

        /*禁止跟随屏幕关闭/启动内存压力检测工具*/
        findAndHookMethod("com.android.server.am.SystemPressureController",
                "updateScreenState",
                boolean.class,
                new HookAction() {
                    @Override
                    protected void before(XC_MethodHook.MethodHookParam param) {
                        logSI("Hook updateScreenState");
                        param.setResult(null);
                    }
                }
        );

        /*禁止启动定时任务ProcessKillerIdler*/
        findAndHookMethod("com.android.server.am.ProcessKillerIdler",
                "onStartJob", JobParameters.class,
                new HookAction() {
                    @Override
                    protected void before(XC_MethodHook.MethodHookParam param) {
                        logSI("Hook onStartJob");
                        param.setResult(false);
                    }
                }
        );//

        /*禁止息屏清理内存*/
        hookAllMethods("com.android.server.am.ProcessPowerCleaner",
                "handleAutoLockOff",
                new HookAction() {
                    @Override
                    protected void before(XC_MethodHook.MethodHookParam param) {
                        logSI("Hook handleAutoLockOff");
                        param.setResult(null);
                    }
                }
        );//

        /*禁止温度过高清理*/
        hookAllMethods("com.android.server.am.ProcessPowerCleaner",
                "handleThermalKillProc",
                new HookAction() {
                    @Override
                    protected void before(XC_MethodHook.MethodHookParam param) {
                        logSI("Hook handleThermalKillProc");
                        param.setResult(null);
                    }
                }
        );//

        /*禁止kill_用处暂不确定*/
        /*hookAllMethods("com.android.server.am.ProcessPowerCleaner",
                "handleKillAll",
                new HookAction() {
                    @Override
                    protected void before(MethodHookParam param) {
                        param.setResult(null);
                    }
                }
        );*/

        /*禁止kill_用处暂不确定*/
       /* hookAllMethods("com.android.server.am.ProcessPowerCleaner",
                "handleKillApp",
                new HookAction() {
                    @Override
                    protected void before(MethodHookParam param) {
                        param.setResult(true);l
                    }
                }
        );*/

        /*禁止ProcessMemoryCleaner$H响应*/
        findAndHookMethod("com.android.server.am.ProcessMemoryCleaner$H",
                "handleMessage", Message.class,
                new HookAction() {
                    @Override
                    protected void before(XC_MethodHook.MethodHookParam param) {
                        logSI("Hook ProcessMemoryCleaner$H handleMessage");
                        param.setResult(null);
                    }
                }
        );//

       /* hookAllMethods("com.android.server.am.ProcessMemoryCleaner",
                "checkBackgroundProcCompact",
                new HookAction() {
                    @Override
                    protected void before(MethodHookParam param) {
                        param.setResult(null);
                    }
                }
        );*/

        /*谎称清理成功*/
        hookAllMethods("com.android.server.am.ProcessMemoryCleaner",
                "cleanUpMemory",
                new HookAction() {
                    @Override
                    protected void before(XC_MethodHook.MethodHookParam param) {
                        logSI("Hook ProcessMemoryCleaner cleanUpMemory");
                        param.setResult(true);
                    }
                }
        );//

        /*禁止kill，这个是最终的kill方法，专用于释放内存*/
        hookAllMethods("com.android.server.am.ProcessMemoryCleaner",
                "killProcess",
                new HookAction() {
                    @Override
                    protected void before(XC_MethodHook.MethodHookParam param) {
                        logSI("Hook killProcess");
                        param.setResult(0L);
                    }
                }
        );

        /*禁止相机kill*/
        hookAllMethods("com.android.server.am.CameraBooster",
                "boostCameraIfNeeded",
                new HookAction() {
                    @Override
                    protected void before(XC_MethodHook.MethodHookParam param) {
                        logSI("Hook boostCameraIfNeeded");
                        param.setResult(null);
                    }
                }
        );//

        /*禁止Cpu使用检查*/
        findAndHookMethod("com.miui.server.smartpower.SmartCpuPolicyManager",
                "handleLimitCpuException", int.class, new HookAction() {
                    @Override
                    protected void before(XC_MethodHook.MethodHookParam param) {
                        logSI("Hook handleLimitCpuException");
                        param.setResult(null);
                    }
                }
        );

        /*禁用MiuiMemoryService*/
        findAndHookMethod("com.android.server.am.MiuiMemoryService$MiuiMemServiceThread",
                "run",
                new HookAction() {
                    @Override
                    protected void before(XC_MethodHook.MethodHookParam param) {
                        logSI("Hook MiuiMemoryService$MiuiMemServiceThread run");
                        param.setResult(null);
                    }
                }
        );

        /*禁用MiuiMemoryService*/
        findAndHookMethod("com.android.server.am.MiuiMemoryService$ConnectionHandler",
                "run",
                new HookAction() {
                    @Override
                    protected void before(XC_MethodHook.MethodHookParam param) {
                        logSI("Hook MiuiMemoryService$ConnectionHandler run");
                        param.setResult(null);
                    }
                }
        );

        /*禁用MiuiMemoryService*/
        findAndHookConstructor("com.android.server.am.MiuiMemoryService",
                Context.class,
                new HookAction() {
                    @Override
                    protected void after(XC_MethodHook.MethodHookParam param) {
                        logSI("Hook MiuiMemoryService");
                        getDeclaredField(param, "sCompactionEnable", false);
                        getDeclaredField(param, "sCompactSingleProcEnable", false);
                        getDeclaredField(param, "sWriteEnable", false);
                    }
                }
        );

        /*禁用MiuiMemReclaimer*/
        findAndHookConstructor("com.android.server.am.MiuiMemReclaimer",
                new HookAction() {
                    @Override
                    protected void after(XC_MethodHook.MethodHookParam param) {
                        logSI("Hook MiuiMemReclaimer");
                        getDeclaredField(param, "RECLAIM_IF_NEEDED", false);
                        getDeclaredField(param, "USE_LEGACY_COMPACTION", false);
                    }
                }
        );

        /*禁用MiuiMemReclaimer*/
        findAndHookMethod("com.android.server.am.MiuiMemReclaimer$CompactorHandler",
                "handleMessage",
                Message.class, new HookAction() {
                    @Override
                    protected void before(XC_MethodHook.MethodHookParam param) {
                        logSI("Hook MiuiMemReclaimer$CompactorHandler handleMessage");
                        param.setResult(null);
                    }
                }
        );
    }
}
