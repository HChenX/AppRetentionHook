package Com.HChen.Hook.Ui;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.Nullable;

import Com.HChen.Hook.App.MainFragment;
import Com.HChen.Hook.Base.SettingsActivity;
import Com.HChen.Hook.R;
import Com.HChen.Hook.Utils.ShellUtils;

public class MainActivity extends SettingsActivity {
    //    public static GetKey<String, Object> mPrefsMap = BasePutKey.mPrefsMap;
    String TAG = "MainActivity";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setImmersionMenuEnabled(true);
        setFragment(new MainFragment());
        suGet();
//        Log.i("TextHook", "onC reate: " + getPackageCodePath());
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.restart) {
            RestartAlertDialog dialog = new RestartAlertDialog(this);
            dialog.setTitle(item.getTitle());
            dialog.show();
        }
        /*else if (itemId == R.id.settings) {
            Intent intent = new Intent(this, ModuleSettingsActivity.class);
            startActivity(intent);
        } else if (itemId == R.id.about) {
            SettingLauncherHelper.onStartSettings(this, SubSettings.class, AboutFragment.class, item.getTitle().toString());
        }*/
        return super.onOptionsItemSelected(item);
    }
}
