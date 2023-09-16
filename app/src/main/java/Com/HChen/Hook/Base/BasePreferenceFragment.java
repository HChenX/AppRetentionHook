package com.hchen.hook.base;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;

import androidx.fragment.app.Fragment;

import moralnorm.preference.Preference;
import moralnorm.preference.PreferenceManager;
import moralnorm.preference.compat.PreferenceFragment;

public class BasePreferenceFragment extends PreferenceFragment {
    private static final String PREFS_NAME = "HChen_prefs";
    private PreferenceManager preferenceManager;

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        preferenceManager = getPreferenceManager();
        preferenceManager.setSharedPreferencesName(PREFS_NAME);
        preferenceManager.setSharedPreferencesMode(Context.MODE_PRIVATE);
        preferenceManager.setStorageDeviceProtected();
    }

    public void setTitle(int titleResId) {
        setTitle(getString(titleResId));
    }

    public void setTitle(String title) {
        if (!TextUtils.isEmpty(title)) {
            getActivity().setTitle(title);
        }
    }

    public String getFragmentName(Fragment fragment) {
        return fragment.getClass().getName();
    }

    public String getPreferenceTitle(Preference preference) {
        return preference.getTitle().toString();
    }

    public String getPreferenceKey(Preference preference) {
        return preference.getKey();
    }

    public void finish() {
        getActivity().finish();
    }
}
