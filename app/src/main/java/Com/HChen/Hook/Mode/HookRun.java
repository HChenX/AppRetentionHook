package Com.HChen.Hook.Mode;

import Com.HChen.Hook.Base.BaseGetKey;
import Com.HChen.Hook.Hook.AthenaApp;
import Com.HChen.Hook.Hook.AthenaKill;
import Com.HChen.Hook.Hook.MiuiService;
import Com.HChen.Hook.Hook.OplusService;
import Com.HChen.Hook.Hook.PowerKeeper;
import Com.HChen.Hook.Hook.SystemService;
import Com.HChen.Hook.Mode.DexKit.DexKit;
import Com.HChen.Hook.Utils.GetKey;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class HookRun extends HookLog {
    public static final String TAG = "HookRun";
    public static LoadPackageParam param;

    public static final GetKey<String, Object> mPrefsMap = BaseGetKey.mPrefsMap;

    public static void HookPackage(LoadPackageParam loadPackageParam) {
        param = loadPackageParam;
        init(loadPackageParam);
//        Process.myUid();
    }

    public static void init(LoadPackageParam param) {
        switch (param.packageName) {
            case "android" -> {
                initHook(new SystemService(), mPrefsMap.getBoolean("system_service"));
                initHook(new MiuiService(), mPrefsMap.getBoolean("miui_service"));
                initHook(new OplusService(), true);
            }
            case "com.miui.powerkeeper" -> {
                initHook(new PowerKeeper(), mPrefsMap.getBoolean("powerkeeper"));
//                initHook(new AthenaApp(), true);
            }
            case "com.oplus.athena" -> {
                AthenaApp.getPid(AthenaKill.init(param));
                DexKit.INSTANCE.initDexKit(param);
                initHook(new AthenaApp(), true);
                DexKit.INSTANCE.closeDexKit();
            }
            /*测试用*/
            /*case "com.android.settings" -> {
                if (BuildConfig.DEBUG)
                    initHook(new TestHook(), mPrefsMap.getBoolean("text"));
            }*/
        }
    }

    public static void initHook(HookMode hook, boolean what) {
        if (what) {
            hook.Run(param);
        }
        /*else {
            logI("Hook " + hchenHookMode + " is :" + isTrue);
        }*/
    }
}
