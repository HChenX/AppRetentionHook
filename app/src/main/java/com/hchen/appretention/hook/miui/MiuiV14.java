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
package com.hchen.appretention.hook.miui;

import static com.hchen.appretention.data.field.HyperField.IS_ENABLE_RECLAIM;
import static com.hchen.appretention.data.field.HyperField.PROCESS_CLEANER_ENABLED;
import static com.hchen.appretention.data.field.HyperField.PROC_CPU_EXCEPTION_ENABLE;
import static com.hchen.appretention.data.field.HyperField.RECLAIM_IF_NEEDED;
import static com.hchen.appretention.data.field.HyperField.sCompactSingleProcEnable;
import static com.hchen.appretention.data.field.HyperField.sCompactionEnable;
import static com.hchen.appretention.data.method.HyperMethod.cleanUpMemory;
import static com.hchen.appretention.data.method.HyperMethod.doClean;
import static com.hchen.appretention.data.method.HyperMethod.getDeviceLevelForRAM;
import static com.hchen.appretention.data.method.HyperMethod.handleAutoLockOff;
import static com.hchen.appretention.data.method.HyperMethod.handleKillAll;
import static com.hchen.appretention.data.method.HyperMethod.handleKillApp;
import static com.hchen.appretention.data.method.HyperMethod.handleLimitCpuException;
import static com.hchen.appretention.data.method.HyperMethod.handleScreenOff;
import static com.hchen.appretention.data.method.HyperMethod.handleThermalKillProc;
import static com.hchen.appretention.data.method.HyperMethod.isMiuiLiteVersion;
import static com.hchen.appretention.data.method.HyperMethod.killPackage;
import static com.hchen.appretention.data.method.HyperMethod.killProcess;
import static com.hchen.appretention.data.method.HyperMethod.killProcessByMinAdj;
import static com.hchen.appretention.data.method.HyperMethod.nStartPressureMonitor;
import static com.hchen.appretention.data.method.HyperMethod.onStartJob;
import static com.hchen.appretention.data.method.HyperMethod.performCompaction;
import static com.hchen.appretention.data.method.HyperMethod.preloadAppEnqueue;
import static com.hchen.appretention.data.method.HyperMethod.reclaimBackground;
import static com.hchen.appretention.data.method.HyperMethod.updateScreenState;
import static com.hchen.appretention.data.path.HyperClass.AppStateManager$AppState$RunningProcess;
import static com.hchen.appretention.data.path.HyperClass.Build;
import static com.hchen.appretention.data.path.HyperClass.GameMemoryReclaimer;
import static com.hchen.appretention.data.path.HyperClass.LifecycleConfig;
import static com.hchen.appretention.data.path.HyperClass.MiuiMemReclaimer;
import static com.hchen.appretention.data.path.HyperClass.MiuiMemoryService;
import static com.hchen.appretention.data.path.HyperClass.PeriodicCleanerService;
import static com.hchen.appretention.data.path.HyperClass.PreloadAppControllerImpl;
import static com.hchen.appretention.data.path.HyperClass.PressureStateSettings;
import static com.hchen.appretention.data.path.HyperClass.ProcessConfig;
import static com.hchen.appretention.data.path.HyperClass.ProcessKillerIdler;
import static com.hchen.appretention.data.path.HyperClass.ProcessMemoryCleaner;
import static com.hchen.appretention.data.path.HyperClass.ProcessPowerCleaner;
import static com.hchen.appretention.data.path.HyperClass.SmartCpuPolicyManager;
import static com.hchen.appretention.data.path.HyperClass.SystemPressureController;
import static com.hchen.appretention.data.prop.SystemProp.FALSE;

import android.app.job.JobParameters;

import com.hchen.appretention.hook.hyper.CameraOpt;
import com.hchen.collect.HookEntrance;
import com.hchen.hooktool.BaseHC;
import com.hchen.hooktool.tool.additional.SystemPropTool;

import java.util.List;

/**
 * Miui14
 *
 * @author 焕晨HChen
 */
@HookEntrance(targetBrand = "Xiaomi", targetPackage = "android", targetOS = 14f)
public class MiuiV14 extends BaseHC {
    @Override
    public void init() {
        /*
         * 关闭 spc。
         * */
        SystemPropTool.setProp("persist.sys.spc.enabled", FALSE);
        SystemPropTool.setProp("persist.sys.spc.cpuexception.enable", FALSE);
        setStaticField(PressureStateSettings, PROCESS_CLEANER_ENABLED, false);
        setStaticField(PressureStateSettings, PROC_CPU_EXCEPTION_ENABLE, false);
        // setStaticField(PressureStateSettings, PROCESS_TRACKER_ENABLE, false); // Miui14 不包含

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

        /*
         * 禁用 PeriodicCleanerService, Only Miui.
         * */
        SystemPropTool.setProp("persist.sys.periodic.enable", FALSE);
        hookMethod(PeriodicCleanerService,
            handleScreenOff,
            doNothing()
        );
        hookMethod(PeriodicCleanerService,
            doClean,
            int.class, int.class, int.class, String.class,
            doNothing()
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
            // Changed: Miui14 不包含
            // .method(foregroundActivityChangedLocked, ControllerActivityInfo)
            // .doNothing()
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
         * 禁止压缩进程。
         * */
        SystemPropTool.setProp("persist.sys.mms.compact_enable", FALSE);
        SystemPropTool.setProp("persist.sys.mms.single_compact_enable", FALSE);

        setStaticField(MiuiMemReclaimer, RECLAIM_IF_NEEDED, false);
        setStaticField(MiuiMemoryService, sCompactionEnable, false);
        setStaticField(MiuiMemoryService, sCompactSingleProcEnable, false);
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
                .doNothing()

            // Changed: 多余的 Hook。
            // .method(startPreloadApp, PreloadLifecycle)
            // .hook(new IHook() {
            //     @Override
            //     public void before() {
            //         setResult(getStaticField(PreloadAppControllerImpl, START_PRELOAD_IS_DISABLE));
            //     }
            // })
        );

        /*
         * 是 MiuiMemoryService 几个核心方法。
         * */
        // Changed: Support Miui14
        chain(ProcessMemoryCleaner, method(cleanUpMemory, List.class, long.class)
            .returnResult(true)

            .method(killPackage, AppStateManager$AppState$RunningProcess, int.class, String.class)
            .returnResult(0L)

            .method(killProcess, AppStateManager$AppState$RunningProcess, int.class, String.class)
            .returnResult(0L)

            .method(killProcessByMinAdj, int.class, String.class, List.class)
            .doNothing()

            // Changed: 多余的 Hook。
            // .method(checkBackgroundAppException, String.class, int.class)
            // .returnResult(0)
        );

        CameraOpt.doHook();
    }
}
