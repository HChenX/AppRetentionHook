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
