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
package Com.HChen.Hook.Base;

import android.content.Context;

import moralnorm.appcompat.app.AlertDialog;

public class AlertDialogFactory {

    public static void makeAlertDialog(Context context, String mTitle, String mMessage,
                                       Runnable mCodeOk, Runnable mCodeCancel, boolean setCancelable, int num) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setCancelable(setCancelable);
        builder.setTitle(mTitle);
        builder.setMessage(mMessage);
        builder.setHapticFeedbackEnabled(true);
        switch (num) {
            case 1 -> {
                builder.setPositiveButton(android.R.string.ok, (dialog, which) -> {
                    if (mCodeOk != null) mCodeOk.run();
                });
            }
            case 2 -> {
                builder.setPositiveButton(android.R.string.ok, (dialog, which) -> {
                    if (mCodeOk != null) mCodeOk.run();
                });
                builder.setNegativeButton(android.R.string.cancel, (dialog, which) -> {
                    if (mCodeCancel != null) mCodeCancel.run();
                });
            }
            case 3 -> {
                builder.setNegativeButton(android.R.string.cancel, (dialog, which) -> {
                    if (mCodeCancel != null) mCodeCancel.run();
                });
            }
            case 4 -> {

            }
        }
        builder.show();
    }
}
