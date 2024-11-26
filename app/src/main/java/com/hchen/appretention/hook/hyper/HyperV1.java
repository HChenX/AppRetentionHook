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
package com.hchen.appretention.hook.hyper;

import static com.hchen.appretention.data.field.Hyper.IS_ENABLE_RECLAIM;
import static com.hchen.appretention.data.field.Hyper.PROCESS_CLEANER_ENABLED;
import static com.hchen.appretention.data.field.Hyper.PROCESS_TRACKER_ENABLE;
import static com.hchen.appretention.data.field.Hyper.PROC_CPU_EXCEPTION_ENABLE;
import static com.hchen.appretention.data.field.Hyper.RECLAIM_IF_NEEDED;
import static com.hchen.appretention.data.field.Hyper.START_PRELOAD_IS_DISABLE;
import static com.hchen.appretention.data.method.Hyper.addMiuiPeriodicCleanerService;
import static com.hchen.appretention.data.method.Hyper.boostCameraByThreshold;
import static com.hchen.appretention.data.method.Hyper.checkBackgroundAppException;
import static com.hchen.appretention.data.method.Hyper.cleanUpMemory;
import static com.hchen.appretention.data.method.Hyper.doAdjBoost;
import static com.hchen.appretention.data.method.Hyper.foregroundActivityChangedLocked;
import static com.hchen.appretention.data.method.Hyper.getBackgroundAppCount;
import static com.hchen.appretention.data.method.Hyper.getDeviceLevelForRAM;
import static com.hchen.appretention.data.method.Hyper.getPolicy;
import static com.hchen.appretention.data.method.Hyper.handleAutoLockOff;
import static com.hchen.appretention.data.method.Hyper.handleKillAll;
import static com.hchen.appretention.data.method.Hyper.handleKillAny;
import static com.hchen.appretention.data.method.Hyper.handleKillApp;
import static com.hchen.appretention.data.method.Hyper.handleLimitCpuException;
import static com.hchen.appretention.data.method.Hyper.handleThermalKillProc;
import static com.hchen.appretention.data.method.Hyper.init;
import static com.hchen.appretention.data.method.Hyper.interceptAppRestartIfNeeded;
import static com.hchen.appretention.data.method.Hyper.isAllowAdjBoost;
import static com.hchen.appretention.data.method.Hyper.isEnable;
import static com.hchen.appretention.data.method.Hyper.isMiuiLiteVersion;
import static com.hchen.appretention.data.method.Hyper.isNeedCompact;
import static com.hchen.appretention.data.method.Hyper.killBackgroundApps;
import static com.hchen.appretention.data.method.Hyper.killPackage;
import static com.hchen.appretention.data.method.Hyper.killProcess;
import static com.hchen.appretention.data.method.Hyper.killProcessByMinAdj;
import static com.hchen.appretention.data.method.Hyper.nStartPressureMonitor;
import static com.hchen.appretention.data.method.Hyper.newInstance;
import static com.hchen.appretention.data.method.Hyper.notifyActivityChanged;
import static com.hchen.appretention.data.method.Hyper.notifyCameraForegroundChange;
import static com.hchen.appretention.data.method.Hyper.notifyCameraForegroundState;
import static com.hchen.appretention.data.method.Hyper.notifyCameraPostProcessState;
import static com.hchen.appretention.data.method.Hyper.onStartJob;
import static com.hchen.appretention.data.method.Hyper.performCompaction;
import static com.hchen.appretention.data.method.Hyper.preloadAppEnqueue;
import static com.hchen.appretention.data.method.Hyper.reclaimBackground;
import static com.hchen.appretention.data.method.Hyper.reclaimMemoryForCamera;
import static com.hchen.appretention.data.method.Hyper.startPreloadApp;
import static com.hchen.appretention.data.method.Hyper.updateCameraBoosterCloudData;
import static com.hchen.appretention.data.method.Hyper.updateScreenState;
import static com.hchen.appretention.data.path.Hyper.ActivityTaskManagerService;
import static com.hchen.appretention.data.path.Hyper.Build;
import static com.hchen.appretention.data.path.Hyper.CameraBooster;
import static com.hchen.appretention.data.path.Hyper.CameraBoosterNew;
import static com.hchen.appretention.data.path.Hyper.CameraOpt;
import static com.hchen.appretention.data.path.Hyper.ControllerActivityInfo;
import static com.hchen.appretention.data.path.Hyper.GameMemoryCleanerDeprecated;
import static com.hchen.appretention.data.path.Hyper.GameMemoryReclaimer;
import static com.hchen.appretention.data.path.Hyper.IAppState$IRunningProcess;
import static com.hchen.appretention.data.path.Hyper.ICameraBooster;
import static com.hchen.appretention.data.path.Hyper.LifecycleConfig;
import static com.hchen.appretention.data.path.Hyper.MemoryFreezeStubImpl;
import static com.hchen.appretention.data.path.Hyper.MemoryStandardProcessControl;
import static com.hchen.appretention.data.path.Hyper.MiuiMemReclaimer;
import static com.hchen.appretention.data.path.Hyper.OomAdjusterImpl;
import static com.hchen.appretention.data.path.Hyper.PreloadAppControllerImpl;
import static com.hchen.appretention.data.path.Hyper.PreloadLifecycle;
import static com.hchen.appretention.data.path.Hyper.PressureStateSettings;
import static com.hchen.appretention.data.path.Hyper.ProcessConfig;
import static com.hchen.appretention.data.path.Hyper.ProcessKillerIdler;
import static com.hchen.appretention.data.path.Hyper.ProcessManagerInternal;
import static com.hchen.appretention.data.path.Hyper.ProcessMemoryCleaner;
import static com.hchen.appretention.data.path.Hyper.ProcessPowerCleaner;
import static com.hchen.appretention.data.path.Hyper.ProcessSceneCleaner;
import static com.hchen.appretention.data.path.Hyper.ServiceThread;
import static com.hchen.appretention.data.path.Hyper.SmartCpuPolicyManager;
import static com.hchen.appretention.data.path.Hyper.SystemPressureController;
import static com.hchen.appretention.data.path.Hyper.SystemServerImpl;
import static com.hchen.appretention.data.path.System.ActivityManagerService;

