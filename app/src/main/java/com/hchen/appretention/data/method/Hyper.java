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
 * Hyper 方法名
 *
 * @author 焕晨HChen
 */
public class Hyper {
    public static final String resumeMemLeak = "resumeMemLeak";
    public static final String reclaimMemoryForGameIfNeed = "reclaimMemoryForGameIfNeed";
    public static final String reclaimBackground = "reclaimBackground";
    public static final String isMiuiLiteVersion = "isMiuiLiteVersion";
    public static final String getDeviceLevelForRAM = "getDeviceLevelForRAM";
    public static final String getBackgroundAppCount = "getBackgroundAppCount";
    public static final String compactBackgroundProcess = "compactBackgroundProcess";
    public static final String doClean = "doClean";
    public static final String addMiuiPeriodicCleanerService = "addMiuiPeriodicCleanerService";
    public static final String doFgTrim = "doFgTrim";
    public static final String handleScreenOff = "handleScreenOff";
    public static final String reportCleanProcess = "reportCleanProcess";
    public static final String reportStartProcess = "reportStartProcess";
    public static final String checkUnused = "checkUnused";
    public static final String isEnable = "isEnable";
    public static final String init = "init";
    public static final String killProcess = "killProcess";
    public static final String killProcessByMinAdj = "killProcessByMinAdj";
    public static final String checkBackgroundAppException = "checkBackgroundAppException";
    public static final String isNeedCompact = "isNeedCompact";
    public static final String cleanUpMemory = "cleanUpMemory";
    public static final String killPackage = "killPackage";
    public static final String checkAndFreeze = "checkAndFreeze";
    public static final String updateScreenState = "updateScreenState";
    public static final String handleThermalKillProc = "handleThermalKillProc";
    public static final String handleKillAll = "handleKillAll";
    public static final String handleKillApp = "handleKillApp";
    public static final String getPolicy = "getPolicy";
    public static final String handleKillAny = "handleKillAny";
    public static final String handleAutoLockOff = "handleAutoLockOff";
    public static final String handleScreenOffEvent = "handleScreenOffEvent";
    public static final String powerFrozenAll = "powerFrozenAll";
    public static final String resetLockOffConfig = "resetLockOffConfig";
    public static final String nStartPressureMonitor = "nStartPressureMonitor";
    public static final String powerKillProcess = "powerKillProcess";
    public static final String foregroundActivityChangedLocked = "foregroundActivityChangedLocked";
    public static final String onReceive = "onReceive";
    public static final String performCompaction = "performCompaction";
    public static final String killBackgroundApps = "killBackgroundApps";
    public static final String handleLimitCpuException = "handleLimitCpuException";
    public static final String onStartJob = "onStartJob";
    public static final String preloadAppEnqueue = "preloadAppEnqueue";
    public static final String startPreloadApp = "startPreloadApp";
    public static final String newInstance = "newInstance";
    public static final String reclaimMemoryForCamera = "reclaimMemoryForCamera";
    public static final String updateCameraBoosterCloudData = "updateCameraBoosterCloudData";
    public static final String callStaticMethod = "callStaticMethod";
    public static final String boostCameraIfNeeded = "boostCameraIfNeeded";
    public static final String boostCameraByThreshold = "boostCameraByThreshold";
    public static final String doReclaimMemory = "doReclaimMemory";
    public static final String interceptAppRestartIfNeeded = "interceptAppRestartIfNeeded";
    public static final String isAllowAdjBoost = "isAllowAdjBoost";
    public static final String notifyCameraForegroundChange = "notifyCameraForegroundChange";
    public static final String notifyCameraForegroundState = "notifyCameraForegroundState";
    public static final String notifyCameraPostProcessState = "notifyCameraPostProcessState";
    public static final String notifyActivityChanged = "notifyActivityChanged";
    public static final String sendDataToLmkd = "sendDataToLmkd";
    public static final String closeLmkdSocket = "closeLmkdSocket";
    public static final String changeProcessMemCgroup = "changeProcessMemCgroup";
    public static final String killApplication = "killApplication";
    public static final String doAdjBoost = "doAdjBoost";
    public static final String ensureService = "ensureService";
}
