package Com.HChen.Hook.App;

import android.view.View;

import Com.HChen.Hook.Base.BaseSettingsActivity;
import Com.HChen.Hook.R;
import Com.HChen.Hook.Ui.SettingsPreferenceFragment;

public class PowerKeeper extends SettingsPreferenceFragment {
    @Override
    public int getContentResId() {
        return R.xml.powerkeeper_xml;
    }

    @Override
    public View.OnClickListener addRestartListener() {
        return view -> ((BaseSettingsActivity) getActivity()).showRestartDialog(
            getResources().getString(R.string.powerkeeper),
            "com.miui.powerkeeper"
        );
    }
}
