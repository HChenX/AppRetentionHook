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

 * Copyright (C) 2023-2024 HChenX
 */
package com.hchen.appretention.hook.system;

import static com.hchen.appretention.data.field.System.CUR_MAX_CACHED_PROCESSES;
import static com.hchen.appretention.data.field.System.MAX_PHANTOM_PROCESSES;
import static com.hchen.appretention.data.field.System.PROACTIVE_KILLS_ENABLED;
import static com.hchen.appretention.data.field.System.USE_MODERN_TRIM;
import static com.hchen.appretention.data.field.System.mGlobalMaxNumTasks;
import static com.hchen.appretention.data.field.System.mKillBgRestrictedAndCachedIdle;
import static com.hchen.appretention.data.field.System.mMemFactorOverride;
import static com.hchen.appretention.data.field.System.mNextNoKillDebugMessageTime;
import static com.hchen.appretention.data.method.System.checkExcessivePowerUsageLPr;
import static com.hchen.appretention.data.method.System.isInVisibleRange;
import static com.hchen.appretention.data.method.System.killProcessesWhenImperceptible;
import static com.hchen.appretention.data.method.System.performIdleMaintenance;
import static com.hchen.appretention.data.method.System.shouldKillExcessiveProcesses;
import static com.hchen.appretention.data.method.System.trimInactiveRecentTasks;
import static com.hchen.appretention.data.method.System.trimPhantomProcessesIfNecessary;
import static com.hchen.appretention.data.method.System.updateAndTrimProcessLSP;
import static com.hchen.appretention.data.method.System.updateKillBgRestrictedCachedIdle;
import static com.hchen.appretention.data.method.System.updateMaxCachedProcesses;
import static com.hchen.appretention.data.method.System.updateMaxPhantomProcesses;
import static com.hchen.appretention.data.method.System.updatePerfConfigConstants;
import static com.hchen.appretention.data.method.System.updateProactiveKillsEnabled;
import static com.hchen.appretention.data.method.System.updateProcessCpuStatesLocked;
import static com.hchen.appretention.data.method.System.updateUseModernTrim;
import static com.hchen.appretention.data.path.System.ActiveUids;
import static com.hchen.appretention.data.path.System.ActivityManagerConstants;
import static com.hchen.appretention.data.path.System.ActivityManagerService;
import static com.hchen.appretention.data.path.System.AppProfiler;
import static com.hchen.appretention.data.path.System.LowMemDetector;
import static com.hchen.appretention.data.path.System.OomAdjuster;
import static com.hchen.appretention.data.path.System.PhantomProcessList;
import static com.hchen.appretention.data.path.System.ProcessCpuTracker;
import static com.hchen.appretention.data.path.System.ProcessList;
import static com.hchen.appretention.data.path.System.ProcessRecord;
import static com.hchen.appretention.data.path.System.RecentTasks;
import static com.hchen.appretention.data.path.System.Task;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.hchen.hooktool.BaseHC;
import com.hchen.hooktool.hook.IHook;
import com.hchen.processor.HookEntrance;

/**
 * 安卓 13
 *
 * @author 焕晨HChen
 */
@HookEntrance(targetPackage = "android", targetSdk = 33)
public class AndroidT extends BaseHC {
    @Override
    public void init() {
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
            doNothing());

        /*
         * ProcessList$ImperceptibleKillRunner 类内部的私有进程 kill 方法。
         *
         * 调用了 ProcessList$ImperceptibleKillRunner 方法 handleDeviceIdle、handleUidStateChanged
         *
         * Changed: 多余的 Hook
         * */
        /*
         * hookMethod(ProcessList$ImperceptibleKillRunner,
         *    killProcessLocked,
         *    int.class, int.class, long.class, String.class, int.class,
         *    DropBoxManager.class, boolean.class,
         *    returnResult(true)
         * );
         * */

