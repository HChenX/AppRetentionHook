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
package com.hchen.appretention.annotation.color;

import com.hchen.appretention.mode.Hook;

/**
 * 这些都是已经弃用的 Hook 方法，仅留档。
 */
public class AthenaApp extends Hook {
    @Override
    public void init() {
        // 旧实现
        /*HookDexKit.beforeDexKit(methodData, loadPackageParam,
            new ActionTiming() {
                @Override
                public void before(@NonNull XC_MethodHook.MethodHookParam param) {
                    String reason = (String) param.args[7];
                    // 不稳定
                    int code = (int) param.args[5];
                    if (!"oneclick".equals(reason)) {
                        param.setResult(false);
                    }
                }
            }
        );
        try {
            HookFactory.createMethodHook(methodData.getMethodInstance(loadPackageParam.classLoader),
                new Consumer<HookFactory>() {
                    @Override
                    public void accept(HookFactory hookFactory) {
                        hookFactory.before(
                            new callBack() {
                                @Override
                                public void hookMethod(@NonNull XC_MethodHook.MethodHookParam methodHookParam) {
                                    String reason = (String) methodHookParam.args[7];
                                    int code = (int) methodHookParam.args[4];
                                    if (!("oneclick".equals(reason) && code == 2)) {
                                        methodHookParam.setResult(false);
                                    }
                                }
                            }
                        );
                    }
                }
            );
        } catch (Throwable e) {
            logE("AthenaApp", "NoSuchMethodException :" + e);
        }*/

        /*HookDexKit.beforeDexKit(methodData1, loadPackageParam,
                new ActionTiming() {
                    @Override
                    public void before(@NonNull XC_MethodHook.MethodHookParam param) {
                        String reason = (String) param.args[6];
                        switch (reason) {
                            case "swipe up", "removeTask" -> {

                            }
                            default -> param.setResult(null);
                        }
                    }
                }
            );*/
        /*try {*/
           /* HookFactory.createMethodHook(methodData1.getMethodInstance(loadPackageParam.classLoader), new Consumer<HookFactory>() {
                @Override
                public void accept(HookFactory hookFactory) {
                    hookFactory.before(new callBack() {
                        @Override
                        public void hookMethod(@NonNull XC_MethodHook.MethodHookParam methodHookParam) {

                        }
                    });
                }
            });
        } catch (Throwable e) {
            logE("AthenaApp", "NoSuchMethodException :" + e);
        }*/
        /*与Boolean c重复*/
        /*MethodData methodData2 = DexKit.INSTANCE.getDexKitBridge().findMethod(
            FindMethod.create()
                .matcher(MethodMatcher.create()
                    .declaredClass(ClassMatcher.create()
                        .usingStrings("kill process group synchronously"))
                    .usingStrings("kill process group synchronously"))
        ).firstOrNull();
        HookDexKit.beforeDexKit(methodData2, loadPackageParam,
            new ActionTiming() {
                @Override
                public void before(@NonNull XC_MethodHook.MethodHookParam param) {

                }
            }
        );*/

         /*HookDexKit.beforeDexKit(methodData2, loadPackageParam,
                new ActionTiming() {
                    @Override
                    public void before(@NonNull XC_MethodHook.MethodHookParam param) {
                        param.setResult(null);
                    }
                }
            );*/
    }
}
