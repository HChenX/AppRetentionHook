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

import static com.hchen.appretention.data.method.Hyper.init;
import static com.hchen.appretention.data.method.OneUi.setLmkdParameter;
import static com.hchen.appretention.data.path.OneUi.DynamicHiddenApp$LmkdParameter;
import static com.hchen.appretention.data.path.System.ActiveUids;
import static com.hchen.appretention.data.path.System.ActivityManagerService;
import static com.hchen.appretention.data.path.System.PlatformCompat;
import static com.hchen.appretention.data.path.System.ProcessList;
import static com.hchen.hooktool.log.XposedLog.logD;
import static com.hchen.hooktool.log.XposedLog.logI;
import static com.hchen.hooktool.tool.CoreTool.callStaticMethod;
import static com.hchen.hooktool.tool.CoreTool.findClass;
import static com.hchen.hooktool.tool.CoreTool.hookMethod;

import android.util.Pair;

import com.hchen.hooktool.hook.IHook;
import com.hchen.hooktool.tool.CoreTool;

import java.util.Arrays;
import java.util.HashMap;

/**
 * 初始化并替换 lmkd 参数
 *
 * @author 焕晨HChen
 */
public final class LmkdParameter {
    private static final String TAG = "LmkdParameter";
    final static String[] mLmkdParameter = new String[]{
        "LMK_LOW_ADJ", "LMK_MEDIUM_ADJ", "LMK_CRITICAL_ADJ", "LMK_DEBUG",
        "LMK_CRITICAL_UPGRADE", "LMK_UPGRADE_PRESSURE", "LMK_DOWNGRADE_PRESSURE",
        "LMK_KILL_HEAVIEST_TASK", "LMK_KILL_TIMEOUT_MS", "LMK_USE_MINFREE_LEVELS",
        "LMK_ENABLE_USERSPACE_LMK", "LMK_ENABLE_CMARBINFREE_SUB", "LMK_ENABLE_UPGRADE_CRIADJ",
        "LMK_FREELIMIT_ENABLE", "LMK_FREELIMIT_VAL", "LMK_PSI_LOW_TH", "LMK_PSI_MEDIUM_TH",
        "LMK_PSI_CRITICAL_TH", "LMK_SET_SWAPTOTAL", "LMK_SET_BG_KEEPING"
    };
    final static HashMap<Integer, Pair<String, Integer>> mOrdinalAndParameterMap = new HashMap<>();
    static boolean isInit = false;

    public static void init() {
        if (isInit) return;
        Class<?> lmkdParameter = findClass(DynamicHiddenApp$LmkdParameter).get();
        if (lmkdParameter == null)
            return;

        Arrays.stream(mLmkdParameter).forEach(s -> {
            Object param = CoreTool.getStaticField(lmkdParameter, s);
            if (param == null) return;

            Integer ordinal = CoreTool.callMethod(param, "ordinal");
            if (ordinal == null) return;

            switch (s) {
                case "LMK_LOW_ADJ", "LMK_MEDIUM_ADJ", "LMK_CRITICAL_ADJ":
                    mOrdinalAndParameterMap.put(ordinal, new Pair<>(s, 1001));
                    break;
                case "LMK_DEBUG", "LMK_CRITICAL_UPGRADE", "LMK_KILL_HEAVIEST_TASK",
                     "LMK_ENABLE_UPGRADE_CRIADJ", "LMK_FREELIMIT_ENABLE":
                    mOrdinalAndParameterMap.put(ordinal, new Pair<>(s, 0));
                    break;
                case "LMK_UPGRADE_PRESSURE", "LMK_DOWNGRADE_PRESSURE":
                    mOrdinalAndParameterMap.put(ordinal, new Pair<>(s, 100));
                    break;
                case "LMK_SET_BG_KEEPING":
                    mOrdinalAndParameterMap.put(ordinal, new Pair<>(s, 1));
                    break;
            }
        });

        // 只输出日志
        mOrdinalAndParameterMap.forEach((integer, stringIntegerPair) ->
            logI(TAG, "Map: lmkd parameter: " + stringIntegerPair.first + ", value: " + stringIntegerPair.second + ", ordinal: " + integer));

        isInit = true;
    }

    public static void forceReplace() {
        hookMethod(ProcessList,
            init,
            ActivityManagerService, ActiveUids, PlatformCompat,
            new IHook() {
                @Override
                public void after() {
                    mOrdinalAndParameterMap.forEach((integer, stringIntegerPair) -> {
                        callStaticMethod(ProcessList, setLmkdParameter, integer, stringIntegerPair.second);
                        logD(TAG, "Force ste lmkd parameter: " + stringIntegerPair.first + ", value: " + stringIntegerPair.second + ", ordinal: " + integer);
                    });
                }
            }
        );
    }

    public static void replace(IHook iHook) {
        if (!isInit)
            init();

        int ordinal = iHook.getArgs(0);
        int value = iHook.getArgs(1);

        if (mOrdinalAndParameterMap.get(ordinal) != null) {
            Pair<String, Integer> param = mOrdinalAndParameterMap.get(ordinal);
            assert param != null;
            int newValue = param.second;
            iHook.setArgs(1, newValue);
            logD(TAG, "Lmkd parameter: " + param.first + ", old value: " + value + ", new value: " + newValue);
        }
    }
}