import android.app.job.JobParameters;
import android.content.Context;

import com.hchen.appretention.data.field.Hyper;
import com.hchen.hooktool.BaseHC;
import com.hchen.hooktool.hook.IHook;

import java.lang.reflect.Method;
import java.util.List;

/**
 * Hyper OS V1
 *
 * @author 焕晨HChen
 */
public class HyperV1 extends BaseHC {
    private static ClassLoader mCameraOptClassLoader = null;

    @Override
    public void init() {
        /*
         * 关闭 scout。
         * */
        /*
         * 禁用意义不大，主要针对内存泄漏的检查。
         * setStaticField(ScoutHelper, ENABLED_SCOUT, false);
         * setStaticField(ActivityThreadImpl, ENABLED_SCOUT, false);
         * setStaticField(ScoutHelper, BINDER_FULL_KILL_PROC, false);
         * setStaticField(ScoutDisplayMemoryManager, ENABLE_SCOUT_MEMORY_MONITOR, false); // 关闭内存监视器
         * setStaticField(ScoutDisplayMemoryManager, SCOUT_MEMORY_DISABLE_GPU, true); // 关闭内存监视器
         * setStaticField(ScoutDisplayMemoryManager, SCOUT_MEMORY_DISABLE_DMABUF, true); // 关闭内存监视器
         * */

        /*
         * 关闭 spc。
         * */
        setStaticField(PressureStateSettings, PROCESS_CLEANER_ENABLED, false);
        setStaticField(PressureStateSettings, PROC_CPU_EXCEPTION_ENABLE, false);
        setStaticField(PressureStateSettings, PROCESS_TRACKER_ENABLE, false);

        /*
         * 禁止为了游戏回收内存。
         *
         * 调用了 GameProcessCompactor, GameProcessKiller 方法 doAction
         * 被调用 GameMemoryCleaner, MiGardService 方法 reclaimMemoryForGameIfNeed
         * */
        hookMethod(GameMemoryReclaimer,
            reclaimBackground,
            long.class,
            doNothing()
        );

        /*
         * 不是低内存设备！
         * */
        hookMethod(Build,
            isMiuiLiteVersion,
            returnResult(false)
        );

        /*
         * 谎报内存等级。
         * */
        hookMethod(Build,
            getDeviceLevelForRAM,
            int.class,
            returnResult(3)
        );

        // 后台限制。
        if (existsMethod(OomAdjusterImpl, getBackgroundAppCount))
            hookMethod(OomAdjusterImpl, getBackgroundAppCount, returnResult(100));

        /*
         * 阻止定期清洁。
         * 由于 PeriodicCleanerService 继承 SystemService 并由如下方法启动；
         * 所以使此方法失效即可彻底禁用 PeriodicCleanerService。
         *
         * 新机型 HyperOS1 已删除 PeriodicCleanerService。
         * */
        if (existsMethod(SystemServerImpl, addMiuiPeriodicCleanerService, ActivityTaskManagerService)) {
            hookMethod(SystemServerImpl,
                addMiuiPeriodicCleanerService,
                ActivityTaskManagerService,
                doNothing()
            );
        }

        /*
         * 禁用 MemoryFreezeStubImpl。
         * */
        hookMethod(MemoryFreezeStubImpl,
            isEnable,
            returnResult(false).shouldObserveCall(false)
        );

        /*
         * 禁用 MemoryStandardProcessControl。
         * 它由一个后台 job (MemoryControlServiceImpl) 启动。
         *  */
        chain(MemoryStandardProcessControl, method(isEnable)
            .returnResult(false)

            .method(init, Context.class, ActivityManagerService)
            .returnResult(false)
        );

        /*
         * 禁止系统压力控制器清理内存。
         * */
        setStaticField(SystemPressureController, IS_ENABLE_RECLAIM, false);
        chain(SystemPressureController,
            /*
             * 禁止随屏幕状态启动压力监测器。
             * */
            method(updateScreenState, boolean.class)
                .doNothing()

                /*
                 * 禁止启动内存压力监测器。
                 * */
                .method(nStartPressureMonitor)
                .doNothing()

                /*
                 * 无奖竞猜。
                 * */
                .method(foregroundActivityChangedLocked, ControllerActivityInfo)
                .doNothing().shouldObserveCall(false)
        );

        chain(ProcessPowerCleaner,
            /*
             * 禁止因温度 kill。
             * REASON_AUTO_THERMAL_KILL_ALL_LEVEL_1
             * */
            method(handleThermalKillProc, ProcessConfig)
                .doNothing()

                /*
                 * REASON_AUTO_SLEEP_CLEAN
                 * REASON_AUTO_SYSTEM_ABNORMAL_CLEAN
                 * REASON_AUTO_THERMAL_KILL_ALL_LEVEL_2
                 * */
                .method(handleKillAll, ProcessConfig, boolean.class)
                .doNothing()

                /*
                 * ProcessPolicy.REASON_AUTO_POWER_KILL
                 * ProcessPolicy.REASON_AUTO_THERMAL_KILL
                 * ProcessPolicy.REASON_AUTO_IDLE_KILL
                 */
                .method(handleKillApp, ProcessConfig)
                .returnResult(true)

                /*
                 * 禁止锁屏 kill。
                 * */
                .method(handleAutoLockOff).doNothing()
        );

        /*
         * 是 MiuiMemoryService 几个核心方法。
         * */
        chain(ProcessMemoryCleaner, method(cleanUpMemory, List.class, long.class)
            .returnResult(true)

            .method(killPackage, IAppState$IRunningProcess, int.class, String.class)
            .returnResult(0L)

            .method(killProcess, IAppState$IRunningProcess, int.class, String.class)
            .returnResult(0L)

            .method(killProcessByMinAdj, int.class, String.class, List.class)
            .doNothing()

            .method(checkBackgroundAppException, String.class, int.class)
            .returnResult(0)

            .method(isNeedCompact, IAppState$IRunningProcess).returnResult(false).shouldObserveCall(false)
        );

        chain(ProcessSceneCleaner,
            /*
             * REASON_ONE_KEY_CLEAN
             * REASON_FORCE_CLEAN
             * REASON_GAME_CLEAN
             * REASON_OPTIMIZATION_CLEAN
             * */
            method(handleKillAll, ProcessConfig)
                .hook(new IHook() {
                    @Override
                    public void before() {
                        Object config = getArgs(0);
                        int mPolicy = callMethod(config, getPolicy);
                        if (!ProcessPolicy.getKillReason(mPolicy).equals(ProcessPolicy.REASON_OPTIMIZATION_CLEAN)
                            && !ProcessPolicy.getKillReason(mPolicy).equals(ProcessPolicy.REASON_ONE_KEY_CLEAN))
                            returnNull();
                    }
                })

                /*
                 * REASON_LOCK_SCREEN_CLEAN
                 * REASON_GARBAGE_CLEAN
                 * REASON_USER_DEFINED
                 * */
                .method(handleKillAny, ProcessConfig)
                .doNothing()
        );

        /*
         * 禁止压缩进程。
         * */
        setStaticField(MiuiMemReclaimer, RECLAIM_IF_NEEDED, false);
        hookMethod(MiuiMemReclaimer,
            performCompaction,
            String.class, int.class,
            doNothing()
        );

        /*
         * 管理游戏内存，可能已经弃用。
         *
         * 新机型 HyperOS1 已删除 GameMemoryCleanerDeprecated。
         * */
        if (existsClass(GameMemoryCleanerDeprecated)) {
            hookMethod(GameMemoryCleanerDeprecated,
                killBackgroundApps,
                doNothing()
            );
        }

        /*
         * 禁止 kill 长时间占 cpu 的应用。
         * */
        hookMethod(SmartCpuPolicyManager,
            handleLimitCpuException,
            int.class,
            doNothing()
        );

        /*
         * 禁止空闲 kill。
         * */
        hookMethod(ProcessKillerIdler,
            onStartJob,
            JobParameters.class,
            returnResult(false)
        );

        /*
         * 禁止预启动。
         * */
        chain(PreloadAppControllerImpl, method(preloadAppEnqueue, String.class, boolean.class, LifecycleConfig)
            .doNothing().shouldObserveCall(false)

            .method(startPreloadApp, PreloadLifecycle)
            .hook(new IHook() {
                @Override
                public void before() {
                    setResult(getStaticField(PreloadAppControllerImpl, START_PRELOAD_IS_DISABLE));
                }
            }).shouldObserveCall(false)
        );

        /*
         * 从此开始，下方为针对相机杀后台而 hook 的内容。
         * */
        Class<?> mCameraOpt = findClass(CameraOpt).getNoReport();
        if (mCameraOpt != null) {
            // 帮助 CameraOpt 初始化
            Class<?> mCameraBoosterClazz = getStaticField(mCameraOpt, Hyper.mCameraBoosterClazz);
            Class<?> mQuickCameraClazz = getStaticField(mCameraOpt, Hyper.mQuickCameraClazz);
            if (mCameraBoosterClazz != null || mQuickCameraClazz != null) {
                mCameraOptClassLoader = mCameraBoosterClazz != null ? mCameraBoosterClazz.getClassLoader() : mQuickCameraClazz.getClassLoader();
                doHookCameraOpt(findClass(CameraBooster, mCameraOptClassLoader).get());
            }
        } else {
            hookMethod(ICameraBooster,
                newInstance,
                ProcessManagerInternal, ActivityManagerService, ServiceThread, Context.class,
                new IHook() {
                    @Override
                    public void after() {
                        Object mICameraBooster = getResult();
                        mCameraOptClassLoader = mICameraBooster.getClass().getClassLoader();
                        doHookCameraOpt(findClass(CameraBoosterNew, mCameraOptClassLoader).get());
                    }
                }
            );
        }
    } // END

