package Com.HChen.Hook;

import android.content.SharedPreferences;
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
        suGet();
//        Log.i("TextHook", "onCreate: " + getPackageCodePath());
//        ShellUtils.RootCommand("chmod 0777 " + getPackageCodePath());
//        setContentView(R.xml.main_xml);
    }

    private void suGet() {
        final String ExecutedCommand = "ExecutedCommand";
        SharedPreferences sharedPreferences = getSharedPreferences(ExecutedCommand, MODE_PRIVATE);
        boolean hasExecutedCommand = sharedPreferences.getBoolean("hasExecutedCommand", false);
        String PackageCodePath = sharedPreferences.getString("packageCodePath", "null");
        String Now_PackageCodePath = getPackageCodePath();
        // BuildConfig.VERSION_CODE
        if (!hasExecutedCommand || !PackageCodePath.equals(Now_PackageCodePath)) {
            ShellUtils.RootCommand("chmod 0777 " + getPackageCodePath());

            // 标记命令已执行
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("hasExecutedCommand", true);
            editor.putString("packageCodePath", Now_PackageCodePath);
            editor.apply();
        }
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
