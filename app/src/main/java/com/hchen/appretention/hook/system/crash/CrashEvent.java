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
package com.hchen.appretention.hook.system.crash;

import static com.hchen.appretention.data.method.SystemMethod.handleAppCrashInActivityController;
import static com.hchen.appretention.data.path.SystemClass.AppErrors;

import android.app.ApplicationErrorReport;
import android.content.Context;

import com.hchen.appretention.data.field.SystemField;
import com.hchen.collect.HookEntrance;
import com.hchen.hooktool.HCBase;
import com.hchen.hooktool.hook.IHook;

import java.lang.reflect.Method;
import java.util.Optional;

/**
 * 系统崩溃事件捕捉
 *
 * @author 焕晨HChen
 */
@HookEntrance(targetPackage = "android")
public class CrashEvent extends HCBase {
    @Override
    public void init() {
        Class<?> appError = findClass(AppErrors);
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
                    Context mContext = (Context) getThisField(SystemField.mContext);
                    Object proc = getArg(0);
                    ApplicationErrorReport.CrashInfo crashInfo = (ApplicationErrorReport.CrashInfo) getArg(1);
                    if (crashInfo == null) return;

                    String shortMsg = (String) getArg(2);
                    String longMsg = (String) getArg(3);
                    String stackTrace = (String) getArg(4);
                    long timeMillis = (long) Optional.ofNullable(getArg(5)).orElse(-1);
                    int callingPid = (int) Optional.ofNullable(getArg(6)).orElse(-1);
                    int callingUid = (int) Optional.ofNullable(getArg(7)).orElse(-1);

                    if ("Native crash".equals(crashInfo.exceptionClassName))
                        return; // 跳过 Native crash 事件

                    logE(TAG, "A crash event has occurred! Caught! Please note that crashes are not necessarily caused by modules!" +
                        "\n[Crash Package]: " + mContext.getPackageName() + "\n[Proc]: " + proc +
                        "\n[Time]: " + timeMillis + " ms\n[Calling PID]: " + callingPid + "\n[Calling UID]: " + callingUid +
                        "\n[Crash Info]: " + crashInfo + "\n[Short Msg]: " + shortMsg + "\n[Long Msg]: " + longMsg + "\n[Stack]: " + stackTrace);
                }
            }
        );
    }
}
