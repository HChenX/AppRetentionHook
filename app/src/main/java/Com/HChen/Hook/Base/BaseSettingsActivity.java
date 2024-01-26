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
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentFactory;

import java.util.Objects;

import Com.HChen.Hook.R;
import Com.HChen.Hook.Utils.ShellUtils;
import Com.HChen.Hook.Utils.SystemLog;
import moralnorm.appcompat.app.AppCompatActivity;

public class BaseSettingsActivity extends AppCompatActivity {

    private String initialFragmentName;
    AppCompatActivity context;
    public final String TAG = "BaseSettingsActivity";
//    public static List<BaseSettingsActivity> mActivityList = new ArrayList<>();

    @Override
    public void onCreate(@Nullable Bundle bundle) {
        Intent intent = getIntent();
        context = this;
        initialFragmentName = intent.getStringExtra(":settings:show_fragment");
        if (TextUtils.isEmpty(initialFragmentName)) {
            initialFragmentName = intent.getStringExtra(":android:show_fragment");
        }

        SystemLog.logI(TAG, "onCreate: " + intent.getExtras() + " Bundle: " + bundle);
        super.onCreate(bundle);
        if (intent.getExtras() != null) {
            createUiFromIntent(intent);
        } else {
            setContentView(R.layout.main_layout);
        }
    }

    protected void createUiFromIntent(Intent intent) {
        setContentView(R.layout.main_layout);
//        initActionBar();
//        mActivityList.add(this);
        Fragment targetFragment = getTargetFragment(this, initialFragmentName);
        if (targetFragment != null) {
            targetFragment.setArguments(getArguments(intent));
            setFragment(targetFragment);
            SystemLog.logI(TAG, "createUiFromIntent: " + targetFragment);
        }
//        showXposedActiveDialog();
    }

    public Fragment getTargetFragment(Context context, String initialFragmentName) {
        try {
            FragmentFactory fragmentManager = getSupportFragmentManager().getFragmentFactory();
            Fragment fragment = fragmentManager.instantiate(context.getClassLoader(), initialFragmentName);
//            fragment.setArguments(savedInstanceState);
            SystemLog.logI(TAG, "getTargetFragment:  " + fragment);
            return fragment;
//            return Fragment.instantiate(context, initialFragmentName, savedInstanceState);
        } catch (Exception e) {
            SystemLog.logE(TAG, "Unable to get target fragment", e);
            return null;
        }
    }

    public Bundle getArguments(Intent intent) {
        Bundle args = intent.getBundleExtra(":settings:show_fragment_args");
        String showFragmentTitle = intent.getStringExtra(":settings:show_fragment_title");
        int showFragmentTitleResId = intent.getIntExtra(":settings:show_fragment_title_resid", 0);
        assert args != null;
        /*标题和id*/
        args.putString(":fragment:show_title", showFragmentTitle);
        args.putInt(":fragment:show_title_resid", showFragmentTitleResId);
        SystemLog.logI(TAG, "getArguments: " + args);
        return args;
    }

    public void setFragment(Fragment fragment) {
        getSupportFragmentManager()
            .beginTransaction()
            .replace(R.id.frame_content, fragment)
            .commit();
    }

    public void setRestartView(View.OnClickListener l) {
        if (l != null) {
            ImageView mRestartView = new ImageView(this);
            mRestartView.setImageResource(R.drawable.ic_reboot_small);
            mRestartView.setOnClickListener(l);
            setActionBarEndView(mRestartView);
        }
    }

    public void setActionBarEndView(View view) {
        Objects.requireNonNull(getAppCompatActionBar()).setEndView(view);
    }

    public void showRestartSystemDialog() {
        showRestartDialog(true, "", "");
    }

    public void showRestartDialog(String appLabel, String packagename) {
        showRestartDialog(false, appLabel, packagename);
    }

    public void showRestartDialog(boolean isRestartSystem, String appLabel, String packagename) {
        AlertDialogFactory.makeAlertDialog(this,
            getResources().getString(R.string.soft_reboot) + " " + appLabel,
            getResources().getString(R.string.restart_app_desc1) + appLabel + getResources().getString(R.string.restart_app_desc2),
            () -> doRestart(packagename, isRestartSystem),
            () -> Toast.makeText(this, getResources().getString(R.string.cancel), Toast.LENGTH_SHORT).show(),
            true,
            2);
    }

    public void doRestart(String packagename, boolean isRestartSystem) {
        boolean result;

        if (isRestartSystem) {
            result = ShellUtils.RootCommand("reboot");
        } else {
            result = ShellUtils.RootCommand("killall " + packagename);
        }
        if (!result) {
            AlertDialogFactory.makeAlertDialog(this,
                getResources().getString(R.string.tip),
                isRestartSystem ? getResources().getString(R.string.reboot_failed) : getResources().getString(R.string.kill_failed),
                null,
                null,
                false,
                1
            );
        } else {
            Toast.makeText(this, getResources().getString(R.string.sucess) + packagename, Toast.LENGTH_SHORT).show();
        }
    }
}
