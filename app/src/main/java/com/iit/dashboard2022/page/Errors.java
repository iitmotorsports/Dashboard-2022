package com.iit.dashboard2022.page;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.iit.dashboard2022.R;

import java.text.DateFormat;
import java.util.Date;

public class Errors extends Page {

    private TextView errorText;
    private ScrollView errorScroller;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.tab_error_layout, container, false);
        errorText = rootView.findViewById(R.id.errorText);
        errorScroller = rootView.findViewById(R.id.errorScroller);
        return rootView;
    }

    public void postError(@NonNull String tag, @NonNull CharSequence msg) {
        errorText.post(() -> {
            String epochStr = DateFormat.getTimeInstance().format(new Date());
            errorText.append(TextUtils.concat(epochStr + " [" + tag + "] ", msg, "\n"));
            errorScroller.fullScroll(ScrollView.FOCUS_DOWN);
        });
    }

    @NonNull
    @Override
    public String getTitle() {
        return "Errors";
    }
}
