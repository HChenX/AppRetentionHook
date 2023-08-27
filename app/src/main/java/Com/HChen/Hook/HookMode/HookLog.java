package Com.HChen.Hook.HookMode;

import android.util.Log;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;

public abstract class HookLog extends XC_MethodHook {
    String Tag;
    String Tags;

    public int smOr() {
        return -1;
    }

    public void setTag() {
        if (smOr() == 1) {
            Tag = "[ HChenHook ]: SystemHook: ";
        } else if (smOr() == 2) {
            Tag = "[ HChenHook ]: MiuiHook: ";
        } else {
            Tag = "[ HChenHook ]: ";
        }
    }

    public void setTags(int smOr) {
        if (smOr == 1) {
            Tags = "[ HChenHook ]: SystemHook: ";
        } else if (smOr == 2) {
            Tags = "[ HChenHook ]: MiuiHook: ";
        }
    }

    public HookLog() {
        super();
    }

    public HookLog(int priority) {
        super(priority);
    }

    public void setLog(int smOr, String classLog, String nameLog) {
        logSI(smOr, "Hook Class: " + classLog + " methodName: " + nameLog);
    }

    public void setLog(int smOr, String classLog) {
        logSI(smOr, "Hook Class: " + classLog);
    }

    public void logI(String Log) {
        setTag();
        XposedBridge.log(Tag + "I: " + Log);
    }

    public void logW(String Log) {
        setTag();
        XposedBridge.log(Tag + "Warning by: " + Log);
    }

    public void logE(String Log) {
        setTag();
        XposedBridge.log(Tag + "Error by: " + Log);
    }

    public void logSI(int smOr, String log) {
        setTags(smOr);
        Log.i(Tags, "I: " + log);
    }

    public void logSW(int smOr, String log) {
        setTags(smOr);
        Log.w(Tag, "I: " + log);
    }

    public void logSE(int smOr, String log) {
        setTags(smOr);
        Log.e(Tag, "I: " + log);
    }
}
