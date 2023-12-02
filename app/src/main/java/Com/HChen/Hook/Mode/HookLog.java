package Com.HChen.Hook.Mode;

import android.util.Log;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;

public class HookLog {
    public static final String hookMain = "[HChen]";
    public static final String mHook = "[HChen:";
    public static final String mode = "]";
    public static String methodProcessed = null;

    public static Info paramCheck(XC_MethodHook.MethodHookParam param) {
        String method = null;
        String thisObject = null;
        if (param.method != null) {
            method = param.method.toString();
        }
        if (param.thisObject != null) {
            thisObject = param.thisObject.toString();
        }
        if (param.method == null && param.thisObject == null)
            logE("paramCheck", "param.method is: " + param.method
                + " param.thisObject is: " + param.thisObject);
        return new Info(method, thisObject, null);
    }

    public static Info getInfo(String method, String thisObject) {
//            int lastIndex = all.lastIndexOf(".");
        if (method == null) return
            new Info(null, null, null);
        if (thisObject != null) {
            Pattern pattern = Pattern.compile(".*\\.(.*\\(.*\\))");
            Matcher matcher = pattern.matcher(method);
            if (thisObject.contains("@")) {
                if (matcher.find()) {
                    methodProcessed = matcher.group(1);
                } else methodProcessed = null;
                pattern = Pattern.compile(".*\\.(\\w+)\\..*\\(.*\\)");
                matcher = pattern.matcher(method);
                if (matcher.find()) {
                    thisObject = matcher.group(1);
                } else thisObject = null;
            } else {
                if (matcher.find()) {
                    thisObject = matcher.group(1);
                } else methodProcessed = "constructor";
            }
        } else {
            Pattern pattern = Pattern.compile(".*\\.(\\w+)\\.(.*\\(.*\\))");
            Matcher matcher = pattern.matcher(method);
            if (matcher.find()) {
                thisObject = matcher.group(1);
                methodProcessed = matcher.group(2);
            } else return new Info(null, method, thisObject);
        }
        return new Info(null, thisObject, methodProcessed);
    }

    public static StringBuilder paramLog(XC_MethodHook.MethodHookParam param) {
        StringBuilder log = null;
        for (int i = 0; i < param.args.length; i++) {
            log = (log == null ? new StringBuilder() : log).append("param(").append(i).append("): ").append(param.args[i]).append(" ");
        }
        return log;
    }

    public static class Info {
        public String method;
        public String thisObject;
        public String methodProcessed;

        public Info(String method, String thisObject, String methodProcessed) {
            this.method = method;
            this.thisObject = thisObject;
            this.methodProcessed = methodProcessed;
        }
    }

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

    public static void logSW(String tag, String log) {
        Log.w(hookMain, "[" + tag + "]: W: " + log);
    }

    public void logSE(String tag, String log) {
        Log.e(hookMain, "[" + tag + "]: E: " + log);
    }
}
