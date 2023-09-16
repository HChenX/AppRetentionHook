package com.hchen.hook.base;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.hchen.hook.subsettings.SubSettings;
import com.hchen.hook.util.SettingLauncher;

public class SettingsActivity extends BaseSettingsActivity implements PreferenceFragmentCompat.OnPreferenceStartFragmentCallback {

    public void onStartSettingsForArguments(Preference preference, boolean isBundleEnable) {
        onStartSettingsForArguments(SubSettings.class, preference, isBundleEnable);
    }

    @Override
    public boolean onPreferenceStartFragment(@NonNull PreferenceFragmentCompat preferenceFragmentCompat, @NonNull Preference preference) {
        onStartSettingsForArguments(preference, true);
        return true;
    }

    public void onStartSettingsForArguments(Class<?> cls, Preference preference, boolean isEnableBundle) {
        Bundle args = isEnableBundle ? createBundleWithPreferenceKey(preference) : null;
        String fragmentName = preference.getFragment();
        String title = preference.getTitle().toString();
        onStartSettingsForArguments(context, cls, fragmentName, args, 0, title);
    }

    public static void onStartSettingsForArguments(Context context, Class<?> cls, String fragment, Bundle args, int titleResId, String title) {
        if (args == null) {
            args = new Bundle();
        }
        onStartSettings(context, cls, fragment, null, args, titleResId, title);
    }

    public static void onStartSettings(Context context, Class<?> cls, String fragment, Bundle extras, Bundle args, int titleResId, String title) {
        new SettingLauncher(context)
            .setClass(cls)
            .setDestination(fragment)
            .setTitleText(title)
            .setTitleRes(titleResId)
            .setExtras(extras)
            .setArguments(args)
            .launch();
    }

    private Bundle createBundleWithPreferenceKey(Preference preference) {
        Bundle args = new Bundle();
        args.putString("key", preference.getKey());
        return args;
    }
}
