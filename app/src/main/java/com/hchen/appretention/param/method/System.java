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
package com.hchen.appretention.param.method;

public final class System {
    public static final String checkExcessivePowerUsage = "checkExcessivePowerUsage";
    public static final String checkExcessivePowerUsageLPr = "checkExcessivePowerUsageLPr";
    public static final String pruneStaleProcessesLocked = "pruneStaleProcessesLocked";
    public static final String getOrCreatePhantomProcessIfNeededLocked = "getOrCreatePhantomProcessIfNeededLocked";
    public static final String killLocked = "killLocked";
    public static final String handleDeviceIdle = "handleDeviceIdle";
    public static final String onSystemReady = "onSystemReady";
    public static final String unfreezeAppInternalLSP = "unfreezeAppInternalLSP";
    public static final String getBinderFreezeInfo = "getBinderFreezeInfo";
    public static final String killProcessesBelowAdj = "killProcessesBelowAdj";
    public static final String performIdleMaintenance = "performIdleMaintenance";
    public static final String getAppStartModeLOSP = "getAppStartModeLOSP";
    public static final String stopInBackgroundLocked = "stopInBackgroundLocked";
    public static final String doLowMemReportIfNeededLocked = "doLowMemReportIfNeededLocked";
    public static final String getLastMemoryLevelLocked = "getLastMemoryLevelLocked";
    public static final String isLastMemoryLevelNormal = "isLastMemoryLevelNormal";
    public static final String getMemFactor = "getMemFactor";
    public static final String isAvailable = "isAvailable";
    public static final String getMemFactorLocked = "getMemFactorLocked";
    public static final String trimMemoryUiHiddenIfNecessaryLSP = "trimMemoryUiHiddenIfNecessaryLSP";
    public static final String killAppIfBgRestrictedAndCachedIdleLocked = "killAppIfBgRestrictedAndCachedIdleLocked";
    public static final String shouldNotKillOnBgRestrictedAndIdle = "shouldNotKillOnBgRestrictedAndIdle";
    public static final String updateBackgroundRestrictedForUidPackageLocked = "updateBackgroundRestrictedForUidPackageLocked";
    public static final String runKillAll = "runKillAll";
    public static final String shouldKillExcessiveProcesses = "shouldKillExcessiveProcesses";
    public static final String updateAndTrimProcessLSP = "updateAndTrimProcessLSP";
    public static final String trimPhantomProcessesIfNecessary = "trimPhantomProcessesIfNecessary";
    public static final String trimInactiveRecentTasks = "trimInactiveRecentTasks";
    public static final String isInVisibleRange = "isInVisibleRange";
    public static final String run = "run";
    public static final String waitForPressure = "waitForPressure";
    public static final String killPids = "killPids";
    public static final String killAllBackgroundProcesses = "killAllBackgroundProcesses";
    public static final String killAllBackgroundProcessesExceptLSP = "killAllBackgroundProcessesExceptLSP";
    public static final String setOomAdj = "setOomAdj";
    public static final String killAllBackgroundProcessesExcept = "killAllBackgroundProcessesExcept";
    public static final String trimApplicationsLocked = "trimApplicationsLocked";
    public static final String getDefaultMaxCachedProcesses = "getDefaultMaxCachedProcesses";
    public static final String updateMaxCachedProcesses = "updateMaxCachedProcesses";
    public static final String getOverrideMaxCachedProcesses = "getOverrideMaxCachedProcesses";
    public static final String updateKillBgRestrictedCachedIdle = "updateKillBgRestrictedCachedIdle";
    public static final String updateLowMemStateLSP = "updateLowMemStateLSP";
    public static final String setProcessMemoryTrimLevel = "setProcessMemoryTrimLevel";
    public static final String setMemFactorOverride = "setMemFactorOverride";
    public static final String killProcessesBelowForeground = "killProcessesBelowForeground";
    public static final String setOverrideMaxCachedProcesses = "setOverrideMaxCachedProcesses";
    public static final String isKilledByAm = "isKilledByAm";


}
