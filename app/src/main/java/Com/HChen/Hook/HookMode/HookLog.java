package Com.HChen.Hook.HookMode;

import android.util.Log;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;

public abstract class HookLog extends XC_MethodHook {
    final String hookTag = "Hook Class: ";
    final String hookMain = "[ HChenHook ]: ";
    final String methodName = "methodName: ";
    String Tag;
    String Tags;

    public int smOr() {
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
    }

    public HookLog() {
        super();
    }

    public HookLog(int priority) {
        super(priority);
    }

    public void setLog(int smOr, String grade, String classLog, String nameLog) {
        switch (grade) {
            case "I" -> logSI(smOr, hookTag + classLog + methodName + nameLog);
            case "W" -> logSW(smOr, hookTag + classLog + methodName + nameLog);
            case "E" -> logSE(smOr, hookTag + classLog + methodName + nameLog);
        }
    }

    public void setLog(int smOr, String grade, String classLog) {
        switch (grade) {
            case "I" -> logSI(smOr, hookTag + classLog);
            case "W" -> logSW(smOr, hookTag + classLog);
            case "E" -> logSE(smOr, hookTag + classLog);
        }
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
