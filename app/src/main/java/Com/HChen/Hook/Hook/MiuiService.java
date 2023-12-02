package Com.HChen.Hook.Hook;

import static Com.HChen.Hook.Param.Name.MiuiName.CameraBooster;
import static Com.HChen.Hook.Param.Name.MiuiName.GameMemoryCleaner;
import static Com.HChen.Hook.Param.Name.MiuiName.MemoryStandardProcessControl;
import static Com.HChen.Hook.Param.Name.MiuiName.PeriodicCleanerService;
import static Com.HChen.Hook.Param.Name.MiuiName.PressureStateSettings;
import static Com.HChen.Hook.Param.Name.MiuiName.ProcessKiller;
import static Com.HChen.Hook.Param.Name.MiuiName.ProcessKillerIdler;
import static Com.HChen.Hook.Param.Name.MiuiName.ProcessMemoryCleaner;
import static Com.HChen.Hook.Param.Name.MiuiName.ProcessPowerCleaner;
import static Com.HChen.Hook.Param.Name.MiuiName.ScoutDisplayMemoryManager;
import static Com.HChen.Hook.Param.Name.MiuiName.ScoutHelper;
import static Com.HChen.Hook.Param.Name.MiuiName.SystemPressureController;
import static Com.HChen.Hook.Param.Value.MiuiValue.boostCameraIfNeeded;
import static Com.HChen.Hook.Param.Value.MiuiValue.cleanUpMemory;
import static Com.HChen.Hook.Param.Value.MiuiValue.doClean;
import static Com.HChen.Hook.Param.Value.MiuiValue.handleAutoLockOff;
import static Com.HChen.Hook.Param.Value.MiuiValue.handleKillAll;
import static Com.HChen.Hook.Param.Value.MiuiValue.handleKillApp;
import static Com.HChen.Hook.Param.Value.MiuiValue.handleScreenOff;
import static Com.HChen.Hook.Param.Value.MiuiValue.handleThermalKillProc;
import static Com.HChen.Hook.Param.Value.MiuiValue.isEnableScoutMemory;
import static Com.HChen.Hook.Param.Value.MiuiValue.killApplication;
import static Com.HChen.Hook.Param.Value.MiuiValue.killPackage;
import static Com.HChen.Hook.Param.Value.MiuiValue.killProcess;
import static Com.HChen.Hook.Param.Value.MiuiValue.killProcessByMinAdj;
import static Com.HChen.Hook.Param.Value.MiuiValue.nStartPressureMonitor;
import static Com.HChen.Hook.Param.Value.MiuiValue.onStartJob;
import static Com.HChen.Hook.Param.Value.MiuiValue.reclaimMemoryForGameIfNeed;
import static Com.HChen.Hook.Param.Value.MiuiValue.updateScreenState;

import android.app.job.JobParameters;
import android.content.Context;

import Com.HChen.Hook.Mode.HookMode;

public class MiuiService extends HookMode {

    public static String name = "MiuiService";

