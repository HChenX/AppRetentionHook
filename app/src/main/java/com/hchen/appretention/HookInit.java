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
package com.hchen.appretention;

import com.hchen.appretention.hook.hyper.HyperV1;
import com.hchen.appretention.hook.powerkeeper.PowerKeeper;
import com.hchen.appretention.hook.system.AndroidU;
import com.hchen.hooktool.BaseHC;
import com.hchen.hooktool.HCInit;

import org.luckypray.dexkit.DexKitBridge;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/* Hook入口。*/
public class HookInit implements IXposedHookLoadPackage, IXposedHookZygoteInit {
    private static final String TAG = "AppRetention";
    private static final AndroidU androidU = new AndroidU();
    private static final HyperV1 hyperV1 = new HyperV1();
    private static final PowerKeeper powerKeeper = new PowerKeeper();

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam loadPackageParam) {
        HCInit.initLoadPackageParam(loadPackageParam);
        switch (loadPackageParam.packageName) {
            case "android" -> {
                androidU.onLoadPackage();
                hyperV1.onLoadPackage();
            }
            case "com.miui.powerkeeper" -> {
                powerKeeper.onLoadPackage();
            }
            case "com.oplus.athena" -> {

            }
            case "com.oplus.battery" -> {
            }
        }
    }

    @Override
    public void initZygote(StartupParam startupParam) {
        HCInit.initBasicData(new HCInit.BasicData()
            .setTag("AppRetention")
            .setModulePackageName(BuildConfig.APPLICATION_ID)
            .setLogLevel(HCInit.LOG_D)
        );
        HCInit.useLogExpand(new String[]{
            "com.hchen.appretention.hook"
        });
        HCInit.initStartupParam(startupParam);
    }

    private void initHook(BaseHC baseHC) {
        baseHC.onLoadPackage();
    }

    private DexKitBridge bridge = null;

    public DexKitBridge initDexkit(XC_LoadPackage.LoadPackageParam loadPackageParam) {
        if (bridge == null) {
            System.loadLibrary("dexkit");
            bridge = DexKitBridge.create(loadPackageParam.appInfo.sourceDir);
        }
        return bridge;
    }

    public void closeDexkit() {
        if (bridge != null) {
            bridge.close();
            bridge = null;
        }
    }
}