    // 执行对 cameraOpt 的 hook 动作
    private void doHookCameraOpt(Class<?> cameraBooster) {
        String[] mCameraOptShouldHookMethodList = new String[]{
            boostCameraByThreshold,
            doAdjBoost, // 禁用 adj 加速
            interceptAppRestartIfNeeded, // 不允许限制应用重启
            isAllowAdjBoost, // 返回 true, 防止崩溃
            notifyCameraForegroundChange,
            notifyCameraForegroundState,
            notifyCameraPostProcessState,
            notifyActivityChanged,
            reclaimMemoryForCamera, // 禁止压缩进程
            updateCameraBoosterCloudData // 禁止云更新
        };

        for (String m : mCameraOptShouldHookMethodList) {
            if (existsAnyMethod(cameraBooster, m)) {
                Method method = findAllMethod(cameraBooster, m).first();
                if (method == null) continue;
                if (method.getName().equals(interceptAppRestartIfNeeded)) {
                    hook(method, returnResult(false).shouldObserveCall(false));
                } else if (isAllowAdjBoost.equals(method.getName())) {
                    hook(method, returnResult(true).shouldObserveCall(false));
                } else
                    hook(method, doNothing().shouldObserveCall(false));
            }
        }
    }

    @Override
    public void copy() {
    }

