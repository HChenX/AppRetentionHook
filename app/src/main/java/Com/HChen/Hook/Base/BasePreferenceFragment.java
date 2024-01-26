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

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;

import androidx.fragment.app.Fragment;

import java.util.Objects;

import Com.HChen.Hook.Utils.SystemLog;
import moralnorm.preference.Preference;
import moralnorm.preference.PreferenceManager;
import moralnorm.preference.compat.PreferenceFragment;

public class BasePreferenceFragment extends PreferenceFragment {
    public final String TAG = "BasePreferenceFragment";
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
        mPreferenceManager.setStorageDefault();
        SystemLog.logI(TAG, "onCreatePreferences: " + mPreferenceManager);
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
