package Com.HChen.Hook.Hook;

import static Com.HChen.Hook.Param.Name.OplusName.OplusOsenseKillAction;
import static Com.HChen.Hook.Param.Value.OplusValue.killLocked;

import Com.HChen.Hook.Mode.HookMode;

public class OplusService extends HookMode {
    @Override
    public void init() {
        hookAllMethods(OplusOsenseKillAction, killLocked,
            new HookAction() {
                @Override
                protected void before(MethodHookParam param) {
                    param.setResult(null);
                }
            }
        );
    }
}
