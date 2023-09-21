package Com.HChen.Hook.App;

import android.view.View;

import Com.HChen.Hook.Base.BaseSettingsActivity;
import Com.HChen.Hook.Base.SettingsPreferenceFragment;
import Com.HChen.Hook.R;

public class PowerKeeper extends SettingsPreferenceFragment {
    @Override
    public int getContentResId() {
        return R.xml.powerkeeper_xml;
    }

    @Override
    public View.OnClickListener addRestartListener() {
        return view -> ((BaseSettingsActivity) requireActivity()).showRestartDialog(
            getResources().getString(R.string.powerkeeper),
            "com.miui.powerkeeper"
        );
    }
}
