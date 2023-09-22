package Com.HChen.Hook.Base;

import android.content.Context;

import moralnorm.appcompat.app.AlertDialog;

public class AlertDialogFactory {

    public void makeAlertDialog(Context context, String mTitle, String mMessage, Runnable mCode, Runnable mCodeT) {
        new AlertDialog.Builder(context)
            .setCancelable(true)
            .setTitle(mTitle)
            .setMessage(mMessage)
            .setHapticFeedbackEnabled(true)
            .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                if (mCode != null) mCode.run();
            })
            .setNegativeButton(android.R.string.cancel, (dialog, which) -> {
                if (mCodeT != null) mCodeT.run();
            })
            .show();
    }

    public void makeAlertDialog(Context context, String mTitle, String mMessage, Runnable mCode) {
        new AlertDialog.Builder(context)
            .setCancelable(true)
            .setTitle(mTitle)
            .setMessage(mMessage)
            .setHapticFeedbackEnabled(true)
            .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                if (mCode != null) mCode.run();
            })
            .show();
    }

    public void makeAlertDialog(Context context, String mTitle, String mMessage, Runnable mCodeT, boolean needT) {
        new AlertDialog.Builder(context)
            .setCancelable(true)
            .setTitle(mTitle)
            .setMessage(mMessage)
            .setHapticFeedbackEnabled(true)
            .setNegativeButton(android.R.string.cancel, (dialog, which) -> {
                if (mCodeT != null) mCodeT.run();
            })
            .show();
    }
}
