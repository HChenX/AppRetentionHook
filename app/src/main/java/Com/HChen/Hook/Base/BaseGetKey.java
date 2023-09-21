package Com.HChen.Hook.Base;

import android.annotation.SuppressLint;
import android.content.Context;

import java.util.Map;

import Com.HChen.Hook.BuildConfig;
import Com.HChen.Hook.SystemLog;
import Com.HChen.Hook.Utils.GetKey;
import de.robv.android.xposed.XSharedPreferences;

public class BaseGetKey extends SystemLog {
    public static GetKey<String, Object> mPrefsMap = new GetKey<>();
    public static final String mPrefsName = "HChen_prefs";
    public final String TAG = "BaseGetKey";

    /**
     * @noinspection deprecation
     */
    @SuppressLint("WorldReadableFiles")
    public static void getSharedPrefs(Context context) {
        context = context.createDeviceProtectedStorageContext();
        context.getSharedPreferences(mPrefsName, Context.MODE_WORLD_READABLE);
    }

    public void setXSharedPrefs() {
        if (mPrefsMap.size() == 0) {
            XSharedPreferences mXSharedPreferences;
            try {
                mXSharedPreferences = new XSharedPreferences(BuildConfig.APPLICATION_ID, mPrefsName);
                mXSharedPreferences.makeWorldReadable();
                Map<String, ?> allPrefs = mXSharedPreferences == null ? null : mXSharedPreferences.getAll();
                if (allPrefs != null) {
                    logI(TAG, "setXSharedPrefs: " + allPrefs);
                    mPrefsMap.putAll(allPrefs);
                } else {
                    logE(TAG, "setXSharedPrefs: null");
                }
            } catch (Throwable t) {
                logW(TAG, "setXSharedPrefs: get mXSharedPreferences fail");
            }
        }
    }

}
