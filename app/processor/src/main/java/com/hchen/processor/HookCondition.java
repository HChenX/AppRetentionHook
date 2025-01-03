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
package com.hchen.processor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Hook 执行条件
 *
 * @author 焕晨HChen
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface HookCondition {
    /**
     * 目标品牌
     */
    String targetBrand() default "Any";

    /**
     * 目标作用域
     */
    String targetPackage();

    /**
     * 目标安卓版本
     */
    int targetSdk() default 0;

    /**
     * 目标系统 ROM 版本
     */
    float targetOS() default -1f;
}
