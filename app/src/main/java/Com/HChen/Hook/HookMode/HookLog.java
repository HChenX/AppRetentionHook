package Com.HChen.Hook.HookMode;

import android.util.Log;

import de.robv.android.xposed.XposedBridge;

public class HookLog {
    String Tag = "[ HChenHook ]: ";

    public void setLog(String classLog, String nameLog) {
        logSI("Hook Class: " + classLog + " methodName: " + nameLog);
    }

    public void setLog(String classLog) {
        logSI("Hook Class: " + classLog);
    }

    public void logI(String Log) {
        XposedBridge.log(Tag + "I: " + Log);
    }

    public void logW(String Log) {
        XposedBridge.log(Tag + "Warning by: " + Log);
    }

    public void logE(String Log) {
        XposedBridge.log(Tag + "Error by: " + Log);
    }

    public void logSI(String log) {
        Log.i(Tag, "I: " + log);
    }

    public void logSW(String log) {
        Log.w(Tag, "I: " + log);
    }

    public void logSE(String log) {
        Log.e(Tag, "I: " + log);
    }
}
