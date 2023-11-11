package Com.HChen.Hook.Hook;

import static Com.HChen.Hook.Param.Name.MiuiName.ProcessManager;
import static Com.HChen.Hook.Param.Value.MiuiValue.kill;

import Com.HChen.Hook.Mode.HookMode;

public class PowerKeeper extends HookMode {
    @Override
    public void init() {
        hookAllMethods(ProcessManager,
            kill,
            new HookAction() {
                @Override
                protected void before(MethodHookParam param) {
                    param.setResult(false);
                }
            }
        );
    }
}
