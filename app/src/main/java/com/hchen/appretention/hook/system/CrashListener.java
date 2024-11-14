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

import static com.hchen.appretention.data.method.System.handleAppCrashInActivityController;
import static com.hchen.appretention.data.path.System.AppErrors;
import static com.hchen.hooktool.log.XposedLog.logE;

import android.app.ApplicationErrorReport;
import android.content.Context;

import com.hchen.appretention.data.field.System;
import com.hchen.hooktool.BaseHC;
import com.hchen.hooktool.hook.IHook;

import java.lang.reflect.Method;

/**
 * 系统崩溃事件捕捉
 *
 * @author 焕晨HChen
 */
public class CrashListener extends BaseHC {
    @Override
    public void init() {
        Class<?> appError = findClass(AppErrors).get();
        if (appError == null) {
            logE(TAG, "No such 'com.android.server.am.AppErrors'");
            return;
        }
        Method hookError = null;
        for (Method error : appError.getDeclaredMethods()) {
            if (handleAppCrashInActivityController.equals(error.getName()))
                if (error.getReturnType().equals(boolean.class)) {
                    hookError = error;
                    break;
                }
        }
        if (hookError == null) {
            logE(TAG, "No such method: 'handleAppCrashInActivityController' in 'com.android.server.am.AppErrors'");
            return;
        }

        hook(hookError, new IHook() {
                @Override
                public void after() {
                    Context mContext = getThisField(System.mContext);
                    Object proc = getArgs(0);
                    ApplicationErrorReport.CrashInfo crashInfo = getArgs(1);
                    String shortMsg = getArgs(2);
                    String longMsg = getArgs(3);
                    String stackTrace = getArgs(4);
                    long timeMillis = getArgs(5);
                    int callingPid = getArgs(6);
                    int callingUid = getArgs(7);
                    if (crashInfo == null) return;
                    if ("Native crash".equals(crashInfo.exceptionClassName))
                        return; // 跳过 Native crash 事件

                    logE(TAG, "A crash event has occurred! Caught! Please note that crashes are not necessarily caused by modules!" +
                        "\n[Crash Package]: " + mContext.getPackageName() + "\n[Proc]: " + proc +
                        "\n[Time]: " + timeMillis + "ms\n[Calling PID]: " + callingPid + "\n[Calling UID]: " + callingUid +
                        "\n[Crash Info]: " + crashInfo + "\n[Short Msg]: " + shortMsg + "\n[Long Msg]: " + longMsg + "\n[Stack]: " + stackTrace);
                }
            }
        );
    }
}
