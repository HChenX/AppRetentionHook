package Com.HChen.Hook.MiuiService;

import static Com.HChen.Hook.HookName.MiuiName.*;
import static Com.HChen.Hook.HookValue.MiuiValue.*;

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
        findAndHookConstructor(ScoutDisplayMemoryManager,
                new HookAction() {
                    @Override
                    protected void after(XC_MethodHook.MethodHookParam param) {
                        setLog(ScoutDisplayMemoryManager);
                        getDeclaredField(param, "ENABLE_SCOUT_MEMORY_MONITOR", false);
                        getDeclaredField(param, "SCOUT_MEMORY_DISABLE_KGSL", false);
                    }
                }
        );//

        /*关闭Scout的一个功能*/
        findAndHookMethod(ScoutDisplayMemoryManager,
                isEnableResumeFeature, new HookAction() {
                    @Override
                    protected void before(XC_MethodHook.MethodHookParam param) {
                        setLog(ScoutDisplayMemoryManager, isEnableResumeFeature);
                        param.setResult(false);
                    }
                }
        );//

        /*关闭一堆Scout的功能*/
        findAndHookConstructor(ScoutHelper, new HookAction() {
                    @Override
                    protected void after(XC_MethodHook.MethodHookParam param) {
                        setLog(ScoutHelper);
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
        findAndHookMethod(GameMemoryCleaner,
                reclaimMemoryForGameIfNeed, String.class,
                new HookAction() {
                    @Override
                    protected void before(XC_MethodHook.MethodHookParam param) {
                        setLog(GameMemoryCleaner, reclaimMemoryForGameIfNeed);
                        param.setResult(null);
                    }

                }
        );//

        /*禁用PeriodicCleaner的clean*/
        findAndHookMethod(PeriodicCleanerService,
                doClean, int.class, int.class, int.class, String.class,
                new HookAction() {
                    @Override
                    protected void before(XC_MethodHook.MethodHookParam param) {
                        setLog(PeriodicCleanerService, doClean);
                        param.setResult(null);
                    }
                }
        );//

        /*禁用PeriodicCleaner的响应*/
        findAndHookMethod(PeriodicCleanerService + "$MyHandler",
                handleMessage,
                Message.class,
                new HookAction() {
                    @Override
                    protected void before(XC_MethodHook.MethodHookParam param) {
                        setLog(PeriodicCleanerService + "$MyHandler", handleMessage);
                        param.setResult(null);
                    }
                }
        );

        /*禁用PeriodicCleaner清理*/
        findAndHookMethod(PeriodicCleanerService + "$PeriodicShellCmd",
                runClean, PrintWriter.class,
                new HookAction() {
                    @Override
                    protected void before(XC_MethodHook.MethodHookParam param) {
                        setLog(PeriodicCleanerService + "$PeriodicShellCmd", runClean);
                        param.setResult(null);
                    }
                }
        );

        /*禁用PeriodicCleaner*/
        findAndHookConstructor(PeriodicCleanerService,
                Context.class,
                new HookAction() {
                    @Override
                    protected void after(XC_MethodHook.MethodHookParam param) {
                        setLog(PeriodicCleanerService, "mEnable");
                        getDeclaredField(param, "mEnable", false);
                    }
                }
        );//

        /*禁止清理内存*/
        findAndHookMethod(SystemPressureController,
                cleanUpMemory, long.class,
                new HookAction() {
                    @Override
                    protected void before(XC_MethodHook.MethodHookParam param) {
                        setLog(SystemPressureController, cleanUpMemory);
                        param.setResult(null);
                    }
                }
        );//

        /*禁止启动内存压力检查工具*/
        hookAllMethods(SystemPressureController,
                nStartPressureMonitor,
                new HookAction() {
                    @Override
                    protected void before(XC_MethodHook.MethodHookParam param) {
                        setLog(SystemPressureController, nStartPressureMonitor);
                        param.setResult(null);
                    }
                }
        );//

        /*禁止跟随屏幕关闭/启动内存压力检测工具*/
        findAndHookMethod(SystemPressureController,
                updateScreenState,
                boolean.class,
                new HookAction() {
                    @Override
                    protected void before(XC_MethodHook.MethodHookParam param) {
                        setLog(SystemPressureController, updateScreenState);
                        param.setResult(null);
                    }
                }
        );

        /*禁止启动定时任务ProcessKillerIdler*/
        findAndHookMethod(ProcessKillerIdler,
                onStartJob, JobParameters.class,
                new HookAction() {
                    @Override
                    protected void before(XC_MethodHook.MethodHookParam param) {
                        setLog(ProcessKillerIdler, onStartJob);
                        param.setResult(false);
                    }
                }
        );//

        /*禁止息屏清理内存*/
        hookAllMethods(ProcessPowerCleaner,
                handleAutoLockOff,
                new HookAction() {
                    @Override
                    protected void before(XC_MethodHook.MethodHookParam param) {
                        setLog(ProcessPowerCleaner, handleAutoLockOff);
                        param.setResult(null);
                    }
                }
        );//

        /*禁止温度过高清理*/
        hookAllMethods(ProcessPowerCleaner,
                handleThermalKillProc,
                new HookAction() {
                    @Override
                    protected void before(XC_MethodHook.MethodHookParam param) {
                        setLog(ProcessPowerCleaner, handleThermalKillProc);
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
        findAndHookMethod(ProcessMemoryCleaner + "$H",
                handleMessage, Message.class,
                new HookAction() {
                    @Override
                    protected void before(XC_MethodHook.MethodHookParam param) {
                        setLog(ProcessMemoryCleaner + "$H", handleMessage);
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
        hookAllMethods(ProcessMemoryCleaner,
                cleanUpMemory,
                new HookAction() {
                    @Override
                    protected void before(XC_MethodHook.MethodHookParam param) {
                        setLog(ProcessMemoryCleaner, cleanUpMemory);
                        param.setResult(true);
                    }
                }
        );//

        /*禁止kill，这个是最终的kill方法，专用于释放内存*/
        hookAllMethods(ProcessMemoryCleaner,
                killProcess,
                new HookAction() {
                    @Override
                    protected void before(XC_MethodHook.MethodHookParam param) {
                        setLog(ProcessMemoryCleaner, killProcess);
                        param.setResult(0L);
                    }
                }
        );

        /*禁止相机kill*/
        hookAllMethods(CameraBooster,
                boostCameraIfNeeded,
                new HookAction() {
                    @Override
                    protected void before(XC_MethodHook.MethodHookParam param) {
                        setLog(CameraBooster, boostCameraIfNeeded);
                        param.setResult(null);
                    }
                }
        );//

        /*禁止Cpu使用检查*/
        findAndHookMethod(SmartCpuPolicyManager,
                handleLimitCpuException, int.class, new HookAction() {
                    @Override
                    protected void before(XC_MethodHook.MethodHookParam param) {
                        setLog(SmartCpuPolicyManager, handleLimitCpuException);
                        param.setResult(null);
                    }
                }
        );

        /*禁用MiuiMemoryService*/
        findAndHookMethod(MiuiMemoryService + "$MiuiMemServiceThread",
                run,
                new HookAction() {
                    @Override
                    protected void before(XC_MethodHook.MethodHookParam param) {
                        setLog(MiuiMemoryService + "$MiuiMemServiceThread", run);
                        param.setResult(null);
                    }
                }
        );

        /*禁用MiuiMemoryService*/
        findAndHookMethod(MiuiMemoryService + "$ConnectionHandler",
                run,
                new HookAction() {
                    @Override
                    protected void before(XC_MethodHook.MethodHookParam param) {
                        setLog(MiuiMemoryService + "$ConnectionHandler", run);
                        param.setResult(null);
                    }
                }
        );

        /*禁用MiuiMemoryService*/
        findAndHookConstructor(MiuiMemoryService,
                Context.class,
                new HookAction() {
                    @Override
                    protected void after(XC_MethodHook.MethodHookParam param) {
                        setLog(MiuiMemoryService);
                        getDeclaredField(param, "sCompactionEnable", false);
                        getDeclaredField(param, "sCompactSingleProcEnable", false);
                        getDeclaredField(param, "sWriteEnable", false);
                    }
                }
        );

        /*禁用MiuiMemReclaimer*/
        findAndHookConstructor(MiuiMemReclaimer,
                new HookAction() {
                    @Override
                    protected void after(XC_MethodHook.MethodHookParam param) {
                        setLog(MiuiMemReclaimer);
                        getDeclaredField(param, "RECLAIM_IF_NEEDED", false);
                        getDeclaredField(param, "USE_LEGACY_COMPACTION", false);
                    }
                }
        );

        /*禁用MiuiMemReclaimer*/
        findAndHookMethod(MiuiMemReclaimer + "$CompactorHandler",
                handleMessage,
                Message.class, new HookAction() {
                    @Override
                    protected void before(XC_MethodHook.MethodHookParam param) {
                        setLog(MiuiMemReclaimer + "$CompactorHandler", handleMessage);
                        param.setResult(null);
                    }
                }
        );
    }
}
