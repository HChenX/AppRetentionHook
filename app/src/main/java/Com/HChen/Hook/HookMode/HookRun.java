package Com.HChen.Hook.HookMode;

import Com.HChen.Hook.Base.BasePutKey;
import Com.HChen.Hook.BuildConfig;
import Com.HChen.Hook.MiuiFramework.PowerKeeper;
import Com.HChen.Hook.MiuiService.MiuiService;
import Com.HChen.Hook.SystemService.SystemService;
import Com.HChen.Hook.TextHook.TextHook;
import Com.HChen.Hook.Utils.GetKey;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class HookRun extends HookLog {
    LoadPackageParam loadPackageParam;

    public final GetKey<String, Object> mPrefsMap = BasePutKey.mPrefsMap;

    public void HookPackage(LoadPackageParam loadPackageParam) {
        String PackageName = loadPackageParam.packageName;
        this.loadPackageParam = loadPackageParam;
        init(PackageName);
    }

    public void init(String PackageName) {
        switch (PackageName) {
            case "android" -> {
                initHook(new SystemService(), true);
                initHook(new MiuiService(), true);
            }
            case "com.miui.powerkeeper" -> initHook(new PowerKeeper(), true);
            case "Com.HChen.App" -> {
                if (BuildConfig.DEBUG) {
                    initHook(new TextHook(), true);
                }
            }
            case "com.android.settings" -> initHook(new TextHook(), mPrefsMap.getBoolean("text"));
        }
    }

    public void initHook(HookMode hchenHookMode, boolean isTrue) {
        if (isTrue) {
            hchenHookMode.Run(loadPackageParam);
        }
        /*else {
            logI("Hook " + hchenHookMode + " is :" + isTrue);
        }*/
    }
}
