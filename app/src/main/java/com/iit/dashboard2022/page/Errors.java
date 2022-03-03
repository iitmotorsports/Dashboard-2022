package com.iit.dashboard2022.page;

import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.iit.dashboard2022.R;
import com.iit.dashboard2022.ui.UITester;

import java.text.DateFormat;
import java.util.Date;

public class Errors extends Page implements UITester.TestUI {

    private TextView errorText;
    private ScrollView errorScroller;

    private int ErrorColor, WarnColor;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.tab_error_layout, container, false);
        errorText = rootView.findViewById(R.id.errorText);
        errorScroller = rootView.findViewById(R.id.errorScroller);
        ErrorColor = rootView.getContext().getColor(R.color.red);
        WarnColor = rootView.getContext().getColor(R.color.yellow);

        UITester.addTest(this);
        return rootView;
    }

    public void postError(@NonNull String tag, @NonNull CharSequence msg) {
        String epochStr = DateFormat.getTimeInstance().format(new Date());
        SpannableStringBuilder spannable = new SpannableStringBuilder(TextUtils.concat(epochStr, " ERROR: [", tag, "] ", msg, "\n"));
        spannable.setSpan(new ForegroundColorSpan(ErrorColor), 0, spannable.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        errorText.post(() -> {
            errorText.append(spannable);
            errorScroller.fullScroll(ScrollView.FOCUS_DOWN);
        });
    }

    public void postWarning(@NonNull String tag, @NonNull CharSequence msg) {
        String epochStr = DateFormat.getTimeInstance().format(new Date());
        SpannableStringBuilder spannable = new SpannableStringBuilder(TextUtils.concat(epochStr, " WARNING: [", tag, "] ", msg, "\n"));
        spannable.setSpan(new ForegroundColorSpan(WarnColor), 0, spannable.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        errorText.post(() -> {
            errorText.append(spannable);
            errorScroller.fullScroll(ScrollView.FOCUS_DOWN);
        });
    }

    public void clear() {
        errorText.post(() -> errorText.setText(""));
    }

    @NonNull
    @Override
    public String getTitle() {
        return "Errors";
    }

    @Override
    public void testUI(float percent) {
        if (percent == 0) {
            clear();
        } else if (percent > 0.8f) {
            postError(UITester.rndStr(4), UITester.rndStr(8));
            postWarning(UITester.rndStr(4), UITester.rndStr(8));
        }
    }
}
