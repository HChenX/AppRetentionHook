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
package Com.HChen.Hook.param.classpath;

public final class SystemName {
    public static final String ActivityManagerService = "com.android.server.am.ActivityManagerService";
    public static final String ProcessRecord = "com.android.server.am.ProcessRecord";
    public static final String CachedAppOptimizer = "com.android.server.am.CachedAppOptimizer";
    public static final String ProcessList = "com.android.server.am.ProcessList";
    public static final String ProcessStateRecord = "com.android.server.am.ProcessStateRecord";
    public static final String ActivityManagerShellCommand = "com.android.server.am.ActivityManagerShellCommand";
    public static final String ProcessList$ImperceptibleKillRunner = "com.android.server.am.ProcessList$ImperceptibleKillRunner";

    public static final String OomAdjuster = "com.android.server.am.OomAdjuster";
    public static final String AmsExtImpl = "com.mediatek.server.am.AmsExtImpl";
    public static final String PhantomProcessList = "com.android.server.am.PhantomProcessList";
    public static final String RecentTasks = "com.android.server.wm.RecentTasks";
    public static final String LowMemDetector = "com.android.server.am.LowMemDetector";
    public static final String ProcessStatsService = "com.android.server.am.ProcessStatsService";

    public static final String ActivityManagerConstants = "com.android.server.am.ActivityManagerConstants";
    public static final String ActivityManagerServiceStub = "com.android.server.am.ActivityManagerServiceStub";

    public static final String AppProfiler = "com.android.server.am.AppProfiler";

}