    private static class ProcessPolicy {
        public static final String REASON_ANR = "anr";
        public static final String REASON_AUTO_IDLE_KILL = "AutoIdleKill";
        public static final String REASON_AUTO_LOCK_OFF_CLEAN = "AutoLockOffClean";
        public static final String REASON_AUTO_LOCK_OFF_CLEAN_BY_PRIORITY = "AutoLockOffCleanByPriority";
        public static final String REASON_AUTO_POWER_KILL = "AutoPowerKill";
        public static final String REASON_AUTO_SLEEP_CLEAN = "AutoSleepClean";
        public static final String REASON_AUTO_SYSTEM_ABNORMAL_CLEAN = "AutoSystemAbnormalClean";
        public static final String REASON_AUTO_THERMAL_KILL = "AutoThermalKill";
        public static final String REASON_AUTO_THERMAL_KILL_ALL_LEVEL_1 = "AutoThermalKillAll1";
        public static final String REASON_AUTO_THERMAL_KILL_ALL_LEVEL_2 = "AutoThermalKillAll2";
        public static final String REASON_CRASH = "crash";
        public static final String REASON_DISPLAY_SIZE_CHANGED = "DisplaySizeChanged";
        public static final String REASON_FORCE_CLEAN = "ForceClean";
        public static final String REASON_GAME_CLEAN = "GameClean";
        public static final String REASON_GARBAGE_CLEAN = "GarbageClean";
        public static final String REASON_LOCK_SCREEN_CLEAN = "LockScreenClean";
        public static final String REASON_LOW_MEMO = "lowMemory";
        public static final String REASON_MIUI_MEMO_SERVICE = "MiuiMemoryService";
        public static final String REASON_ONE_KEY_CLEAN = "OneKeyClean";
        public static final String REASON_OPTIMIZATION_CLEAN = "OptimizationClean";
        public static final String REASON_SCREEN_OFF_CPU_CHECK_KILL = "ScreenOffCPUCheckKill";
        public static final String REASON_SWIPE_UP_CLEAN = "SwipeUpClean";
        public static final String REASON_UNKNOWN = "Unknown";
        public static final String REASON_USER_DEFINED = "UserDefined";

