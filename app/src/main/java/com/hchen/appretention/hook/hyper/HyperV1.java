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

 * Copyright (C) 2023-2025 HChenX
 */
package com.hchen.appretention.hook.hyper;

import static com.hchen.appretention.data.field.HyperField.IS_ENABLE_RECLAIM;
import static com.hchen.appretention.data.field.HyperField.PROCESS_CLEANER_ENABLED;
import static com.hchen.appretention.data.field.HyperField.PROCESS_TRACKER_ENABLE;
import static com.hchen.appretention.data.field.HyperField.PROC_CPU_EXCEPTION_ENABLE;
import static com.hchen.appretention.data.field.HyperField.RECLAIM_IF_NEEDED;
import static com.hchen.appretention.data.field.HyperField.sCompactSingleProcEnable;
import static com.hchen.appretention.data.field.HyperField.sCompactionEnable;
import static com.hchen.appretention.data.method.HyperMethod.addMiuiPeriodicCleanerService;
import static com.hchen.appretention.data.method.HyperMethod.getBackgroundAppCount;
import static com.hchen.appretention.data.method.HyperMethod.getDeviceLevelForRAM;
import static com.hchen.appretention.data.method.HyperMethod.handleAutoLockOff;
import static com.hchen.appretention.data.method.HyperMethod.handleKillAll;
import static com.hchen.appretention.data.method.HyperMethod.handleKillApp;
import static com.hchen.appretention.data.method.HyperMethod.handleLimitCpuException;
import static com.hchen.appretention.data.method.HyperMethod.handleThermalKillProc;
import static com.hchen.appretention.data.method.HyperMethod.isEnable;
import static com.hchen.appretention.data.method.HyperMethod.isMiuiLiteVersion;
import static com.hchen.appretention.data.method.HyperMethod.killBackgroundApps;
import static com.hchen.appretention.data.method.HyperMethod.killPackage;
import static com.hchen.appretention.data.method.HyperMethod.killProcess;
import static com.hchen.appretention.data.method.HyperMethod.killProcessByMinAdj;
import static com.hchen.appretention.data.method.HyperMethod.nStartPressureMonitor;
import static com.hchen.appretention.data.method.HyperMethod.onStartJob;
import static com.hchen.appretention.data.method.HyperMethod.performCompaction;
import static com.hchen.appretention.data.method.HyperMethod.preloadAppEnqueue;
import static com.hchen.appretention.data.method.HyperMethod.reclaimBackground;
import static com.hchen.appretention.data.method.HyperMethod.scanProcessAndCleanUpMemory;
import static com.hchen.appretention.data.method.HyperMethod.updateScreenState;
import static com.hchen.appretention.data.path.HyperClass.ActivityTaskManagerService;
import static com.hchen.appretention.data.path.HyperClass.Build;
import static com.hchen.appretention.data.path.HyperClass.GameMemoryCleanerDeprecated;
import static com.hchen.appretention.data.path.HyperClass.GameMemoryReclaimer;
import static com.hchen.appretention.data.path.HyperClass.IAppState$IRunningProcess;
import static com.hchen.appretention.data.path.HyperClass.LifecycleConfig;
import static com.hchen.appretention.data.path.HyperClass.MemoryFreezeStubImpl;
import static com.hchen.appretention.data.path.HyperClass.MemoryStandardProcessControl;
import static com.hchen.appretention.data.path.HyperClass.MiuiMemReclaimer;
import static com.hchen.appretention.data.path.HyperClass.MiuiMemoryService;
import static com.hchen.appretention.data.path.HyperClass.OomAdjusterImpl;
import static com.hchen.appretention.data.path.HyperClass.PreloadAppControllerImpl;
import static com.hchen.appretention.data.path.HyperClass.PressureStateSettings;
import static com.hchen.appretention.data.path.HyperClass.ProcessConfig;
import static com.hchen.appretention.data.path.HyperClass.ProcessKillerIdler;
import static com.hchen.appretention.data.path.HyperClass.ProcessMemoryCleaner;
import static com.hchen.appretention.data.path.HyperClass.ProcessPowerCleaner;
import static com.hchen.appretention.data.path.HyperClass.SmartCpuPolicyManager;
import static com.hchen.appretention.data.path.HyperClass.SystemPressureController;
import static com.hchen.appretention.data.path.HyperClass.SystemServerImpl;
import static com.hchen.appretention.data.prop.SystemProp.FALSE;
import static com.hchen.appretention.data.prop.SystemProp.ZERO;

import android.app.job.JobParameters;

import com.hchen.hooktool.BaseHC;
import com.hchen.hooktool.tool.additional.SystemPropTool;
import com.hchen.processor.HookEntrance;

import java.util.List;

/**
 * Hyper OS V1
 *
 * @author 焕晨HChen
 */
