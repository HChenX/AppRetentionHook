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

 * Copyright (C) 2023-2024 HChenX
 */
package com.hchen.appretention;

import static com.hchen.hooktool.log.XposedLog.logENoSave;

import com.hchen.appretention.hook.ConditionMap;
import com.hchen.appretention.hook.TestHook;
import com.hchen.appretention.log.LogToFile;
import com.hchen.hooktool.BaseHC;
import com.hchen.hooktool.HCEntrance;
import com.hchen.hooktool.HCInit;
import com.hchen.hooktool.tool.additional.DeviceTool;

import org.luckypray.dexkit.DexKitBridge;

import java.lang.reflect.InvocationTargetException;
import java.util.function.BiConsumer;

import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * Hook 入口
 *
 * @author 焕晨HChen
 */
public class HookInit extends HCEntrance {
    private static final String TAG = "AppRetention";
    private static final String[] hookPackages = {
        "android",
        "com.miui.powerkeeper",
        "com.oplus.athena",
        "com.oplus.battery",
        "com.android.systemui"
    };

    @Override
    public HCInit.BasicData initHC(HCInit.BasicData basicData) {
        return basicData.setTag("AppRetention")
            .setModulePackageName(BuildConfig.APPLICATION_ID)
            .setLogLevel(HCInit.LOG_D)
            .initLogExpand(new String[]{
                "com.hchen.appretention"
            });
    }

    @Override
    public void onLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        ConditionMap.get().forEach(new BiConsumer<>() {
            @Override
            public void accept(String s, ConditionMap conditionMap) {
                if (!conditionMap.mTargetPackage.equals(lpparam.packageName))
                    return;
                if (!conditionMap.mTargetBrand.equals("Any") && !DeviceTool.isRightRom(conditionMap.mTargetBrand))
                    return;
                if (!(conditionMap.mTargetSdk == 0) && !DeviceTool.isAndroidVersion(conditionMap.mTargetSdk))
                    return;
                if (!(conditionMap.mTargetOS == -1f) &&
                    !(DeviceTool.isHyperOSVersion(conditionMap.mTargetOS) || DeviceTool.isMiuiVersion(conditionMap.mTargetOS)))
                    return;
                try {
                    Class<?> hookClass = getClass().getClassLoader().loadClass(s);
                    BaseHC baseHC = (BaseHC) hookClass.getDeclaredConstructor().newInstance();
                    String className = baseHC.TAG;
                    LogToFile.initLogToFile(className);
                    HCInit.initLoadPackageParam(lpparam);
                    baseHC.onLoadPackage();
                } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException |
                         InstantiationException | InvocationTargetException e) {
                    logENoSave(TAG, e);
                }
            }
        });
        if (lpparam.packageName.equals("com.hchen.himiuixdemo")) {
            HCInit.initLoadPackageParam(lpparam);
            new TestHook().onLoadPackage();
        }
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
