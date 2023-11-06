package Com.HChen.Hook.Execute.Test;

import Com.HChen.Hook.Mode.HookMode;

public class TestHook extends HookMode {
    @Override
    public void init() {
        /*findAndHookMethod("Com.HChen.App.HChenMain$$ExternalSyntheticLambda6",
                "onClick", View.class,
                new HookAction() {
                    @Override
                    protected void after(MethodHookParam param) {
//                        View view = (View) param.args[0];
//                        Context context = (Context) param.args[1];
//                        view.setOnClickListener(v -> {
//                            Toast.makeText(context, "被Hook了", Toast.LENGTH_SHORT).show();
//                            logSI(1, "onClick");
//                        });
//                        param.setResult(null);
                        String Text = (String) XposedHelpers.getObjectField(param.thisObject, "Text");
                        logI(Text);
                    }
                }
        );
        findAndHookConstructor("Com.HChen.App.HChenMain",
                new HookAction() {
                    @Override
                    protected void after(MethodHookParam param) {
                        logI("Hooking");
                        getDeclaredField(param, "Text", "Hooking");
                    }
                }
        );*/
//        int get = SystemProperties.getInt("ro.miui.ui.version.code", 1);
//        logI("Hook ON! get prop: " + get);
    }
}
