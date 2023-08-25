package Com.HChen.Hook.MiuiFramework;

import static Com.HChen.Hook.HookValue.MiuiValue.*;
import static Com.HChen.Hook.HookName.MiuiName.*;

import Com.HChen.Hook.HookMode.HookMode;
import de.robv.android.xposed.XC_MethodHook;

public class PowerKeeper extends HookMode {
    @Override
    public void init() {
        hookAllMethods(ProcessManager,
                kill,
                new HookAction() {
                    @Override
                    protected void before(XC_MethodHook.MethodHookParam param) {
                        setLog(ProcessManager, kill);
                        param.setResult(null);
                    }
                }
        );
    }
}
