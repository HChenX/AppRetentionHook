package Com.HChen.Hook.Ui.Fragment;

import android.view.View;

import Com.HChen.Hook.Base.BaseSettingsActivity;
import Com.HChen.Hook.Base.SettingsPreferenceFragment;
import Com.HChen.Hook.R;

public class MiuiService extends SettingsPreferenceFragment {
    @Override
    public int getContentResId() {
        return R.xml.miui_xml;
    }

    @Override
    public View.OnClickListener addRestartListener() {
        return view -> ((BaseSettingsActivity) requireActivity()).showRestartSystemDialog();
    }
}
