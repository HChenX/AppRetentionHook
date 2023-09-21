package Com.HChen.Hook;

import android.util.Log;

public class SystemLog {
    public final String mTAG = "[ HChenHookLogs ]";

    public void logI(String c, String log) {
        Log.i(mTAG, "LogI: " + c + ": " + log);
    }

    public void logW(String c, String log) {
        Log.w(mTAG, "logW: " + c + ": " + log);
    }

    public void logW(String c, String log, Throwable e) {
        Log.w(mTAG, "logW: " + c + ": " + log, e);
    }

    public void logE(String c, String log) {
        Log.e(mTAG, "logE: " + c + ": " + log);
    }

    public void logE(String c, String log, Throwable e) {
        Log.e(mTAG, "logE: " + c + ": " + log, e);
    }
}
