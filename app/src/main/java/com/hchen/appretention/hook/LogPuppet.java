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
package com.hchen.appretention.hook;

import static com.hchen.appretention.data.method.SystemUi.onCreate;
import static com.hchen.appretention.data.path.SystemUi.SystemUIApplication;
import static com.hchen.appretention.log.LogToFile.ACTION_LOG_SERVICE_CONTENT;
import static com.hchen.appretention.log.LogToFile.SETTINGS_LOG_SERVICE_COMPLETED;
import static com.hchen.appretention.log.LogToFile.isUserUnlockedCompeted;
import static com.hchen.hooktool.log.XposedLog.logE;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.os.Build;
import android.os.Handler;
import android.provider.Settings;

import com.hchen.appretention.BuildConfig;
import com.hchen.appretention.log.LogToFile;
import com.hchen.hooktool.BaseHC;
import com.hchen.hooktool.hook.IHook;
import com.hchen.hooktool.log.AndroidLog;
import com.hchen.processor.HookCondition;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 劫持系统界面作为傀儡日志写入工具
 *
 * @author 焕晨HChen
 */
@HookCondition(targetPackage = "com.android.systemui")
public class LogPuppet extends BaseHC {
    private boolean isRegisterReceiver = false;
    private static boolean isKillEventRecording = false;
    private static final String SETTINGS_KILL_EVENT_LOG_RECORD_ENABLE = "kill_event_log_record_enable";

    @Override
    public void init() {
        hookMethod(SystemUIApplication,
            onCreate,
            new IHook() {
                @Override
                public void after() {
                    isRegisterReceiver = false;
                    Application application = (Application) thisObject();
                    Settings.System.putString(application.getContentResolver(), SETTINGS_LOG_SERVICE_COMPLETED, "0");

                    if (!isUserUnlockedCompeted()) {
                        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_BOOT_COMPLETED);
                        application.registerReceiver(new BroadcastReceiver() {
                            @Override
                            public void onReceive(Context context, Intent intent) {
                                if (isRegisterReceiver) return;
                                registerLogBroadcastReceiver(application);

                                Settings.System.putString(application.getContentResolver(), SETTINGS_LOG_SERVICE_COMPLETED, "1");
                                AndroidLog.logI(TAG, "system boot completed!!!!!");
                                application.unregisterReceiver(this);
                                isRegisterReceiver = true;
                            }
                        }, intentFilter);
                    } else {
                        registerLogBroadcastReceiver(application);
                        Settings.System.putString(application.getContentResolver(), SETTINGS_LOG_SERVICE_COMPLETED, "1");
                    }

                    IntentFilter intentFilter = new IntentFilter();
                    intentFilter.addAction(Intent.ACTION_SHUTDOWN);
                    intentFilter.addAction(Intent.ACTION_REBOOT);
                    application.registerReceiver(new BroadcastReceiver() {
                        @Override
                        public void onReceive(Context context, Intent intent) {
                            LogToFile.removeAllOldLogFileAndCopyLogFileToOldPathIfNeed();
                            AndroidLog.logI(TAG, "system will shutdown or reboot!!!");
                        }
                    }, intentFilter);
                }
            }
        );
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    private void registerLogBroadcastReceiver(Application application) {
        IntentFilter filter = new IntentFilter(ACTION_LOG_SERVICE_CONTENT);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            application.registerReceiver(new LogServiceBroadcastReceiver(), filter, Context.RECEIVER_EXPORTED);
        } else
            application.registerReceiver(new LogServiceBroadcastReceiver(), filter);
        AndroidLog.logI(TAG, "register log broadcast receiver!!");
        KillEventLogRecord.init(application);
        RecordSystemProp.startRecord();
        application.getContentResolver().registerContentObserver(Settings.System.getUriFor(SETTINGS_KILL_EVENT_LOG_RECORD_ENABLE),
            false, new ContentObserver(new Handler(application.getMainLooper())) {
                @Override
                public void onChange(boolean selfChange) {
                    if (selfChange) return;
                    KillEventLogRecord.init(application);
                }
            }
        );
    }

    public static class LogServiceBroadcastReceiver extends BroadcastReceiver {
        private static final String TAG = "LogPuppet";

        @Override
        public void onReceive(Context context, Intent intent) {
            if ("com.hchen.appretention.LOG_SERVICE_CONTENT".equals(intent.getAction())) {
                LogToFile.LogContentData logContentData;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    logContentData = intent.getParcelableExtra("logData", LogToFile.LogContentData.class);
                } else
                    logContentData = intent.getParcelableExtra("logData");
                if (logContentData == null) {
                    AndroidLog.logW(TAG, "broadcast receiver: log content data is null!");
                    return;
                }

                String id = logContentData.mLogId;
                String key = logContentData.mLogFileName;
                ArrayList<String> content = logContentData.mOldLogContent;

                LogToFile.createFile(key);
                LogToFile.openFile(key, id);
                LogToFile.writeFile(key, content);

                setResultCode(Activity.RESULT_OK);
                AndroidLog.logI(TAG, "broadcast receiver: key: " + key + ", id: " + id + ", content: " + content);
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

        private static void init(Application application) {
            if (BuildConfig.DEBUG || Settings.System.getString(application.getContentResolver(), SETTINGS_KILL_EVENT_LOG_RECORD_ENABLE).equals("true")) {
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
            LogToFile.createFile(mKillEventRecordFile);
            LogToFile.openFile(mKillEventRecordFile, LogToFile.getRandomNumber());
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
                            LogToFile.writeFile(mKillEventRecordFile, line);
                    }
                } catch (IOException e) {
                    logE(TAG, "start record kill event failed!", e);
                    isKillEventRecording = false;
                    LogToFile.closeFile(mKillEventRecordFile);
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
            LogToFile.closeFile(mKillEventRecordFile);
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
            LogToFile.createFile(mRecordFile);
            LogToFile.openFile(mRecordFile, LogToFile.getRandomNumber());
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
                        LogToFile.writeFile(mRecordFile, line);
                    }
                } catch (IOException e) {
                    logE(TAG, "start record system prop failed!", e);
                    isKillEventRecording = false;
                    LogToFile.closeFile(mRecordFile);
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
