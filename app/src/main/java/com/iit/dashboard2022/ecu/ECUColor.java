package com.iit.dashboard2022.ecu;

import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;

import androidx.annotation.ColorInt;
import androidx.annotation.WorkerThread;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.Hashtable;

public class ECUColor {
    private static final Hashtable<String, Spannable> colorMsgMemo = new Hashtable<>();

    public static Spannable getColoredString(String string, @ColorInt int color) {
        Spannable spannable = new SpannableString(string);
        spannable.setSpan(new ForegroundColorSpan(color), 0, spannable.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return spannable;
    }

    private enum MsgType {
        INFO("[INFO] ", Color.WHITE),
        DEBUG("[DEBUG]", Color.DKGRAY),
        ERROR("[ERROR]", Color.RED),
        WARN("[WARN] ", Color.YELLOW),
        FATAL("[FATAL]", Color.MAGENTA),
        LOG("[ LOG ]", Color.LTGRAY);

        String key;
        int color;

        MsgType(String key, @ColorInt int color) {
            this.key = key;
            this.color = color;
        }
    }

    @ColorInt
    public static int getMsgColor(String msg) {
        for (MsgType p : MsgType.values()) {
            if (msg.contains(p.key))
                return p.color;
        }
        return Color.LTGRAY;
    }

    @WorkerThread
    public static Spannable colorMsgString(String msg) {
        SpannableStringBuilder spannable = new SpannableStringBuilder();
        new BufferedReader(new StringReader(msg)).lines().forEachOrdered((line) -> {
            Spannable lineSpan = colorMsgMemo.get(line);
            if (lineSpan == null) {
                lineSpan = getColoredString(line + "\n", getMsgColor(line));
                colorMsgMemo.put(line, lineSpan);
            }
            spannable.append(lineSpan);
        });
        return spannable;
    }

}