    @Override
    public void init() {
        /*设置禁止Scout功能*/
        /*findAndHookConstructor(ScoutDisplayMemoryManager,
            new HookAction() {
                @Override
                protected void after(MethodHookParam param) {
                    getDeclaredField(param, "ENABLE_SCOUT_MEMORY_MONITOR", false);
                    *//*报告内存泄露？*//*
//                    getDeclaredField(param, "SCOUT_MEMORY_DISABLE_KGSL", false);
                }
            }
        );*/

        /*设置禁止Scout功能*/
        findAndHookMethod(ScoutDisplayMemoryManager,
            isEnableScoutMemory, new HookAction() {
                @Override
                public String hookLog() {
                    return name;
                }

                @Override
                protected void before(MethodHookParam param) {
                    param.setResult(false);
                }
            }
        );

        /*测试用*/
        /*findAndHookMethod(ScoutDisplayMemoryManager,
            isEnableScoutMemory,
            new HookAction() {
                @Override
                protected void before(MethodHookParam param) {
                    setLog(2, logW, ScoutDisplayMemoryManager, isEnableScoutMemory);
                }
            }
        );

        findAndHookMethod(ScoutDisplayMemoryManager,
            checkKgslLeak,
            new HookAction() {
                @Override
                protected void before(MethodHookParam param) {
                    setLog(2, logW, ScoutDisplayMemoryManager, checkKgslLeak);
                }
            }
        );*/

        /*关闭Scout的一个功能，
        内存泄露恢复功能，
        注意启用改功能可能使导致内存泄露的程序被杀，
        但这是合理的*/
        /*findAndHookMethod(ScoutDisplayMemoryManager,
            isEnableResumeFeature, new HookAction() {
                @Override
                protected void before(MethodHookParam param) {
                    param.setResult(false);
                }
            }
        );*/

        /*关闭一堆Scout的功能*/
        findAndHookConstructor(ScoutHelper, new HookAction() {
                @Override
                public String hookLog() {
                    return name;
                }

                @Override
                protected void after(MethodHookParam param) {
                    /*系统监察功能 关闭可以节省功耗？*/
                    setBoolean(param.thisObject, "ENABLED_SCOUT", false);
                    setBoolean(param.thisObject, "ENABLED_SCOUT_DEBUG", false);
                    setBoolean(param.thisObject, "BINDER_FULL_KILL_PROC", false);
//                    setBoolean(param.thisObject, "SCOUT_BINDER_GKI", false);
                    /*是崩溃相关*/
                    /*setBoolean(param.thisObject, "PANIC_D_THREAD", false);
                    setBoolean(param.thisObject, "SYSRQ_ANR_D_THREAD", false);
                    setBoolean(param.thisObject, "PANIC_ANR_D_THREAD", false);
                    setBoolean(param.thisObject, "DISABLE_AOSP_ANR_TRACE_POLICY", true);*/
                }
            }
        );

        /*禁止在开游戏时回收内存*/
        findAndHookMethod(GameMemoryCleaner,
            reclaimMemoryForGameIfNeed, String.class,
            new HookAction() {
                @Override
                public String hookLog() {
                    return name;
                }

                @Override
                protected void before(MethodHookParam param) {
                    param.setResult(null);
                    /*logSI(reclaimMemoryForGameIfNeed, "game: " + param.args[0]);*/
                }

            }
        );

        /*关闭内存回收功能，寄生于游戏清理*/
        hookAllConstructors(GameMemoryCleaner,
            new HookAction() {
                @Override
                public String hookLog() {
                    return name;
                }

                @Override
                protected void after(MethodHookParam param) {
                    getDeclaredField(param, "IS_MEMORY_CLEAN_ENABLED", false);
                }
            }
        );

        try {
            /*findClassIfExists(PeriodicCleanerService).getDeclaredMethod(doClean, int.class, int.class, int.class, String.class);*/
            checkDeclaredMethod(PeriodicCleanerService, doClean, int.class, int.class, int.class, String.class);
            /*禁用PeriodicCleaner的clean*/
            findAndHookMethod(PeriodicCleanerService,
                doClean, int.class, int.class, int.class, String.class,
                new HookAction() {
                    @Override
                    public String hookLog() {
                        return name;
                    }

                    @Override
                    protected void before(MethodHookParam param) {
                        param.setResult(null);
                        /*logSI(doClean, "thresHold: " + param.args[0] +
                            " killLevel: " + param.args[1] +
                            " pressure: " + param.args[2] +
                            " reason: " + param.args[3]);*/
                    }
                }
            );

            /*禁止息屏清理后台*/
            findAndHookMethod(PeriodicCleanerService,
                handleScreenOff, new HookAction() {
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

            /*禁用PeriodicCleaner的响应
             * 与上面重复*/
        /*findAndHookMethod(PeriodicCleanerService + "$MyHandler",
            handleMessage,
            Message.class,
            new HookAction() {
                @Override
                protected void before(MethodHookParam param) {
                    param.setResult(null);
                    logSI(handleMessage, "msg: " + param.args[0]);
                }
            }
        );*/

            /*禁用PeriodicCleaner清理
             * 与上面重复*/
        /*findAndHookMethod(PeriodicCleanerService + "$PeriodicShellCmd",
            runClean, PrintWriter.class,
            new HookAction() {
                @Override
                protected void before(MethodHookParam param) {
                    param.setResult(null);
                }
            }
        );*/

            /*禁用PeriodicCleaner*/
            findAndHookConstructor(PeriodicCleanerService,
                Context.class,
                new HookAction() {
                    @Override
                    public String hookLog() {
                        return name;
                    }

                    @Override
                    protected void after(MethodHookParam param) {
                        getDeclaredField(param, "mEnable", false);
                    }
                }
            );
        } catch (NoSuchMethodException e) {
            /*安卓14的东西*/
            findAndHookConstructor(MemoryStandardProcessControl,
                new HookAction() {
                    @Override
                    public String hookLog() {
                        return name;
                    }

                    @Override
                    protected void after(MethodHookParam param) {
                        setBoolean(param.thisObject, "mEnable", false);
                    }
                }
            );

            /*虽然上面设置false下面就不会执行了，但是多层保障*/
            findAndHookMethod(MemoryStandardProcessControl,
                killProcess, boolean.class,
                new HookAction() {
                    @Override
                    public String hookLog() {
                        return name;
                    }

                    @Override
                    protected void before(MethodHookParam param) {
                        param.setResult(null);
                        /*logSI(killProcess, "canKillhighPriorityProcess: " + param.args[0]);*/
                    }
                }
            );
        }

        /*禁止清理内存*/
        findAndHookMethod(SystemPressureController,
            cleanUpMemory, long.class,
            new HookAction() {
                @Override
                public String hookLog() {
                    return name;
                }

                @Override
                protected void before(MethodHookParam param) {
                    param.setResult(null);
                    /*logSI(cleanUpMemory, "targetReleaseMem: " + param.args[0]);*/
                }
            }
        );

        /*禁止启动内存压力检查工具*/
        hookAllMethods(SystemPressureController,
            nStartPressureMonitor,
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

        /*禁止跟随屏幕关闭/启动内存压力检测工具*/
        findAndHookMethod(SystemPressureController,
            updateScreenState,
            boolean.class,
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

        /*禁止启动定时任务ProcessKillerIdler*/
        findAndHookMethod(ProcessKillerIdler,
            onStartJob, JobParameters.class,
            new HookAction() {
                @Override
                public String hookLog() {
                    return name;
                }

                @Override
                protected void before(MethodHookParam param) {
                    param.setResult(false);
                }
            }
        );

        /*禁止息屏清理内存*/
        hookAllMethods(ProcessPowerCleaner,
            handleAutoLockOff,
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

        /*禁止温度过高清理*/
        hookAllMethods(ProcessPowerCleaner,
            handleThermalKillProc,
            new HookAction() {
                @Override
                public String hookLog() {
                    return name;
                }

                @Override
                protected void before(MethodHookParam param) {
                    param.setResult(null);
                    /*logSI(handleThermalKillProc, "config: " + param.args[0]);*/
                }
            }
        );

        /*禁止kill进程*/
        hookAllMethods(ProcessPowerCleaner,
            handleKillAll,
            new HookAction() {
                @Override
                public String hookLog() {
                    return name;
                }

                @Override
                protected void before(MethodHookParam param) {
                    param.setResult(null);
                    /*logSI(handleKillAll, "config: " + param.args[0] + " isKillSystemProc: " + param.args[1]);*/
                }
            }
        );

        hookAllMethods(ProcessPowerCleaner,
            handleKillApp, new HookAction() {
                @Override
                public String hookLog() {
                    return name;
                }

                @Override
                protected void before(MethodHookParam param) {
                    param.setResult(null);
                    /*logSI(handleKillApp, "config: " + param.args[0]);*/
                }
            }
        );

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

        /*禁止ProcessMemoryCleaner的killPackage*/
        hookAllMethods(ProcessMemoryCleaner,
            killPackage, new HookAction() {
                @Override
                public String hookLog() {
                    return name;
                }

                @Override
                protected void before(MethodHookParam param) {
                    param.setResult(0L);
                }
            }
        );

        /*禁止kill，这个是最终的kill方法，专用于释放内存*/
        hookAllMethods(ProcessMemoryCleaner,
            killProcess,
            new HookAction() {
                @Override
                public String hookLog() {
                    return name;
                }

                @Override
                protected void before(MethodHookParam param) {
                    param.setResult(0L);
                }
            }
        );

        /*禁止ProcessMemoryCleaner$H响应*/
        /*findAndHookMethod(ProcessMemoryCleaner + "$H",
            handleMessage, Message.class,
            new HookAction() {
                @Override
                protected void before(MethodHookParam param) {
                    param.setResult(null);
                }
            }
        );//*/

        /*禁止ProcessMemoryCleaner的killProcessByMinAdj*/
        hookAllMethods(ProcessMemoryCleaner,
            killProcessByMinAdj, new HookAction() {
                @Override
                public String hookLog() {
                    return name;
                }

                @Override
                protected void before(MethodHookParam param) {
                    param.setResult(null);
                    /*logSI(killProcessByMinAdj, "minAdj: " + param.args[0] +
                        " reason: " + param.args[1] +
                        " whiteList: " + param.args[2]);*/
                }
            }
        );

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
                public String hookLog() {
                    return name;
                }

                @Override
                protected void before(MethodHookParam param) {
                    param.setResult(true);
                    /*logSI(cleanUpMemory, "runningProcList: " + param.args[0] + " targetReleaseMem: " + param.args[1]);*/
                }
            }
        );

        try {
            checkDeclaredMethod(CameraBooster, boostCameraIfNeeded, long.class, boolean.class);
            /*findClassIfExists(CameraBooster).getDeclaredMethod(boostCameraIfNeeded, long.class, boolean.class);*/
            /*禁止相机kill*/
            findAndHookMethod(CameraBooster,
                boostCameraIfNeeded, long.class, boolean.class,
                new HookAction() {
                    @Override
                    public String hookLog() {
                        return name;
                    }

                    @Override
                    protected void before(MethodHookParam param) {
                        param.setResult(null);
                        /*logSI(boostCameraIfNeeded, "memThreshold: " + param.args[0] + " isMiuiCamera: " + param.args[1]);*/
                    }
                }
            );
        } catch (NoSuchMethodException f) {
            /*安卓14的逻辑，相机kill的最终调用，当然还有进程检查也调用了*/
            hookAllMethods(ProcessKiller,
                killApplication,
                new HookAction() {
                    @Override
                    public String hookLog() {
                        return name;
                    }

                    @Override
                    protected void before(MethodHookParam param) {
                        param.setResult(false);
                        /*logSI(killApplication, "app: " + param.args[0] + " reason: " + param.args[1] + " evenForeground: " + param.args[2]);*/
                    }
                }
            );
        }

        /*禁止Cpu使用检查
         * 调用killProcess*/
        /*findAndHookMethod(SmartCpuPolicyManager,
            handleLimitCpuException, int.class, new HookAction() {
                @Override
                protected void before(MethodHookParam param) {
                    param.setResult(null);
                    logSI(handleLimitCpuException, "type: " + param.args[0]);
                }
            }
        );*/

        /*多余操作*/
        /* *//*禁用MiuiMemoryService*//*
        findAndHookMethod(MiuiMemoryService + "$MiuiMemServiceThread",
            run,
            new HookAction() {
                @Override
                protected void before(MethodHookParam param) {
                    setLog(2, logI, MiuiMemoryService + "$MiuiMemServiceThread", run);
                    param.setResult(null);
                }
            }
        );

        *//*禁用MiuiMemoryService*//*
        findAndHookMethod(MiuiMemoryService + "$ConnectionHandler",
            run,
            new HookAction() {
                @Override
                protected void before(MethodHookParam param) {
                    setLog(2, logI, MiuiMemoryService + "$ConnectionHandler", run);
                    param.setResult(null);
                }
            }
        );*/

        /*禁用MiuiMemoryService
         * 似乎控制内存压缩，不需要关闭*/
        /*findAndHookConstructor(MiuiMemoryService,
            Context.class,
            new HookAction() {
                @Override
                protected void after(MethodHookParam param) {
                    getDeclaredField(param, "sCompactionEnable", false);
                    getDeclaredField(param, "sCompactSingleProcEnable", false);
                    getDeclaredField(param, "sWriteEnable", false);
                }
            }
        );//*/

        /*禁用mi_reclaim
         * 什么陈年逻辑，看不懂，似乎和压缩有关*/
        /*findAndHookConstructor(MiuiMemReclaimer,
            new HookAction() {
                @Override
                protected void after(MethodHookParam param) {
                    getDeclaredField(param, "RECLAIM_IF_NEEDED", false);
                    *//*getDeclaredField(param, "USE_LEGACY_COMPACTION", false);*//*
                }
            }
        );//*/

        /*压缩进程的*/
        /*禁用MiuiMemReclaimer*//*
        findAndHookMethod(MiuiMemReclaimer + "$CompactorHandler",
            handleMessage,
            Message.class, new HookAction() {
                @Override
                protected void before(MethodHookParam param) {
                    setLog(2, logI, MiuiMemReclaimer + "$CompactorHandler", handleMessage);
                    param.setResult(null);
                }
            }
        );*/

        /*禁用spc*/
        findAndHookConstructor(PressureStateSettings,
            new HookAction() {
                @Override
                public String hookLog() {
                    return name;
                }

                @Override
                protected void after(MethodHookParam param) {
                    setBoolean(param.thisObject, "PROCESS_CLEANER_ENABLED", false);
                }
            }
        );
    }
}
