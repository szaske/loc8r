package com.loc8r.seattle.utils;

import android.app.Dialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.Window;
import android.view.WindowManager;

import com.loc8r.seattle.R;

/**
 * Helper class to get a progress bar dialog
 */

public class ProgressDialog
{
    public static Dialog getDialog(@NonNull Context context, boolean cancelable)
    {
        Dialog dialog = new Dialog(context, R.style.DialogTransparentBackgroundTheme);
        dialog.setCancelable(cancelable);
        dialog.setContentView(R.layout.progress_dialog);
        Window window = dialog.getWindow();
        if (window != null)
        {
            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
        }
        return dialog;
    }
}
