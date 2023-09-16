package Com.HChen.Hook.Execute.MiuiService;

import android.app.job.JobParameters;
import android.content.Context;
import android.os.Message;
import java.io.PrintWriter;
import Com.HChen.Hook.Mode.HookMode;
import de.robv.android.xposed.XC_MethodHook;

import static Com.HChen.Hook.Name.MiuiName.*;
import static Com.HChen.Hook.Value.MiuiValue.*;

public class MiuiService extends HookMode {
    private final String logI = "I";
    private final String logW = "W";
    private final String logE = "E";

    @Override
    public int smOr() {
        return 2;
    }

    @Override
    public void init() {
        setScoutDisplayMemoryManagerHooks();
        setScoutHelperHooks();
        setGameMemoryCleanerHooks();
        setPeriodicCleanerServiceHooks();
        setSystemPressureControllerHooks();
        setProcessKillerIdlerHooks();
        setProcessPowerCleanerHooks();
        setProcessMemoryCleanerHooks();
        setCameraBoosterHooks();
        setSmartCpuPolicyManagerHooks();
        setMiuiMemoryServiceHooks();
        setMiuiMemReclaimerHooks();
        setPressureStateSettingsHooks();
    }

    private void setScoutDisplayMemoryManagerHooks() {
        findAndHookConstructor(ScoutDisplayMemoryManager, new HookAction() {
            @Override
            protected void after(XC_MethodHook.MethodHookParam param) {
                setLog(2, logI, ScoutDisplayMemoryManager);
                getDeclaredField(param, "ENABLE_SCOUT_MEMORY_MONITOR", false);
                getDeclaredField(param, "SCOUT_MEMORY_DISABLE_KGSL", false);
            }
        });

        findAndHookMethod(ScoutDisplayMemoryManager, isEnableResumeFeature, new HookAction() {
            @Override
            protected void before(XC_MethodHook.MethodHookParam param) {
                setLog(2, logI, ScoutDisplayMemoryManager, isEnableResumeFeature);
                param.setResult(false);
            }
        });
    }

    private void setScoutHelperHooks() {
        findAndHookConstructor(ScoutHelper, new HookAction() {
            @Override
            protected void after(XC_MethodHook.MethodHookParam param) {
                setLog(2, logI, ScoutHelper);
                setBoolean(param.thisObject, "ENABLED_SCOUT", false);
                setBoolean(param.thisObject, "ENABLED_SCOUT_DEBUG", false);
                setBoolean(param.thisObject, "BINDER_FULL_KILL_PROC", false);
                setBoolean(param.thisObject, "SCOUT_BINDER_GKI", false);
            }
        });
    }

    private void setGameMemoryCleanerHooks() {
        findAndHookMethod(GameMemoryCleaner, reclaimMemoryForGameIfNeed, String.class, new HookAction() {
            @Override
            protected void before(XC_MethodHook.MethodHookParam param) {
                setLog(2, logI, GameMemoryCleaner, reclaimMemoryForGameIfNeed);
                param.setResult(null);
            }
        });
    }

    private void setPeriodicCleanerServiceHooks() {
        findAndHookMethod(PeriodicCleanerService, doClean, int.class, int.class, int.class, String.class, new HookAction() {
            @Override
            protected void before(XC_MethodHook.MethodHookParam param) {
                setLog(2, logI, PeriodicCleanerService, doClean);
                param.setResult(null);
            }
        });

        findAndHookMethod(PeriodicCleanerService + "$MyHandler", handleMessage, Message.class, new HookAction() {
            @Override
            protected void before(XC_MethodHook.MethodHookParam param) {
                setLog(2, logI, PeriodicCleanerService + "$MyHandler", handleMessage);
                param.setResult(null);
            }
        });

        findAndHookMethod(PeriodicCleanerService + "$PeriodicShellCmd", runClean, PrintWriter.class, new HookAction() {
            @Override
            protected void before(XC_MethodHook.MethodHookParam param) {
                setLog(2, logI, PeriodicCleanerService + "$PeriodicShellCmd", runClean);
                param.setResult(null);
            }
        });

        findAndHookConstructor(PeriodicCleanerService, Context.class, new HookAction() {
            @Override
            protected void after(XC_MethodHook.MethodHookParam param) {
                setLog(2, logI, PeriodicCleanerService, "mEnable");
                getDeclaredField(param, "mEnable", false);
            }
        });
    }

    private void setSystemPressureControllerHooks() {
        findAndHookMethod(SystemPressureController, cleanUpMemory, long.class, new HookAction() {
            @Override
            protected void before(XC_MethodHook.MethodHookParam param) {
                setLog(2, logI, SystemPressureController, cleanUpMemory);
                param.setResult(null);
            }
        });

        hookAllMethods(SystemPressureController, nStartPressureMonitor, new HookAction() {
            @Override
            protected void before(XC_MethodHook.MethodHookParam param) {
                setLog(2, logI, SystemPressureController, nStartPressureMonitor);
                param.setResult(null);
            }
        });

        findAndHookMethod(SystemPressureController, updateScreenState, boolean.class, new HookAction() {
            @Override
            protected void before(XC_MethodHook.MethodHookParam param) {
                setLog(2, logI, SystemPressureController, updateScreenState);
                param.setResult(null);
            }
        });
    }

