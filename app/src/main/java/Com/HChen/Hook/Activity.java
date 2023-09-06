package Com.HChen.Hook;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import Com.HChen.Hook.Ui.MainFragment;
import moralnorm.appcompat.app.AppCompatActivity;

public class Activity extends AppCompatActivity {
    private final MainFragment mMainFrag = new MainFragment();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_layout);
        setImmersionMenuEnabled(true);
        setFragment(mMainFrag);
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
}
