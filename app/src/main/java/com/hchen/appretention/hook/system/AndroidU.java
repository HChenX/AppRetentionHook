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
package com.hchen.appretention.hook.system;

import static com.hchen.appretention.data.field.System.CUR_MAX_CACHED_PROCESSES;
import static com.hchen.appretention.data.field.System.CUR_MAX_EMPTY_PROCESSES;
import static com.hchen.appretention.data.field.System.CUR_TRIM_CACHED_PROCESSES;
import static com.hchen.appretention.data.field.System.CUR_TRIM_EMPTY_PROCESSES;
import static com.hchen.appretention.data.field.System.MAX_CACHED_PROCESSES;
import static com.hchen.appretention.data.field.System.MAX_PHANTOM_PROCESSES;
import static com.hchen.appretention.data.field.System.PROACTIVE_KILLS_ENABLED;
import static com.hchen.appretention.data.field.System.USE_MODERN_TRIM;
import static com.hchen.appretention.data.field.System.isChangedOomMinFree;
import static com.hchen.appretention.data.field.System.mCompactionHandler;
import static com.hchen.appretention.data.field.System.mGlobalMaxNumTasks;
import static com.hchen.appretention.data.field.System.mKillBgRestrictedAndCachedIdle;
import static com.hchen.appretention.data.field.System.mMemFactorOverride;
import static com.hchen.appretention.data.field.System.mNextNoKillDebugMessageTime;
import static com.hchen.appretention.data.field.System.mOptRecord;
import static com.hchen.appretention.data.field.System.mPendingCompactionProcesses;
import static com.hchen.appretention.data.field.System.mState;
import static com.hchen.appretention.data.field.System.mUseBootCompact;
import static com.hchen.appretention.data.field.System.mUseCompaction;
import static com.hchen.appretention.data.method.System.checkExcessivePowerUsageLPr;
import static com.hchen.appretention.data.method.System.getCurAdj;
import static com.hchen.appretention.data.method.System.getLastCompactTime;
import static com.hchen.appretention.data.method.System.getSetAdj;
import static com.hchen.appretention.data.method.System.getSetProcState;
import static com.hchen.appretention.data.method.System.hasPendingCompact;
import static com.hchen.appretention.data.method.System.interruptProcCompaction;
import static com.hchen.appretention.data.method.System.isInVisibleRange;
import static com.hchen.appretention.data.method.System.killProcessLocked;
import static com.hchen.appretention.data.method.System.killProcessesWhenImperceptible;
import static com.hchen.appretention.data.method.System.onLmkdConnect;
import static com.hchen.appretention.data.method.System.onOomAdjustChanged;
import static com.hchen.appretention.data.method.System.performIdleMaintenance;
import static com.hchen.appretention.data.method.System.resolveCompactionProfile;
import static com.hchen.appretention.data.method.System.setAppStartingMode;
import static com.hchen.appretention.data.method.System.setHasPendingCompact;
import static com.hchen.appretention.data.method.System.setReqCompactProfile;
import static com.hchen.appretention.data.method.System.setReqCompactSource;
import static com.hchen.appretention.data.method.System.shouldKillExcessiveProcesses;
import static com.hchen.appretention.data.method.System.shouldRssThrottleCompaction;
import static com.hchen.appretention.data.method.System.shouldThrottleMiscCompaction;
import static com.hchen.appretention.data.method.System.shouldTimeThrottleCompaction;
import static com.hchen.appretention.data.method.System.trimInactiveRecentTasks;
import static com.hchen.appretention.data.method.System.trimPhantomProcessesIfNecessary;
import static com.hchen.appretention.data.method.System.updateAndTrimProcessLSP;
import static com.hchen.appretention.data.method.System.updateKillBgRestrictedCachedIdle;
import static com.hchen.appretention.data.method.System.updateLowMemStateLSP;
import static com.hchen.appretention.data.method.System.updateMaxCachedProcesses;
import static com.hchen.appretention.data.method.System.updateMaxPhantomProcesses;
import static com.hchen.appretention.data.method.System.updateOomLevels;
import static com.hchen.appretention.data.method.System.updateProactiveKillsEnabled;
import static com.hchen.appretention.data.method.System.updateProcessCpuStatesLocked;
import static com.hchen.appretention.data.method.System.updateUseCompaction;
import static com.hchen.appretention.data.method.System.updateUseModernTrim;
import static com.hchen.appretention.data.method.System.writeLmkd;
import static com.hchen.appretention.data.path.System.ActiveUids;
import static com.hchen.appretention.data.path.System.ActivityManagerConstants;
import static com.hchen.appretention.data.path.System.ActivityManagerService;
import static com.hchen.appretention.data.path.System.AppProfiler;
import static com.hchen.appretention.data.path.System.CachedAppOptimizer;
import static com.hchen.appretention.data.path.System.CachedAppOptimizer$CompactProfile;
import static com.hchen.appretention.data.path.System.CachedAppOptimizer$CompactSource;
import static com.hchen.appretention.data.path.System.CachedAppOptimizer$DefaultProcessDependencies;
import static com.hchen.appretention.data.path.System.CachedAppOptimizer$MemCompactionHandler;
import static com.hchen.appretention.data.path.System.CachedAppOptimizer$ProcessDependencies;
import static com.hchen.appretention.data.path.System.CachedAppOptimizer$PropertyChangedCallbackForTest;
import static com.hchen.appretention.data.path.System.LowMemDetector;
import static com.hchen.appretention.data.path.System.OomAdjuster;
import static com.hchen.appretention.data.path.System.PhantomProcessList;
import static com.hchen.appretention.data.path.System.ProcessCpuTracker;
import static com.hchen.appretention.data.path.System.ProcessList;
import static com.hchen.appretention.data.path.System.ProcessList$ImperceptibleKillRunner;
import static com.hchen.appretention.data.path.System.ProcessRecord;
import static com.hchen.appretention.data.path.System.RecentTasks;
import static com.hchen.appretention.data.path.System.Task;

