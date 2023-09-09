package Com.HChen.Hook.Base;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;

import java.util.Map;

import Com.HChen.Hook.BuildConfig;
import Com.HChen.Hook.Utils.GetKey;
import de.robv.android.xposed.XSharedPreferences;

public class BasePutKey {
    public static GetKey<String, Object> mPrefsMap = new GetKey<>();
    public static String mPrefsName = "HChen_prefs";
    final String TAG = "[ HChenHook ]: ";

    /**
     * @noinspection deprecation
     */
    @SuppressLint("WorldReadableFiles")
    public static void getSharedPrefs(Context context, boolean multiProcess) {
        context = context.createDeviceProtectedStorageContext();
        try {
            context.getSharedPreferences(mPrefsName, multiProcess ? Context.MODE_MULTI_PROCESS | Context.MODE_WORLD_READABLE : Context.MODE_WORLD_READABLE | Context.MODE_PRIVATE);
        } catch (Throwable t) {
            context.getSharedPreferences(mPrefsName, multiProcess ? Context.MODE_MULTI_PROCESS | Context.MODE_PRIVATE : Context.MODE_PRIVATE);
        }
    }

    public static void getSharedPrefs(Context context) {
        getSharedPrefs(context, false);
    }

    public void setXSharedPrefs() {
        if (mPrefsMap.size() == 0) {
            XSharedPreferences mXSharedPreferences;
            try {
                mXSharedPreferences = new XSharedPreferences(BuildConfig.APPLICATION_ID, mPrefsName);
                mXSharedPreferences.makeWorldReadable();
                Map<String, ?> allPrefs = mXSharedPreferences == null ? null : mXSharedPreferences.getAll();
                if (allPrefs != null) {
                    Log.i(TAG, "setXSharedPrefs: " + allPrefs);
                    mPrefsMap.putAll(allPrefs);
                } else {
                    Log.w(TAG, "setXSharedPrefs: null");
                }
            } catch (Throwable t) {

            }
        }
    }


}
