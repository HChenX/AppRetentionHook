package Com.HChen.Hook.Base;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Map;

import Com.HChen.Hook.BuildConfig;
import Com.HChen.Hook.Utils.GetKey;
import de.robv.android.xposed.XSharedPreferences;

public class BasePutKey {
    public static GetKey<String, Object> mPrefsMap = new GetKey<>();
    public static String mPrefsName = "HChen_prefs";

    public static SharedPreferences getSharedPrefs(Context context, boolean multiProcess) {
        context = context.createDeviceProtectedStorageContext();
        try {
            return context.getSharedPreferences(mPrefsName, multiProcess ? Context.MODE_MULTI_PROCESS | Context.MODE_WORLD_READABLE : Context.MODE_WORLD_READABLE | Context.MODE_PRIVATE);
        } catch (Throwable t) {
            return context.getSharedPreferences(mPrefsName, multiProcess ? Context.MODE_MULTI_PROCESS | Context.MODE_PRIVATE : Context.MODE_PRIVATE);
        }
    }

    public static SharedPreferences getSharedPrefs(Context context) {
        return getSharedPrefs(context, false);
    }

    public void setXSharedPrefs() {
        if (mPrefsMap.size() == 0) {
            XSharedPreferences mXSharedPreferences = null;
            try {
                mXSharedPreferences = new XSharedPreferences(BuildConfig.APPLICATION_ID, mPrefsName);
                mXSharedPreferences.makeWorldReadable();
                Map<String, ?> allPrefs = mXSharedPreferences == null ? null : mXSharedPreferences.getAll();
                mPrefsMap.putAll(allPrefs);
            } catch (Throwable t) {

            }
        }
    }


}
