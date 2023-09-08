package Com.HChen.Hook;

import Com.HChen.Hook.Base.BasePutKey;
import Com.HChen.Hook.HookMode.HookRun;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class HookInlet extends BasePutKey implements IXposedHookLoadPackage, IXposedHookZygoteInit {
    @Override
    public void initZygote(StartupParam startupParam) throws Throwable {
        BasePutKey basePutKey = new BasePutKey();
        basePutKey.setXSharedPrefs();
    }

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {
        HookRun hookRun = new HookRun();
        hookRun.HookPackage(loadPackageParam);
    }
}
