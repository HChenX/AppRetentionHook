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
package com.hchen.appretention.hook.miui;

import static com.hchen.appretention.param.classpath.Miui.ProcessManager;
import static com.hchen.appretention.param.method.Miui.kill;

import com.hchen.appretention.mode.Hook;

public class PowerKeeper extends Hook {
    public static String name = "PowerKeeper";

    @Override
    public void init() {
        hookAllMethods(ProcessManager,
            kill,
            new HookAction(name) {
                @Override
                protected void before(MethodHookParam param) {
                    param.setResult(false);
                }
            }
        );
    }
}
