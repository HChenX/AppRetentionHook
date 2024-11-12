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
package com.hchen.appretention.hook.powerkeeper;

import static com.hchen.appretention.data.method.PowerKeeper.kill;
import static com.hchen.appretention.data.path.PowerKeeper.ProcessManager;

import com.hchen.hooktool.BaseHC;

/**
 * 禁止电量和性能杀后台
 *
 * @author 焕晨HChen
 */
public class PowerKeeper extends BaseHC {
    @Override
    public void init() {
        /*
         * 禁止电量和性能杀后台
         * */
        hookAllMethod(ProcessManager,
            kill,
            returnResult(false)
        );
    }

    @Override
    public void copy() {

    }
}
