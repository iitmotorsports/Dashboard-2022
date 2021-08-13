package com.iit.dashboard2022.ui.widget;

import android.content.Context;
import android.content.res.ColorStateList;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import com.iit.dashboard2022.R;
import com.iit.dashboard2022.ui.UITester;

public class StartLight extends LinearLayout implements UITester.TestUI {
    private final TextView currentState;
    private final RadioButton startLight;
    private final ColorStateList colorOn, colorOff;
    private final CharSequence defaultText;

    public StartLight(Context context) {
        this(context, null);
    }

    public StartLight(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public StartLight(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        View.inflate(context, R.layout.widget_start_light, this);

        defaultText = context.getText(R.string.current_state);

        currentState = findViewById(R.id.currentState);
        startLight = findViewById(R.id.startLight);

        colorOn = context.getColorStateList(R.color.green);
        colorOff = context.getColorStateList(R.color.backgroundText);

        UITester.addTest(this);
    }

    public void light(boolean isOn) {
        startLight.setButtonTintList(isOn ? colorOn : colorOff);
    }

    public void setState(CharSequence state) {
        currentState.setText(state);
    }

    @Override
    protected void finalize() throws Throwable {
        UITester.removeTest(this);
        super.finalize();
    }

    @Override
    public void testUI(float percent) {
        light(percent % 0.2 > 0.1f);
        if (percent == 0)
            setState(defaultText);
        else
            setState(UITester.rndStr((int) (percent * 25)));
    }
}
