package Com.HChen.Hook.Base;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;

import androidx.fragment.app.Fragment;

import java.util.Objects;

import Com.HChen.Hook.SystemLog;
import moralnorm.preference.Preference;
import moralnorm.preference.PreferenceManager;
import moralnorm.preference.compat.PreferenceFragment;

public class BasePreferenceFragment extends PreferenceFragment {
    public final String TAG = "BasePreferenceFragment";
    SystemLog systemLog = new SystemLog();
    public static final String mPrefsName = "HChen_prefs";

    /**
     * @noinspection deprecation
     */
    @SuppressLint("WorldReadableFiles")
    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        PreferenceManager mPreferenceManager = getPreferenceManager();
        mPreferenceManager.setSharedPreferencesName(mPrefsName);
        mPreferenceManager.setSharedPreferencesMode(Context.MODE_WORLD_READABLE);
        mPreferenceManager.setStorageDeviceProtected();
        systemLog.logI(TAG, "onCreatePreferences: " + mPreferenceManager);
    }

    public void setTitle(int titleResId) {
        setTitle(getResources().getString(titleResId));
    }

    public void setTitle(String title) {
        if (!TextUtils.isEmpty(title)) {
            requireActivity().setTitle(title);
        }
    }

    public String getFragmentName(Fragment fragment) {
        return fragment.getClass().getName();
    }

    public String getPreferenceTitle(Preference preference) {
        return Objects.requireNonNull(preference.getTitle()).toString();
    }

    public String getPreferenceKey(Preference preference) {
        return preference.getKey();
    }

    public void finish() {
        requireActivity().finish();
    }
}
