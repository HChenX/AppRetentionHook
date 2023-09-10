package Com.HChen.Hook.Base;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;

import Com.HChen.Hook.SubSettings;
import moralnorm.preference.Preference;
import moralnorm.preference.PreferenceFragmentCompat;

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
        Bundle args = null;
        if (isEnableBundle) {
            args = new Bundle();
            args.putString("key", preference.getKey());
        }
        String mFragmentName = preference.getFragment();
        String mTitle = preference.getTitle().toString();
        onStartSettingsForArguments(context, cls, mFragmentName, args, 0, mTitle);
    }

    public static void onStartSettingsForArguments(Context context, Class<?> cls, String fragment, Bundle args, int titleResId, String title) {
        if (args == null) args = new Bundle();
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
}
