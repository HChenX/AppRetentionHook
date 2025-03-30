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

import static com.hchen.appretention.data.field.SystemField.mContext;
import static com.hchen.appretention.data.method.SystemMethod.applyOomAdjLSP;
import static com.hchen.appretention.data.method.SystemMethod.forEachLruProcessesLOSP;
import static com.hchen.appretention.data.method.SystemMethod.getCurProcState;
import static com.hchen.appretention.data.method.SystemMethod.procStateToImportance;
import static com.hchen.appretention.data.method.SystemMethod.removeLruProcessLocked;
import static com.hchen.appretention.data.method.SystemMethod.setCurAdj;
import static com.hchen.appretention.data.method.SystemMethod.setCurRawAdj;
import static com.hchen.appretention.data.method.SystemMethod.systemReady;
import static com.hchen.appretention.data.method.SystemMethod.updateLruProcessLocked;
import static com.hchen.appretention.data.path.HyperClass.ServiceThread;
import static com.hchen.appretention.data.path.SystemClass.ActiveUids;
import static com.hchen.appretention.data.path.SystemClass.ActivityManager$RunningAppProcessInfo;
import static com.hchen.appretention.data.path.SystemClass.ActivityManagerService;
import static com.hchen.appretention.data.path.SystemClass.Injector;
import static com.hchen.appretention.data.path.SystemClass.OomAdjuster;
import static com.hchen.appretention.data.path.SystemClass.ProcessList;
import static com.hchen.appretention.data.path.SystemClass.ProcessRecord;
import static com.hchen.appretention.data.path.SystemClass.TimingsTraceAndSlog;
import static com.hchen.hooktool.log.XposedLog.logW;
import static com.hchen.hooktool.tool.CoreTool.callMethod;
import static com.hchen.hooktool.tool.CoreTool.callStaticMethod;
import static com.hchen.hooktool.tool.CoreTool.existsConstructor;
import static com.hchen.hooktool.tool.CoreTool.existsMethod;
import static com.hchen.hooktool.tool.CoreTool.findConstructor;
import static com.hchen.hooktool.tool.CoreTool.findMethod;
import static com.hchen.hooktool.tool.CoreTool.getField;
import static com.hchen.hooktool.tool.CoreTool.hook;
import static com.hchen.hooktool.tool.CoreTool.hookMethod;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.content.pm.SigningInfo;

import com.hchen.appretention.data.field.SystemField;
import com.hchen.hooktool.hook.IHook;
import com.hchen.hooktool.log.AndroidLog;
import com.hchen.hooktool.log.XposedLog;
import com.hchen.hooktool.tool.ChainTool;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Adj 计算
 *
 * @author 焕晨HChen
 */
public class ApplyAdjOpt {
    private static final String TAG = "ApplyAdjOpt";
    private static final ArrayList<Object> mPreviousBackgroundAppList = new ArrayList<>();
    private static final HashSet<String> mSystemSigningAppMap = new HashSet<>();
    private static Object mService;
    private static Object mProcessList;
    private static final int MAIN_PROCESS_MIN_ADJ = 600;
    private static final int MAIN_PROCESS_MAX_ADJ = 699;
    private static final int SUB_PROCESS_MIN_ADJ = 700;
    private static final int SUB_PROCESS_MAX_ADJ = 799;