        /*
         * Warning: test hook!!
         *
         * 获取 mOomAdj、mOomMinFree 具体值。
         * K50 12G
         * oom adj: [0, 100, 200, 250, 900, 950], oom min free: [73728, 92160, 110592, 129024, 221184, 322560]
         * -900 75497472 -800 75497472 -700 75497472 0 75497472 100 94371840 200 113246208
         * 225 132120576 250 132120576 300 226492416 400 226492416 500 226492416 600 226492416 700 226492416
         * 800 226492416 900 226492416 999 330301440
         * */
        /*
         * hook(ProcessList,
         * getMemLevel,
         * int.class,
         * new IHook() {
         *    @Override
         *    public void after() {
         *      int[] mOomAdj = getThisField("mOomAdj");
         *      int[] mOomMinFree = getThisField("mOomMinFree");
         *      logI(TAG, "oom adj: " + Arrays.toString(mOomAdj)
         *           + " oom min free: " + Arrays.toString(mOomMinFree));
         *      logI(TAG, "memlevel: " + getResult());
         *     }
         *   }
         * );
         * */

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
                public void before() {
                    setThisField(mMemFactorOverride, 0);
                }
            }
        );

        /*
         * 保持 mMemFactorOverride 为 0；
         * 即可使 memFactor 保持为 0。
         *
         * Changed: 多余的 Hook
         * */
        /* hookMethod(AppProfiler,
         *    updateLowMemStateLSP,
         *    int.class, int.class, int.class, long.class,
         *    new IHook() {
         *       @Override
         *       public void before() {
         *           setThisField(mMemFactorOverride, 0);
         *       }
         *    }.shouldObserveCall(false)
         * );
         * */

        // 废弃的
        /*
         * 返回指定内存因子 0，代表内存正常。
         * 0 -> 内存正常。
         * 1 -> 内存偏低。
         * 2 -> 内存低。
         * 3 -> 内存极低。
         * 内存因子决定系统是否会对应用进行内存修剪。
         *
         * 被调用 AppProfiler 方法 updateLowMemStateLSP
         * */
        /*
         *hook(LowMemDetector,
         *   getMemFactor,
         *   returnResult(0).shouldObserveCall(false)
         *);
         * */

        /*
         * 为不支持 LowMemDetector 功能的设备伪装支持。
         * */
        /*
         *hook(LowMemDetector,
         *   isAvailable,
         *   returnResult(true).shouldObserveCall(false)
         *);
         * */

        // ------------- OomAdjuster -------------
        /*
         * 是否允许 kill 过量的 cached/empty 进程。
         *
         * 被调用 OomAdjuster 方法 updateAndTrimProcessLSP
         * */
        hookMethod(OomAdjuster,
            shouldKillExcessiveProcesses,
            long.class,
            returnResult(false).shouldObserveCall(false)
        );

        /*
         * 更新和修剪进程。
         * 设置此方法第三个参数为 0L，是为了使以下代码返回假：
         * app.getLastActivityTime() < oldTime
         * */
        hookMethod(OomAdjuster,
            updateAndTrimProcessLSP,
            long.class, long.class, long.class,
            ActiveUids, int.class,
            new IHook() {
                @Override
                public void before() {
                    setThisField(mNextNoKillDebugMessageTime, Long.MAX_VALUE); // 处理频繁的日志
                    // setArgs(2, 0L); // 不保护空进程
                }
            }.shouldObserveCall(false)
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
            }.shouldObserveCall(false)
        );

        /*
         * 是否使处于可见范围。
         * 当超过可见范围或非活动时间超出阈值时, 部分任务将会变得不可见。
         * 且超出可见范围的检查优于时间范围检查，所以使 numVisibleTasks 为 0 即可使其始终处于可见范围。
         * */
        hookMethod(RecentTasks,
            isInVisibleRange,
            Task, int.class, int.class, boolean.class,
            new IHook() {
                @Override
                public void before() {
                    setArgs(2, 0);
                }
            }.shouldObserveCall(false)
        );

        // ----------- ActivityManagerConstants -------------
        /*
         * 各种基本常量设置。
         * */
        chain(ActivityManagerConstants, constructor(
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

                    if (existsField(mClass, USE_MODERN_TRIM))
                        setThisField(USE_MODERN_TRIM, true); // 使用现代 trim。Note: AndroidV 删除
                }
            })

            /* 一般情况不会被主动调用，仅保险使用 */
            .method(updateKillBgRestrictedCachedIdle)
            .doNothing()

            .methodIfExist(updateUseModernTrim) // Note: AndroidV 删除
            .doNothing()

            .method(updateProactiveKillsEnabled)
            .doNothing()

            .method(updateMaxCachedProcesses)
            .doNothing()

            .method(updateMaxPhantomProcesses)
            .doNothing()

            .methodIfExist(updatePerfConfigConstants) // 高通的东西
            .doNothing()
        );

        /*
         * 禁止主动杀戮。
         * */
        setStaticField(ActivityManagerConstants, PROACTIVE_KILLS_ENABLED, false);
    }
}
