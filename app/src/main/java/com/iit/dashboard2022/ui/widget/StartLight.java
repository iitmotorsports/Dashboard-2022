package com.iit.dashboard2022.ui.widget;

import android.content.Context;
import android.content.res.ColorStateList;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.AttrRes;

import com.iit.dashboard2022.R;

public class StartLight extends FrameLayout {
    private final TextView currentState;
    private final RadioButton startLight;
    private final ColorStateList colorOn, colorOff;

    public StartLight(Context context) {
        this(context, null);
    }

    public StartLight(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public StartLight(Context context, AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        View.inflate(context, R.layout.widget_start_light, this);

        currentState = findViewById(R.id.currentState);
        startLight = findViewById(R.id.startLight);

        colorOn = context.getColorStateList(R.color.green);
        colorOff = context.getColorStateList(R.color.midground);
    }

    public void setLight(boolean isOn) {
        startLight.setButtonTintList(isOn ? colorOn : colorOff);
    }

    public void setState(CharSequence state) {
        post(() -> currentState.setText(state));
    }

}
