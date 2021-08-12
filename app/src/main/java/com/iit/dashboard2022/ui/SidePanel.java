package com.iit.dashboard2022.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.iit.dashboard2022.R;
import com.iit.dashboard2022.ui.widget.SideRadio;
import com.iit.dashboard2022.ui.widget.SideSwitch;
import com.iit.dashboard2022.ui.widget.SideToggle;

public class SidePanel extends ConstraintLayout {
    private final SideRadio asciiRButton, hexRButton, rawRButton;
    private final SideSwitch uiTestSwitch, reverseSwitch, consoleSwitch;
    private final SideToggle chargeToggle, JSONToggle, connToggle;

    public enum CheckableWidget { // TODO: better way of manipulating UI
        asciiRButton,
        hexRButton,
        rawRButton,
        uiTestSwitch,
        reverseSwitch,
        consoleSwitch,
        chargeToggle,
        JSONToggle,
        connToggle,
    }

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

        chargeToggle = findViewById(R.id.chargeToggle);
        JSONToggle = findViewById(R.id.JSONToggle);
        connToggle = findViewById(R.id.connToggle);

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

    private void setActionedCheck(SideSwitch checkable, boolean checked) {
        if (checkable.isChecked() == checked)
            return;
        checkable.performClick();
    }

    private void setActionedCheck(SideRadio checkable, boolean checked) {
        if (checkable.isChecked() == checked)
            return;
        checkable.performClick();
    }

    private void setActionedCheck(SideToggle checkable, boolean checked) {
        if (checkable.isChecked() == checked)
            return;
        checkable.performClick();
    }

    public void setChecked(CheckableWidget widget, boolean checked) {
        switch (widget) {
            case connToggle:
                setActionedCheck(connToggle, checked);
                break;
            case hexRButton:
                setActionedCheck(hexRButton, checked);
                break;
            case JSONToggle:
                setActionedCheck(JSONToggle, checked);
                break;
            case asciiRButton:
                setActionedCheck(asciiRButton, checked);
                break;
            case rawRButton:
                setActionedCheck(rawRButton, checked);
                break;
            case uiTestSwitch:
                setActionedCheck(uiTestSwitch, checked);
                break;
            case reverseSwitch:
                setActionedCheck(reverseSwitch, checked);
                break;
            case consoleSwitch:
                setActionedCheck(consoleSwitch, checked);
                break;
            case chargeToggle:
                setActionedCheck(chargeToggle, checked);
                break;
        }
    }

}
