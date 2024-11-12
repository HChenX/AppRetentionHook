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

import static com.hchen.appretention.data.field.Hyper.BINDER_FULL_KILL_PROC;
import static com.hchen.appretention.data.field.Hyper.ENABLE;
import static com.hchen.appretention.data.field.Hyper.ENABLED_SCOUT;
import static com.hchen.appretention.data.field.Hyper.ENABLE_SCOUT_MEMORY_MONITOR;
import static com.hchen.appretention.data.field.Hyper.PROCESS_CLEANER_ENABLED;
import static com.hchen.appretention.data.field.Hyper.PROCESS_TRACKER_ENABLE;
import static com.hchen.appretention.data.field.Hyper.PROC_CPU_EXCEPTION_ENABLE;
import static com.hchen.appretention.data.field.Hyper.RECLAIM_IF_NEEDED;
import static com.hchen.appretention.data.field.Hyper.SCOUT_MEMORY_DISABLE_DMABUF;
import static com.hchen.appretention.data.field.Hyper.SCOUT_MEMORY_DISABLE_GPU;
import static com.hchen.appretention.data.method.Hyper.boostCameraIfNeeded;
import static com.hchen.appretention.data.method.Hyper.callStaticMethod;
import static com.hchen.appretention.data.method.Hyper.changeProcessMemCgroup;
import static com.hchen.appretention.data.method.Hyper.checkAndFreeze;
import static com.hchen.appretention.data.method.Hyper.checkBackgroundAppException;
import static com.hchen.appretention.data.method.Hyper.checkUnused;
import static com.hchen.appretention.data.method.Hyper.cleanUpMemory;
import static com.hchen.appretention.data.method.Hyper.closeLmkdSocket;
import static com.hchen.appretention.data.method.Hyper.doAdjBoost;
import static com.hchen.appretention.data.method.Hyper.doClean;
import static com.hchen.appretention.data.method.Hyper.doFgTrim;
import static com.hchen.appretention.data.method.Hyper.doReclaimMemory;
import static com.hchen.appretention.data.method.Hyper.foregroundActivityChangedLocked;
import static com.hchen.appretention.data.method.Hyper.getBackgroundAppCount;
import static com.hchen.appretention.data.method.Hyper.getDeviceLevelForRAM;
import static com.hchen.appretention.data.method.Hyper.handleLimitCpuException;
import static com.hchen.appretention.data.method.Hyper.handleScreenOff;
import static com.hchen.appretention.data.method.Hyper.interceptAppRestartIfNeeded;
import static com.hchen.appretention.data.method.Hyper.isMiuiLiteVersion;
import static com.hchen.appretention.data.method.Hyper.killApplication;
import static com.hchen.appretention.data.method.Hyper.killBackgroundApps;
import static com.hchen.appretention.data.method.Hyper.killPackage;
import static com.hchen.appretention.data.method.Hyper.killProcess;
import static com.hchen.appretention.data.method.Hyper.killProcessByMinAdj;
import static com.hchen.appretention.data.method.Hyper.nStartPressureMonitor;
import static com.hchen.appretention.data.method.Hyper.onReceive;
import static com.hchen.appretention.data.method.Hyper.onStartJob;
import static com.hchen.appretention.data.method.Hyper.performCompaction;
import static com.hchen.appretention.data.method.Hyper.powerKillProcess;
import static com.hchen.appretention.data.method.Hyper.reclaimBackground;
import static com.hchen.appretention.data.method.Hyper.reportCleanProcess;
import static com.hchen.appretention.data.method.Hyper.reportStartProcess;
import static com.hchen.appretention.data.method.Hyper.sendDataToLmkd;
import static com.hchen.appretention.data.method.Hyper.updateScreenState;
import static com.hchen.appretention.data.path.Hyper.ActivityThreadImpl;
import static com.hchen.appretention.data.path.Hyper.Build;
import static com.hchen.appretention.data.path.Hyper.CameraAdjAdjuster;
import static com.hchen.appretention.data.path.Hyper.CameraKillPolicy;
import static com.hchen.appretention.data.path.Hyper.CameraLmkdSocket;
import static com.hchen.appretention.data.path.Hyper.CameraMemLimit;
import static com.hchen.appretention.data.path.Hyper.CameraMemoryReclaim;
import static com.hchen.appretention.data.path.Hyper.CameraOpt;
import static com.hchen.appretention.data.path.Hyper.ControllerActivityInfo;
import static com.hchen.appretention.data.path.Hyper.GameMemoryCleanerDeprecated;
import static com.hchen.appretention.data.path.Hyper.GameMemoryReclaimer;
import static com.hchen.appretention.data.path.Hyper.IAppState$IRunningProcess;
import static com.hchen.appretention.data.path.Hyper.MemoryFreezeStubImpl;
import static com.hchen.appretention.data.path.Hyper.MemoryStandardProcessControl;
import static com.hchen.appretention.data.path.Hyper.MiuiMemReclaimer;
import static com.hchen.appretention.data.path.Hyper.OomAdjusterImpl;
import static com.hchen.appretention.data.path.Hyper.PeriodicCleanerService;
import static com.hchen.appretention.data.path.Hyper.PreloadAppControllerImpl;
import static com.hchen.appretention.data.path.Hyper.PressureStateSettings;
import static com.hchen.appretention.data.path.Hyper.ProcessConfig;
import static com.hchen.appretention.data.path.Hyper.ProcessKillerIdler;
import static com.hchen.appretention.data.path.Hyper.ProcessManagerAdapter;
import static com.hchen.appretention.data.path.Hyper.ProcessMemoryCleaner;
import static com.hchen.appretention.data.path.Hyper.ProcessPowerCleaner$ScreenStatusReceiver;
import static com.hchen.appretention.data.path.Hyper.ProcessRecord;
import static com.hchen.appretention.data.path.Hyper.ScoutDisplayMemoryManager;
import static com.hchen.appretention.data.path.Hyper.ScoutHelper;
import static com.hchen.appretention.data.path.Hyper.SmartCpuPolicyManager;
import static com.hchen.appretention.data.path.Hyper.SystemPressureController;

