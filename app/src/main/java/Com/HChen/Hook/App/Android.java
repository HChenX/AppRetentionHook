package Com.HChen.Hook.App;

import android.view.View;

import Com.HChen.Hook.Base.BaseSettingsActivity;
import Com.HChen.Hook.R;
import Com.HChen.Hook.Ui.SettingsPreferenceFragment;

public class Android extends SettingsPreferenceFragment {
    @Override
    public int getContentResId() {
        return R.xml.android_xml;
    }

    @Override
    public View.OnClickListener addRestartListener() {
        return view -> ((BaseSettingsActivity) getActivity()).showRestartSystemDialog();
    }
}
