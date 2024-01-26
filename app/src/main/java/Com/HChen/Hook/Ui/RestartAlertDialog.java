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
package Com.HChen.Hook.Ui;

import android.content.Context;
import android.content.res.Resources;

import java.util.Arrays;
import java.util.List;

import Com.HChen.Hook.Base.MultipleChoiceView;
import Com.HChen.Hook.R;
import Com.HChen.Hook.Utils.ShellUtils;
import moralnorm.appcompat.app.AlertDialog;

public class RestartAlertDialog extends AlertDialog {

    List<String> mAppNameList;
    List<String> mAppPackageNameList;

    public RestartAlertDialog(Context context) {
        super(context);
        setView(createMultipleChoiceView(context));
    }

    private MultipleChoiceView createMultipleChoiceView(Context context) {
        Resources mRes = context.getResources();
        MultipleChoiceView view = new MultipleChoiceView(context);
        /*设置应用名称*/
        mAppNameList = Arrays.asList(mRes.getStringArray(R.array.restart_apps_name));
        /*获取应用包名*/
        mAppPackageNameList = Arrays.asList(mRes.getStringArray(R.array.restart_apps_packagename));
        view.setData(mAppNameList, null);
        view.deselectAll();
        view.setOnCheckedListener(sparseBooleanArray -> {
            dismiss();
            for (int i = 0; i < sparseBooleanArray.size(); i++) {
                if (sparseBooleanArray.get(i)) {
                    if (mAppPackageNameList.get(i).equals("reboot")) {
                        ShellUtils.RootCommand("reboot");
                    } else {
                        ShellUtils.RootCommand("killall " + mAppPackageNameList.get(i));
                    }
                }
            }
        });
        return view;
    }
}
