package Com.HChen.Hook.HookMode;

import Com.HChen.Hook.BuildConfig;
import Com.HChen.Hook.MiuiFramework.PowerKeeper;
import Com.HChen.Hook.MiuiService.MiuiService;
import Com.HChen.Hook.SystemService.SystemService;
import Com.HChen.Hook.TextHook.TextHook;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class HookRun {
    LoadPackageParam loadPackageParam;

    public void HookPackage(LoadPackageParam loadPackageParam) {
        String PackageName = loadPackageParam.packageName;
        this.loadPackageParam = loadPackageParam;
        switch (PackageName) {
            case "android" -> {
                initHook(new SystemService());
                initHook(new MiuiService());
            }
            case "com.miui.powerkeeper" -> initHook(new PowerKeeper());
            case "Com.HChen.App" -> {
                if (BuildConfig.DEBUG) {
                    initHook(new TextHook());
                }
            }
        }
    }

    public void initHook(HookMode hchenHookMode) {
        hchenHookMode.Run(loadPackageParam);
    }
}
