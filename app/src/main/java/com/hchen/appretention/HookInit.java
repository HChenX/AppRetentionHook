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

import com.hchen.appretention.hook.LogPuppet;
import com.hchen.appretention.hook.hyper.HyperV1;
import com.hchen.appretention.hook.powerkeeper.PowerKeeper;
import com.hchen.appretention.hook.system.AndroidU;
import com.hchen.appretention.hook.system.CrashListener;
import com.hchen.appretention.hook.system.UserUnlockListener;
import com.hchen.appretention.log.LogToFile;
import com.hchen.hooktool.BaseHC;
import com.hchen.hooktool.HCInit;

import org.luckypray.dexkit.DexKitBridge;

import java.util.Arrays;
import java.util.HashMap;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * Hook 入口
 *
 * @author 焕晨HChen
 */
public class HookInit implements IXposedHookLoadPackage, IXposedHookZygoteInit {
    private static final String TAG = "AppRetention";
    private static final AndroidU androidU = new AndroidU();
    private static final HyperV1 hyperV1 = new HyperV1();
    private static final UserUnlockListener userUnlockListener = new UserUnlockListener();
    private static final CrashListener crashListener = new CrashListener();
    private static final PowerKeeper powerKeeper = new PowerKeeper();
    private static final HashMap<String, BaseHC[]> baseHCs = new HashMap<>();
    private static final HashMap<String, String> hookAsTag = new HashMap<>();
    private static final String[] hookPackages = {
        "android",
        "com.miui.powerkeeper",
        "com.oplus.athena",
        "com.oplus.battery",
        "com.android.systemui"
    };

    static {
        baseHCs.put("android", new BaseHC[]{androidU, hyperV1, userUnlockListener, crashListener});
        baseHCs.put("com.miui.powerkeeper", new BaseHC[]{powerKeeper});
        baseHCs.put("com.android.systemui", new BaseHC[]{new LogPuppet()});
        hookAsTag.put("android", "Android");
        hookAsTag.put("com.miui.powerkeeper", "PowerKeeper");
        hookAsTag.put("com.android.systemui", "LogPuppet");
    }

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam loadPackageParam) {
        baseHCs.forEach((packageName, baseHCs) -> {
            if (packageName.equals(loadPackageParam.packageName)) {
                LogToFile.initLogToFile(hookAsTag.get(packageName));
                HCInit.initLoadPackageParam(loadPackageParam);
                Arrays.stream(baseHCs).forEach(BaseHC::onLoadPackage);
            }
        });
    }

    @Override
    public void initZygote(StartupParam startupParam) {
        HCInit.initBasicData(new HCInit.BasicData()
            .setTag("AppRetention")
            .setModulePackageName(BuildConfig.APPLICATION_ID)
            .setLogLevel(HCInit.LOG_D)
        );
        HCInit.useLogExpand(new String[]{
            "com.hchen.appretention"
        });
        HCInit.setLogFileRootName("AppRetention");
        HCInit.setModuleVersion(BuildConfig.VERSION_NAME + "(" + BuildConfig.VERSION_CODE + ")");
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
