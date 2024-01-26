/*
 * This file is part of AppRetentionHook.

 * AppRetentionHook is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.

 * Author of this project: 焕晨HChen
 * You can reference the code of this project,
 * but as a project developer, I hope you can indicate it when referencing.

 * Copyright (C) 2023-2024 AppRetentionHook Contributions
 */
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
