package Com.HChen.Hook.Base;

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

import java.util.ArrayList;
import java.util.List;

import Com.HChen.Hook.R;
import Com.HChen.Hook.Utils.ShellUtils;
import moralnorm.appcompat.app.AlertDialog;
import moralnorm.appcompat.app.AppCompatActivity;

public class BaseSettingsActivity extends AppCompatActivity {
    private String initialFragmentName;
    AppCompatActivity context;
    public static List<BaseSettingsActivity> mActivityList = new ArrayList<>();

    @Override
    public void onCreate(@Nullable Bundle bundle) {
        Intent intent = getIntent();
        context = this;
        initialFragmentName = intent.getStringExtra(":settings:show_fragment");
        if (TextUtils.isEmpty(initialFragmentName)) {
            initialFragmentName = intent.getStringExtra(":android:show_fragment");
        }
        super.onCreate(bundle);
        createUiFromIntent(bundle, intent);
    }

    protected void createUiFromIntent(Bundle savedInstanceState, Intent intent) {
        setContentView(R.layout.main_layout);
//        initActionBar();
        mActivityList.add(this);
        Fragment targetFragment = getTargetFragment(this, initialFragmentName, savedInstanceState);
        if (targetFragment != null) {
            targetFragment.setArguments(getArguments(intent));
            setFragment(targetFragment);
        }
//        showXposedActiveDialog();
    }

    public Fragment getTargetFragment(Context context, String initialFragmentName, Bundle savedInstanceState) {
        try {
            return Fragment.instantiate(context, initialFragmentName, savedInstanceState);
        } catch (Exception e) {
            Log.e("Settings", "Unable to get target fragment", e);
            return null;
        }
    }

    public Bundle getArguments(Intent intent) {
        Bundle args = intent.getBundleExtra(":settings:show_fragment_args");
        String showFragmentTitle = intent.getStringExtra(":settings:show_fragment_title");
        int showFragmentTitleResId = intent.getIntExtra(":settings:show_fragment_title_resid", 0);
        args.putString(":fragment:show_title", showFragmentTitle);
        args.putInt(":fragment:show_title_resid", showFragmentTitleResId);
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
        getAppCompatActionBar().setEndView(view);
    }

    public void showRestartSystemDialog() {
        showRestartDialog(true, "", "");
    }

    public void showRestartDialog(String appLabel, String packagename) {
        showRestartDialog(false, appLabel, packagename);
    }

    public void showRestartDialog(boolean isRestartSystem, String appLabel, String packagename) {
        new AlertDialog.Builder(this)
            .setCancelable(true)
            .setTitle(getResources().getString(R.string.soft_reboot) + " " + appLabel)
            .setMessage(getResources().getString(R.string.restart_app_desc1
            ) + appLabel + getResources().getString(R.string.restart_app_desc2))
            .setHapticFeedbackEnabled(true)
            .setPositiveButton(android.R.string.ok, (dialog, which) -> doRestart(packagename, isRestartSystem))
            .setNegativeButton(android.R.string.cancel, (dialog, which) -> Toast.makeText(this, getResources().getString(R.string.cancel), Toast.LENGTH_SHORT).show())
            .create()
            .show();
    }

    public void doRestart(String packagename, boolean isRestartSystem) {
        boolean result;

        if (isRestartSystem) {
            result = ShellUtils.RootCommand("reboot");
        } else {
            result = ShellUtils.RootCommand("killall " + packagename);
        }
        if (!result) {
            new AlertDialog.Builder(this)
                .setCancelable(false)
                .setTitle(getResources().getString(R.string.tip))
                .setMessage(isRestartSystem ? getResources().getString(R.string.reboot_failed) : getResources().getString(R.string.kill_failed))
                .setHapticFeedbackEnabled(true)
                .setPositiveButton(android.R.string.ok, null)
                .show();
        } else {
            Toast.makeText(this, getResources().getString(R.string.sucess) + packagename, Toast.LENGTH_SHORT).show();
        }
    }
}
