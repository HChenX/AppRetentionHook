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
package com.hchen.appretention.hook.system;

import static com.hchen.appretention.data.field.SystemField.CUR_MAX_CACHED_PROCESSES;
import static com.hchen.appretention.data.field.SystemField.MAX_PHANTOM_PROCESSES;
import static com.hchen.appretention.data.field.SystemField.USE_MODERN_TRIM;
import static com.hchen.appretention.data.field.SystemField.mGlobalMaxNumTasks;
import static com.hchen.appretention.data.field.SystemField.mKillBgRestrictedAndCachedIdle;
import static com.hchen.appretention.data.field.SystemField.mMemFactorOverride;
import static com.hchen.appretention.data.field.SystemField.mMinNumVisibleTasks;
import static com.hchen.appretention.data.field.SystemField.mNextNoKillDebugMessageTime;
import static com.hchen.appretention.data.method.SystemMethod.checkExcessivePowerUsageLPr;
import static com.hchen.appretention.data.method.SystemMethod.isInVisibleRange;
import static com.hchen.appretention.data.method.SystemMethod.killProcessesWhenImperceptible;
import static com.hchen.appretention.data.method.SystemMethod.performIdleMaintenance;
import static com.hchen.appretention.data.method.SystemMethod.shouldKillExcessiveProcesses;
import static com.hchen.appretention.data.method.SystemMethod.trimInactiveRecentTasks;
import static com.hchen.appretention.data.method.SystemMethod.trimPhantomProcessesIfNecessary;
import static com.hchen.appretention.data.method.SystemMethod.updateAndTrimProcessLSP;
import static com.hchen.appretention.data.method.SystemMethod.updateKillBgRestrictedCachedIdle;
import static com.hchen.appretention.data.method.SystemMethod.updateMaxCachedProcesses;
import static com.hchen.appretention.data.method.SystemMethod.updateMaxPhantomProcesses;
import static com.hchen.appretention.data.method.SystemMethod.updatePerfConfigConstants;
import static com.hchen.appretention.data.method.SystemMethod.updateProcessCpuStatesLocked;
import static com.hchen.appretention.data.method.SystemMethod.updateUseModernTrim;
import static com.hchen.appretention.data.path.SystemClass.ActiveUids;
import static com.hchen.appretention.data.path.SystemClass.ActivityManagerConstants;
import static com.hchen.appretention.data.path.SystemClass.ActivityManagerService;
import static com.hchen.appretention.data.path.SystemClass.AppProfiler;
import static com.hchen.appretention.data.path.SystemClass.LowMemDetector;
import static com.hchen.appretention.data.path.SystemClass.OomAdjuster;
import static com.hchen.appretention.data.path.SystemClass.PhantomProcessList;
import static com.hchen.appretention.data.path.SystemClass.ProcessCpuTracker;
import static com.hchen.appretention.data.path.SystemClass.ProcessList;
import static com.hchen.appretention.data.path.SystemClass.ProcessRecord;
import static com.hchen.appretention.data.path.SystemClass.RecentTasks;
import static com.hchen.appretention.data.path.SystemClass.Task;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.hchen.appretention.hook.system.opt.ApplyAdjOpt;
import com.hchen.appretention.hook.system.opt.CacheCompaction;
import com.hchen.appretention.hook.system.opt.OomLevelsOpt;
import com.hchen.collect.HookEntrance;
import com.hchen.hooktool.HCBase;
import com.hchen.hooktool.hook.IHook;

/**
 * 安卓 13
 *
 * @author 焕晨HChen
 */
