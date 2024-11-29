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
package com.hchen.appretention.hook.samsung;

import static com.hchen.appretention.data.field.OneUi.ENABLE_KILL_LONG_RUNNING_PROCESS;
import static com.hchen.appretention.data.field.OneUi.INSTANCE;
import static com.hchen.appretention.data.field.OneUi.KPM_BTIME_ENABLE;
import static com.hchen.appretention.data.field.OneUi.KPM_POLICY_ENABLE;
import static com.hchen.appretention.data.field.OneUi.MARs_ENABLE;
import static com.hchen.appretention.data.field.OneUi.MAX_LONG_LIVE_APP;
import static com.hchen.appretention.data.field.OneUi.WRITEBACK_ENABLED;
import static com.hchen.appretention.data.method.OneUi.IsForceKillHeavyProcess;
import static com.hchen.appretention.data.method.OneUi.activeLaunchKillCheck;
import static com.hchen.appretention.data.method.OneUi.addLongLivePackageLocked;
import static com.hchen.appretention.data.method.OneUi.checkKeptProcess;
import static com.hchen.appretention.data.method.OneUi.getInstance;
import static com.hchen.appretention.data.method.OneUi.getMARsEnabled;
import static com.hchen.appretention.data.method.OneUi.getMaxLongLiveApps;
import static com.hchen.appretention.data.method.OneUi.isBEKCondition;
import static com.hchen.appretention.data.method.OneUi.isExcessiveResourceUsage;
import static com.hchen.appretention.data.method.OneUi.isPmmEnabled;
import static com.hchen.appretention.data.method.OneUi.killTimeOverEmptyProcess;
import static com.hchen.appretention.data.method.OneUi.setLmkdCameraKillBoost;
import static com.hchen.appretention.data.method.OneUi.setLmkdParameter;
import static com.hchen.appretention.data.method.OneUi.updateNapProcessProtection;
import static com.hchen.appretention.data.path.OneUi.ActivityManagerServiceExt;
import static com.hchen.appretention.data.path.OneUi.BGProtectManager;
import static com.hchen.appretention.data.path.OneUi.ChimeraManagerService;
import static com.hchen.appretention.data.path.OneUi.DynamicHiddenApp;
import static com.hchen.appretention.data.path.OneUi.KillPolicyManager;
import static com.hchen.appretention.data.path.OneUi.MARsPolicyManager;
import static com.hchen.appretention.data.path.OneUi.PerProcessNandswap;
import static com.hchen.appretention.data.path.System.ActivityManagerService;
import static com.hchen.appretention.data.path.System.ProcessList;
import static com.hchen.appretention.data.path.System.ProcessRecord;
import static com.hchen.hooktool.log.XposedLog.logD;

import android.content.Context;

import com.hchen.hooktool.BaseHC;
import com.hchen.hooktool.hook.IHook;
import com.hchen.processor.HookCondition;

/**
 * 三星 OneUi
 *
 * @author 焕晨HChen
 */
@HookCondition(targetPackage = "android", targetBrand = "samsung")
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
