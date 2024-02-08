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
package Com.HChen.Hook.mode.log;

import android.util.Log;

public class SystemLog {
    public static final String mTAG = "[HChenLog]";

    public static void logI(String c, String log) {
        Log.i(mTAG, "I: " + c + ": " + log);
    }

    public static void logW(String c, String log) {
        Log.w(mTAG, "W: " + c + ": " + log);
    }

    public static void logW(String c, String log, Throwable e) {
        Log.w(mTAG, "W: " + c + ": " + log, e);
    }

    public static void logE(String c, String log) {
        Log.e(mTAG, "E: " + c + ": " + log);
    }

    public static void logE(String c, String log, Throwable e) {
        Log.e(mTAG, "E: " + c + ": " + log, e);
    }
}
