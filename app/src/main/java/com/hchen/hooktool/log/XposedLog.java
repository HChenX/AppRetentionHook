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
package com.hchen.hooktool.log;

import com.hchen.appretention.log.LogToFile;
import com.hchen.hooktool.data.ToolData;

import de.robv.android.xposed.XposedBridge;

/**
 * LSP 框架日志
 *
 * @author 焕晨HChen
 */
public class XposedLog {
    // -------- logE -------------
    public static void logE(String tag, String log) {
        if (ToolData.mInitLogLevel < 1) return;
        XposedBridge.log(ToolData.mInitTag + "[" + tag + "]" + "[E]: " + log);
        LogToFile.saveLogContent(ToolData.mInitTag + "[" + tag + "]" + "[E]: " + log);
    }

    public static void logE(String tag, String log, String msg) {
        if (ToolData.mInitLogLevel < 1) return;
        XposedBridge.log(ToolData.mInitTag + "[" + tag + "]" + "[E]: " + log + " \n[Error Msg]: " + msg);
        LogToFile.saveLogContent(ToolData.mInitTag + "[" + tag + "]" + "[E]: " + log + " \n[Error Msg]: " + msg);
    }

    public static void logE(String tag, Throwable e) {
        if (ToolData.mInitLogLevel < 1) return;
        XposedBridge.log(ToolData.mInitTag + "[" + tag + "]" + "[E]: \n" + LogExpand.printStackTrace(e));
        LogToFile.saveLogContent(ToolData.mInitTag + "[" + tag + "]" + "[E]: \n" + LogExpand.printStackTrace(e));
    }

    public static void logE(String tag, String log, Throwable e) {
        if (ToolData.mInitLogLevel < 1) return;
        XposedBridge.log(ToolData.mInitTag + "[" + tag + "]" + "[E]: " + log + " \n[Error Msg]: " + LogExpand.printStackTrace(e));
        LogToFile.saveLogContent(ToolData.mInitTag + "[" + tag + "]" + "[E]: " + log + " \n[Error Msg]: " + LogExpand.printStackTrace(e));
    }

    public static void logENoSave(String tag, String log) {
        if (ToolData.mInitLogLevel < 1) return;
        XposedBridge.log(ToolData.mInitTag + "[" + tag + "]" + "[E]: " + log);
    }

    public static void logENoSave(String tag, String log, Throwable e) {
        if (ToolData.mInitLogLevel < 1) return;
        XposedBridge.log(ToolData.mInitTag + "[" + tag + "]" + "[E]: " + log + " \n[Error Msg]: " + LogExpand.printStackTrace(e));
    }

    // ----------- logW --------------
    public static void logW(String tag, String log) {
        if (ToolData.mInitLogLevel < 2) return;
        XposedBridge.log(ToolData.mInitTag + "[" + tag + "]" + "[W]: " + log);
        LogToFile.saveLogContent(ToolData.mInitTag + "[" + tag + "]" + "[W]: " + log);
    }

    public static void logW(String tag, Throwable e) {
        if (ToolData.mInitLogLevel < 2) return;
        XposedBridge.log(ToolData.mInitTag + "[" + tag + "]" + "[W]: \n" + LogExpand.printStackTrace(e));
        LogToFile.saveLogContent(ToolData.mInitTag + "[" + tag + "]" + "[W]: \n" + LogExpand.printStackTrace(e));
    }

    public static void logW(String tag, String log, Throwable e) {
        if (ToolData.mInitLogLevel < 2) return;
        XposedBridge.log(ToolData.mInitTag + "[" + tag + "]" + "[W]: " + log + " \n[Warning Msg]: " + LogExpand.printStackTrace(e));
        LogToFile.saveLogContent(ToolData.mInitTag + "[" + tag + "]" + "[W]: " + log + " \n[Warning Msg]: " + LogExpand.printStackTrace(e));
    }

    // ----------- logI --------------
    public static void logI(String log) {
        if (ToolData.mInitLogLevel < 3) return;
        XposedBridge.log(ToolData.mInitTag + "[I]: " + log);
        LogToFile.saveLogContent(ToolData.mInitTag + "[I]: " + log);
    }

    public static void logI(String tag, String log) {
        if (ToolData.mInitLogLevel < 3) return;
        XposedBridge.log(ToolData.mInitTag + "[" + tag + "]" + "[I]: " + log);
        LogToFile.saveLogContent(ToolData.mInitTag + "[" + tag + "]" + "[I]: " + log);
    }

    public static void logI(String tag, String pkg, String log) {
        if (ToolData.mInitLogLevel < 3) return;
        XposedBridge.log(ToolData.mInitTag + "[" + tag + "]" + "[" + pkg + "][I]: " + log);
        LogToFile.saveLogContent(ToolData.mInitTag + "[" + tag + "]" + "[" + pkg + "][I]: " + log);
    }

    public static void logI(String tag, String log, Throwable e) {
        if (ToolData.mInitLogLevel < 3) return;
        XposedBridge.log(ToolData.mInitTag + "[" + tag + "]" + "[I]: " + log + " \n[Info Msg]: " + LogExpand.printStackTrace(e));
        LogToFile.saveLogContent(ToolData.mInitTag + "[" + tag + "]" + "[I]: " + log + " \n[Info Msg]: " + LogExpand.printStackTrace(e));
    }

    // ------------ logD --------------
    public static void logD(String tag, Throwable e) {
        if (ToolData.mInitLogLevel < 4) return;
        XposedBridge.log(ToolData.mInitTag + "[" + tag + "]" + "[D]: \n" + LogExpand.printStackTrace(e));
        LogToFile.saveLogContent(ToolData.mInitTag + "[" + tag + "]" + "[D]: \n" + LogExpand.printStackTrace(e));
    }

    public static void logD(String tag, String log) {
        if (ToolData.mInitLogLevel < 4) return;
        XposedBridge.log(ToolData.mInitTag + "[" + tag + "]" + "[D]: " + log);
        LogToFile.saveLogContent(ToolData.mInitTag + "[" + tag + "]" + "[D]: " + log);
    }

    public static void logD(String tag, String log, Throwable e) {
        if (ToolData.mInitLogLevel < 4) return;
        XposedBridge.log(ToolData.mInitTag + "[" + tag + "]" + "[D]: " + log + " \n[Debug Msg]: " + LogExpand.printStackTrace(e));
        LogToFile.saveLogContent(ToolData.mInitTag + "[" + tag + "]" + "[D]: " + log + " \n[Debug Msg]: " + LogExpand.printStackTrace(e));
    }
}
