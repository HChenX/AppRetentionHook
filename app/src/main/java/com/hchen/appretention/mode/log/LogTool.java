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

import static com.hchen.appretention.mode.log.HookLog.logE;
import static com.hchen.appretention.mode.log.HookLog.logSW;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.robv.android.xposed.XC_MethodHook;

public class LogTool {
    private String mMethod = null;
    private String mClass = null;
    private final String log;
    private final Pattern findMethod;
    private final Pattern findClass;
    private final Pattern findAny;

    /**
     * @noinspection FieldCanBeLocal
     */
    private Matcher matcher = null;

    private String lastMethod = null;
    private String lastClass = null;
    private Info mInfo = null;


    public LogTool(String callClass) {
        log = callClass;
        findMethod = Pattern.compile(".*\\.(.*\\(.*\\))");
        findClass = Pattern.compile(".*\\.(\\w+)\\..*\\(.*\\)");
        findAny = Pattern.compile(".*\\.(\\w+)\\.(.*\\(.*\\))");
    }

    public Info paramCheck(XC_MethodHook.MethodHookParam param) {
        String method = null;
        String thisObject = null;
        if (param.method != null) {
            method = param.method.toString();
        }
        if (param.thisObject != null) {
            thisObject = param.thisObject.toString();
        }
        if (param.method == null && param.thisObject == null)
            logE(log + ":paramCheck", "param.method is: null && param.thisObject is: null!!");
        return new Info(method, thisObject, null, null);
    }

    public Info getInfo(String method, String thisObject) {
        if (lastMethod != null && lastClass != null) {
            if (mInfo != null) {
                return mInfo;
            }
        }
        if (method == null) return
            new Info(null, null, null, null);
        if (thisObject != null) {
            matcher = findMethod.matcher(method);
            if (thisObject.contains("@")) {
                if (matcher.find()) {
                    mMethod = matcher.group(1);
                } else mMethod = null;
                matcher = findClass.matcher(method);
                if (matcher.find()) {
                    mClass = matcher.group(1);
                } else mClass = null;
            } else {
                if (matcher.find()) {
                    mClass = matcher.group(1);
                } else mMethod = "constructor";
            }
        } else {
            matcher = findAny.matcher(method);
            if (matcher.find()) {
                mClass = matcher.group(1);
                mMethod = matcher.group(2);
            } else {
                logSW(log + ":getInfo", "Method: " + method + " can't find anything!");
                return new Info(null, null, null, null);
            }
        }
        mInfo = new Info(null, null, mClass, mMethod);
        lastMethod = mMethod;
        lastClass = mClass;
        return mInfo;
    }

    public StringBuilder paramLog(XC_MethodHook.MethodHookParam param) {
        StringBuilder log = null;
        for (int i = 0; i < param.args.length; i++) {
            log = (log == null ? new StringBuilder() : log).append("param(").append(i).append(")->").append("[").append(param.args[i]).append("]").append(" ");
        }
        return log;
    }

    public static class Info {
        public String method;
        public String thisObject;
        public String mClass;
        public String mMethod;

        public Info(String method, String thisObject, String cl, String mMethod) {
            this.method = method;
            this.thisObject = thisObject;
            this.mClass = cl;
            this.mMethod = mMethod;
        }
    }

}
