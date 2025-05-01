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
package com.hchen.appretention.hook.system.opt;

import static com.hchen.appretention.data.field.SystemField.mCachedAppOptimizerThread;
import static com.hchen.appretention.data.field.SystemField.mCompactionHandler;
import static com.hchen.appretention.data.field.SystemField.mOptRecord;
import static com.hchen.appretention.data.field.SystemField.mPendingCompactionProcesses;
import static com.hchen.appretention.data.field.SystemField.mState;
import static com.hchen.appretention.data.field.SystemField.mUseBootCompact;
import static com.hchen.appretention.data.field.SystemField.mUseCompaction;
import static com.hchen.appretention.data.method.SystemMethod.applyOomAdjLSP;
import static com.hchen.appretention.data.method.SystemMethod.compactApp;
import static com.hchen.appretention.data.method.SystemMethod.getBoolean;
import static com.hchen.appretention.data.method.SystemMethod.getCurAdj;
import static com.hchen.appretention.data.method.SystemMethod.getLastCompactTime;
import static com.hchen.appretention.data.method.SystemMethod.getSetAdj;
import static com.hchen.appretention.data.method.SystemMethod.getSetProcState;
import static com.hchen.appretention.data.method.SystemMethod.hasPendingCompact;
import static com.hchen.appretention.data.method.SystemMethod.interruptProcCompaction;
import static com.hchen.appretention.data.method.SystemMethod.onOomAdjustChanged;
import static com.hchen.appretention.data.method.SystemMethod.resolveCompactionProfile;
import static com.hchen.appretention.data.method.SystemMethod.setAppStartingMode;
import static com.hchen.appretention.data.method.SystemMethod.setForceCompact;
import static com.hchen.appretention.data.method.SystemMethod.setHasPendingCompact;
import static com.hchen.appretention.data.method.SystemMethod.setProperty;
import static com.hchen.appretention.data.method.SystemMethod.setReqCompactAction;
import static com.hchen.appretention.data.method.SystemMethod.setReqCompactProfile;
import static com.hchen.appretention.data.method.SystemMethod.setReqCompactSource;
import static com.hchen.appretention.data.method.SystemMethod.setThreadGroupAndCpuset;
import static com.hchen.appretention.data.method.SystemMethod.shouldRssThrottleCompaction;
import static com.hchen.appretention.data.method.SystemMethod.shouldThrottleMiscCompaction;
import static com.hchen.appretention.data.method.SystemMethod.shouldTimeThrottleCompaction;
import static com.hchen.appretention.data.method.SystemMethod.updateUseCompaction;
import static com.hchen.appretention.data.path.HyperClass.ServiceThread;
import static com.hchen.appretention.data.path.SystemClass.ActiveUids;
import static com.hchen.appretention.data.path.SystemClass.ActivityManagerService;
import static com.hchen.appretention.data.path.SystemClass.CachedAppOptimizer;
import static com.hchen.appretention.data.path.SystemClass.CachedAppOptimizer$CompactProfile;
import static com.hchen.appretention.data.path.SystemClass.CachedAppOptimizer$CompactSource;
import static com.hchen.appretention.data.path.SystemClass.CachedAppOptimizer$DefaultProcessDependencies;
import static com.hchen.appretention.data.path.SystemClass.CachedAppOptimizer$MemCompactionHandler;
import static com.hchen.appretention.data.path.SystemClass.CachedAppOptimizer$ProcessDependencies;
import static com.hchen.appretention.data.path.SystemClass.CachedAppOptimizer$PropertyChangedCallbackForTest;
import static com.hchen.appretention.data.path.SystemClass.DeviceConfig;
import static com.hchen.appretention.data.path.SystemClass.Injector;
import static com.hchen.appretention.data.path.SystemClass.OomAdjuster;
import static com.hchen.appretention.data.path.SystemClass.ProcessList;
import static com.hchen.appretention.data.path.SystemClass.ProcessRecord;
import static com.hchen.appretention.data.prop.SystemProp.TRUE;
import static com.hchen.hooktool.core.CoreTool.buildChain;
import static com.hchen.hooktool.core.CoreTool.callMethod;
import static com.hchen.hooktool.core.CoreTool.callStaticMethod;
import static com.hchen.hooktool.core.CoreTool.existsClass;
import static com.hchen.hooktool.core.CoreTool.existsConstructor;
import static com.hchen.hooktool.core.CoreTool.existsField;
import static com.hchen.hooktool.core.CoreTool.existsMethod;
import static com.hchen.hooktool.core.CoreTool.findConstructor;
import static com.hchen.hooktool.core.CoreTool.findMethod;
import static com.hchen.hooktool.core.CoreTool.getField;
import static com.hchen.hooktool.core.CoreTool.getStaticField;
import static com.hchen.hooktool.core.CoreTool.hook;
import static com.hchen.hooktool.core.CoreTool.hookAllMethod;
import static com.hchen.hooktool.core.CoreTool.hookMethod;
import static com.hchen.hooktool.core.CoreTool.newInstance;
import static com.hchen.hooktool.core.CoreTool.returnResult;
import static com.hchen.hooktool.log.XposedLog.logD;
import static com.hchen.hooktool.log.XposedLog.logW;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Process;

