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
package com.hchen.appretention.data.method;

public class Hyper {
    public static final String resumeMemLeak = "resumeMemLeak";
    public static final String reclaimMemoryForGameIfNeed = "reclaimMemoryForGameIfNeed";
    public static final String reclaimBackground = "reclaimBackground";
    public static final String isMiuiLiteVersion = "isMiuiLiteVersion";
    public static final String getDeviceLevelForRAM = "getDeviceLevelForRAM";
    public static final String getBackgroundAppCount = "getBackgroundAppCount";
    public static final String doClean = "doClean";
    public static final String doFgTrim = "doFgTrim";
    public static final String handleScreenOff = "handleScreenOff";
    public static final String reportCleanProcess = "reportCleanProcess";
    public static final String reportStartProcess = "reportStartProcess";
    public static final String checkUnused = "checkUnused";
    public static final String killProcess = "killProcess";
    public static final String killProcessByMinAdj = "killProcessByMinAdj";
    public static final String checkBackgroundAppException = "checkBackgroundAppException";
    public static final String cleanUpMemory = "cleanUpMemory";
    public static final String killPackage = "killPackage";
    public static final String checkAndFreeze = "checkAndFreeze";
    public static final String updateScreenState = "updateScreenState";
    public static final String nStartPressureMonitor = "nStartPressureMonitor";
    public static final String powerKillProcess = "powerKillProcess";
    public static final String foregroundActivityChangedLocked = "foregroundActivityChangedLocked";
    public static final String onReceive = "onReceive";
    public static final String performCompaction = "performCompaction";
    public static final String killBackgroundApps = "killBackgroundApps";
    public static final String handleLimitCpuException = "handleLimitCpuException";
    public static final String onStartJob = "onStartJob";
    public static final String reclaimMemoryForCamera = "reclaimMemoryForCamera";
    public static final String callStaticMethod = "callStaticMethod";
    public static final String boostCameraIfNeeded = "boostCameraIfNeeded";
    public static final String doReclaimMemory = "doReclaimMemory";
    public static final String interceptAppRestartIfNeeded = "interceptAppRestartIfNeeded";
    public static final String sendDataToLmkd = "sendDataToLmkd";
    public static final String closeLmkdSocket = "closeLmkdSocket";
    public static final String changeProcessMemCgroup = "changeProcessMemCgroup";
    public static final String killApplication = "killApplication";
    public static final String doAdjBoost = "doAdjBoost";
}