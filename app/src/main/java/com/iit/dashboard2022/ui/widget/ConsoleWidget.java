package com.iit.dashboard2022.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.iit.dashboard2022.R;


public class ConsoleWidget extends ConstraintLayout {

    private final TextView text;

    public ConsoleWidget(@NonNull Context context) {
        this(context, null);
    }

    public ConsoleWidget(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ConsoleWidget(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        View.inflate(context, R.layout.widget_console, this);
        text = findViewById(R.id.consoleText);
    }

    public void post(CharSequence msg) {
        if (text.getLineCount() > 1000) { // TODO: Move to handler
            text.setText("");
        }
        text.append(msg);
    }

}
