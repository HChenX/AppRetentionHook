package Com.HChen.Hook.Utils;

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
