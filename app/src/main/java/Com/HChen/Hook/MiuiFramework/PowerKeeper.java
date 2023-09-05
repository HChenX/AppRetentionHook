package Com.HChen.Hook.MiuiFramework;

import static Com.HChen.Hook.HookName.MiuiName.*;
import static Com.HChen.Hook.HookValue.MiuiValue.*;

import Com.HChen.Hook.HookMode.HookMode;
import de.robv.android.xposed.XC_MethodHook;

public class PowerKeeper extends HookMode {
    final String logI = "I";
    final String logW = "W";
    final String logE = "E";

    @Override
    public int smOr() {
        return 2;
    }

    @Override
    public void init() {
        hookAllMethods(ProcessManager,
                kill,
                new HookAction() {
                    @Override
                    protected void before(XC_MethodHook.MethodHookParam param) {
                        setLog(2, logI, ProcessManager, kill);
                        param.setResult(false);
                    }
                }
        );
    }
}