@HookEntrance(targetBrand = "Xiaomi", targetPackage = "android", targetOS = 1.0f, isHyperOS = true)
public class HyperV1 extends BaseHC {

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
        SystemPropTool.setProp("persist.sys.spc.enabled", FALSE);
        SystemPropTool.setProp("persist.sys.spc.cpuexception.enable", FALSE);
        SystemPropTool.setProp("persist.sys.spc.process.tracker.enable", FALSE);
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
         * 部分新机型 HyperOSV1 删除了 PeriodicCleanerService。
         * */
        SystemPropTool.setProp("persist.sys.periodic.u.enable", FALSE);
        SystemPropTool.setProp("persist.sys.periodic.u.startprocess.enable", FALSE);

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
        SystemPropTool.setProp("persist.miui.extm.enable", ZERO);
        SystemPropTool.setProp("persist.sys.mfz.enable", FALSE);
        hookMethod(MemoryFreezeStubImpl,
            isEnable,
            returnResult(false).shouldObserveCall(false)
        );

        /*
         * 禁用 MemoryStandardProcessControl。
         *  */
        SystemPropTool.setProp("persist.sys.memory_standard.enable", FALSE);
        chain(MemoryStandardProcessControl, method(isEnable)
                .returnResult(false)

            // .method(init, Context.class, ActivityManagerService)
            // .returnResult(false) // Changed: 多余的 Hook
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
             *
             * Changed: 多余的 hook，PROCESS_CLEANER_ENABLED 设置 false 后即可。
             * */
            // .method(foregroundActivityChangedLocked, ControllerActivityInfo)
            // .doNothing().shouldObserveCall(false)
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
        chain(ProcessMemoryCleaner, method(scanProcessAndCleanUpMemory, long.class) // Changed: 更好的 Hook 点位。
                .returnResult(true)

                .method(killPackage, IAppState$IRunningProcess, int.class, String.class)
                .returnResult(0L)

                .method(killProcess, IAppState$IRunningProcess, int.class, String.class)
                .returnResult(0L)

                .method(killProcessByMinAdj, int.class, String.class, List.class)
                .doNothing()

            // Changed: 多余的 Hook。
            // .method(checkBackgroundAppException, String.class, int.class)
            // .returnResult(0)

            // .method(isNeedCompact, IAppState$IRunningProcess).returnResult(false).shouldObserveCall(false)
        );

        // chain(ProcessSceneCleaner,
        /*
         * REASON_ONE_KEY_CLEAN (一键清理 > 最近任务/悬浮球)
         * REASON_FORCE_CLEAN (强力清理 > 负一屏)
         * REASON_GAME_CLEAN (游戏清理 > 安全中心)
         * REASON_OPTIMIZATION_CLEAN (优化清理 > 安全中心)
         *
         * Doc: https://dev.mi.com/xiaomihyperos/documentation/detail?pId=1607
         *
         * method(handleKillAll, ProcessConfig)
         *   .hook(new IHook() {
         *       @Override
         *       public void before() {
         *          Object config = getArgs(0);
         *          int mPolicy = callMethod(config, getPolicy);
         *          if (!ProcessPolicy.getKillReason(mPolicy).equals(ProcessPolicy.REASON_OPTIMIZATION_CLEAN)
         *               && !ProcessPolicy.getKillReason(mPolicy).equals(ProcessPolicy.REASON_ONE_KEY_CLEAN))
         *              returnNull();
         *     }
         * })
         * */

        /*
         * REASON_LOCK_SCREEN_CLEAN (锁屏清理 > 安全中心)
         * REASON_GARBAGE_CLEAN (垃圾清理 > 安全中心)
         * REASON_USER_DEFINED
         *
         * Changed: 多余的 Hook
         * */
        /*
         * method(handleKillAny, ProcessConfig)
         *    .hook(new IHook() {
         *        @Override
         *        public void before() {
         *            Object config = getArgs(0);
         *            int mPolicy = (int) callMethod(config, getPolicy);
         *            if (!ProcessPolicy.getKillReason(mPolicy).equals(ProcessPolicy.REASON_GARBAGE_CLEAN))
         *                returnNull();
         *        }
         *     })
         */
        // );

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
         * 禁止压缩进程。
         * */
        SystemPropTool.setProp("persist.sys.mms.compact_enable", FALSE);
        SystemPropTool.setProp("persist.sys.mms.single_compact_enable", FALSE);
        setStaticField(MiuiMemoryService, sCompactionEnable, false);
        setStaticField(MiuiMemoryService, sCompactSingleProcEnable, false);
        setStaticField(MiuiMemReclaimer, RECLAIM_IF_NEEDED, false);
        // Changed: 多余的 Hook。
        // hookMethod(OomAdjusterImpl, compactBackgroundProcess, ProcessRecord, doNothing().shouldObserveCall(false));
        hookMethod(MiuiMemReclaimer,
            performCompaction,
            String.class, int.class,
            doNothing()
        );

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

            // Changed: 多余的 Hook。
            // .method(startPreloadApp, PreloadLifecycle)
            // .hook(new IHook() {
            //     @Override
            //     public void before() {
            //         setResult(getStaticField(PreloadAppControllerImpl, START_PRELOAD_IS_DISABLE));
            //     }
            // }).shouldObserveCall(false)
        );

        CameraOpt.doHook();
    }
}
