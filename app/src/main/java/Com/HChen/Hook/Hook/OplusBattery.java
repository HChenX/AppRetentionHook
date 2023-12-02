package Com.HChen.Hook.Hook;

import static Com.HChen.Hook.Param.Name.OplusName.ControllerCenter;
import static Com.HChen.Hook.Param.Value.OplusValue.startClearApps;
import static Com.HChen.Hook.Param.Value.OplusValue.startMorningClearApps;

import Com.HChen.Hook.Mode.HookMode;

public class OplusBattery extends HookMode {
    public static String name = "OplusBattery";

    @Override
    public void init() {
        findAndHookMethod(ControllerCenter,
            startClearApps, new HookAction() {
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

        findAndHookMethod(ControllerCenter,
            startMorningClearApps, new HookAction() {
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
