package com.iit.dashboard2022.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.iit.dashboard2022.R;
import com.iit.dashboard2022.ui.widget.SideRadio;
import com.iit.dashboard2022.ui.widget.SideSwitch;

public class SidePanel extends ConstraintLayout {
    private final SideRadio asciiRButton, hexRButton, rawRButton;
    private final SideSwitch uiTestSwitch, reverseSwitch, consoleSwitch;

    public SidePanel(Context context) {
        this(context, null);
    }

    public SidePanel(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SidePanel(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        View.inflate(getContext(), R.layout.side_panel_layout, this);

        asciiRButton = findViewById(R.id.asciiRButton);
        hexRButton = findViewById(R.id.hexRButton);
        rawRButton = findViewById(R.id.rawRButton);

        uiTestSwitch = findViewById(R.id.uiTestSwitch);
        reverseSwitch = findViewById(R.id.reverseSwitch);
        consoleSwitch = findViewById(R.id.consoleSwitch);

    }

    public void setUiTestSwitchListener(@Nullable View.OnClickListener listener) {
        uiTestSwitch.setOnClickListener(listener);
    }

    public void setReverseSwitchListener(@Nullable View.OnClickListener listener) {
        reverseSwitch.setOnClickListener(listener);
    }

    public void setConsoleSwitchListener(@Nullable View.OnClickListener listener) {
        consoleSwitch.setOnClickListener(listener);
    }

}
