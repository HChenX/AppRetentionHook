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

import androidx.annotation.NonNull;

import com.hchen.appretention.hook.EntranceMap;
import com.hchen.appretention.log.SaveLog;
import com.hchen.hooktool.HCBase;
import com.hchen.hooktool.HCEntrance;
import com.hchen.hooktool.HCInit;
import com.hchen.hooktool.utils.DeviceTool;
import com.hchen.hooktool.utils.SystemPropTool;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.BiConsumer;

import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * Hook 入口
 *
 * @author 焕晨HChen
 */
public class HookInit extends HCEntrance {
    private static final String TAG = "AppRetention";

    @NonNull
    @Override
    public HCInit.BasicData initHC(@NonNull HCInit.BasicData basicData) {
        return basicData
            .setTag(TAG)
            .setModulePackageName(BuildConfig.APPLICATION_ID)
            .setLogLevel(HCInit.LOG_D)
            .setLogExpandPath("com.hchen.appretention.hook");
    }

    @Override
    public void onLoadPackage(@NonNull XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {
        EntranceMap.get().forEach(new BiConsumer<>() {
            @Override
            public void accept(String s, EntranceMap entranceMap) {
                if (!entranceMap.mTargetPackage.equals(loadPackageParam.packageName))
                    return;
                if (!"Any".equals(entranceMap.mTargetBrand) && !DeviceTool.isRightRom(entranceMap.mTargetBrand))
                    return;
                if (!(entranceMap.mTargetSdks[0] == 0) && Arrays.stream(entranceMap.mTargetSdks).noneMatch(DeviceTool::isAndroidVersion))
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

                if (Objects.equals(entranceMap.mTargetBrand, "samsung")) {
                    if (!isEnableOneUi())
                        return; // 暂时关闭 OneUi 修改
                }
                try {
                    Class<?> hookClass = getClass().getClassLoader().loadClass(s);
                    HCBase hcBase = (HCBase) hookClass.getDeclaredConstructor().newInstance();
                    String className = hcBase.TAG;
                    SaveLog.initLogToFile(className);
                    // SaveLog.initSaveLog(className);
                    HCInit.initLoadPackageParam(loadPackageParam);
                    hcBase.onLoadPackage();
                } catch (ClassNotFoundException | NoSuchMethodException |
                         IllegalAccessException |
                         InstantiationException | InvocationTargetException e) {
                    logENoSave(TAG, e);
                }
            }
        });
    }

    private boolean isEnableOneUi() {
        return SystemPropTool.getProp("persist.hchen.oneui.enable", false);
    }
}
