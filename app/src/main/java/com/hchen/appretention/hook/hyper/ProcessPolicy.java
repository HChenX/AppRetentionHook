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
package com.hchen.appretention.hook.hyper;

/**
 * Hyper 的一些清理动作
 *
 * @author 焕晨HChen
 */
public final class ProcessPolicy {
    public static final String REASON_ANR = "anr";
    public static final String REASON_AUTO_IDLE_KILL = "AutoIdleKill";
    public static final String REASON_AUTO_LOCK_OFF_CLEAN = "AutoLockOffClean";
    public static final String REASON_AUTO_LOCK_OFF_CLEAN_BY_PRIORITY = "AutoLockOffCleanByPriority";
    public static final String REASON_AUTO_POWER_KILL = "AutoPowerKill";
    public static final String REASON_AUTO_SLEEP_CLEAN = "AutoSleepClean";
    public static final String REASON_AUTO_SYSTEM_ABNORMAL_CLEAN = "AutoSystemAbnormalClean";
    public static final String REASON_AUTO_THERMAL_KILL = "AutoThermalKill";
    public static final String REASON_AUTO_THERMAL_KILL_ALL_LEVEL_1 = "AutoThermalKillAll1";
    public static final String REASON_AUTO_THERMAL_KILL_ALL_LEVEL_2 = "AutoThermalKillAll2";
    public static final String REASON_CRASH = "crash";
    public static final String REASON_DISPLAY_SIZE_CHANGED = "DisplaySizeChanged";
    public static final String REASON_FORCE_CLEAN = "ForceClean";
    public static final String REASON_GAME_CLEAN = "GameClean";
    public static final String REASON_GARBAGE_CLEAN = "GarbageClean";
    public static final String REASON_LOCK_SCREEN_CLEAN = "LockScreenClean";
    public static final String REASON_LOW_MEMO = "lowMemory";
    public static final String REASON_MIUI_MEMO_SERVICE = "MiuiMemoryService";
    public static final String REASON_ONE_KEY_CLEAN = "OneKeyClean";
    public static final String REASON_OPTIMIZATION_CLEAN = "OptimizationClean";
    public static final String REASON_SCREEN_OFF_CPU_CHECK_KILL = "ScreenOffCPUCheckKill";
    public static final String REASON_SWIPE_UP_CLEAN = "SwipeUpClean";
    public static final String REASON_UNKNOWN = "Unknown";
    public static final String REASON_USER_DEFINED = "UserDefined";

    public static String getKillReason(int policy) {
        return switch (policy) {
            case 1 -> ProcessPolicy.REASON_ONE_KEY_CLEAN;
            case 2 -> ProcessPolicy.REASON_FORCE_CLEAN;
            case 3 -> ProcessPolicy.REASON_LOCK_SCREEN_CLEAN;
            case 4 -> ProcessPolicy.REASON_GAME_CLEAN;
            case 5 -> ProcessPolicy.REASON_OPTIMIZATION_CLEAN;
            case 6 -> ProcessPolicy.REASON_GARBAGE_CLEAN;
            case 7 -> ProcessPolicy.REASON_SWIPE_UP_CLEAN;
            case 10 -> ProcessPolicy.REASON_USER_DEFINED;
            case 11 -> ProcessPolicy.REASON_AUTO_POWER_KILL;
            case 12 -> ProcessPolicy.REASON_AUTO_THERMAL_KILL;
            case 13 -> ProcessPolicy.REASON_AUTO_IDLE_KILL;
            case 14 -> ProcessPolicy.REASON_AUTO_SLEEP_CLEAN;
            case 16 -> ProcessPolicy.REASON_AUTO_SYSTEM_ABNORMAL_CLEAN;
            case 19 -> ProcessPolicy.REASON_AUTO_THERMAL_KILL_ALL_LEVEL_1;
            case 20 -> ProcessPolicy.REASON_AUTO_THERMAL_KILL_ALL_LEVEL_2;
            case 22 -> ProcessPolicy.REASON_SCREEN_OFF_CPU_CHECK_KILL;
            default -> ProcessPolicy.REASON_UNKNOWN;
        };
    }
}
