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
package com.hchen.appretention.hook.samsung;

import static com.hchen.appretention.data.field.OneUiField.ENABLE_KILL_LONG_RUNNING_PROCESS;
import static com.hchen.appretention.data.field.OneUiField.INSTANCE;
import static com.hchen.appretention.data.field.OneUiField.KPM_BTIME_ENABLE;
import static com.hchen.appretention.data.field.OneUiField.KPM_POLICY_ENABLE;
import static com.hchen.appretention.data.field.OneUiField.MARs_ENABLE;
import static com.hchen.appretention.data.field.OneUiField.MAX_LONG_LIVE_APP;
import static com.hchen.appretention.data.field.OneUiField.WRITEBACK_ENABLED;
import static com.hchen.appretention.data.method.OneUiMethod.IsForceKillHeavyProcess;
import static com.hchen.appretention.data.method.OneUiMethod.activeLaunchKillCheck;
import static com.hchen.appretention.data.method.OneUiMethod.addLongLivePackageLocked;
import static com.hchen.appretention.data.method.OneUiMethod.checkKeptProcess;
import static com.hchen.appretention.data.method.OneUiMethod.getInstance;
import static com.hchen.appretention.data.method.OneUiMethod.getMARsEnabled;
import static com.hchen.appretention.data.method.OneUiMethod.getMaxLongLiveApps;
import static com.hchen.appretention.data.method.OneUiMethod.isBEKCondition;
import static com.hchen.appretention.data.method.OneUiMethod.isExcessiveResourceUsage;
import static com.hchen.appretention.data.method.OneUiMethod.isPmmEnabled;
import static com.hchen.appretention.data.method.OneUiMethod.killTimeOverEmptyProcess;
import static com.hchen.appretention.data.method.OneUiMethod.setLmkdCameraKillBoost;
import static com.hchen.appretention.data.method.OneUiMethod.setLmkdParameter;
import static com.hchen.appretention.data.method.OneUiMethod.updateNapProcessProtection;
import static com.hchen.appretention.data.path.OneUiClass.ActivityManagerServiceExt;
import static com.hchen.appretention.data.path.OneUiClass.BGProtectManager;
import static com.hchen.appretention.data.path.OneUiClass.ChimeraManagerService;
import static com.hchen.appretention.data.path.OneUiClass.DynamicHiddenApp;
import static com.hchen.appretention.data.path.OneUiClass.KillPolicyManager;
import static com.hchen.appretention.data.path.OneUiClass.MARsPolicyManager;
import static com.hchen.appretention.data.path.OneUiClass.PerProcessNandswap;
import static com.hchen.appretention.data.path.SystemClass.ActivityManagerService;
import static com.hchen.appretention.data.path.SystemClass.ProcessList;
import static com.hchen.appretention.data.path.SystemClass.ProcessRecord;

import android.content.Context;

import com.hchen.hooktool.BaseHC;
import com.hchen.hooktool.hook.IHook;
import com.hchen.processor.HookEntrance;

/**
 * 三星 OneUi
 *
 * @author 焕晨HChen
 */
@HookEntrance(targetPackage = "android", targetBrand = "samsung")
public class OneUi extends BaseHC {
    @Override
    public void init() {
        LmkdParameter.init();
        LmkdParameter.forceReplace();

        // ------------- ProcessList -------------------
        /*
         * 替换三星写入 lmkd 的参数。
         * */
        hookMethod(ProcessList,
            setLmkdParameter,
            int.class, int.class,
            new IHook() {
                @Override
                public void before() {
                    LmkdParameter.replace(this);
                }
            }
        );

        /*
         * 禁止相机 kill。
         * */
        hookMethod(ProcessList,
            setLmkdCameraKillBoost,
            int.class, int.class, int.class,
            doNothing()
        );

        // --------------- ChimeraManagerService ----------
        /*
         * 阻止构造函数中逻辑执行，即可彻底废掉其所包含的功能。
         * */
        hookConstructor(ChimeraManagerService,
            Context.class, ActivityManagerService,
            doNothing()
        );

        // ------------ DynamicHiddenApp ----------------
        /*
         * 禁止主动触发 kill。
         * */
        hookMethod(DynamicHiddenApp,
            activeLaunchKillCheck,
            ProcessRecord,
            doNothing()
        );

        /*
         * 禁止 kill 超时的空进程。
         * */
        hookMethod(DynamicHiddenApp,
            killTimeOverEmptyProcess,
            ProcessRecord, int.class, long.class,
            doNothing()
        );

        // --------------- BGProtectManager -------------
        /*
         * 禁止 kill 超时的进程。
         * */
        hookMethod(BGProtectManager,
            updateNapProcessProtection,
            ProcessRecord,
            doNothing()
        );

        /*
         * 禁止 kill 重型进程。
         * */
        hookMethod(BGProtectManager,
            IsForceKillHeavyProcess,
            String.class,
            returnResult(false)
        );

        /*
         * 保留空进程。
         * */
        hookMethod(BGProtectManager,
            isBEKCondition,
            ProcessRecord,
            returnResult(true)
        );

        hookMethod(BGProtectManager,
            checkKeptProcess,
            ProcessRecord,
            returnResult(0)
        );

        // ---------------- ProcessRecord -----------------
        /*
         * 不是过度使用系统资源。
         * */
        hookMethod(ProcessRecord,
            isExcessiveResourceUsage,
            returnResult(false)
        );

        // -------------- PerProcessNandswap ----------------
        /*
         * 防止崩溃？
         * */
        hookMethod(PerProcessNandswap,
            getInstance,
            new IHook() {
                @Override
                public void after() {
                    Object perProcessNandswap = getStaticField(PerProcessNandswap, INSTANCE);
                    if (perProcessNandswap == null)
                        perProcessNandswap = getResult();
                    if (perProcessNandswap != null)
                        setField(perProcessNandswap, WRITEBACK_ENABLED, false);

                    logD(TAG, "PerProcessNandswap: " + perProcessNandswap);
                }
            }
        );

        // ------------------ ActivityManagerServiceExt ---------------
        /*
         * 解除最大不被 kill 应用的数量限制。
         * */
        chain(ActivityManagerServiceExt, method(addLongLivePackageLocked, String.class)
            .hook(new IHook() {
                @Override
                public void before() {
                    setStaticField(ActivityManagerServiceExt, MAX_LONG_LIVE_APP, Integer.MAX_VALUE);
                }
            })

            .method(getMaxLongLiveApps)
            .returnResult(Integer.MAX_VALUE)
        );

        // ------------------ MARsPolicyManager -------------------------
        /*
         * 禁用 MARs
         * */
        setStaticField(MARsPolicyManager, ENABLE_KILL_LONG_RUNNING_PROCESS, false);
        setStaticField(MARsPolicyManager, MARs_ENABLE, false);

        hookMethod(MARsPolicyManager,
            getMARsEnabled,
            returnResult(false)
        );

        // ------------------ KillPolicyManager -----------------------
        /*
         * 禁用 PPM
         * */
        hookMethod(ActivityManagerService,
            isPmmEnabled,
            returnResult(false)
        );

        setStaticField(KillPolicyManager, KPM_POLICY_ENABLE, false);
        setStaticField(KillPolicyManager, KPM_BTIME_ENABLE, false);
    }
}