        public static String getKillReason(int policy) {
            return switch (policy) {
                case 1 -> ProcessPolicy.REASON_ONE_KEY_CLEAN;
                case 2 -> ProcessPolicy.REASON_FORCE_CLEAN;
                case 3 -> ProcessPolicy.REASON_LOCK_SCREEN_CLEAN;
                case 4 -> ProcessPolicy.REASON_GAME_CLEAN;
                case 5 -> ProcessPolicy.REASON_OPTIMIZATION_CLEAN;
                case 6 -> ProcessPolicy.REASON_GARBAGE_CLEAN;
                case 7 -> ProcessPolicy.REASON_SWIPE_UP_CLEAN;
                case 10 -> ProcessPolicy.REASON_USER_DEFINED;
                case 11 -> ProcessPolicy.REASON_AUTO_POWER_KILL;
                case 12 -> ProcessPolicy.REASON_AUTO_THERMAL_KILL;
                case 13 -> ProcessPolicy.REASON_AUTO_IDLE_KILL;
                case 14 -> ProcessPolicy.REASON_AUTO_SLEEP_CLEAN;
                case 16 -> ProcessPolicy.REASON_AUTO_SYSTEM_ABNORMAL_CLEAN;
                case 19 -> ProcessPolicy.REASON_AUTO_THERMAL_KILL_ALL_LEVEL_1;
                case 20 -> ProcessPolicy.REASON_AUTO_THERMAL_KILL_ALL_LEVEL_2;
                case 22 -> ProcessPolicy.REASON_SCREEN_OFF_CPU_CHECK_KILL;
                default -> ProcessPolicy.REASON_UNKNOWN;
            };
        }
    }
}
