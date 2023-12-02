package Com.HChen.Hook.Hook;

import static Com.HChen.Hook.Param.Name.OplusName.AthenaApplication;
import static Com.HChen.Hook.Param.Value.OplusValue.onCreate;

import Com.HChen.Hook.Mode.HookLog;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class AthenaKill {
    public static int myPid = 0;
    public static pidCallBackListener mPid;
//    public static int myUid = 0;

    public interface pidCallBackListener {
        void pidCallBack(int pid);
    }

    public static void setPidCallBackListener(pidCallBackListener pid) {
        mPid = pid;
    }

    public static int init(XC_LoadPackage.LoadPackageParam loadPackageParam) {
        try {
            XposedHelpers.findAndHookMethod(AthenaApplication, loadPackageParam.classLoader, onCreate,
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        super.beforeHookedMethod(param);
                        myPid = (int) XposedHelpers.callStaticMethod(
                            XposedHelpers.findClassIfExists("android.os.Process", loadPackageParam.classLoader), "myPid");
//                    myUid = (int) XposedHelpers.callStaticMethod(
//                        XposedHelpers.findClassIfExists("android.os.Process", loadPackageParam.classLoader), "myUid");
                        mPid.pidCallBack(myPid);
                    }
                }
            );
        } catch (Throwable throwable) {
            HookLog.logE("AthenaApplication", "onCreate hook e: " + throwable);
        }
        return myPid;
    }
}
