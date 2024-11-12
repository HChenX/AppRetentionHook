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
package com.hchen.appretention.log;

import static com.hchen.hooktool.log.XposedLog.logENoSave;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.PersistableBundle;
import android.provider.Settings;
import android.service.restrictions.RestrictionsReceiver;

import androidx.annotation.Nullable;

import com.hchen.hooktool.data.ToolData;
import com.hchen.hooktool.log.AndroidLog;
import com.hchen.hooktool.tool.additional.ContextTool;
import com.hchen.hooktool.tool.additional.DeviceTool;
import com.hchen.hooktool.tool.additional.InvokeTool;
import com.hchen.hooktool.tool.additional.SystemPropTool;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 输出日志至指定文件中
 *
 * @author 焕晨HChen
 */
public class LogToFile {
    public static final String ACTION_LOG_SERVICE_CONTENT = "com.hchen.appretention.LOG_SERVICE_CONTENT";
    public static final String USER_UNLOCKED_COMPLETED_PROP = "persist.sys.user.unlocked.completed";
    public static final String SETTINGS_LOG_SERVICE_COMPLETED = "log_service_boot_complete";
    private static final String TAG = "LogToFile";
    private static final String LOG_FILE_PATH = "/storage/emulated/0/Android/" + ToolData.mLogFileRootName + "/";
    private static final HashMap<String, LogFileStateData> mLogFileStateDataMap = new HashMap<>();
    private static String LOG_FILE_FULL_PATH = "";
    private static boolean isWaitingSystemBootCompleted = false;
    private static boolean isWaitingLogServiceBootCompleted = false;
    private static LogContentData mLogContentData;
    private static boolean hasProcessingBroadcast = false;

    public static void initLogToFile(String fileName) {
        if (fileName == null) return;
        if (fileName.isEmpty()) return;
        mLogContentData = new LogContentData();
        mLogContentData.mLogFileName = fileName;
        mLogContentData.mLogId = getRandomNumber();
        waitSystemBootCompletedIfNeed();
    }

    public static void createFile(String key) {
        LOG_FILE_FULL_PATH = LOG_FILE_PATH + key + ".log";
        LogFileStateData data = mLogFileStateDataMap.get(key);
        if (data != null && data.isCreatedFile) return;
        data = new LogFileStateData();

        File file = new File(LOG_FILE_FULL_PATH);
        File path = file.getParentFile();
        if (path == null) return;
        try {
            if (!path.exists()) {
                if (!path.mkdirs()) {
                    logENoSave(TAG, "Create log dirs failed! Path: " + LOG_FILE_FULL_PATH);
                    return;
                }
            }
            if (!file.exists() && !file.createNewFile()) {
                logENoSave(TAG, "Create log file failed! Path: " + LOG_FILE_FULL_PATH);
            }
            data.isCreatedFile = true;
        } catch (IOException e) {
            logENoSave(TAG, "Create log file failed! Path: " + LOG_FILE_FULL_PATH, e);
        }
        mLogFileStateDataMap.put(key, data);
    }

    public static void openFile(String key, String logId) {
        LogFileStateData data = mLogFileStateDataMap.get(key);
        if (data == null) return;
        if (!data.isCreatedFile) return;
        if (data.isOpened) closeFile(key);
        try {
            data.mWriter = new BufferedWriter(new FileWriter(LOG_FILE_FULL_PATH, true));
            data.mReader = new BufferedReader(new FileReader(LOG_FILE_FULL_PATH));
            data.isOpened = true;
        } catch (IOException e) {
            logENoSave(TAG, "Open log file failed! Path: " + LOG_FILE_FULL_PATH, e);
        }
        if (shouldResetFile(key, logId)) {
            resetFile(key);
            initFileContent(key, logId);
        }
    }

    @Nullable
    private static ArrayList<String> readFile(String key) {
        LogFileStateData data = mLogFileStateDataMap.get(key);
        if (data == null) return null;
        if (!data.isCreatedFile) return null;
        if (!data.isOpened || data.mReader == null) return null;
        try {
            ArrayList<String> strings = new ArrayList<>();
            String line;
            while ((line = data.mReader.readLine()) != null) {
                strings.add(line);
            }
            return strings;
        } catch (IOException e) {
            logENoSave(TAG, "Read log file failed! Path: " + LOG_FILE_FULL_PATH, e);
        }
        return null;
    }

