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
package com.hchen.appretention.hook;

import static com.hchen.appretention.log.LogToFile.ACTION_LOG_SERVICE_CONTENT;
import static com.hchen.appretention.log.LogToFile.SETTINGS_LOG_SERVICE_COMPLETED;
import static com.hchen.appretention.log.LogToFile.isUserUnlockedCompeted;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.provider.Settings;

import com.hchen.appretention.log.LogToFile;
import com.hchen.hooktool.BaseHC;
import com.hchen.hooktool.hook.IHook;
import com.hchen.hooktool.log.AndroidLog;

import java.util.ArrayList;

/**
 * 劫持系统界面作为傀儡日志写入工具
 *
 * @author 焕晨HChen
 */
public class LogPuppet extends BaseHC {
    private boolean isRegisterReceiver = false;

    @Override
    public void init() {
        hookMethod("com.android.systemui.SystemUIApplication",
            "onCreate",
            new IHook() {
                @Override
                public void after() {
                    isRegisterReceiver = false;
                    Application application = thisObject();
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
    }

    public static class LogServiceBroadcastReceiver extends BroadcastReceiver {
        private static final String TAG = "LogPuppet";

        @Override
        public void onReceive(Context context, Intent intent) {
            if ("com.hchen.appretention.LOG_SERVICE_CONTENT".equals(intent.getAction())) {
                String id = intent.getStringExtra("logId");
                String key = intent.getStringExtra("logFileName");
                ArrayList<String> content = intent.getStringArrayListExtra("logContent");

                LogToFile.createFile(key);
                LogToFile.openFile(key, id);
                LogToFile.writeFile(key, content);

                setResultCode(Activity.RESULT_OK);
                AndroidLog.logI(TAG, "broadcast receiver: key: " + key + ", id: " + id + ", content: " + content);
            }
        }
    }
}
