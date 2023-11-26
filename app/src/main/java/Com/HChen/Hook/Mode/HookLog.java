package Com.HChen.Hook.Mode;

import android.util.Log;

import de.robv.android.xposed.XposedBridge;

public class HookLog {
    //    static final String hookTag = "Hook Class: ";
    static final String hookMain = "[HChenHook]";
//    static final String methodNameS = "methodName: ";

    public static void logI(String tag, String Log) {
//        setTag();
        XposedBridge.log(hookMain + "[" + tag + "]: " + "Info: " + Log);
    }

    public static void logW(String tag, String Log) {
//        setTag();
        XposedBridge.log(hookMain + "[" + tag + "]: " + "Warning: " + Log);
    }

    public static void logE(String tag, String Log) {
//        setTag();
        XposedBridge.log(hookMain + "[" + tag + "]: " + "Error: " + Log);
    }

    public static void logSI(String tag, String log) {
//        setTags(smOr);
        Log.i(hookMain, "[" + tag + "]: Info: " + log);
    }

    public static void logSW(String tag, String log) {
//        setTags(smOr);
        Log.w(hookMain, "[" + tag + "]: Warning: " + log);
    }

    public void logSE(String tag, String log) {
//        setTags(smOr);
        Log.e(hookMain, "[" + tag + "]: Error: " + log);
    }

    /*public int smOr() {
        return -1;
    }

    public void setTag() {
        if (smOr() == 1) {
            Tag = hookMain + "SystemHook: ";
        } else if (smOr() == 2) {
            Tag = hookMain + "MiuiHook: ";
        } else {
            Tag = hookMain;
        }
    }

    public void setTags(int smOr) {
        if (smOr == 1) {
            Tags = hookMain + "SystemHook: ";
        } else if (smOr == 2) {
            Tags = hookMain + "MiuiHook: ";
        }
    }*/


   /* public void setLog(int smOr, String grade, String classLog, String nameLog) {
        switch (grade) {
            case "I" -> logSI(smOr, hookTag + classLog + methodNameS + nameLog);
            case "W" -> logSW(smOr, hookTag + classLog + methodNameS + nameLog);
            case "E" -> logSE(smOr, hookTag + classLog + methodNameS + nameLog);
        }
    }

    public void setLog(int smOr, String grade, String classLog) {
        switch (grade) {
            case "I" -> logSI(smOr, hookTag + classLog);
            case "W" -> logSW(smOr, hookTag + classLog);
            case "E" -> logSE(smOr, hookTag + classLog);
        }
    }*/
}
