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

import com.hchen.hooktool.BaseHC;
import com.hchen.processor.HookEntrance;

/**
 * 安卓 13
 *
 * @author 焕晨HChen
 */
@HookEntrance(targetPackage = "android", targetSdk = 33)
public class AndroidT extends BaseHC {
    @Override
    public void init() {
    }

    @Override
    public void copy() {
    }
}