    public static synchronized void saveLogContent(String log) {
        if (mLogContentData == null) return;
        if (isWaitingSystemBootCompleted || isWaitingLogServiceBootCompleted) {
            mLogContentData.mLogContent.add(getDate() + " " + log);
        } else {
            mLogContentData.mLogContent.add(getDate() + " " + log);
            pushWithAsyncContext(LogToFile::sendLogContentBroadcast);
        }
        AndroidLog.logI(TAG, "isWaitingSystemBootCompleted: " + isWaitingSystemBootCompleted +
            " isWaitingLogServiceBootCompleted: " + isWaitingLogServiceBootCompleted +
            " log: " + log + " content: " + mLogContentData.mLogContent);
    }

    public static void writeFile(String key, ArrayList<String> logs) {
        LogFileStateData data = mLogFileStateDataMap.get(key);
        if (data == null) return;
        if (!data.isCreatedFile) return;
        if (!data.isOpened || data.mWriter == null) return;
        try {
            for (String log : logs) {
                data.mWriter.write(log);
                data.mWriter.newLine();
            }
            data.mWriter.flush();
        } catch (IOException e) {
            logENoSave(TAG, "Write log file failed! Path: " + LOG_FILE_FULL_PATH, e);
        }
    }

    private static void resetFile(String key) {
        LogFileStateData data = mLogFileStateDataMap.get(key);
        if (data == null) return;
        if (!data.isCreatedFile) return;
        try {
            BufferedWriter reset = new BufferedWriter(new FileWriter(LOG_FILE_FULL_PATH));
            reset.write("");
            reset.flush();
            reset.close();
        } catch (IOException e) {
            logENoSave(TAG, "Reset log file failed! Path: " + LOG_FILE_FULL_PATH, e);
        }
    }

    public static void closeFile(String key) {
        LogFileStateData data = mLogFileStateDataMap.get(key);
        if (data == null) return;
        if (!data.isCreatedFile) return;
        try {
            if (data.mWriter != null) data.mWriter.close();
            if (data.mReader != null) data.mReader.close();
            data.mWriter = null;
            data.mReader = null;
            data.isOpened = false;
        } catch (IOException e) {
            logENoSave(TAG, "Close log file failed! Path: " + LOG_FILE_FULL_PATH, e);
        }
    }

    private static boolean shouldResetFile(String key, String logId) {
        LogFileStateData data = mLogFileStateDataMap.get(key);
        if (data == null) return false;
        if (!data.isCreatedFile) return false;
        ArrayList<String> content = readFile(key);
        if (content == null) return false;
        if (content.isEmpty()) return true;
        for (String s : content) {
            if (s.contains("# Log Id: ")) {
                String id = s.replace("# Log Id: ", "").replace("\n", "");
                return !logId.equals(id);
            }
            if (s.contains("!!!!!!! Module Will Be Start Hook Process !!!!!!!"))
                return true;
        }
        return false;
    }

    private static void initFileContent(String key, String logId) {
        LogFileStateData data = mLogFileStateDataMap.get(key);
        if (data == null) return;
        if (!data.isCreatedFile) return;
        if (data.mWriter == null) return;
        Long totalMemory = InvokeTool.callStaticMethod(InvokeTool.findClass("android.os.Process"), "getTotalMemory", new Class[]{});
        try {
            data.mWriter.write("###############################################");
            data.mWriter.newLine();
            data.mWriter.write("# Brand Info: " + SystemPropTool.getProp("ro.product.brand", "Unknown"));
            data.mWriter.newLine();
            data.mWriter.write("# Device Info: " + SystemPropTool.getProp("ro.product.device", "Unknown"));
            data.mWriter.newLine();
            data.mWriter.write("# MarketName Info: " + SystemPropTool.getProp("ro.product.marketname", "Unknown"));
            data.mWriter.newLine();
            data.mWriter.write("# Model Info: " + SystemPropTool.getProp("ro.product.model", "Unknown"));
            data.mWriter.newLine();
            data.mWriter.write("# OS Version: " + SystemPropTool.getProp("ro.product.build.version.incremental", "Unknown"));
            data.mWriter.newLine();
            data.mWriter.write("# Android Version: A" + SystemPropTool.getProp("ro.product.build.version.release", "Unknown"));
            data.mWriter.newLine();
            data.mWriter.write("# SDK Version: " + SystemPropTool.getProp("ro.build.version.sdk", "Unknown"));
            data.mWriter.newLine();
            data.mWriter.write("# Memory Info: " + (totalMemory != null ? totalMemory + "(" + ((totalMemory / 1024 / 1024 / 1024) + 1) + "GB)" : "Unknown"));
            data.mWriter.newLine();
            data.mWriter.write("# Module Version: " + ToolData.mModuleVersion);
            data.mWriter.newLine();
            data.mWriter.write("# Date Info: " + new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(System.currentTimeMillis()));
            data.mWriter.newLine();
            data.mWriter.write("# Log Id: " + logId);
            data.mWriter.newLine();
            data.mWriter.write("###############################################");
            data.mWriter.newLine();
            data.mWriter.write("!!!!!!! Module Will Be Start Hook Process !!!!!!!");
            data.mWriter.newLine();
            data.mWriter.newLine();
            data.mWriter.flush();
        } catch (IOException e) {
            logENoSave(TAG, "Init log file content failed! Path: " + LOG_FILE_FULL_PATH, e);
        }
    }

