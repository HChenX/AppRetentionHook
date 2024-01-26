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
package Com.HChen.Hook.Hook;

import static Com.HChen.Hook.Param.Name.OplusName.OplusOsenseKillAction;
import static Com.HChen.Hook.Param.Value.OplusValue.killLocked;

import Com.HChen.Hook.Mode.HookMode;

public class OplusService extends HookMode {
    public static String name = "OplusService";

    @Override
    public void init() {
        hookAllMethods(OplusOsenseKillAction, killLocked,
            new HookAction() {
                @Override
                public String hookLog() {
                    return name;
                }

                @Override
                protected void before(MethodHookParam param) {
                    param.setResult(null);
                }
            }
        );
    }
}