import android.app.job.JobParameters;
import android.content.Context;
import android.content.Intent;

import com.hchen.appretention.data.field.Hyper;
import com.hchen.hooktool.BaseHC;
import com.hchen.hooktool.hook.IHook;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Hyper OS v1
 *
 * @author 焕晨HChen
 */
public class HyperV1 extends BaseHC {
    private static final ArrayList<UnHook> mCameraOptHookList = new ArrayList<>();
    private static ClassLoader mCameraOptClassLoader = null;
    private static boolean isCameraOptFinalHooked = false;

    @Override
    public void init() {
        setStaticField(ScoutHelper, ENABLED_SCOUT, false); // 关闭 scout 功能
        setStaticField(ScoutHelper, BINDER_FULL_KILL_PROC, false); // 关闭 scout 功能
        setStaticField(ScoutDisplayMemoryManager, ENABLE_SCOUT_MEMORY_MONITOR, false); // 关闭内存监视器
        setStaticField(ScoutDisplayMemoryManager, SCOUT_MEMORY_DISABLE_GPU, true); // 关闭内存监视器
        setStaticField(ScoutDisplayMemoryManager, SCOUT_MEMORY_DISABLE_DMABUF, true); // 关闭内存监视器

        setStaticField(ActivityThreadImpl, ENABLED_SCOUT, false); // 关闭 scout 功能

        setStaticField(PreloadAppControllerImpl, ENABLE, false); // 禁止预加载

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
         * 解除后台 app 最大计数限制。
         * */
        hookMethod(OomAdjusterImpl,
            getBackgroundAppCount,
            returnResult(100)
        );

        /*
         * 阻止定期清洁。
         * */
        chain(PeriodicCleanerService, method(doClean,
            int.class, int.class, int.class, String.class)
            .doNothing()

            .method(doFgTrim, int.class)
            .doNothing()

            .method(handleScreenOff)
            .doNothing()

            .method(reportCleanProcess, int.class, int.class, String.class)
            .doNothing()

            .method(reportStartProcess, String.class)
            .doNothing()
        );

        /*
         * 禁止冻结软件
         * */
        chain(MemoryFreezeStubImpl, method(checkUnused)
            .doNothing()

            .method(checkAndFreeze, int.class, String.class)
            .doNothing()
        );

        /*
         * 一个很神奇的东西，我不知道它存在的意义，也不知道它是否应该存在 >.<
         * 就像这个模块一样，存在的意义是什么？
         * 直接禁止。
         * */
        hookMethod(MemoryStandardProcessControl,
            killProcess,
            boolean.class,
            doNothing()
        );

        /*
         * 禁止系统压力控制器清理内存。
         * */
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
                 * 禁止因为阿巴阿巴 kill。
                 * */
                .method(powerKillProcess, ProcessConfig)
                .returnResult(true)

                /*
                 * 无奖竞猜。
                 * */
                .method(foregroundActivityChangedLocked, ControllerActivityInfo)
                .doNothing().shouldObserveCall(false)
        );

        /*
         * 是 MiuiMemoryService 几个核心的 kill 方法。
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
        );

        /*
         * 不要监测屏幕状态！
         * */
        hookMethod(ProcessPowerCleaner$ScreenStatusReceiver,
            onReceive,
            Context.class, Intent.class,
            doNothing()
        );

        /*
         * 禁止压缩进程。
         * */
        hookMethod(MiuiMemReclaimer,
            performCompaction,
            String.class, int.class,
            doNothing()
        );

        setStaticField(MiuiMemReclaimer, RECLAIM_IF_NEEDED, false);

