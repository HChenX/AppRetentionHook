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

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import Com.HChen.Hook.Utils.SystemLog;

public abstract class SettingsPreferenceFragment extends BasePreferenceFragment {
    public final String TAG = "SettingsPreferenceFragment";
    public String mTitle;
    public int mContentResId = 0;
    public int mTitleResId = 0;

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        super.onCreatePreferences(bundle, s);
        Bundle args = getArguments();
        if (args != null) {
            mTitle = args.getString(":fragment:show_title");
            mTitleResId = args.getInt(":fragment:show_title_resid");
            mContentResId = args.getInt("contentResId");
        }
        SystemLog.logI(TAG, "onCreatePreferences: " + args);
        if (mTitleResId != 0) setTitle(mTitleResId);
        if (!TextUtils.isEmpty(mTitle)) setTitle(mTitle);
        mContentResId = mContentResId != 0 ? mContentResId : getContentResId();
        if (mContentResId != 0) {
            setPreferencesFromResource(mContentResId, s);
            initPrefs();
        }
        ((BaseSettingsActivity) requireActivity()).setRestartView(addRestartListener());
    }

    public View.OnClickListener addRestartListener() {
        return null;
    }

    /*public SharedPreferences getSharedPreferences() {
        return PrefsUtils.mSharedPreferences;
    }

    public boolean hasKey(String key) {
        return getSharedPreferences().contains(key);
    }*/

    public void initPrefs() {
    }

    public abstract int getContentResId();
}
