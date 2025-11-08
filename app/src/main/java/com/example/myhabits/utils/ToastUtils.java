package com.example.myhabits.utils;

import android.content.Context;
import android.view.Gravity;
import android.widget.Toast;
import android.os.Handler;
import android.os.Looper;

public class ToastUtils {
    private static Toast currentToast;
    private static final int OFFSET_Y = 150;

    public static void showToast(final Context context, final String message) {
        if (currentToast != null) {
            currentToast.cancel();
        }

        try {
            // Create and configure new toast
            currentToast = Toast.makeText(context.getApplicationContext(), message, Toast.LENGTH_SHORT);
            currentToast.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, OFFSET_Y);

            // Show toast
            currentToast.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void showToastAndFinish(final Context context, final String message, final Runnable finishCallback) {
        if (currentToast != null) {
            currentToast.cancel();
        }

        try {
            currentToast = Toast.makeText(context.getApplicationContext(), message, Toast.LENGTH_SHORT);
            currentToast.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, OFFSET_Y);
            currentToast.show();

            // Delay finish callback
            new android.os.Handler(Looper.getMainLooper()).postDelayed(finishCallback, 200);
        } catch (Exception e) {
            e.printStackTrace();
            // If there's an error, still execute the finish callback
            finishCallback.run();
        }
    }

}