@HookEntrance(targetPackage = "android", targetSdks = 33)
public class AndroidT extends HCBase {
    @Override
    public void init() {
        OomLevelsOpt.init();
        CacheCompaction.init();
        ApplyAdjOpt.init();

        // ----------- ProcessList ----------------------
        /*
         * 将不可感知的进程添加进列表 mWorkItems (ProcessList$ImperceptibleKillRunner)
         * 并由 ProcessList$ImperceptibleKillRunner 中 handleDeviceIdle 等方法处理。
         *
         * 调用了 ActivityManagerService 方法 killProcessesWhenImperceptible
         * 被调用 ImperceptibleKillRunner 方法 enqueueLocked
         * */
        hookMethod(ProcessList,
            killProcessesWhenImperceptible,
            int[].class, String.class, int.class,
            doNothing()
        );

        // ----------------- PhantomProcessList ---------------
        /*
         * 根据进程 CPU 状态创建影子进程列表。
         * 根据调用链观察本方法是 PhantomProcessList 的核心方法。
         * 由此方法将 PhantomProcessList 中各种列表字段进行 put 装填。
         * hook 此方法后，PhantomProcessList 基本失效。
         *
         * 调用了 PhantomProcessList 方法 lookForPhantomProcessesLocked、
         *      getOrCreatePhantomProcessIfNeededLocked、pruneStaleProcessesLocked
         *
         * 被调用 AppProfiler 方法 updateCpuStatsNow
         * */
        hookMethod(PhantomProcessList,
            updateProcessCpuStatesLocked,
            ProcessCpuTracker,
            doNothing()
        );

        /*
         * 修剪影子进程的方法。
         * hook updateProcessCpuStatesLocked 后，此方法基本失效，做保险使用。
         * */
        hookMethod(PhantomProcessList,
            trimPhantomProcessesIfNecessary,
            doNothing()
        );

        // ----------- ActivityManagerService ------------
        /*
         * 检查进程是否存在过高的电量消耗。
         *
         * 调用了 ActivityManagerService 方法
         * updateAppProcessCpuTimeLPr、updatePhantomProcessCpuTimeLPr
         * */
        hookMethod(ActivityManagerService,
            checkExcessivePowerUsageLPr,
            long.class, boolean.class, long.class,
            String.class, String.class, int.class,
            ProcessRecord,
            returnResult(false)
        );

        /*
         * 禁止空闲清理。
         * */
        hookMethod(ActivityManagerService,
            performIdleMaintenance,
            doNothing()
        );

        // ------------ AppProfiler ------------

        /*
         * 使 mMemFactorOverride 初始化为 0。
         *
         * 虽然 setMemFactorOverrideLocked 可能会改变其参数值，但几乎不会被触发。
         * */
        hookConstructor(AppProfiler,
            ActivityManagerService, Looper.class, LowMemDetector,
            new IHook() {
                @Override
                public void after() {
                    setThisField(mMemFactorOverride, 0);
                }
            }
        );

        // ------------- OomAdjuster -------------
        /*
         * 是否允许 kill 过量的 cached/empty 进程。
         *
         * 被调用 OomAdjuster 方法 updateAndTrimProcessLSP
         * */
        hookMethod(OomAdjuster,
            shouldKillExcessiveProcesses,
            long.class,
            returnResult(false)
        );

        /*
         * 更新和修剪进程。
         * 设置此方法第三个参数为 0L，是为了使以下代码返回假：
         * app.getLastActivityTime() < oldTime
         * */
        hookMethod(OomAdjuster,
            updateAndTrimProcessLSP,
            long.class, long.class, long.class,
            ActiveUids, // int.class, AndroidT 不包含
            new IHook() {
                @Override
                public void before() {
                    setThisField(mNextNoKillDebugMessageTime, Long.MAX_VALUE); // 处理频繁的日志
                    // setArgs(2, 0L); // 不保护空进程
                }
            }
        );

        // ------------ RecentTasks ---------------
        /*
         * 修剪最近不活跃的任务卡片。
         * 设置 mGlobalMaxNumTasks 为 MAX_VALUE 可防止它从列表中删除 task。
         * */
        hookMethod(RecentTasks,
            trimInactiveRecentTasks,
            new IHook() {
                @Override
                public void before() {
                    setThisField(mGlobalMaxNumTasks, Integer.MAX_VALUE);
                }
            }
        );

        /*
         * 是否使处于可见范围。
         * 设置 mMinNumVisibleTasks 为最大值则可以解除限制。
         * */
        hookMethod(RecentTasks,
            isInVisibleRange,
            Task, int.class, int.class, boolean.class,
            new IHook() {
                @Override
                public void before() {
                    setThisField(mMinNumVisibleTasks, Integer.MAX_VALUE);
                }
            }
        );

        // ----------- ActivityManagerConstants -------------
        /*
         * 各种基本常量设置。
         * */
        buildChain(ActivityManagerConstants)
            .findConstructor(
                Context.class, ActivityManagerService, Handler.class)
            .hook(new IHook() {
                @Override
                public void after() {
                    setThisField(CUR_MAX_CACHED_PROCESSES, 6144); // 最大缓存进程数
                    // setThisField(CUR_MAX_EMPTY_PROCESSES, (6144 / 6)); // 最大空进程数。Changed: 不要更改空进程限制
                    // setThisField(CUR_TRIM_CACHED_PROCESSES, -1); // 修剪缓存进程数 (别问为啥是 -1。Changed: 不需要修改
                    // setThisField(CUR_TRIM_EMPTY_PROCESSES, Integer.MAX_VALUE); // 修剪空进程数 (别问为啥是又是 max 了。Changed: 不要更改空进程限制
                    // setThisField(MAX_CACHED_PROCESSES, Integer.MAX_VALUE); // 最大缓存进程数量。Changed: 没用的修改
                    setThisField(MAX_PHANTOM_PROCESSES, Integer.MAX_VALUE); // 最大虚幻进程数量
                    setThisField(mKillBgRestrictedAndCachedIdle, false); // 禁止 kill 后台受限和缓存空闲的应用

                    if (existsField(getMember().getDeclaringClass(), USE_MODERN_TRIM))
                        setThisField(USE_MODERN_TRIM, true); // 使用现代 trim。Note: AndroidV 删除
                }
            })

            /* 一般情况不会被主动调用，仅保险使用 */
            .findMethod(updateKillBgRestrictedCachedIdle)
            .doNothing()

            .findMethodIfExist(updateUseModernTrim) // Note: AndroidV 删除
            .doNothing()

            /*.method(updateProactiveKillsEnabled)
            .doNothing()*/ // AndroidT 不包含

            .findMethod(updateMaxCachedProcesses)
            .doNothing()

            .findMethod(updateMaxPhantomProcesses)
            .doNothing()

            .findMethodIfExist(updatePerfConfigConstants) // 高通的东西
            .doNothing();

        /*
         * 禁止主动杀戮。
         * */
        // AndroidT 不包含
        // setStaticField(ActivityManagerConstants, PROACTIVE_KILLS_ENABLED, false);
    }
}
