package Com.HChen.Hook.Hook;

import static Com.HChen.Hook.Param.Name.OplusName.OplusOsenseKillAction;
import static Com.HChen.Hook.Param.Value.OplusValue.killLocked;

import Com.HChen.Hook.Mode.HookMode;

public class OplusService extends HookMode {
    public static String name = "OplusService";
    @Override
    public void init() {
        hookAllMethods(OplusOsenseKillAction, killLocked,
            new HookAction() {
                @Override
                public String hookLog() {
                    return name;
                }

                @Override
                protected void before(MethodHookParam param) {
                    param.setResult(null);
                }
            }
        );
    }
}