import androidx.annotation.NonNull;

import com.hchen.appretention.data.field.SystemField;
import com.hchen.appretention.data.other.PrecessAdjInfo;
import com.hchen.hooktool.hook.IHook;
import com.hchen.hooktool.utils.SystemPropTool;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Optional;

/**
 * 激进化系统的内存压缩
 *
 * @author 焕晨HChen
 */
public final class CacheCompaction {
    private static final String TAG = "CacheCompaction";
    private static boolean isHandlerNotInit = false;
    private static Object mCachedAppOptimizer = null;
    private static Object NONE;
    private static Object SOME;
    private static Object ANON;
    private static Object FULL;
    private static Object ANON_MORE;
    private static Object APP;
    private static Object SHELL;

    // --------- old ---------
    private static boolean useOldCompactMode = false;
    private static final int COMPACT_ACTION_NONE = 0;
    private static final int COMPACT_ACTION_FILE = 1;
    private static final int COMPACT_ACTION_ANON = 2;
    private static final int COMPACT_ACTION_FULL = 3;

    public static void init() {
        if (!isEnabled()) {
            logD(TAG, "CacheCompaction is disabled!!");
            return;
        }
        // compactionAppCache();
        compactionAppCacheNew();

        Constructor<?> oomAdjuster = null;
        if (existsConstructor(OomAdjuster, ActivityManagerService, ProcessList, ActiveUids, ServiceThread, Injector)) {
            oomAdjuster = findConstructor(OomAdjuster, ActivityManagerService, ProcessList, ActiveUids, ServiceThread, Injector);
        } else if (existsConstructor(OomAdjuster, ActivityManagerService, ProcessList, ActiveUids, ServiceThread))
            oomAdjuster = findConstructor(OomAdjuster, ActivityManagerService, ProcessList, ActiveUids, ServiceThread);
        if (oomAdjuster == null) {
            logW(TAG, "oomAdjuster is null! can't use CacheCompaction!!");
            return;
        }

        hook(oomAdjuster,
            new IHook() {
                @Override
                public void after() {
                    mCachedAppOptimizer = getThisField(SystemField.mCachedAppOptimizer);
                    initEnumIfNeed();
                }
            }
        );
    }

