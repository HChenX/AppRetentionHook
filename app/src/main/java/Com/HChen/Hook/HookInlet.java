package Com.HChen.Hook;

import Com.HChen.Hook.Base.BaseGetKey;
import Com.HChen.Hook.Mode.HookRun;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/* Hook入口。*/
public class HookInlet extends BaseGetKey implements IXposedHookLoadPackage, IXposedHookZygoteInit {
    @Override
    public void initZygote(StartupParam startupParam) {
        BaseGetKey basePutKey = new BaseGetKey();
        basePutKey.setXSharedPrefs();
    }

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam loadPackageParam) {
        HookRun hookRun = new HookRun();
        hookRun.HookPackage(loadPackageParam);
    }
}
