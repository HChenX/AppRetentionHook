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
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import androidx.fragment.app.Fragment;

import Com.HChen.Hook.Utils.SystemLog;

public class SettingLauncher extends SystemLog {
    public final String TAG = "SettingLauncher";
    private final Context mContext;
    private boolean mLaunched;

    Bundle mExtras;
    Bundle mArguments;
    CharSequence mTitle;
    String mDestinationName;
    Class<?> mClass;

    int mTitleResId;

    public SettingLauncher(Context context) {
        if (context == null) {
            throw new IllegalArgumentException("Context must be non-null.");
        }
        mContext = context;
    }

    public SettingLauncher setClass(Class<?> cls) {
        mClass = cls;
        return this;
    }

    public SettingLauncher setDestination(String name) {
        mDestinationName = name;
        return this;
    }

    public SettingLauncher setTitleRes(int titleResId) {
        mTitleResId = titleResId;
        return this;
    }

    public SettingLauncher setTitleText(CharSequence title) {
        mTitle = title;
        return this;
    }

    public SettingLauncher setArguments(Bundle args) {
        mArguments = args;
        return this;
    }

    public SettingLauncher setExtras(Bundle extras) {
        mExtras = extras;
        return this;
    }

    public void launch() {
        if (mLaunched) {
            throw new IllegalStateException("This launcher has already been executed. Do not reuse");
        }
        mLaunched = true;
        Intent intent = toIntent();
        launch(intent);
    }

    public Intent toIntent() {
        Intent intent = new Intent("android.intent.action.MAIN");
        copyExtras(intent);
        intent.setClass(mContext, mClass);
        if (TextUtils.isEmpty(mDestinationName)) {
            throw new IllegalArgumentException("Destination fragment must be set");
        }
        intent.putExtra(":settings:show_fragment", mDestinationName);
        intent.putExtra(":settings:show_fragment_args", mArguments);
        intent.putExtra(":settings:show_fragment_title", mTitle);
        intent.putExtra(":settings:show_fragment_title_resid", mTitleResId);
        logI(TAG, "toIntent: " + intent.getExtras());
        /*intent.putExtra(":settings:show_fragment_contentResId", mContentResId);*/
        return intent;
    }

    void launch(Intent intent) {
        mContext.startActivity(intent);
    }

    /**
     * @noinspection deprecation
     */
    void launchForResult(Fragment fragment, Intent intent, int i) {
        fragment.startActivityForResult(intent, i);
    }

    private void copyExtras(Intent intent) {
        if (mExtras != null) {
            intent.replaceExtras(mExtras);
        }
    }

}
