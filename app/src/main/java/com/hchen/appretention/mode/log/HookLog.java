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
package com.hchen.appretention.mode.log;

import android.util.Log;

import de.robv.android.xposed.XposedBridge;

public class HookLog {
    public static final String hookMain = "[HChen]";
    public static final String mHook = "[HChen:";
    public static final String mode = "]";

    public static void logFilter(String into, String[] filter, Runnable runF, Runnable runO) {
        for (String get : filter) {
            if (into.equals(get)) {
                runF.run();
            } else runO.run();
        }
    }

    public static void logI(String tag, String Log) {
        XposedBridge.log(hookMain + "[" + tag + "]: " + "I: " + Log);
    }

    public static void logI(String name, String tag, String Log) {
        XposedBridge.log(mHook + name + mode + "[" + tag + "]: " + "I: " + Log);
    }

    public static void logW(String tag, String Log) {
        XposedBridge.log(hookMain + "[" + tag + "]: " + "W: " + Log);
    }

    public static void logE(String tag, String Log) {
        XposedBridge.log(hookMain + "[" + tag + "]: " + "E: " + Log);
    }

    public static void logSI(String name, String tag, String log) {
        Log.i(mHook + name + mode, "[" + tag + "]: I: " + log);
    }

    public static void logSI(String tag, String log) {
        Log.i(hookMain, "[" + tag + "]: I: " + log);
    }

    public static void logSW(String tag, String log) {
        Log.w(hookMain, "[" + tag + "]: W: " + log);
    }

    public void logSE(String tag, String log) {
        Log.e(hookMain, "[" + tag + "]: E: " + log);
    }
}