    public static void init() {
        Constructor<?> oomAdjuster = null;
        if (existsConstructor(OomAdjuster, ActivityManagerService, ProcessList, ActiveUids, ServiceThread, Injector)) {
            oomAdjuster = findConstructor(OomAdjuster, ActivityManagerService, ProcessList, ActiveUids, ServiceThread, Injector);

        } else if (existsConstructor(OomAdjuster, ActivityManagerService, ProcessList, ActiveUids, ServiceThread))
            oomAdjuster = findConstructor(OomAdjuster, ActivityManagerService, ProcessList, ActiveUids, ServiceThread);
        if (oomAdjuster == null) {
            logW(TAG, "oomAdjuster is null! can't use ApplyAdjOpt!!");
            return;
        }

        hook(oomAdjuster,
            new IHook() {
                @Override
                public void after() {
                    mService = getThisField(SystemField.mService);
                    mProcessList = getThisField(SystemField.mProcessList);
                }
            }
        );

        Method applyOomAdjLSPMethod = null;
        if (existsMethod(OomAdjuster, applyOomAdjLSP, ProcessRecord, boolean.class, long.class, long.class, int.class, boolean.class))
            applyOomAdjLSPMethod = findMethod(OomAdjuster, applyOomAdjLSP, ProcessRecord, boolean.class, long.class, long.class, int.class, boolean.class);
        else if (existsMethod(OomAdjuster, applyOomAdjLSP, ProcessRecord, boolean.class, long.class, long.class, int.class)) {
            applyOomAdjLSPMethod = findMethod(OomAdjuster, applyOomAdjLSP, ProcessRecord, boolean.class, long.class, long.class, int.class);
        } else if (existsMethod(OomAdjuster, applyOomAdjLSP, ProcessRecord, boolean.class, long.class, long.class)) {
            applyOomAdjLSPMethod = findMethod(OomAdjuster, applyOomAdjLSP, ProcessRecord, boolean.class, long.class, long.class);
        }
        if (applyOomAdjLSPMethod == null) {
            logW(TAG, "applyOomAdjLSPMethod is null! can't use ApplyAdjOpt!!");
            return;
        }

        hookSystemReady();

        ChainTool.chain(ProcessList,
            new ChainTool().method(updateLruProcessLocked,
                    ProcessRecord, boolean.class, ProcessRecord)
                .hook(new IHook() {
                    @Override
                    public void after() {
                        updateBackgroundAppList();
                    }
                })

                .method(removeLruProcessLocked,
                    ProcessRecord)
                .hook(new IHook() {
                    @Override
                    public void after() {
                        updateBackgroundAppList();
                    }
                })
        );

        hook(applyOomAdjLSPMethod,
            new IHook() {
                @Override
                public void before() {
                    if (mPreviousBackgroundAppList.isEmpty()) return;

                    Object app = getArgs(0);
                    if (app == null) return;

                    int index = mPreviousBackgroundAppList.indexOf(app);
                    if (index == -1) return;

                    ApplyAdjOpt.ProcessRecord pr = new ApplyAdjOpt.ProcessRecord(app);
                    int adj = (pr.isMainProcess || pr.isolated || pr.isSdkSandbox) ?
                        Math.min(MAIN_PROCESS_MIN_ADJ + index, MAIN_PROCESS_MAX_ADJ) :
                        Math.min(SUB_PROCESS_MIN_ADJ + index, SUB_PROCESS_MAX_ADJ);
                    pr.setCurAdj(adj);
                    pr.setCurRawAdj(adj);
                    AndroidLog.logD(TAG, "update: packageName=" + pr.packageName + ", processName=" + pr.processName + ", adj=" + adj);
                }
            }
        );
    }

    private static void updateBackgroundAppList() {
        if (mService == null || mProcessList == null) return;
        synchronized (mService) {
            mPreviousBackgroundAppList.clear();

            callMethod(mProcessList, forEachLruProcessesLOSP, false, new Consumer<Object>() {
                @Override
                public void accept(Object pr) {
                    ApplicationInfo info = (ApplicationInfo) getField(pr, SystemField.info);
                    if (info != null) {
                        boolean isSystem = isSystemApp(info);
                        if (!isSystem) {
                            Object mState = getField(pr, SystemField.mState);
                            Integer importance = (Integer) callStaticMethod(
                                ActivityManager$RunningAppProcessInfo,
                                procStateToImportance,
                                callMethod(mState, getCurProcState)
                            );
                            if (importance != null) {
                                if (importance > ImportanceInfo.IMPORTANCE_VISIBLE) { // 假定为后台
                                    mPreviousBackgroundAppList.add(pr); // 根据 mProcessList 顺序，越不重要越在前面
                                }
                            }
                        }
                    }
                }
            });
        }
    }

