package Com.HChen.Hook;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import Com.HChen.Hook.Ui.MainFragment;
import Com.HChen.Hook.Utils.ShellUtils;
import moralnorm.appcompat.app.AlertDialog;
import moralnorm.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
//    public static GetKey<String, Object> mPrefsMap = BasePutKey.mPrefsMap;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_layout);
        setImmersionMenuEnabled(true);
        setFragment(new MainFragment());
        ShellUtils.RootCommand("chmod 0777 " + getPackageCodePath());
//        setContentView(R.xml.main_xml);
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
            .setCancelable(false)
            .setTitle("重启" + " " + appLabel)
            .setMessage("确认? " + appLabel)
            .setHapticFeedbackEnabled(true)
            .setPositiveButton(android.R.string.ok, (dialog, which) -> doRestart(packagename, isRestartSystem))
            .setNegativeButton(android.R.string.cancel, null)
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
                .setTitle("提示")
                .setMessage(isRestartSystem ? "重启" : "重启")
                .setHapticFeedbackEnabled(true)
                .setPositiveButton(android.R.string.ok, null)
                .show();
        }
    }
}