import android.content.Context;
import android.os.DropBoxManager;
import android.os.Handler;
import android.os.Looper;

import com.hchen.appretention.data.field.System;
import com.hchen.hooktool.BaseHC;
import com.hchen.hooktool.hook.IHook;

import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * 安卓 14
 *
 * @author 焕晨HChen
 */
public class AndroidU extends BaseHC {
    private static final int OOM_MIN_FREE_DISCOUNT = 4;
    private Object processListInstance = null;

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
            int[].class, java.lang.String.class, int.class,
            doNothing());

        /*
         * ProcessList$ImperceptibleKillRunner 类内部的私有进程 kill 方法。
         *
         * 调用了 ProcessList$ImperceptibleKillRunner 方法 handleDeviceIdle、handleUidStateChanged
         * */
        hookMethod(ProcessList$ImperceptibleKillRunner,
            killProcessLocked,
            int.class, int.class, long.class, String.class, int.class,
            DropBoxManager.class, boolean.class,
            returnResult(true)
        );

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
        /*hook(ProcessList,
            getMemLevel,
            int.class,
            new IHook() {
                @Override
                public void after() {
                    int[] mOomAdj = getThisField("mOomAdj");
                    int[] mOomMinFree = getThisField("mOomMinFree");
                    logI(TAG, "oom adj: " + Arrays.toString(mOomAdj)
                        + " oom min free: " + Arrays.toString(mOomMinFree));
                    logI(TAG, "memlevel: " + getResult());
                }
            }
        );*/


        /*
         * 获取 ProcessList 的实例
         * */
        hookConstructor(ProcessList,
            new IHook() {
                @Override
                public void after() {
                    processListInstance = thisObject();
                }
            }
        );

        /*
         * 当系统连接 lmkd 时会初始化一些 lmkd 参数。
         * 本 hook 将修改系统初始化时写入的 oomMinFree 参数。
         * */
        hookMethod(ProcessList,
            onLmkdConnect,
            OutputStream.class, new IHook() {
                @Override
                public void before() {
                    if (Boolean.TRUE.equals(getThisAdditionalInstanceField(isChangedOomMinFree)))
                        return;
                    int[] mOomMinFree = getThisField(System.mOomMinFree);
                    if (mOomMinFree == null) return;
                    int[] mOomMinFreeArray = Arrays.stream(mOomMinFree).map(operand -> operand / OOM_MIN_FREE_DISCOUNT).toArray();
                    setThisField(System.mOomMinFree, mOomMinFreeArray);
                    setThisAdditionalInstanceField(isChangedOomMinFree, true);
                }
            }
        );

        /*
         * 系统更新 oomLevel 时使用，监控 oomMinFree 更改。
         * */
        hookMethod(ProcessList,
            updateOomLevels,
            int.class, int.class, boolean.class,
            new IHook() {
                @Override
                public void before() {
                    setThisAdditionalInstanceField(isChangedOomMinFree, false);
                }

                @Override
                public void after() {
                    if ((getArgs(2) instanceof Boolean b) && !b) {
                        int[] mOomMinFree = getThisField(System.mOomMinFree);
                        if (mOomMinFree == null) return;
                        int[] mOomMinFreeArray = Arrays.stream(mOomMinFree).map(operand -> operand / OOM_MIN_FREE_DISCOUNT).toArray();
                        setThisField(System.mOomMinFree, mOomMinFreeArray);
                        setThisAdditionalInstanceField(isChangedOomMinFree, true);
                    }
                }
            }
        );

        /*
         * 设置一些 lmkd 参数。
         * */
        hookMethod(ProcessList,
            writeLmkd,
            ByteBuffer.class, ByteBuffer.class,
            new IHook() {
                @Override
                public void before() {
                    ByteBuffer buffer = getArgs(0);
                    ByteBuffer bufCopy = buffer.duplicate();
                    bufCopy.rewind();
                    if (bufCopy.getInt() == 0) {
                        if (processListInstance == null)
                            return;

                        // false 说明 oomMinFree 未被更改。
                        if (Boolean.FALSE.equals(getAdditionalInstanceField(processListInstance, isChangedOomMinFree))) {
                            setOomMinFreeBuf(bufCopy);
                            setArgs(0, buffer);
                        }
                    }
                }

                /*
                 * 设置 OomMinFree 值。
                 * */
                private void setOomMinFreeBuf(ByteBuffer bufCopy) {
                    bufCopy.rewind();
                    bufCopy.putInt(0);
                    int[] mOomAdj = getField(processListInstance, System.mOomAdj);
                    int[] mOomMinFree = getField(processListInstance, System.mOomMinFree);
                    if (mOomMinFree == null || mOomAdj == null)
                        return;

                    int[] mOomMinFreeArray = Arrays.stream(mOomMinFree).map(operand -> operand / OOM_MIN_FREE_DISCOUNT).toArray();
                    setField(processListInstance, System.mOomMinFree, mOomMinFreeArray);
                    setAdditionalInstanceField(processListInstance, isChangedOomMinFree, true);
                    for (int i = 0; i < mOomAdj.length; i++) {
                        bufCopy.putInt(((mOomMinFreeArray[i] * 1024) / 4096));
                        bufCopy.putInt(mOomAdj[i]);
                    }
                }
            }.shouldObserveCall(false)
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
         * */
        hookMethod(AppProfiler,
            updateLowMemStateLSP,
            int.class, int.class, int.class, long.class,
            new IHook() {
                @Override
                public void before() {
                    setThisField(mMemFactorOverride, 0);
                }
            }.shouldObserveCall(false)
        );

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
        /*hook(LowMemDetector,
            getMemFactor,
            returnResult(0).shouldObserveCall(false)
        );*/

        /*
         * 为不支持 LowMemDetector 功能的设备伪装支持。
         * */
        /*hook(LowMemDetector,
            isAvailable,
            returnResult(true).shouldObserveCall(false)
        );*/

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
                    setArgs(2, 0L);
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

        // --------------- CachedAppOptimizer ----------------
        /*
         * 接管系统的压缩流程，并激进化流程。
         * */
        chain(CachedAppOptimizer, method(onOomAdjustChanged,
            int.class, int.class, ProcessRecord).hook(
                new IHook() {
                    private static final Object SOME = getStaticField(CachedAppOptimizer$CompactProfile, System.SOME);
                    private static final Object FULL = getStaticField(CachedAppOptimizer$CompactProfile, System.FULL);
                    private static final Object ANON = getStaticField(CachedAppOptimizer$CompactProfile, System.ANON);
                    private static final Object SHEll = getStaticField(CachedAppOptimizer$CompactSource, System.SHELL);
                    private static final Object APP = getStaticField(CachedAppOptimizer$CompactSource, System.APP);
                    private Object state;
                    private Object optRecord;
                    private Handler compactionHandler;
                    private ArrayList pendingCompactionProcesses;

                    @Override
                    public void before() {
                        Object app = getArgs(2);
                        if (app == null) return;
                        state = getField(app, mState);
                        optRecord = getField(app, mOptRecord);
                        compactionHandler = getThisField(mCompactionHandler);
                        pendingCompactionProcesses = getThisField(mPendingCompactionProcesses);
                        if (getCurAdj() > PrecessAdjInfo.PERCEPTIBLE_APP_ADJ && getCurAdj() < PrecessAdjInfo.PREVIOUS_APP_ADJ) {
                            setReqCompactSource(SHEll);
                            setReqCompactProfile(ANON);
                            if (!hasPendingCompact()) {
                                setHasPendingCompact(true);
                                pendingCompactionProcesses.add(app);
                                compactionHandler.sendMessage(compactionHandler.obtainMessage(1, getCurAdj(), getSetProcState()));
                            }
                        } else if (getCurAdj() >= PrecessAdjInfo.PREVIOUS_APP_ADJ && getCurAdj() <= PrecessAdjInfo.CACHED_APP_MAX_ADJ) {
                            setReqCompactSource(SHEll);
                            setReqCompactProfile(FULL);
                            if (!hasPendingCompact()) {
                                setHasPendingCompact(true);
                                pendingCompactionProcesses.add(app);
                                compactionHandler.sendMessage(compactionHandler.obtainMessage(1, getCurAdj(), getSetProcState()));
                            }
                        }
                        returnNull();
                    }

                    private int getSetAdj() {
                        return callMethod(state, getSetAdj);
                    }

                    private int getCurAdj() {
                        return callMethod(state, getCurAdj);
                    }

                    private int getSetProcState() {
                        return callMethod(state, getSetProcState);
                    }

                    private void setReqCompactProfile(Object obj) {
                        callMethod(optRecord, setReqCompactProfile, obj);
                    }

                    private void setReqCompactSource(Object obj) {
                        callMethod(optRecord, setReqCompactSource, obj);
                    }

                    private boolean hasPendingCompact() {
                        return callMethod(optRecord, hasPendingCompact);
                    }

                    private void setHasPendingCompact(boolean pendingCompact) {
                        callMethod(optRecord, setHasPendingCompact, pendingCompact);
                    }
                }).shouldObserveCall(false)

            .method(resolveCompactionProfile, CachedAppOptimizer$CompactProfile)
            .hook(new IHook() {
                @Override
                public void before() {
                    setResult(getArgs(0));
                }
            }).shouldObserveCall(false)

            .method(updateUseCompaction)
            .hook(new IHook() {
                @Override
                public void after() {
                    setThisField(mUseCompaction, true);
                }
            }).shouldObserveCall(false)

            .constructor(ActivityManagerService,
                CachedAppOptimizer$PropertyChangedCallbackForTest,
                CachedAppOptimizer$ProcessDependencies)
            .hook(new IHook() {
                @Override
                public void after() {
                    setThisField(mUseBootCompact, true);
                }
            }).shouldObserveCall(false)
        );

        chain(CachedAppOptimizer$MemCompactionHandler, /* method(shouldOomAdjThrottleCompaction, ProcessRecord)
            .returnResult(false).shouldObserveCall(false) 进程恢复到可感知状态了 */

            method(shouldThrottleMiscCompaction, ProcessRecord, int.class)
                .returnResult(false).shouldObserveCall(false)

                .method(shouldTimeThrottleCompaction, ProcessRecord, long.class, CachedAppOptimizer$CompactProfile, CachedAppOptimizer$CompactSource)
                .hook(new IHook() {
                    @Override
                    public void before() {
                        Object opt = getField(getArgs(0), mOptRecord);
                        long lastCompactTime = callMethod(opt, getLastCompactTime);
                        long start = getArgs(1);
                        // 10 秒内不允许再次触发。
                        if (lastCompactTime != 0) {
                            if (start - lastCompactTime < 10000) {
                                setResult(true);
                                return;
                            }
                        }
                        setResult(false);
                    }
                }).shouldObserveCall(false)

                .method(shouldRssThrottleCompaction, CachedAppOptimizer$CompactProfile, int.class, String.class, long[].class)
                .hook(new IHook() {
                    @Override
                    public void before() {
                        long[] rssBefore = getArgs(3);
                        long anonRssBefore = rssBefore[2];
                        if (rssBefore[0] == 0 && rssBefore[1] == 0 && rssBefore[2] == 0 && rssBefore[3] == 0) {
                            setResult(true); // 进程可能被杀。
                            return;
                        }
                        if (anonRssBefore < (1024 * 6)) {
                            setResult(true);
                            return;
                        }
                        setResult(false);
                    }
                }).shouldObserveCall(false)
        );

        chain(CachedAppOptimizer$DefaultProcessDependencies, method(interruptProcCompaction)
            .doNothing().shouldObserveCall(false)

            .method(setAppStartingMode, boolean.class)
            .hook(new IHook() {
                @Override
                public void before() {
                    setArgs(0, false);
                }
            }).shouldObserveCall(false)
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
                    setThisField(CUR_MAX_EMPTY_PROCESSES, (6144 / 6)); // 最大空进程数
                    setThisField(CUR_TRIM_CACHED_PROCESSES, -1); // 修剪缓存进程数 (别问为啥是 -1
                    setThisField(CUR_TRIM_EMPTY_PROCESSES, Integer.MAX_VALUE); // 修剪空进程数 (别问为啥是又是 max 了
                    setThisField(MAX_PHANTOM_PROCESSES, Integer.MAX_VALUE); // 最大虚幻进程数量
                    setThisField(MAX_CACHED_PROCESSES, Integer.MAX_VALUE); // 最大缓存进程数量
                    setThisField(USE_MODERN_TRIM, true); // 使用现代 trim
                    setThisField(mKillBgRestrictedAndCachedIdle, false); // 禁止 kill 后台受限和缓存空闲的应用
                }
            })

            /* 一般情况不会被主动调用，仅保险使用 */
            .method(updateKillBgRestrictedCachedIdle)
            .doNothing()

            .method(updateUseModernTrim)
            .doNothing()

            .method(updateProactiveKillsEnabled)
            .doNothing()

            .method(updateMaxCachedProcesses)
            .doNothing()

            .method(updateMaxPhantomProcesses)
            .doNothing()
        );

        /*
         * 禁止主动杀戮。
         * */
        setStaticField(ActivityManagerConstants, PROACTIVE_KILLS_ENABLED, false);
    }

    @Override
    public void copy() {
    }

    private static class PrecessAdjInfo {
        // OOM adjustments for processes in various states:

        // Uninitialized value for any major or minor adj fields
        static final int INVALID_ADJ = -10000;

        // Adjustment used in certain places where we don't know it yet.
        // (Generally this is something that is going to be cached, but we
        // don't know the exact value in the cached range to assign yet.)
        static final int UNKNOWN_ADJ = 1001;

        // This is a process only hosting activities that are not visible,
        // so it can be killed without any disruption.
        static final int CACHED_APP_MAX_ADJ = 999;
        static final int CACHED_APP_MIN_ADJ = 900;

        // This is the oom_adj level that we allow to die first. This cannot be equal to
        // CACHED_APP_MAX_ADJ unless processes are actively being assigned an oom_score_adj of
        // CACHED_APP_MAX_ADJ.
        static final int CACHED_APP_LMK_FIRST_ADJ = 950;

        // Number of levels we have available for different service connection group importance
        // levels.
        static final int CACHED_APP_IMPORTANCE_LEVELS = 5;

        // The B list of SERVICE_ADJ -- these are the old and decrepit
        // services that aren't as shiny and interesting as the ones in the A list.
        static final int SERVICE_B_ADJ = 800;

        // This is the process of the previous application that the user was in.
        // This process is kept above other things, because it is very common to
        // switch back to the previous app.  This is important both for recent
        // task switch (toggling between the two top recent apps) as well as normal
        // UI flow such as clicking on a URI in the e-mail app to view in the browser,
        // and then pressing back to return to e-mail.
        static final int PREVIOUS_APP_ADJ = 700;

        // This is a process holding the home application -- we want to try
        // avoiding killing it, even if it would normally be in the background,
        // because the user interacts with it so much.
        static final int HOME_APP_ADJ = 600;

        // This is a process holding an application service -- killing it will not
        // have much of an impact as far as the user is concerned.
        static final int SERVICE_ADJ = 500;

        // This is a process with a heavy-weight application.  It is in the
        // background, but we want to try to avoid killing it.  Value set in
        // system/rootdir/init.rc on startup.
        static final int HEAVY_WEIGHT_APP_ADJ = 400;

        // This is a process currently hosting a backup operation.  Killing it
        // is not entirely fatal but is generally a bad idea.
        static final int BACKUP_APP_ADJ = 300;

        // This is a process bound by the system (or other app) that's more important than services but
        // not so perceptible that it affects the user immediately if killed.
        static final int PERCEPTIBLE_LOW_APP_ADJ = 250;

        // This is a process hosting services that are not perceptible to the user but the
        // client (system) binding to it requested to treat it as if it is perceptible and avoid killing
        // it if possible.
        static final int PERCEPTIBLE_MEDIUM_APP_ADJ = 225;

        // This is a process only hosting components that are perceptible to the
        // user, and we really want to avoid killing them, but they are not
        // immediately visible. An example is background music playback.
        static final int PERCEPTIBLE_APP_ADJ = 200;

        // This is a process only hosting activities that are visible to the
        // user, so we'd prefer they don't disappear.
        static final int VISIBLE_APP_ADJ = 100;
        static final int VISIBLE_APP_LAYER_MAX = PERCEPTIBLE_APP_ADJ - VISIBLE_APP_ADJ - 1;

        // This is a process that was recently TOP and moved to FGS. Continue to treat it almost
        // like a foreground app for a while.
        // @see TOP_TO_FGS_GRACE_PERIOD
        static final int PERCEPTIBLE_RECENT_FOREGROUND_APP_ADJ = 50;

        // This is the process running the current foreground app.  We'd really
        // rather not kill it!
        static final int FOREGROUND_APP_ADJ = 0;

        // This is a process that the system or a persistent process has bound to,
        // and indicated it is important.
        static final int PERSISTENT_SERVICE_ADJ = -700;

        // This is a system persistent process, such as telephony.  Definitely
        // don't want to kill it, but doing so is not completely fatal.
        static final int PERSISTENT_PROC_ADJ = -800;

        // The system process runs at the default adjustment.
        static final int SYSTEM_ADJ = -900;

        // Special code for native processes that are not being managed by the system (so
        // don't have an oom adj assigned by the system).
        static final int NATIVE_ADJ = -1000;
    }
}