    private void setProcessKillerIdlerHooks() {
        findAndHookMethod(ProcessKillerIdler, onStartJob, JobParameters.class, new HookAction() {
            @Override
            protected void before(XC_MethodHook.MethodHookParam param) {
                setLog(2, logI, ProcessKillerIdler, onStartJob);
                param.setResult(false);
            }
        });
    }

    private void setProcessPowerCleanerHooks() {
        hookAllMethods(ProcessPowerCleaner, handleAutoLockOff, new HookAction() {
            @Override
            protected void before(XC_MethodHook.MethodHookParam param) {
                setLog(2, logI, ProcessPowerCleaner, handleAutoLockOff);
                param.setResult(null);
            }
        });

        hookAllMethods(ProcessPowerCleaner, handleThermalKillProc, new HookAction() {
            @Override
            protected void before(XC_MethodHook.MethodHookParam param) {
                setLog(2, logI, ProcessPowerCleaner, handleThermalKillProc);
                param.setResult(null);
            }
        });
    }

    private void setProcessMemoryCleanerHooks() {
        findAndHookMethod(ProcessMemoryCleaner + "$H", handleMessage, Message.class, new HookAction() {
            @Override
            protected void before(XC_MethodHook.MethodHookParam param) {
                setLog(2, logI, ProcessMemoryCleaner + "$H", handleMessage);
                param.setResult(null);
            }
        });

        hookAllMethods(ProcessMemoryCleaner, cleanUpMemory, new HookAction() {
            @Override
            protected void before(XC_MethodHook.MethodHookParam param) {
                setLog(2, logI, ProcessMemoryCleaner, cleanUpMemory);
                param.setResult(true);
            }
        });

        hookAllMethods(ProcessMemoryCleaner, killProcess, new HookAction() {
            @Override
            protected void before(XC_MethodHook.MethodHookParam param) {
                setLog(2, logI, ProcessMemoryCleaner, killProcess);
                param.setResult(0L);
            }
        });
    }

    private void setCameraBoosterHooks() {
        hookAllMethods(CameraBooster, boostCameraIfNeeded, new HookAction() {
            @Override
            protected void before(XC_MethodHook.MethodHookParam param) {
                setLog(2, logI, CameraBooster, boostCameraIfNeeded);
                param.setResult(null);
            }
        });
    }

    private void setSmartCpuPolicyManagerHooks() {
        findAndHookMethod(SmartCpuPolicyManager, handleLimitCpuException, int.class, new HookAction() {
            @Override
            protected void before(XC_MethodHook.MethodHookParam param) {
                setLog(2, logI, SmartCpuPolicyManager, handleLimitCpuException);
                param.setResult(null);
            }
        });
    }

    private void setMiuiMemoryServiceHooks() {
        findAndHookMethod(MiuiMemoryService + "$MiuiMemServiceThread", run, new HookAction() {
            @Override
            protected void before(XC_MethodHook.MethodHookParam param) {
                setLog(2, logI, MiuiMemoryService + "$MiuiMemServiceThread", run);
                param.setResult(null);
            }
        });

        findAndHookMethod(MiuiMemoryService + "$ConnectionHandler", run, new HookAction() {
            @Override
            protected void before(XC_MethodHook.MethodHookParam param) {
                setLog(2, logI, MiuiMemoryService + "$ConnectionHandler", run);
                param.setResult(null);
            }
        });

        findAndHookConstructor(MiuiMemoryService, Context.class, new HookAction() {
            @Override
            protected void after(XC_MethodHook.MethodHookParam param) {
                setLog(2, logI, MiuiMemoryService);
                getDeclaredField(param, "sCompactionEnable", false);
                getDeclaredField(param, "sCompactSingleProcEnable", false);
                getDeclaredField(param, "sWriteEnable", false);
            }
        });
    }

    private void setMiuiMemReclaimerHooks() {
        findAndHookConstructor(MiuiMemReclaimer, new HookAction() {
            @Override
            protected void after(XC_MethodHook.MethodHookParam param) {
                setLog(2, logI, MiuiMemReclaimer);
                getDeclaredField(param, "RECLAIM_IF_NEEDED", false);
                getDeclaredField(param, "USE_LEGACY_COMPACTION", false);
            }
        });

        findAndHookMethod(MiuiMemReclaimer + "$CompactorHandler", handleMessage, Message.class, new HookAction() {
            @Override
            protected void before(XC_MethodHook.MethodHookParam param) {
                setLog(2, logI, MiuiMemReclaimer + "$CompactorHandler", handleMessage);
                param.setResult(null);
            }
        });
    }

    private void setPressureStateSettingsHooks() {
        findAndHookConstructor(PressureStateSettings, new HookAction() {
            @Override
            protected void after(XC_MethodHook.MethodHookParam param) {
                setLog(2, logI, PressureStateSettings, "PROCESS_CLEANER_ENABLED");
                setBoolean(param.thisObject, "PROCESS_CLEANER_ENABLED", false);
            }
        });
    }
}
