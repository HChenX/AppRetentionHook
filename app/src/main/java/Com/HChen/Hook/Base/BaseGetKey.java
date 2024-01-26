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

import java.util.Map;

import Com.HChen.Hook.BuildConfig;
import Com.HChen.Hook.Utils.GetKey;
import Com.HChen.Hook.Utils.SystemLog;
import de.robv.android.xposed.XSharedPreferences;

public class BaseGetKey extends SystemLog {
    public static GetKey<String, Object> mPrefsMap = new GetKey<>();
    public static final String mPrefsName = "HChen_prefs";
    public static final String TAG = "BaseGetKey";

    /**
     * @noinspection deprecation
     */
    @SuppressLint("WorldReadableFiles")
    public static void getSharedPrefs(Context context) {
        context = context.createDeviceProtectedStorageContext();
        context.getSharedPreferences(mPrefsName, Context.MODE_WORLD_READABLE);
    }

    public static void setXSharedPrefs() {
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
