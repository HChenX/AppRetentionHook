package Com.HChen.Hook.MiuiFramework;

import Com.HChen.Hook.HookMode.HookMode;
import de.robv.android.xposed.XC_MethodHook;

public class PowerKeeper extends HookMode {
    @Override
    public void init() {
        hookAllMethods("miui.process.ProcessManager", "kill",
                new HookAction() {
                    @Override
                    protected void before(XC_MethodHook.MethodHookParam param) {
                        logSI("Hook ProcessManager kill");
                        param.setResult(null);
                    }
                }
        );
    }
}
