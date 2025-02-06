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
package com.hchen.appretention.hook.hyper;

import static com.hchen.appretention.data.method.HyperMethod.boostCameraByThreshold;
import static com.hchen.appretention.data.method.HyperMethod.callMethod;
import static com.hchen.appretention.data.method.HyperMethod.callStaticMethod;
import static com.hchen.appretention.data.method.HyperMethod.doAdjBoost;
import static com.hchen.appretention.data.method.HyperMethod.interceptAppRestartIfNeeded;
import static com.hchen.appretention.data.method.HyperMethod.isAllowAdjBoost;
import static com.hchen.appretention.data.method.HyperMethod.newInstance;
import static com.hchen.appretention.data.method.HyperMethod.notifyActivityChanged;
import static com.hchen.appretention.data.method.HyperMethod.notifyCameraForegroundChange;
import static com.hchen.appretention.data.method.HyperMethod.notifyCameraForegroundState;
import static com.hchen.appretention.data.method.HyperMethod.notifyCameraPostProcessState;
import static com.hchen.appretention.data.method.HyperMethod.reclaimMemoryForCamera;
import static com.hchen.appretention.data.method.HyperMethod.updateCameraBoosterCloudData;
import static com.hchen.appretention.data.path.HyperClass.CameraOpt;
import static com.hchen.appretention.data.path.HyperClass.ICameraBooster;
import static com.hchen.appretention.data.path.HyperClass.ICameraBooster$CameraBoosterProxy;
import static com.hchen.appretention.data.path.HyperClass.ProcessManagerInternal;
import static com.hchen.appretention.data.path.HyperClass.ServiceThread;
import static com.hchen.appretention.data.path.SystemClass.ActivityManagerService;
import static com.hchen.hooktool.tool.CoreTool.doNothing;
import static com.hchen.hooktool.tool.CoreTool.existsAnyMethod;
import static com.hchen.hooktool.tool.CoreTool.existsClass;
import static com.hchen.hooktool.tool.CoreTool.existsField;
import static com.hchen.hooktool.tool.CoreTool.findAllMethod;
import static com.hchen.hooktool.tool.CoreTool.findClass;
import static com.hchen.hooktool.tool.CoreTool.hook;
import static com.hchen.hooktool.tool.CoreTool.hookMethod;
import static com.hchen.hooktool.tool.CoreTool.returnResult;

import android.content.Context;

import com.hchen.appretention.data.field.HyperField;
import com.hchen.hooktool.hook.IHook;
import com.hchen.hooktool.tool.CoreTool;

import java.lang.reflect.Method;

/**
 * 禁用相机优化
 *
 * @author 焕晨HChen
 */
public class CameraOpt {

    public static void doHook() {
        if (existsClass(CameraOpt)) {
            Class<?> mCameraOpt = findClass(CameraOpt);
            if (existsField(mCameraOpt, HyperField.mCameraBoosterClazz) || existsField(mCameraOpt, HyperField.mQuickCameraClazz)) {
                hookMethod(CameraOpt,
                    callStaticMethod,
                    Class.class, String.class, Object[].class,
                    returnResult(null).shouldObserveCall(false)
                );
                // 帮助 CameraOpt 初始化
                // Class<?> mCameraBoosterClazz = (Class<?>) getStaticField(mCameraOpt, Hyper.mCameraBoosterClazz);
                // Class<?> mQuickCameraClazz = (Class<?>) getStaticField(mCameraOpt, Hyper.mQuickCameraClazz);
                // if (mCameraBoosterClazz != null || mQuickCameraClazz != null) {
                //     ClassLoader mCameraOptClassLoader = mCameraBoosterClazz != null ? mCameraBoosterClazz.getClassLoader() : mQuickCameraClazz.getClassLoader();
                //     doHookCameraOpt(findClass(CameraBooster, mCameraOptClassLoader));
                // }
            } else {
                hookMethod(CameraOpt,
                    callMethod,
                    String.class, Object[].class,
                    returnResult(null).shouldObserveCall(false)
                );
                // Class<?> mCameraOptManager = (Class<?>) getStaticField(mCameraOpt, Hyper.mCameraOptManager);
                // if (existsMethod(CameraOptManager, mCameraOptManager.getClassLoader(), ensureService)) {
                //     hookMethod(CameraOptManager,
                //         mCameraOptManager.getClassLoader(),
                //         ensureService,
                //         doNothing()
                //     );
                // } else {
                //     Method service = filterMethod(CameraOptManager, mCameraOptManager.getClassLoader(), new IMemberFilter<Method>() {
                //         @Override
                //         public boolean test(Method member) {
                //             if (member == null) return false;
                //             if (member.getParameterCount() > 0) return false;
                //             if (member.getName().length() > 3) return false;
                //             return true;
                //         }
                //     })[0];
                //     hook(service, doNothing().shouldObserveCall(false));
                // }
            }
        } else {
            hookMethod(ICameraBooster,
                newInstance,
                ProcessManagerInternal, ActivityManagerService, ServiceThread, Context.class,
                new IHook() {
                    @Override
                    public void before() {
                        Object mCameraBoosterProxy = CoreTool.newInstance(ICameraBooster$CameraBoosterProxy);
                        setResult(mCameraBoosterProxy);
                    }

                    @Override
                    public void after() {
                        // Object mICameraBooster = getResult();
                        // ClassLoader mCameraOptClassLoader = mICameraBooster.getClass().getClassLoader();
                        // doHookCameraOpt(findClass(CameraBoosterNew, mCameraOptClassLoader));
                    }
                }
            );
        }
    }

    // 执行对 cameraOpt 的 hook 动作
    @Deprecated // 废弃，过时的复杂实现
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
                Method method = findAllMethod(cameraBooster, m)[0];
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
