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
    SystemLog systemLog = new SystemLog();
    AlertDialogFactory alertDialogFactory = new AlertDialogFactory();
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

        systemLog.logI(TAG, "onCreate: " + intent.getExtras() + " Bundle: " + bundle);
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
            systemLog.logI(TAG, "createUiFromIntent: " + targetFragment);
        }
//        showXposedActiveDialog();
    }

    public Fragment getTargetFragment(Context context, String initialFragmentName) {
        try {
            FragmentFactory fragmentManager = getSupportFragmentManager().getFragmentFactory();
            Fragment fragment = fragmentManager.instantiate(context.getClassLoader(), initialFragmentName);
//            fragment.setArguments(savedInstanceState);
            systemLog.logI(TAG, "getTargetFragment:  " + fragment);
            return fragment;
//            return Fragment.instantiate(context, initialFragmentName, savedInstanceState);
        } catch (Exception e) {
            systemLog.logE(TAG, "Unable to get target fragment", e);
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
        systemLog.logI(TAG, "getArguments: " + args);
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
        alertDialogFactory.makeAlertDialog(this,
            getResources().getString(R.string.soft_reboot) + " " + appLabel,
            getResources().getString(R.string.restart_app_desc1) + appLabel + getResources().getString(R.string.restart_app_desc2),
            () -> doRestart(packagename, isRestartSystem),
            () -> Toast.makeText(this, getResources().getString(R.string.cancel), Toast.LENGTH_SHORT).show());
    }

    public void doRestart(String packagename, boolean isRestartSystem) {
        boolean result;

        if (isRestartSystem) {
            result = ShellUtils.RootCommand("reboot");
        } else {
            result = ShellUtils.RootCommand("killall " + packagename);
        }
        if (!result) {
            alertDialogFactory.makeAlertDialog(this, getResources().getString(R.string.tip),
                isRestartSystem ? getResources().getString(R.string.reboot_failed) : getResources().getString(R.string.kill_failed), null);
        } else {
            Toast.makeText(this, getResources().getString(R.string.sucess) + packagename, Toast.LENGTH_SHORT).show();
        }
    }
}