    public static void enableCompaction() {
        if (!isEnabled()) {
            logD(TAG, "CacheCompaction is disabled!!");
            return;
        }

        hookMethod(CachedAppOptimizer,
            updateUseCompaction,
            new IHook() {
                @Override
                public void before() {
                    boolean enabled = (boolean) Optional.ofNullable(
                        callStaticMethod(DeviceConfig, getBoolean, "activity_manager", "use_compaction", false)
                    ).orElse(false);

                    if (Boolean.FALSE.equals(enabled)) {
                        boolean result = (boolean) Optional.ofNullable(
                            callStaticMethod(DeviceConfig, setProperty, "activity_manager", "use_compaction", TRUE, true)
                        ).orElse(false);

                        if (result) logD(TAG, "Success to put use_compaction new value 'true'");
                        else logW(TAG, "Failed to put use_compaction value to 'true'");
                    }
                }
            }
        );
    }

    private static boolean isEnabled() {
        return SystemPropTool.getProp("persist.hchen.cache.compaction.enable", true);
    }

    private static void initEnumIfNeed() {
        if (mCachedAppOptimizer == null) return;
        if (useOldCompactMode) return;
        if (!existsClass(CachedAppOptimizer$CompactProfile)) {
            useOldCompactMode = true;
            return;
        }

        NONE = getStaticField(CachedAppOptimizer$CompactProfile, SystemField.NONE);
        SOME = getStaticField(CachedAppOptimizer$CompactProfile, SystemField.SOME);
        ANON = getStaticField(CachedAppOptimizer$CompactProfile, SystemField.ANON);
        FULL = getStaticField(CachedAppOptimizer$CompactProfile, SystemField.FULL);
        if (existsField(CachedAppOptimizer$CompactProfile, SystemField.ANON_MORE))
            ANON_MORE = getStaticField(CachedAppOptimizer$CompactProfile, SystemField.ANON_MORE);

        if (existsClass(CachedAppOptimizer$CompactSource)) {
            APP = getStaticField(CachedAppOptimizer$CompactSource, SystemField.APP);
            SHELL = getStaticField(CachedAppOptimizer$CompactSource, SystemField.SHELL);
        }
    }

