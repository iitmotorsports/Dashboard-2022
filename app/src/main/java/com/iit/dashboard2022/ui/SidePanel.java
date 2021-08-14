package com.iit.dashboard2022.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RadioGroup;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.iit.dashboard2022.ECU.ECU;
import com.iit.dashboard2022.R;
import com.iit.dashboard2022.ui.anim.TranslationAnim;
import com.iit.dashboard2022.ui.widget.SideButton;
import com.iit.dashboard2022.ui.widget.SideRadio;
import com.iit.dashboard2022.ui.widget.SideSwitch;
import com.iit.dashboard2022.ui.widget.SideToggle;
import com.iit.dashboard2022.ui.widget.console.ConsoleWidget;

public class SidePanel extends ConstraintLayout {
    public final RadioGroup consoleRadioGroup;
    public final SideRadio asciiRadio, hexRadio, rawRadio;
    public final SideSwitch uiTestSwitch, reverseSwitch, consoleSwitch;
    public final SideToggle chargeToggle, JSONToggle, connToggle;
    public final SideButton clearConsoleButton, canMsgButton, canEchoButton;

    public SidePanel(Context context) {
        this(context, null);
    }

    public SidePanel(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SidePanel(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        View.inflate(getContext(), R.layout.side_panel_layout, this);

        consoleRadioGroup = findViewById(R.id.consoleRadioGroup);
        asciiRadio = findViewById(R.id.asciiRButton);
        hexRadio = findViewById(R.id.hexRButton);
        rawRadio = findViewById(R.id.rawRButton);

        chargeToggle = findViewById(R.id.chargeToggle);
        JSONToggle = findViewById(R.id.JSONToggle);
        connToggle = findViewById(R.id.connToggle);

        uiTestSwitch = findViewById(R.id.uiTestSwitch);
        reverseSwitch = findViewById(R.id.reverseSwitch);
        consoleSwitch = findViewById(R.id.consoleSwitch);

        clearConsoleButton = findViewById(R.id.clearConsoleButton);
        canMsgButton = findViewById(R.id.canMsgButton);
        canEchoButton = findViewById(R.id.canEchoButton);

        uiTestSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> UITester.enable(isChecked));
    }

    public void attachConsole(ConsoleWidget console, ECU frontECU) {
        TranslationAnim consoleAnim = new TranslationAnim(console, TranslationAnim.X_AXIS, TranslationAnim.ANIM_FORWARD);
        consoleAnim.startWhenReady();

        consoleSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                consoleAnim.reverse();
                console.enable(true);
            } else {
                consoleAnim.start();
                console.enable(false);
            }
        });

        consoleRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.asciiRButton) {
                console.setMode(ECU.MODE.ASCII.name());
                frontECU.setInterpreterMode(ECU.MODE.ASCII);
            } else if (checkedId == R.id.hexRButton) {
                console.setMode(ECU.MODE.HEX.name());
                frontECU.setInterpreterMode(ECU.MODE.HEX);
            } else if (checkedId == R.id.rawRButton) {
                console.setMode(ECU.MODE.RAW.name());
                frontECU.setInterpreterMode(ECU.MODE.RAW);
            }
        });
        asciiRadio.setChecked(true);

        clearConsoleButton.setOnClickListener(v -> console.clear());

        JSONToggle.setToggleMediator(button -> {
            frontECU.loadJSON();
            return false;
        });

        frontECU.addStatusListener(JSONToggle::setChecked);
        frontECU.setLogListener(console::post);
    }

}
