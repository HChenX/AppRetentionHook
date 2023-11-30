package Com.HChen.Hook;

import Com.HChen.Hook.Base.BaseGetKey;
import Com.HChen.Hook.Mode.HookRun;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/* Hook入口。*/
public class HookInlet extends BaseGetKey implements IXposedHookLoadPackage, IXposedHookZygoteInit {
    public static final String hookMain = "[HChenHook]";

    @Override
    public void initZygote(StartupParam startupParam) {
        BaseGetKey.setXSharedPrefs();
    }

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam loadPackageParam) {
//        EzXHelper.setLogTag(hookMain);
        HookRun.HookPackage(loadPackageParam);
    }
}