    private static void compactionAppCacheNew() {
        Method applyOomAdjLSPMethod = null;
        if (existsMethod(OomAdjuster, applyOomAdjLSP, ProcessRecord, boolean.class, long.class, long.class, int.class, boolean.class))
            applyOomAdjLSPMethod = findMethod(OomAdjuster, applyOomAdjLSP, ProcessRecord, boolean.class, long.class, long.class, int.class, boolean.class);
        else if (existsMethod(OomAdjuster, applyOomAdjLSP, ProcessRecord, boolean.class, long.class, long.class, int.class)) {
            applyOomAdjLSPMethod = findMethod(OomAdjuster, applyOomAdjLSP, ProcessRecord, boolean.class, long.class, long.class, int.class);
        } else if (existsMethod(OomAdjuster, applyOomAdjLSP, ProcessRecord, boolean.class, long.class, long.class)) {
            applyOomAdjLSPMethod = findMethod(OomAdjuster, applyOomAdjLSP, ProcessRecord, boolean.class, long.class, long.class);
        }
        if (applyOomAdjLSPMethod == null) {
            logW(TAG, "applyOomAdjLSPMethod is null! can't use CacheCompaction!!");
            return;
        }

        enableCompaction();

        hook(applyOomAdjLSPMethod,
            new IHook() {
                @Override
                public void before() {
                    if (mCachedAppOptimizer == null) return;
                    if (isHandlerNotInit) {
                        logW(TAG, "CachedAppOptimizer handler not init! Will cancel hook!!");
                        unHookSelf();
                        return;
                    }
                    Object app = getArg(0);
                    if (app == null) return;

                    Integer curAdj = getCurAdj(app);
                    Integer setAdj = getSetAdj(app);
                    if (curAdj == null || setAdj == null) return;
                    if (Objects.equals(curAdj, setAdj)) return;

                    if (setAdj <= PrecessAdjInfo.PERCEPTIBLE_APP_ADJ && (
                        (curAdj >= PrecessAdjInfo.PREVIOUS_APP_ADJ && curAdj <= PrecessAdjInfo.PREVIOUS_APP_ADJ + 99) ||
                            (curAdj >= PrecessAdjInfo.HOME_APP_ADJ && curAdj <= PrecessAdjInfo.HOME_APP_ADJ + 99)
                    )) { // 应用从可感知进入后台
                        if (ANON != null && ANON_MORE == null) {
                            compactApp(app, COMPACT_ACTION_ANON, ANON, SHELL, false);
                        } else if (ANON_MORE != null) {
                            compactApp(app, COMPACT_ACTION_ANON, ANON_MORE, SHELL, false);
                        }
                    } else if (curAdj >= PrecessAdjInfo.CACHED_APP_MIN_ADJ && curAdj <= PrecessAdjInfo.CACHED_APP_MAX_ADJ) {
                        compactApp(app, COMPACT_ACTION_FULL, FULL, SHELL, false);
                    }
                }
            }
        );

        // 不许替换
        hookAllMethod(CachedAppOptimizer,
            resolveCompactionProfile,
            new IHook() {
                @Override
                public void before() {
                    setResult(getArg(0));
                }
            }
        );

        // 阻止原生功能
        hookAllMethod(CachedAppOptimizer,
            compactApp,
            returnResult(false)
        );

        buildChain(CachedAppOptimizer$MemCompactionHandler)
            /* .findMethod(shouldOomAdjThrottleCompaction, ProcessRecord)
               .returnResult(false) 进程恢复到可感知状态了 */

            .findAllMethod(shouldThrottleMiscCompaction)
            .returnResult(false)

            .findAllMethod(shouldTimeThrottleCompaction)
            .hook(new IHook() {
                @Override
                public void before() {
                    Object opt = getField(getArg(0), mOptRecord);
                    Long lastCompactTime = (Long) callMethod(opt, getLastCompactTime);
                    Long start = (Long) getArg(1);
                    if (lastCompactTime == null || start == null)
                        return;

                    // 15 秒内不允许再次触发。
                    if (lastCompactTime != 0) {
                        if (start - lastCompactTime < 15000) {
                            setResult(true);
                            return;
                        }
                    }
                    setResult(false);
                }
            })

            .findAllMethod(shouldRssThrottleCompaction)
            .hook(new IHook() {
                @Override
                public void before() {
                    long[] rssBefore = (long[]) getArg(3);
                    if (rssBefore == null) return;

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
            });
    }

    private static void compactApp(@NonNull Object app, int action, Object compactProfile, Object source, Object force) {
        Object optRecord = getField(app, mOptRecord);

        Boolean b = (Boolean) callMethod(optRecord, hasPendingCompact);
        if (b != null && !b) {
            Handler compactionHandler = (Handler) getField(mCachedAppOptimizer, mCompactionHandler);
            if (compactionHandler == null) {
                isHandlerNotInit = true;
                return;
            }

            if (useOldCompactMode) {
                callMethod(optRecord, setReqCompactAction, action);
            } else {
                callMethod(optRecord, setReqCompactSource, source);
                callMethod(optRecord, setReqCompactProfile, compactProfile);
            }
            callMethod(optRecord, setHasPendingCompact, true);
            callMethod(optRecord, setForceCompact, force);

            ArrayList<Object> pendingCompactionProcesses = (ArrayList<Object>) getField(mCachedAppOptimizer, mPendingCompactionProcesses);
            assert pendingCompactionProcesses != null; // 不可能是 null
            pendingCompactionProcesses.add(app);

            compactionHandler.sendMessage(compactionHandler.obtainMessage(1, getCurAdj(app), getSetProcState(app)));
        }
    }

    // 当前的 adj 值。
    private static Integer getCurAdj(Object app) {
        Object state = getField(app, mState);
        return (Integer) callMethod(state, getCurAdj);
    }

    // 上一次的 adj 值。
    private static Integer getSetAdj(Object app) {
        Object state = getField(app, mState);
        return (Integer) callMethod(state, getSetAdj);
    }

    private static Integer getSetProcState(Object app) {
        Object state = getField(app, mState);
        return (Integer) callMethod(state, getSetProcState);
    }

    // ---------------------------------------------------------------------------------------

    @Deprecated
    private void compactionAppCache() {
        // --------------- CachedAppOptimizer ----------------
        /*
         * 接管系统的压缩流程，并激进化流程。
         * */
        buildChain(CachedAppOptimizer)
            .findMethod(onOomAdjustChanged,
                int.class, int.class, ProcessRecord).hook(
                new IHook() {
                    private static final Object SOME = getStaticField(CachedAppOptimizer$CompactProfile, SystemField.SOME);
                    private static final Object FULL = getStaticField(CachedAppOptimizer$CompactProfile, SystemField.FULL);
                    private static final Object ANON = getStaticField(CachedAppOptimizer$CompactProfile, SystemField.ANON);
                    private static final Object SHEll = getStaticField(CachedAppOptimizer$CompactSource, SystemField.SHELL);
                    private static final Object APP = getStaticField(CachedAppOptimizer$CompactSource, SystemField.APP);
                    private Object state;
                    private Object optRecord;

                    @Override
                    public void before() {
                        Object app = getArg(2);
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
                })

            .findMethod(resolveCompactionProfile, CachedAppOptimizer$CompactProfile)
            .hook(new IHook() {
                @Override
                public void before() {
                    setResult(getArg(0));
                }
            })

            .findMethod(updateUseCompaction)
            .hook(new IHook() {
                @Override
                public void before() {
                    Boolean result = (Boolean) callStaticMethod(DeviceConfig, setProperty, "activity_manager", "use_compaction", TRUE, true);
                    if (result != null && result) {
                        logD(TAG, "Success to put use_compaction new value 'true'");
                    } else
                        logW(TAG, "Failed to put use_compaction value to 'true'");
                }

                @Override
                public void after() {
                    if (existsField(getMember().getDeclaringClass(), mUseBootCompact))
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
            })

            .findConstructor(ActivityManagerService,
                CachedAppOptimizer$PropertyChangedCallbackForTest,
                CachedAppOptimizer$ProcessDependencies)
            .hook(new IHook() {
                @Override
                public void after() {
                    if (existsField(getMember().getDeclaringClass(), mUseBootCompact))
                        setThisField(mUseBootCompact, true);
                    setThisField(mUseCompaction, true);
                }
            });

        buildChain(CachedAppOptimizer$MemCompactionHandler)
            /* .findMethod(shouldOomAdjThrottleCompaction, ProcessRecord)
               .returnResult(false) 进程恢复到可感知状态了 */

            .findMethod(shouldThrottleMiscCompaction, ProcessRecord, int.class)
            .returnResult(false)

            .findMethod(shouldTimeThrottleCompaction, ProcessRecord, long.class, CachedAppOptimizer$CompactProfile, CachedAppOptimizer$CompactSource)
            .hook(new IHook() {
                @Override
                public void before() {
                    Object opt = getField(getArg(0), mOptRecord);
                    long lastCompactTime = (long) callMethod(opt, getLastCompactTime);
                    long start = (long) getArg(1);
                    // 10 秒内不允许再次触发。
                    if (lastCompactTime != 0) {
                        if (start - lastCompactTime < 10000) {
                            setResult(true);
                            return;
                        }
                    }
                    setResult(false);
                }
            })

            .findMethod(shouldRssThrottleCompaction, CachedAppOptimizer$CompactProfile, int.class, String.class, long[].class)
            .hook(new IHook() {
                @Override
                public void before() {
                    long[] rssBefore = (long[]) getArg(3);
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
            });

        buildChain(CachedAppOptimizer$DefaultProcessDependencies)
            .findMethodIfExist(interruptProcCompaction)
            .doNothing()

            .findMethodIfExist(setAppStartingMode, boolean.class)
            .doNothing();
    }
}
