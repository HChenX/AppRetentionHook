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
package com.hchen.appretention.data.method;

/**
 * 系统框架方法名
 *
 * @author 焕晨HChen
 */
public class System {
    public static final String killProcessesWhenImperceptible = "killProcessesWhenImperceptible";
    public static final String killProcessLocked = "killProcessLocked";
    public static final String onLmkdConnect = "onLmkdConnect";
    public static final String updateOomLevels = "updateOomLevels";
    public static final String updateOomMinFree = "updateOomMinFree";
    public static final String getMemLevel = "getMemLevel";
    public static final String writeLmkd = "writeLmkd";
    public static final String updateProcessCpuStatesLocked = "updateProcessCpuStatesLocked";
    public static final String trimPhantomProcessesIfNecessary = "trimPhantomProcessesIfNecessary";
    public static final String checkExcessivePowerUsageLPr = "checkExcessivePowerUsageLPr";
    public static final String performIdleMaintenance = "performIdleMaintenance";
    public static final String updateLowMemStateLSP = "updateLowMemStateLSP";
    public static final String getMemFactor = "getMemFactor";
    public static final String isAvailable = "isAvailable";
    public static final String shouldKillExcessiveProcesses = "shouldKillExcessiveProcesses";
    public static final String updateAndTrimProcessLSP = "updateAndTrimProcessLSP";
    public static final String trimInactiveRecentTasks = "trimInactiveRecentTasks";
    public static final String isInVisibleRange = "isInVisibleRange";
    public static final String onOomAdjustChanged = "onOomAdjustChanged";
    public static final String resolveCompactionProfile = "resolveCompactionProfile";
    public static final String getSetAdj = "getSetAdj";
    public static final String getCurAdj = "getCurAdj";
    public static final String getSetProcState = "getSetProcState";
    public static final String setReqCompactProfile = "setReqCompactProfile";
    public static final String setReqCompactSource = "setReqCompactSource";
    public static final String hasPendingCompact = "hasPendingCompact";
    public static final String setHasPendingCompact = "setHasPendingCompact";
    public static final String shouldOomAdjThrottleCompaction = "shouldOomAdjThrottleCompaction";
    public static final String interruptProcCompaction = "interruptProcCompaction";
    public static final String setAppStartingMode = "setAppStartingMode";
    public static final String shouldThrottleMiscCompaction = "shouldThrottleMiscCompaction";
    public static final String shouldTimeThrottleCompaction = "shouldTimeThrottleCompaction";
    public static final String getLastCompactTime = "getLastCompactTime";
    public static final String shouldRssThrottleCompaction = "shouldRssThrottleCompaction";
    public static final String updateUseCompaction = "updateUseCompaction";
    public static final String isAlive = "isAlive";
    public static final String start = "start";
    public static final String setProperty = "setProperty";
    public static final String setThreadGroupAndCpuset = "setThreadGroupAndCpuset";
    public static final String getThreadId = "getThreadId";
    public static final String updateKillBgRestrictedCachedIdle = "updateKillBgRestrictedCachedIdle";
    public static final String updateUseModernTrim = "updateUseModernTrim";
    public static final String updateProactiveKillsEnabled = "updateProactiveKillsEnabled";
    public static final String updateMaxCachedProcesses = "updateMaxCachedProcesses";
    public static final String updateMaxPhantomProcesses = "updateMaxPhantomProcesses";
    public static final String updatePerfConfigConstants = "updatePerfConfigConstants";
    public static final String performReceive = "performReceive";
    public static final String handleAppCrashInActivityController = "handleAppCrashInActivityController";
}
