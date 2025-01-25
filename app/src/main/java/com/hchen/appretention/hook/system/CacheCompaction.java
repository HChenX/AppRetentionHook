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

import static com.hchen.appretention.data.field.System.mCachedAppOptimizerThread;
import static com.hchen.appretention.data.field.System.mCompactionHandler;
import static com.hchen.appretention.data.field.System.mOptRecord;
import static com.hchen.appretention.data.field.System.mPendingCompactionProcesses;
import static com.hchen.appretention.data.field.System.mState;
import static com.hchen.appretention.data.field.System.mUseBootCompact;
import static com.hchen.appretention.data.field.System.mUseCompaction;
import static com.hchen.appretention.data.method.System.getCurAdj;
import static com.hchen.appretention.data.method.System.getLastCompactTime;
import static com.hchen.appretention.data.method.System.getSetAdj;
import static com.hchen.appretention.data.method.System.getSetProcState;
import static com.hchen.appretention.data.method.System.hasPendingCompact;
import static com.hchen.appretention.data.method.System.interruptProcCompaction;
import static com.hchen.appretention.data.method.System.onOomAdjustChanged;
import static com.hchen.appretention.data.method.System.resolveCompactionProfile;
import static com.hchen.appretention.data.method.System.setAppStartingMode;
import static com.hchen.appretention.data.method.System.setHasPendingCompact;
import static com.hchen.appretention.data.method.System.setProperty;
import static com.hchen.appretention.data.method.System.setReqCompactProfile;
import static com.hchen.appretention.data.method.System.setReqCompactSource;
import static com.hchen.appretention.data.method.System.setThreadGroupAndCpuset;
import static com.hchen.appretention.data.method.System.shouldRssThrottleCompaction;
import static com.hchen.appretention.data.method.System.shouldThrottleMiscCompaction;
import static com.hchen.appretention.data.method.System.shouldTimeThrottleCompaction;
import static com.hchen.appretention.data.method.System.updateUseCompaction;
import static com.hchen.appretention.data.path.System.ActivityManagerService;
import static com.hchen.appretention.data.path.System.CachedAppOptimizer;
import static com.hchen.appretention.data.path.System.CachedAppOptimizer$CompactProfile;
import static com.hchen.appretention.data.path.System.CachedAppOptimizer$CompactSource;
import static com.hchen.appretention.data.path.System.CachedAppOptimizer$DefaultProcessDependencies;
import static com.hchen.appretention.data.path.System.CachedAppOptimizer$MemCompactionHandler;
import static com.hchen.appretention.data.path.System.CachedAppOptimizer$ProcessDependencies;
import static com.hchen.appretention.data.path.System.CachedAppOptimizer$PropertyChangedCallbackForTest;
import static com.hchen.appretention.data.path.System.DeviceConfig;
import static com.hchen.appretention.data.path.System.ProcessRecord;
import static com.hchen.hooktool.log.XposedLog.logD;
import static com.hchen.hooktool.log.XposedLog.logW;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Process;

import com.hchen.appretention.data.field.System;
import com.hchen.hooktool.BaseHC;
import com.hchen.hooktool.hook.IHook;

import java.util.ArrayList;

/**
 * 激进化系统的内存压缩
 *
 * @author 焕晨HChen
 */
public final class CacheCompaction extends BaseHC {
    @Override
    public void init() {
        compactionAppCache();
    }

    private void compactionAppCache() {
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

                    @Override
                    public void before() {
                        Object app = getArgs(2);
                        if (app == null) return;
                        state = getField(app, mState);
                        optRecord = getField(app, mOptRecord);
                        Handler compactionHandler = (Handler) getThisField(mCompactionHandler);
                        ArrayList<Object> pendingCompactionProcesses = (ArrayList<Object>) getThisField(mPendingCompactionProcesses);
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
                        return (int) callMethod(state, getSetAdj);
                    }

                    private int getCurAdj() {
                        return (int) callMethod(state, getCurAdj);
                    }

                    private int getSetProcState() {
                        return (int) callMethod(state, getSetProcState);
                    }

                    private void setReqCompactProfile(Object obj) {
                        callMethod(optRecord, setReqCompactProfile, obj);
                    }

                    private void setReqCompactSource(Object obj) {
                        callMethod(optRecord, setReqCompactSource, obj);
                    }

                    private boolean hasPendingCompact() {
                        return (boolean) callMethod(optRecord, hasPendingCompact);
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
                public void before() {
                    Boolean result = (Boolean) callStaticMethod(DeviceConfig, setProperty, "activity_manager", "use_compaction", "true", true);
                    if (result != null && result) {
                        logD(TAG, "Success to put use_compaction new value 'true'");
                    } else
                        logW(TAG, "Failed to put use_compaction value to 'true'");
                }

                @Override
                public void after() {
                    if (existsField(mClass, mUseBootCompact))
                        setThisField(mUseBootCompact, true);
                    setThisField(mUseCompaction, true);

                    Object compactionHandler = getThisField(mCompactionHandler);
                    if (compactionHandler == null) {
                        HandlerThread cachedAppOptimizerThread = (HandlerThread) getThisField(mCachedAppOptimizerThread);

                        if (!cachedAppOptimizerThread.isAlive()) {
                            cachedAppOptimizerThread.start();
                        }
                        compactionHandler = newInstance(CachedAppOptimizer$MemCompactionHandler, thisObject(), null);
                        setThisField(mCompactionHandler, compactionHandler);
                        callStaticMethod(Process.class, setThreadGroupAndCpuset, cachedAppOptimizerThread.getThreadId(), 2);
                    }
                }
            }).shouldObserveCall(false)

            .constructor(ActivityManagerService,
                CachedAppOptimizer$PropertyChangedCallbackForTest,
                CachedAppOptimizer$ProcessDependencies)
            .hook(new IHook() {
                @Override
                public void after() {
                    if (existsField(mClass, mUseBootCompact))
                        setThisField(mUseBootCompact, true);
                    setThisField(mUseCompaction, true);
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
                        long lastCompactTime = (long) callMethod(opt, getLastCompactTime);
                        long start = (long) getArgs(1);
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
                        long[] rssBefore = (long[]) getArgs(3);
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

        chain(CachedAppOptimizer$DefaultProcessDependencies, methodIfExist(interruptProcCompaction)
            .doNothing().shouldObserveCall(false)

            .methodIfExist(setAppStartingMode, boolean.class)
            .doNothing().shouldObserveCall(false)
        );
    }
}
