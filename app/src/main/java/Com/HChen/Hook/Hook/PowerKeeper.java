package Com.HChen.Hook.Hook;

import static Com.HChen.Hook.Param.Name.MiuiName.ProcessManager;
import static Com.HChen.Hook.Param.Value.MiuiValue.kill;

import Com.HChen.Hook.Mode.HookMode;

public class PowerKeeper extends HookMode {
    public static String name = "PowerKeeper";
    @Override
    public void init() {
        hookAllMethods(ProcessManager,
            kill,
            new HookAction() {
                @Override
                public String hookLog() {
                    return name;
                }

                @Override
                protected void before(MethodHookParam param) {
                    param.setResult(false);
                }
            }
        );
    }


}
