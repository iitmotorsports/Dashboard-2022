package com.iit.dashboard2022.util;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;
import androidx.annotation.IntDef;
import es.dmoral.toasty.Toasty;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Toaster {
    private static final ConcurrentLinkedQueue<ToastMessage> queue = new ConcurrentLinkedQueue<>();
    private static final Handler uiHandle = new Handler(Looper.getMainLooper());
    private static boolean enable = true;
    private static Runnable newToast;

    public static void setContext(Context context) {
        Toasty.Config.getInstance().tintIcon(true).setTextSize(14).allowQueue(false).apply();
        newToast = () -> {
            Toast toast;
            ToastMessage tm = queue.poll();
            if (tm == null) {
                return;
            }
            switch (tm.status) {
                case NORMAL:
                    toast = Toasty.normal(context, tm.msg, tm.duration);
                    break;
                case INFO:
                    toast = Toasty.info(context, tm.msg, tm.duration);
                    break;
                case SUCCESS:
                    toast = Toasty.success(context, tm.msg, tm.duration);
                    break;
                case WARNING:
                    toast = Toasty.warning(context, tm.msg, tm.duration);
                    break;
                case ERROR:
                    toast = Toasty.error(context, tm.msg, tm.duration);
                    break;
                default:
                    return;
            }
            toast.setGravity(tm.gravity, tm.xOffset, 32);
            toast.show();
        };
    }

    public static void showToast(String msg) {
        showToast(msg, Status.NORMAL);
    }

    public static void showToast(String msg, Status status) {
        showToast(msg, status, Toast.LENGTH_SHORT, Gravity.CENTER);
    }

    public static void showToast(String msg, Status status, @ToasterDuration int duration) {
        showToast(msg, status, duration, Gravity.CENTER);
    }

    public static void showToast(String msg, Status status, @ToasterDuration int duration, @ToasterGravity int gravity) {
        Log.i("Toast", msg);
        if (!enable || newToast == null) {
            return;
        }

        int xOffset = 0;
        if (gravity == Gravity.START || gravity == Gravity.END) {
            xOffset = 32;
        }

        queue.add(new ToastMessage(duration, Gravity.BOTTOM | gravity, status, xOffset, msg));
        uiHandle.postDelayed(newToast, 20);
    }

    public static void setEnabled(boolean enable) {
        Toaster.enable = enable;
    }

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({ Toast.LENGTH_LONG, Toast.LENGTH_SHORT })
    @interface ToasterDuration {
    }

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({ Gravity.START, Gravity.CENTER, Gravity.END })
    @interface ToasterGravity {
    }

    public enum Status {
        NORMAL, INFO, SUCCESS, WARNING, ERROR
    }

    private static class ToastMessage {
        @ToasterDuration
        final
        int duration;
        @ToasterGravity
        final
        int gravity;
        final Status status;
        final int xOffset;
        final String msg;

        public ToastMessage(int duration, int gravity, Status status, int xOffset, String msg) {
            this.duration = duration;
            this.gravity = gravity;
            this.status = status;
            this.xOffset = xOffset;
            this.msg = msg;
        }

    }

}
