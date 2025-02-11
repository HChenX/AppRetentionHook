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

 * Copyright (C) 2023-2025 HChenX
 */
package com.hchen.appretention;

import static com.hchen.hooktool.log.XposedLog.logENoSave;

import com.hchen.appretention.hook.EntranceMap;
import com.hchen.appretention.hook.TestHook;
import com.hchen.appretention.log.SaveLog;
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
    @Deprecated
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
        EntranceMap.get().forEach(new BiConsumer<>() {
            @Override
            public void accept(String s, EntranceMap entranceMap) {
                if (!entranceMap.mTargetPackage.equals(lpparam.packageName))
                    return;
                if (!"Any".equals(entranceMap.mTargetBrand) && !DeviceTool.isRightRom(entranceMap.mTargetBrand))
                    return;
                if (!(entranceMap.mTargetSdk == 0) && !DeviceTool.isAndroidVersion(entranceMap.mTargetSdk))
                    return;
                if ("Xiaomi".equals(entranceMap.mTargetBrand)) {
                    if (entranceMap.mTargetOS != -1) {
                        if (entranceMap.isHyperOS) {
                            if (!DeviceTool.isHyperOSVersion(entranceMap.mTargetOS) && !entranceMap.mUpward && !entranceMap.mDownward)
                                return;
                            if (entranceMap.mUpward && !(DeviceTool.getHyperOSVersion() >= entranceMap.mTargetOS))
                                return;
                            if (entranceMap.mDownward && !(DeviceTool.getHyperOSVersion() <= entranceMap.mTargetOS))
                                return;
                        } else if (DeviceTool.getMiuiVersion() != 0f) {
                            if (!DeviceTool.isMiuiVersion(entranceMap.mTargetOS) && !entranceMap.mUpward && !entranceMap.mDownward)
                                return;
                            if (entranceMap.mUpward && !(DeviceTool.getMiuiVersion() >= entranceMap.mTargetOS))
                                return;
                            if (entranceMap.mDownward && !(DeviceTool.getMiuiVersion() <= entranceMap.mTargetOS))
                                return;
                        } else return;
                    }
                }

                try {
                    Class<?> hookClass = getClass().getClassLoader().loadClass(s);
                    BaseHC baseHC = (BaseHC) hookClass.getDeclaredConstructor().newInstance();
                    String className = baseHC.TAG;
                    SaveLog.initLogToFile(className);
                    // SaveLog.initSaveLog(className);
                    HCInit.initLoadPackageParam(lpparam);
                    baseHC.onLoadPackage();
                } catch (ClassNotFoundException | NoSuchMethodException |
                         IllegalAccessException |
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

    @Deprecated
    private void initHook(BaseHC baseHC) {
        baseHC.onLoadPackage();
    }

    @Deprecated
    private DexKitBridge mBridge = null;

    @Deprecated
    public DexKitBridge initDexkit(XC_LoadPackage.LoadPackageParam loadPackageParam) {
        if (mBridge == null) {
            System.loadLibrary("dexkit");
            mBridge = DexKitBridge.create(loadPackageParam.appInfo.sourceDir);
        }
        return mBridge;
    }

    @Deprecated
    public void closeDexkit() {
        if (mBridge != null) {
            mBridge.close();
            mBridge = null;
        }
    }
}
