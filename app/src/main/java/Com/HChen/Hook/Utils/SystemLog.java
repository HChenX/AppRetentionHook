package Com.HChen.Hook.Utils;

import android.util.Log;

public class SystemLog {
    public static final String mTAG = "[HChenHookLogs]";

    public static void logI(String c, String log) {
        Log.i(mTAG, "LogI: " + c + ": " + log);
    }

    public static void logW(String c, String log) {
        Log.w(mTAG, "logW: " + c + ": " + log);
    }

    public static void logW(String c, String log, Throwable e) {
        Log.w(mTAG, "logW: " + c + ": " + log, e);
    }

    public static void logE(String c, String log) {
        Log.e(mTAG, "logE: " + c + ": " + log);
    }

    public static void logE(String c, String log, Throwable e) {
        Log.e(mTAG, "logE: " + c + ": " + log, e);
    }
}
