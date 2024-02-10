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
package Com.HChen.Hook.mode;

import Com.HChen.Hook.hook.android.SystemService;
import Com.HChen.Hook.hook.color.AthenaApp;
import Com.HChen.Hook.hook.color.AthenaKill;
import Com.HChen.Hook.hook.color.OplusBattery;
import Com.HChen.Hook.hook.color.OplusService;
import Com.HChen.Hook.hook.miui.MiuiService;
import Com.HChen.Hook.hook.miui.PowerKeeper;
import Com.HChen.Hook.mode.dexkit.DexKit;
import Com.HChen.Hook.mode.log.HookLog;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class HookInit extends HookLog {
    public static final String TAG = "HookRun";

    public static void HookPackage(LoadPackageParam loadPackageParam) {
        init(loadPackageParam);
    }

    public static void init(LoadPackageParam param) {
        switch (param.packageName) {
            case "android" -> {
                initHook(new SystemService(), param);
                initHook(new MiuiService(), param);
                initHook(new OplusService(), param);
            }
            case "com.miui.powerkeeper" -> {
                initHook(new PowerKeeper(), param);
            }
            case "com.oplus.athena" -> {
                AthenaApp.getPid(AthenaKill.init(param));
                DexKit.INSTANCE.initDexKit(param);
                initHook(new AthenaApp(), param);
                DexKit.INSTANCE.closeDexKit();
            }
            case "com.oplus.battery" -> initHook(new OplusBattery(), param);
        }
    }

    public static void initHook(Hook hook, LoadPackageParam param) {
        hook.runHook(param);
    }
}