    private static String getDate() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("'['yyyy-MM-dd '|' HH:mm:ss.SSS']'");
        return now.format(formatter);
    }

    private static boolean isLogServiceBootCompleted(Context context) {
        if (context == null) return false;
        String result = Settings.System.getString(context.getContentResolver(), SETTINGS_LOG_SERVICE_COMPLETED);
        if (result == null || "0".equals(result)) return false;
        return "1".equals(result);
    }

    private static boolean isUserUnlockedCompeted() {
        return SystemPropTool.getProp(USER_UNLOCKED_COMPLETED_PROP, "false").equals("true");
    }

    private static void waitSystemBootCompletedIfNeed() {
        if (!DeviceTool.isBootCompleted() || !isUserUnlockedCompeted()) {
            isWaitingSystemBootCompleted = true;
            isWaitingLogServiceBootCompleted = true;
            Executors.newSingleThreadExecutor().submit(() -> {
                while (!DeviceTool.isBootCompleted()) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ignore) {
                    }
                }
                isWaitingSystemBootCompleted = false;

                int maxWhileCount = 10;
                while (maxWhileCount-- > 0) {
                    try {
                        Thread.sleep(10000);
                    } catch (InterruptedException e) {
                    }
                    if (isUserUnlockedCompeted())
                        break;
                }

                AndroidLog.logI(TAG, "user unlocked!!");

                pushWithAsyncContext(context -> {
                    int maxWhileCount1 = 3;
                    while (maxWhileCount1-- > 0) {
                        if (isLogServiceBootCompleted(context)) {
                            sendLogContentBroadcast(context);
                            isWaitingLogServiceBootCompleted = false;
                            break;
                        }
                        try {
                            Thread.sleep(10000);
                        } catch (InterruptedException e) {
                        }
                    }
                });
            });
        } else {
            pushWithAsyncContext(LogToFile::sendLogContentBroadcast);
        }
    }

    private static String getRandomNumber() {
        long randomNumber = 1000000000000L + ThreadLocalRandom.current().nextLong(9000000000000L);
        return String.valueOf(randomNumber);
    }

    @SuppressLint("MissingPermission")
    private static synchronized void sendLogContentBroadcast(Context context) {
        if (context == null || hasProcessingBroadcast) return;
        hasProcessingBroadcast = true;
        ArrayList<String> finalLogContent = new ArrayList<>(mLogContentData.mLogContent);
        mLogContentData.mLogContent.clear();
        Intent intent = new Intent();
        intent.setAction(ACTION_LOG_SERVICE_CONTENT);
        intent.putExtra("logId", mLogContentData.mLogId);
        intent.putExtra("logFileName", mLogContentData.mLogFileName);
        intent.putExtra("logContent", finalLogContent);
        AndroidLog.logI(TAG, "send broadcast logId: " + mLogContentData.mLogId + " logKey: " + mLogContentData.mLogFileName
            + " logContent: " + finalLogContent);
        context.sendOrderedBroadcast(intent, null, new RestrictionsReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (getResultCode() == Activity.RESULT_OK) {
                    hasProcessingBroadcast = false;
                    if (!mLogContentData.mLogContent.isEmpty()) {
                        sendLogContentBroadcast(context);
                    }
                }
                AndroidLog.logI(TAG, "broadcast result code: " + getResultCode());
            }

            @Override
            public void onRequestPermission(Context context, String packageName, String requestType, String requestId, PersistableBundle request) {
            }
        }, null, Activity.RESULT_CANCELED, null, null);
    }

    private static void pushWithAsyncContext(OnContextGetter onContextGetter) {
        ContextTool.getAsyncContext(onContextGetter::push, ContextTool.FLAG_ALL);
    }

    private interface OnContextGetter {
        void push(Context context);
    }

    private static class LogFileStateData {
        private boolean isCreatedFile = false;
        private boolean isOpened = false;
        private BufferedWriter mWriter;
        private BufferedReader mReader;
    }

    private static class LogContentData {
        public String mLogId = "";
        public String mLogFileName = "";
        public ArrayList<String> mLogContent = new ArrayList<>();
    }
}