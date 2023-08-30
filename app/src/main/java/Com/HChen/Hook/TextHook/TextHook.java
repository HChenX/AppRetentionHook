package Com.HChen.Hook.TextHook;

import android.view.View;

import Com.HChen.Hook.HookMode.HookMode;
import de.robv.android.xposed.XposedHelpers;

public class TextHook extends HookMode {
    @Override
    public void init() {
        findAndHookMethod("Com.HChen.App.HChenMain$$ExternalSyntheticLambda6",
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
        );
    }
}
