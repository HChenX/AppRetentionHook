package Com.HChen.Hook.Execute.MiuiFramework;

import static Com.HChen.Hook.Name.MiuiName.ProcessManager;
import static Com.HChen.Hook.Value.MiuiValue.kill;

import Com.HChen.Hook.Mode.HookMode;

public class PowerKeeper extends HookMode {
    @Override
    public void init() {
        hookAllMethods(ProcessManager,
            kill,
            new HookAction() {
                @Override
                protected void before(MethodHookParam param) {
                    super.before(param);
                    param.setResult(false);
                }
            }
        );
    }
}
