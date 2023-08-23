package Com.HChen.Hook;

import Com.HChen.Hook.HookMode.HookRun;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class Hook implements IXposedHookLoadPackage {
    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {
        HookRun hookRun = new HookRun();
        hookRun.HookPackage(loadPackageParam);
    }
}