    private static void hookSystemReady() {
        hookMethod(ActivityManagerService,
            systemReady,
            Runnable.class, TimingsTraceAndSlog,
            new IHook() {
                @Override
                public void after() {
                    Context context = (Context) getThisField(mContext);
                    if (context == null) return;
                    try {
                        PackageManager pm = context.getPackageManager();
                        if (pm == null) return;
                        PackageInfo shellInfo = pm.getPackageInfo("com.android.shell", PackageManager.GET_SIGNING_CERTIFICATES);
                        SigningInfo shellSigning = shellInfo.signingInfo;
                        Signature[] shellSignature = shellSigning.getApkContentsSigners();

                        @SuppressLint("QueryPermissionsNeeded") List<PackageInfo> packageInfos = pm.getInstalledPackages(PackageManager.GET_SIGNING_CERTIFICATES);
                        for (PackageInfo packageInfo : packageInfos) {
                            SigningInfo signingInfo = packageInfo.signingInfo;
                            if (signingInfo == null) continue;

                            Signature[] signatures = signingInfo.getApkContentsSigners();
                            if (shellSignature != null && shellSignature.length > 0 && signatures != null && signatures.length > 0) {
                                if (shellSignature[0].toCharsString().equals(signatures[0].toCharsString())) {
                                    mSystemSigningAppMap.add(packageInfo.packageName);
                                    AndroidLog.logD(TAG, "system signing: " + packageInfo.packageName);
                                }
                            }
                        }
                    } catch (PackageManager.NameNotFoundException e) {
                        XposedLog.logE(TAG, e);
                    }
                }
            }
        );
    }

    private static boolean isSystemApp(ApplicationInfo info) {
        if (Objects.isNull(info))
            return true;

        if (info.uid < 10000)
            return true;
        if ((info.flags & (ApplicationInfo.FLAG_SYSTEM | ApplicationInfo.FLAG_UPDATED_SYSTEM_APP)) != 0)
            return true;
        if (mSystemSigningAppMap.isEmpty()) return false;
        return mSystemSigningAppMap.contains(info.packageName);
    }

    private static class ProcessRecord {
        private final Object instance;
        private final ApplicationInfo info;
        private final String processName;
        private String packageName;
        private final boolean isolated;
        private final boolean isSdkSandbox;
        private final boolean isMainProcess;
        private final int uid;
        private Object mState;

        private ProcessRecord(Object pr) {
            this.instance = pr;
            this.processName = (String) getField(pr, SystemField.processName);
            this.info = (ApplicationInfo) getField(pr, SystemField.info);
            if (info != null) {
                this.packageName = info.packageName;
            }
            this.uid = (int) getField(pr, SystemField.uid);
            this.isolated = (boolean) getField(pr, SystemField.isolated);
            this.isSdkSandbox = (boolean) getField(pr, SystemField.isSdkSandbox);
            this.isMainProcess = Objects.equals(this.processName, this.packageName);
            this.mState = getField(pr, SystemField.mState);
        }

        private void setCurRawAdj(int adj) {
            callMethod(mState, setCurRawAdj, adj);
        }

        private void setCurAdj(int adj) {
            callMethod(mState, setCurAdj, adj);
        }
    }

    private static class ImportanceInfo {
        public static final int IMPORTANCE_BACKGROUND = 400;
        public static final int IMPORTANCE_CACHED = 400;
        public static final int IMPORTANCE_CANT_SAVE_STATE = 350;
        public static final int IMPORTANCE_CANT_SAVE_STATE_PRE_26 = 170;
        public static final int IMPORTANCE_EMPTY = 500;
        public static final int IMPORTANCE_FOREGROUND = 100;
        public static final int IMPORTANCE_FOREGROUND_SERVICE = 125;
        public static final int IMPORTANCE_GONE = 1000;
        public static final int IMPORTANCE_PERCEPTIBLE = 230;
        public static final int IMPORTANCE_PERCEPTIBLE_PRE_26 = 130;
        public static final int IMPORTANCE_SERVICE = 300;
        public static final int IMPORTANCE_TOP_SLEEPING = 325;
        public static final int IMPORTANCE_TOP_SLEEPING_PRE_28 = 150;
        public static final int IMPORTANCE_VISIBLE = 200;
    }
}
