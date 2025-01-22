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

import static com.hchen.hooktool.log.XposedLog.logE;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.provider.Settings;

import com.hchen.appretention.BuildConfig;
import com.hchen.appretention.log.SaveLog;
import com.hchen.hooktool.BaseHC;
import com.hchen.hooktool.hook.IHook;
import com.hchen.hooktool.log.AndroidLog;
import com.hchen.hooktool.tool.additional.SystemPropTool;
import com.hchen.processor.HookEntrance;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 日志服务
 *
 * @author 焕晨HChen
 */
@HookEntrance(targetPackage = "android")
public class LogServices extends BaseHC {
    private static final String SETTINGS_KILL_EVENT_LOG_RECORD_ENABLE = "kill_event_log_record_enable";
    private static boolean isKillEventRecording = false;
    private Context mContext;

    @Override
    protected void init() {
        SystemPropTool.setProp(SaveLog.USER_UNLOCKED_COMPLETED_PROP, "false");

        hookMethod("com.android.server.am.ActivityManagerService",
                "systemReady",
                Runnable.class, "com.android.server.utils.TimingsTraceAndSlog",
                new IHook() {
                    @Override
                    @SuppressLint("UnspecifiedRegisterReceiverFlag")
                    public void after() {
                        mContext = (Context) getThisField("mContext");
                        IntentFilter filter = new IntentFilter();
                        filter.addAction(Intent.ACTION_BOOT_COMPLETED);
                        filter.addAction(Intent.ACTION_SHUTDOWN);
                        filter.addAction(Intent.ACTION_REBOOT);
                        filter.addAction(SaveLog.ACTION_LOG_SERVICE_CONTENT);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            mContext.registerReceiver(new SystemBaseBroadcastReceiver(), filter, Context.RECEIVER_EXPORTED);
                        } else
                            mContext.registerReceiver(new SystemBaseBroadcastReceiver(), filter);
                    }
                }
        );
    }

    private static class SystemBaseBroadcastReceiver extends BroadcastReceiver {
        private static final String TAG = "LogServices";

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action != null) {
                switch (action) {
                    case Intent.ACTION_BOOT_COMPLETED -> {
                        SystemPropTool.setProp(SaveLog.USER_UNLOCKED_COMPLETED_PROP, "true");
                        KillEventLogRecord.init(context);
                        RecordSystemProp.startRecord();
                    }
                    case SaveLog.ACTION_LOG_SERVICE_CONTENT -> {
                        SaveLog.LogContentData logContentData;
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            logContentData = intent.getParcelableExtra("logData", SaveLog.LogContentData.class);
                        } else
                            logContentData = intent.getParcelableExtra("logData");
                        if (logContentData == null) {
                            AndroidLog.logW(TAG, "broadcast receiver: log content data is null!");
                            return;
                        }

                        String id = logContentData.mLogId;
                        String key = logContentData.mLogFileName;
                        ArrayList<String> content = logContentData.mLogContentCache;

                        SaveLog.createFile(key);
                        SaveLog.openFile(key, id);
                        SaveLog.writeFile(key, content);

                        setResultCode(Activity.RESULT_OK);
                        AndroidLog.logI(TAG, "broadcast receiver: key: " + key + ", id: " + id + ", content: " + content);
                    }
                    case Intent.ACTION_SHUTDOWN, Intent.ACTION_REBOOT -> {
                        SaveLog.removeAllOldLogFileAndCopyLogFileToOldPathIfNeed();
                        AndroidLog.logI(TAG, "system will shutdown or reboot!!!");
                    }
                    default -> {
                        AndroidLog.logW(TAG, "unknown action: " + intent.getAction());
                    }
                }
            }
        }
    }

    /**
     * 记录系统 kill 日志
     *
     * @author 焕晨HChen
     */
    private static class KillEventLogRecord {
        private static final String TAG = "KillEventLogRecord";
        private static final String mKillEventRecordFile = "KillEvent";
        private static ExecutorService mExecutorService;
        private static BufferedReader mReader;
        private static Process mLogcat;

        private static void init(Context context) {
            if (BuildConfig.DEBUG || Settings.System.getString(context.getContentResolver(), SETTINGS_KILL_EVENT_LOG_RECORD_ENABLE).equals("true")) {
                if (!isKillEventRecording)
                    startRecord();
                else
                    AndroidLog.logW(TAG, "kill event log record is already started!!");
            } else if (!BuildConfig.DEBUG) {
                if (isKillEventRecording && mExecutorService != null) {
                    mExecutorService.shutdownNow();
                    clear();
                    AndroidLog.logI(TAG, "stop record kill event!!");
                }
            }
        }

        private static void startRecord() {
            SaveLog.createFile(mKillEventRecordFile);
            SaveLog.openFile(mKillEventRecordFile, SaveLog.getRandomNumber());
            mExecutorService = Executors.newSingleThreadExecutor();
            mExecutorService.submit(() -> {
                try {
                    AndroidLog.logI(TAG, "start record kill event!!");
                    isKillEventRecording = true;
                    mLogcat = Runtime.getRuntime().exec("logcat -b events");
                    mReader = new BufferedReader(new InputStreamReader(mLogcat.getInputStream()));
                    String line;
                    while ((line = mReader.readLine()) != null) {
                        if (line.isEmpty())
                            continue;
                        String lowerCaseLine = line.toLowerCase();
                        if (lowerCaseLine.contains("kill") && !lowerCaseLine.contains("killinfo"))
                            SaveLog.writeFile(mKillEventRecordFile, line);
                    }
                } catch (IOException e) {
                    logE(TAG, "start record kill event failed!", e);
                    isKillEventRecording = false;
                    SaveLog.closeFile(mKillEventRecordFile);
                } finally {
                    if (mLogcat != null) {
                        mLogcat.destroy();
                        mLogcat = null;
                    }
                    if (mReader != null) {
                        try {
                            mReader.close();
                            mReader = null;
                        } catch (IOException e) {
                            AndroidLog.logE(TAG, "close reader failed!", e);
                        }
                    }
                }
            });
        }

        private static void clear() {
            SaveLog.closeFile(mKillEventRecordFile);
            if (mLogcat != null) {
                mLogcat.destroy();
                mLogcat = null;
            }
            if (mReader != null) {
                try {
                    mReader.close();
                    mReader = null;
                } catch (IOException e) {
                    AndroidLog.logE(TAG, "close reader failed!", e);
                }
            }
            isKillEventRecording = false;
            AndroidLog.logI(TAG, "clear kll log event record process!!");
        }
    }

    /**
     * 读取并储存系统 prop 键值
     *
     * @author 焕晨HChen
     */
    private static class RecordSystemProp {
        private static final String mRecordFile = "SystemProp";
        private static final String TAG = "RecordSystemProp";
        private static Process mPropData;
        private static BufferedReader mReader;

        public static void startRecord() {
            SaveLog.createFile(mRecordFile);
            SaveLog.openFile(mRecordFile, SaveLog.getRandomNumber());
            ExecutorService mExecutorService = Executors.newSingleThreadExecutor();
            mExecutorService.submit(() -> {
                try {
                    AndroidLog.logI(TAG, "start record system prop!!");
                    isKillEventRecording = true;
                    mPropData = Runtime.getRuntime().exec("getprop");
                    mReader = new BufferedReader(new InputStreamReader(mPropData.getInputStream()));
                    String line;
                    while ((line = mReader.readLine()) != null) {
                        if (line.isEmpty())
                            continue;
                        SaveLog.writeFile(mRecordFile, line);
                    }
                } catch (IOException e) {
                    logE(TAG, "start record system prop failed!", e);
                    isKillEventRecording = false;
                    SaveLog.closeFile(mRecordFile);
                } finally {
                    if (mPropData != null) {
                        mPropData.destroy();
                        mPropData = null;
                    }
                    if (mReader != null) {
                        try {
                            mReader.close();
                            mReader = null;
                        } catch (IOException e) {
                            AndroidLog.logE(TAG, "close reader failed!", e);
                        }
                    }
                    AndroidLog.logI(TAG, "record system prop done, close process success!!");
                }
            });
        }
    }
}
