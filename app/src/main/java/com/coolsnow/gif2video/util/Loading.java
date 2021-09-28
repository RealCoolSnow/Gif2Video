package com.coolsnow.gif2video.util;

import android.content.Context;

import com.coolsnow.gif2video.R;
import com.kaopiz.kprogresshud.KProgressHUD;


public class Loading {
    private KProgressHUD dialog;

    public static Loading getInstance() {
        return Holder.instance;
    }

    public void show(Context context, String text, boolean cancellable) {
        try {
            dialog = KProgressHUD.create(context)
                    .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                    .setLabel(text)
                    .setCancellable(cancellable)
                    .show();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public void show(Context context, String text) {
        show(context, text, true);
    }

    public void show(Context context) {
        show(context, context.getString(R.string.loading));
    }

    public void setMessage(String text) {
        if (dialog != null) {
            dialog.setLabel(text);
        }
    }

    public void setCancellable(boolean cancellable) {
        if (dialog != null) {
            dialog.setCancellable(cancellable);
        }
    }

    public void dismiss() {
        if (dialog != null) {
            try {
                dialog.dismiss();
            } catch (Throwable e) {
                
            }
            dialog = null;
        }
    }

    private static class Holder {
        private static Loading instance = new Loading();
    }
}
