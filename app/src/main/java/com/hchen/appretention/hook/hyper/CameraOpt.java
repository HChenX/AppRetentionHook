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
package com.hchen.appretention.hook.hyper;

import static com.hchen.appretention.data.method.Hyper.boostCameraByThreshold;
import static com.hchen.appretention.data.method.Hyper.doAdjBoost;
import static com.hchen.appretention.data.method.Hyper.ensureService;
import static com.hchen.appretention.data.method.Hyper.interceptAppRestartIfNeeded;
import static com.hchen.appretention.data.method.Hyper.isAllowAdjBoost;
import static com.hchen.appretention.data.method.Hyper.newInstance;
import static com.hchen.appretention.data.method.Hyper.notifyActivityChanged;
import static com.hchen.appretention.data.method.Hyper.notifyCameraForegroundChange;
import static com.hchen.appretention.data.method.Hyper.notifyCameraForegroundState;
import static com.hchen.appretention.data.method.Hyper.notifyCameraPostProcessState;
import static com.hchen.appretention.data.method.Hyper.reclaimMemoryForCamera;
import static com.hchen.appretention.data.method.Hyper.updateCameraBoosterCloudData;
import static com.hchen.appretention.data.path.Hyper.CameraBooster;
import static com.hchen.appretention.data.path.Hyper.CameraBoosterNew;
import static com.hchen.appretention.data.path.Hyper.CameraOpt;
import static com.hchen.appretention.data.path.Hyper.CameraOptManager;
import static com.hchen.appretention.data.path.Hyper.ICameraBooster;
import static com.hchen.appretention.data.path.Hyper.ProcessManagerInternal;
import static com.hchen.appretention.data.path.Hyper.ServiceThread;
import static com.hchen.appretention.data.path.System.ActivityManagerService;
import static com.hchen.hooktool.tool.CoreTool.doNothing;
import static com.hchen.hooktool.tool.CoreTool.existsAnyMethod;
import static com.hchen.hooktool.tool.CoreTool.existsField;
import static com.hchen.hooktool.tool.CoreTool.existsMethod;
import static com.hchen.hooktool.tool.CoreTool.filterMethod;
import static com.hchen.hooktool.tool.CoreTool.findAllMethod;
import static com.hchen.hooktool.tool.CoreTool.findClass;
import static com.hchen.hooktool.tool.CoreTool.getStaticField;
import static com.hchen.hooktool.tool.CoreTool.hook;
import static com.hchen.hooktool.tool.CoreTool.hookMethod;
import static com.hchen.hooktool.tool.CoreTool.returnResult;

import android.content.Context;

import com.hchen.appretention.data.field.Hyper;
import com.hchen.hooktool.hook.IHook;
import com.hchen.hooktool.tool.itool.IMemberFilter;

import java.lang.reflect.Method;

/**
 * 禁用相机优化
 *
 * @author 焕晨HChen
 */
public class CameraOpt {

    public static void doHook() {
        /*
         * 从此开始，下方为针对相机杀后台而 hook 的内容。
         * */
        Class<?> mCameraOpt = findClass(CameraOpt).getNoReport();
        if (mCameraOpt != null) {
            if (existsField(mCameraOpt, Hyper.mCameraBoosterClazz) || existsField(mCameraOpt, Hyper.mQuickCameraClazz)) {
                // 帮助 CameraOpt 初始化
                Class<?> mCameraBoosterClazz = (Class<?>) getStaticField(mCameraOpt, Hyper.mCameraBoosterClazz);
                Class<?> mQuickCameraClazz = (Class<?>) getStaticField(mCameraOpt, Hyper.mQuickCameraClazz);
                if (mCameraBoosterClazz != null || mQuickCameraClazz != null) {
                    ClassLoader mCameraOptClassLoader = mCameraBoosterClazz != null ? mCameraBoosterClazz.getClassLoader() : mQuickCameraClazz.getClassLoader();
                    doHookCameraOpt(findClass(CameraBooster, mCameraOptClassLoader).get());
                }
            } else {
                Class<?> mCameraOptManager = (Class<?>) getStaticField(mCameraOpt, Hyper.mCameraOptManager);
                if (existsMethod(CameraOptManager, mCameraOptManager.getClassLoader(), ensureService)) {
                    hookMethod(CameraOptManager,
                        mCameraOptManager.getClassLoader(),
                        ensureService,
                        doNothing()
                    );
                } else {
                    Method service = filterMethod(CameraOptManager, mCameraOptManager.getClassLoader(), new IMemberFilter<Method>() {
                        @Override
                        public boolean test(Method member) {
                            if (member == null) return false;
                            if (member.getParameterCount() > 0) return false;
                            if (member.getName().length() > 3) return false;
                            return true;
                        }
                    }).get(0);
                    hook(service, doNothing());
                }
            }
        } else {
            hookMethod(ICameraBooster,
                newInstance,
                ProcessManagerInternal, ActivityManagerService, ServiceThread, Context.class,
                new IHook() {
                    @Override
                    public void after() {
                        Object mICameraBooster = getResult();
                        ClassLoader mCameraOptClassLoader = mICameraBooster.getClass().getClassLoader();
                        doHookCameraOpt(findClass(CameraBoosterNew, mCameraOptClassLoader).get());
                    }
                }
            );
        }
    }

    // 执行对 cameraOpt 的 hook 动作
    private static void doHookCameraOpt(Class<?> cameraBooster) {
        String[] mCameraOptShouldHookMethodList = new String[]{
            boostCameraByThreshold,
            doAdjBoost, // 禁用 adj 加速
            interceptAppRestartIfNeeded, // 不允许限制应用重启
            isAllowAdjBoost, // 返回 true, 防止崩溃
            notifyCameraForegroundChange,
            notifyCameraForegroundState,
            notifyCameraPostProcessState,
            notifyActivityChanged,
            reclaimMemoryForCamera, // 禁止压缩进程
            updateCameraBoosterCloudData // 禁止云更新
        };

        for (String m : mCameraOptShouldHookMethodList) {
            if (existsAnyMethod(cameraBooster, m)) {
                Method method = findAllMethod(cameraBooster, m).get(0);
                if (method == null) continue;
                if (method.getName().equals(interceptAppRestartIfNeeded)) {
                    hook(method, returnResult(false).shouldObserveCall(false));
                } else if (isAllowAdjBoost.equals(method.getName())) {
                    hook(method, returnResult(true).shouldObserveCall(false));
                } else
                    hook(method, doNothing().shouldObserveCall(false));
            }
        }
    }
}
