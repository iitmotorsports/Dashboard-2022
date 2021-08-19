package com.iit.dashboard2022.ecu;

import android.content.Context;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;

import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;

import com.iit.dashboard2022.R;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.Hashtable;

public class ECUColor {
    private static final Hashtable<String, MsgMemo> msgMemo = new Hashtable<>();

    private enum MsgType {
        INFO("[INFO] ", R.color.foreground),
        DEBUG("[DEBUG]", R.color.midground),
        ERROR("[ERROR]", R.color.red),
        WARN("[WARN] ", R.color.yellow),
        FATAL("[FATAL]", R.color.magenta),
        LOG("[ LOG ]", R.color.green);

        String key;
        @ColorRes
        int color;

        MsgType(String key, @ColorRes int color) {
            this.key = key;
            this.color = color;
        }
    }

    private static class MsgMemo {
        MsgType type;
        SpannableStringBuilder spannable;

        public MsgMemo(MsgType type, SpannableStringBuilder spannable) {
            this.type = type;
            this.spannable = spannable;
        }
    }

    @NonNull
    private static MsgType getMsgType(@NonNull String msg) {
        for (MsgType p : MsgType.values()) {
            if (msg.contains(p.key)) {
                return p;
            }
        }
        return MsgType.LOG;
    }

    @NonNull
    private static SpannableStringBuilder colorSpannableType(Context context, @NonNull SpannableStringBuilder spannable, @NonNull MsgType type) {
        spannable.setSpan(new ForegroundColorSpan(context.getColor(type.color)), 0, spannable.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return spannable;
    }

    @NonNull
    private static SpannableStringBuilder getMemoSpannable(@NonNull Context context, @NonNull String msg) {
        MsgMemo memo = msgMemo.get(msg);
        if (memo == null) {
            SpannableStringBuilder spannable = new SpannableStringBuilder(msg);
            memo = new MsgMemo(getMsgType(msg), spannable);
            msgMemo.put(msg, memo);
        }
        return colorSpannableType(context, memo.spannable, memo.type);
    }

    @NonNull
    private static CharSequence getTrimmedSpannable(@NonNull Context context, @NonNull String msg) {
        String trimmed = msg.replaceAll("\\d*$", "");
        String number = msg.substring(trimmed.length());
        SpannableStringBuilder memo = getMemoSpannable(context, trimmed);
        return TextUtils.concat(memo, number, "\n");
    }

    @WorkerThread
    public static Spannable colorMsgString(@NonNull Context context, @NonNull String msg) {
        SpannableStringBuilder spannable = new SpannableStringBuilder();
        new BufferedReader(new StringReader(msg)).lines().forEachOrdered((line) -> {
            Spannable lineSpan = getMemoSpannable(context, line + "\n"); // TODO: remove the trailing number for memo, then re-add it
            spannable.append(lineSpan);
        });
        msgMemo.clear();
        return spannable;
    }

}
