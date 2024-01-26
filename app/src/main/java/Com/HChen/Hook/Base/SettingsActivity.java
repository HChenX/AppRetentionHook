/*
 * This file is part of AppRetentionHook.

 * AppRetentionHook is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.

 * Author of this project: 焕晨HChen
 * You can reference the code of this project,
 * but as a project developer, I hope you can indicate it when referencing.

 * Copyright (C) 2023-2024 AppRetentionHook Contributions
 */
package Com.HChen.Hook.Base;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;

import java.util.Objects;

import Com.HChen.Hook.Ui.SubSettings;
import Com.HChen.Hook.Utils.SystemLog;
import moralnorm.preference.Preference;
import moralnorm.preference.PreferenceFragmentCompat;

public class SettingsActivity extends BaseSettingsActivity implements PreferenceFragmentCompat.OnPreferenceStartFragmentCallback {
    public final String TAG = "SettingsActivity";

    public void onStartSettingsForArguments(Preference preference, boolean isBundleEnable) {
        onStartSettingsForArguments(SubSettings.class, preference, isBundleEnable);
    }

    @Override
    public boolean onPreferenceStartFragment(@NonNull PreferenceFragmentCompat preferenceFragmentCompat, @NonNull Preference preference) {
        onStartSettingsForArguments(preference, false);
        return true;
    }

    public void onStartSettingsForArguments(Class<?> cls, Preference preference, boolean isEnableBundle) {
        Bundle args = null;
        if (isEnableBundle) {
            args = new Bundle();
            args.putString("key", preference.getKey());
            SystemLog.logI(TAG, "onStartSettingsForArguments: " + args);
        }
        String mFragmentName = preference.getFragment();
        String mTitle = Objects.requireNonNull(preference.getTitle()).toString();
        if (args == null) args = new Bundle();
//        onStartSettingsForArguments(context, cls, mFragmentName, args, 0, mTitle);
        onStartSettings(context, cls, mFragmentName, null, args, 0, mTitle);
    }

    /*public static void onStartSettingsForArguments(Context context, Class<?> cls, String fragment, Bundle args, int titleResId, String title) {
        if (args == null) args = new Bundle();
        onStartSettings(context, cls, fragment, null, args, titleResId, title);
    }*/

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