        /*
         * 管理游戏内存，可能已经弃用。
         * */
        hookMethod(GameMemoryCleanerDeprecated,
            killBackgroundApps,
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
         * 关闭 spc。
         * */
        setStaticField(PressureStateSettings, PROCESS_CLEANER_ENABLED, false);
        setStaticField(PressureStateSettings, PROC_CPU_EXCEPTION_ENABLE, false);
        setStaticField(PressureStateSettings, PROCESS_TRACKER_ENABLE, false);

        /*
         * 从此开始，下方为针对相机杀后台而 hook 的内容。
         * */
        Class<?> mCameraOpt = findClass(CameraOpt).get();
        if (mCameraOpt != null) {
            // 帮助 CameraOpt 初始化
            Class<?> mCameraBoosterClazz = getStaticField(mCameraOpt, Hyper.mCameraBoosterClazz);
            Class<?> mQuickCameraClazz = getStaticField(mCameraOpt, Hyper.mQuickCameraClazz);
            if (mCameraBoosterClazz != null || mQuickCameraClazz != null) {
                mCameraOptClassLoader = mCameraBoosterClazz != null ? mCameraBoosterClazz.getClassLoader() : mQuickCameraClazz.getClassLoader();

                doHookCameraOpt(mCameraOptClassLoader);
                isCameraOptFinalHooked = false;
            }
        }

        hookMethod(CameraOpt,
            callStaticMethod,
            Class.class, String.class, Object[].class,
            new IHook() {
                @Override
                public void before() {
                    if (isCameraOptFinalHooked)
                        return;
                    Class<?> clazz = getArgs(0);
                    if (clazz == null) return;
                    if (mCameraOptClassLoader == null) {
                        mCameraOptClassLoader = clazz.getClassLoader();
                        doHookCameraOpt(mCameraOptClassLoader);
                    } else {
                        if (!mCameraOptClassLoader.equals(clazz.getClassLoader())) {
                            if (!mCameraOptHookList.isEmpty()) {
                                mCameraOptHookList.forEach(unHook -> {
                                    if (unHook != null)
                                        unHook.unHook();
                                });
                                mCameraOptHookList.clear();
                            }
                            doHookCameraOpt(mCameraOptClassLoader);
                        }
                    }
                    isCameraOptFinalHooked = true;
                    removeSelf(); // 解除 hook
                }
            }
        );
    } // END

    // 执行对 cameraOpt 的 hook 动作，保持同步
    private synchronized void doHookCameraOpt(ClassLoader classLoader) {
        UnHook[] unHooks = new UnHook[]{
            /*
            * 不需要加速相机。
            *
            * 调用了 CameraKillPolicy 方法 boostCameraWithProtect
            * 被调用 CameraKillPolicy 方法 boostCameraByThreshold, notifyCameraForegroundState,
            *       CameraBoostHandler 方法 handleMessage
            * */
            hookMethod(CameraKillPolicy,
                classLoader,
                boostCameraIfNeeded,
                Context.class, long.class, int.class,
                doNothing()
            ),

            /*
            * 不需要回收内存。
            *
            * 调用了 CameraMemoryReclaim 方法 findProcessToKill, runMmsCompaction, runOtherCompaction, trimProcessMemory
            * 被调用 CameraMemoryReclaim 方法 reclaimMemoryForCamera
            * */
            hookMethod(CameraMemoryReclaim,
                classLoader,
                doReclaimMemory,
                Context.class, int.class, int.class, int.class,
                doNothing()
            ),

            /*
            * 不要阻止进程重新启动。
            * */
            hookMethod(CameraKillPolicy,
                classLoader,
                interceptAppRestartIfNeeded,
                String.class, String.class,
                returnResult(false).shouldObserveCall(false)
            ),

            /*
            * 禁止将数据发送至 lmkd。
            *
            * 调用链复杂众多，略过。
            * */
            hookMethod(CameraLmkdSocket,
                classLoader,
                sendDataToLmkd,
                ByteBuffer.class,
                new IHook() {
                    @Override
                    public void before() {
                        callThisMethod(closeLmkdSocket); // 关闭连接
                        setResult(null);
                    }
                }
            ),

            /*
            * 禁止限制进程内存。
            *
            * 调用了 CamOptFileUtils 方法 writeToFile
            * 被调用 CameraMemLimit 方法 restoreProcessMemCgroup, getMemLimitAppProcessList
            * */
            existsClass(CameraMemLimit) ? hookMethod(CameraMemLimit,
                classLoader,
                changeProcessMemCgroup,
                Integer.class, String.class,
                doNothing()
            ) : null,

            /*
            * cameraOpt 的表面上最终的 kill 调用点。
            * */
            hookMethod(ProcessManagerAdapter,
                classLoader,
                killApplication,
                ProcessRecord,
                doNothing()
            ),

            /*
            * 禁止 adj 加速。
            * */
            hookMethod(CameraAdjAdjuster,
                classLoader,
                doAdjBoost,
                String.class, int.class, long.class, int.class, boolean.class,
                doNothing()
            )
        };
        mCameraOptHookList.addAll(Arrays.asList(unHooks));
    }

    @Override
    public void copy() {
    }
}
