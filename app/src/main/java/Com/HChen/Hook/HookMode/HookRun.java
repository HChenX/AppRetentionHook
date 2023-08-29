package Com.HChen.Hook.HookMode;

import Com.HChen.Hook.MiuiFramework.PowerKeeper;
import Com.HChen.Hook.MiuiService.MiuiService;
import Com.HChen.Hook.SystemService.SystemService;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class HookRun {
    LoadPackageParam loadPackageParam;

    public void HookPackage(LoadPackageParam loadPackageParam) {
        String PackageName = loadPackageParam.packageName;
        this.loadPackageParam = loadPackageParam;
        if (PackageName.equals("android")) {
            initHook(new SystemService());
            initHook(new MiuiService());
        } else if (PackageName.equals("com.miui.powerkeeper")) {
            initHook(new PowerKeeper());
        }
    }

    public void initHook(HookMode hchenHookMode) {
        hchenHookMode.Run(loadPackageParam);
    }
}
