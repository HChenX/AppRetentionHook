package com.hchen.hook.base;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.hchen.hook.R;
import com.hchen.hook.utils.ShellUtils;

import java.util.ArrayList;
import java.util.List;

import moralnorm.appcompat.app.AlertDialog;
import moralnorm.appcompat.app.AppCompatActivity;

public class BaseSettingsActivity extends AppCompatActivity {
    private String initialFragmentName;
    private List<BaseSettingsActivity> mActivityList = new ArrayList<>();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_layout);
        mActivityList.add(this);
        handleIntent(getIntent());
    }

    private void handleIntent(Intent intent) {
        initialFragmentName = intent.getStringExtra(":settings:show_fragment");
        if (TextUtils.isEmpty(initialFragmentName)) {
            initialFragmentName = intent.getStringExtra(":android:show_fragment");
        }
        Fragment targetFragment = getTargetFragment(initialFragmentName, getIntent().getExtras());
        if (targetFragment != null) {
            setFragment(targetFragment);
        }
    }

    private Fragment getTargetFragment(String initialFragmentName, Bundle args) {
        try {
            Fragment fragment = Fragment.instantiate(this, initialFragmentName, args);
            if (args != null) {
                args.putString(":fragment:show_title", getString(R.string.fragment_title));
                args.putInt(":fragment:show_title_resid", R.string.fragment_title_resid);
            }
            return fragment;
        } catch (Exception e) {
            Log.e("Settings", "Unable to get target fragment", e);
            return null;
        }
    }

    private void setFragment(Fragment fragment) {
        getSupportFragmentManager()
            .beginTransaction()
            .replace(R.id.frame_content, fragment)
            .commit();
    }

    private void setRestartView(View.OnClickListener listener) {
        if (listener != null) {
            ImageView restartView = new ImageView(this);
            restartView.setImageResource(R.drawable.ic_reboot_small);
            restartView.setOnClickListener(listener);
            setActionBarEndView(restartView);
        }
    }

    private void setActionBarEndView(View view) {
        getAppCompatActionBar().setEndView(view);
    }

    private void showRestartSystemDialog() {
        showRestartDialog(true, "", "");
    }

    private void showRestartDialog(String appLabel, String packageName) {
        showRestartDialog(false, appLabel, packageName);
    }

    private void showRestartDialog(boolean isRestartSystem, String appLabel, String packageName) {
        String title = isRestartSystem ? getString(R.string.soft_reboot) : appLabel;
        String message = getString(R.string.restart_app_desc1) + appLabel + getString(R.string.restart_app_desc2);

        new AlertDialog.Builder(this)
            .setCancelable(true)
            .setTitle(title)
            .setMessage(message)
            .setHapticFeedbackEnabled(true)
            .setPositiveButton(android.R.string.ok, (dialog, which) -> doRestart(packageName, isRestartSystem))
            .setNegativeButton(android.R.string.cancel, (dialog, which) -> Toast.makeText(this, getString(R.string.cancel), Toast.LENGTH_SHORT).show())
            .create()
            .show();
    }

    private void doRestart(String packageName, boolean isRestartSystem) {
        boolean result;

        if (isRestartSystem) {
            result = ShellUtils.RootCommand("reboot");
        } else {
            result = ShellUtils.RootCommand("killall " + packageName);
        }
        if (!result) {
            new AlertDialog.Builder(this)
                .setCancelable(false)
                .setTitle(getString(R.string.tip))
                .setMessage(isRestartSystem ? getString(R.string.reboot_failed) : getString(R.string.kill_failed))
                .setHapticFeedbackEnabled(true)
                .setPositiveButton(android.R.string.ok, null)
                .show();
        } else {
            Toast.makeText(this, getString(R.string.success) + packageName, Toast.LENGTH_SHORT).show();
        }
    }
}
