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

import Com.HChen.Hook.Mode.HookMode;

public class TestHook extends HookMode {
    @Override
    public void init() {
        /*findAndHookMethod("Com.HChen.App.HChenMain$$ExternalSyntheticLambda6",
                "onClick", View.class,
                new HookAction() {
                    @Override
                    protected void after(MethodHookParam param) {
//                        View view = (View) param.args[0];
//                        Context context = (Context) param.args[1];
//                        view.setOnClickListener(v -> {
//                            Toast.makeText(context, "被Hook了", Toast.LENGTH_SHORT).show();
//                            logSI(1, "onClick");
//                        });
//                        param.setResult(null);
                        String Text = (String) XposedHelpers.getObjectField(param.thisObject, "Text");
                        logI(Text);
                    }
                }
        );
        findAndHookConstructor("Com.HChen.App.HChenMain",
                new HookAction() {
                    @Override
                    protected void after(MethodHookParam param) {
                        logI("Hooking");
                        getDeclaredField(param, "Text", "Hooking");
                    }
                }
        );*/
//        int get = SystemProperties.getInt("ro.miui.ui.version.code", 1);
//        logI("Hook ON! get prop: " + get);
    }
}
