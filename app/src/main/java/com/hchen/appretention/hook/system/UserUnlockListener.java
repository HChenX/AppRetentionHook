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
package com.hchen.appretention.hook.system;

import static com.hchen.appretention.data.method.System.performReceive;
import static com.hchen.appretention.data.path.System.UserController$3;
import static com.hchen.appretention.log.LogToFile.USER_UNLOCKED_COMPLETED_PROP;

import android.content.Intent;
import android.os.Bundle;

import com.hchen.hooktool.BaseHC;
import com.hchen.hooktool.hook.IHook;
import com.hchen.hooktool.log.AndroidLog;
import com.hchen.hooktool.tool.additional.SystemPropTool;
import com.hchen.processor.HookCondition;

/**
 * 监听用户重启后解锁的事件
 *
 * @author 焕晨HChen
 */
@HookCondition(targetPackage = "android")
public class UserUnlockListener extends BaseHC {
    @Override
    public void init() {
        SystemPropTool.setProp(USER_UNLOCKED_COMPLETED_PROP, "false");
        hookMethod(UserController$3,
            performReceive,
            Intent.class, int.class, String.class, Bundle.class, boolean.class, boolean.class, int.class,
            new IHook() {
                @Override
                public void after() {
                    SystemPropTool.setProp(USER_UNLOCKED_COMPLETED_PROP, "true");
                    AndroidLog.logI(TAG, "user unlocked completed!!!!");
                }
            }
        );
    }
}
