package com.iit.dashboard2022.ui;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.RadioGroup;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import com.iit.dashboard2022.R;
import com.iit.dashboard2022.ecu.ECU;
import com.iit.dashboard2022.logging.Log;
import com.iit.dashboard2022.logging.ToastLevel;
import com.iit.dashboard2022.page.CarDashboard;
import com.iit.dashboard2022.page.Errors;
import com.iit.dashboard2022.page.LiveData;
import com.iit.dashboard2022.ui.anim.TranslationAnim;
import com.iit.dashboard2022.ui.widget.SideButton;
import com.iit.dashboard2022.ui.widget.SideRadio;
import com.iit.dashboard2022.ui.widget.SideSwitch;
import com.iit.dashboard2022.ui.widget.SideToggle;
import com.iit.dashboard2022.ui.widget.console.ConsoleWidget;
import com.iit.dashboard2022.util.SerialCom;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class SidePanel extends ConstraintLayout {
    public final RadioGroup consoleRadioGroup;
    public final SideRadio asciiRadio, hexRadio, rawRadio;
    public final SideSwitch uiTestSwitch, nearbyAPISwitch, reverseSwitch, consoleSwitch;
    public final SideToggle chargeToggle, JSONToggle, connToggle;
    public final SideButton clearConsoleButton, canMsgButton, canEchoButton;
    public final TranslationAnim sidePanelDrawerAnim;
    public TranslationAnim consoleAnim;
    public ECU.Mode lastChecked = ECU.Mode.ASCII;

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

        nearbyAPISwitch = findViewById(R.id.nearbyAPISwitch);
        uiTestSwitch = findViewById(R.id.uiTestSwitch);
        reverseSwitch = findViewById(R.id.reverseSwitch);
        consoleSwitch = findViewById(R.id.consoleSwitch);

        clearConsoleButton = findViewById(R.id.clearConsoleButton);
        canMsgButton = findViewById(R.id.canMsgButton);
        canEchoButton = findViewById(R.id.canEchoButton);

        uiTestSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> UITester.enable(isChecked));

        sidePanelDrawerAnim = new TranslationAnim(this, TranslationAnim.X_AXIS, TranslationAnim.ANIM_BACKWARD);
        sidePanelDrawerAnim.startWhenReady();
    }

    public void attach(Activity activity, ConsoleWidget console, CarDashboard dashboard, LiveData liveDataPage, Errors errorsPage, ECU frontECU) {
        consoleAnim = console.getAnimator();
        consoleSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                consoleAnim.reverse();
                console.enable(true);
                frontECU.setInterpreterMode(lastChecked);
            } else {
                consoleAnim.start();
                //console.enable(false);
                frontECU.setInterpreterMode(ECU.Mode.DISABLED);
            }
        });

        consoleRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.asciiRButton) {
                lastChecked = ECU.Mode.ASCII;
            } else if (checkedId == R.id.hexRButton) {
                lastChecked = ECU.Mode.HEX;
            } else if (checkedId == R.id.rawRButton) {
                lastChecked = ECU.Mode.RAW;
            }
            frontECU.setInterpreterMode(lastChecked);
            console.setMode(lastChecked.name());
        });
        asciiRadio.setChecked(true);

        clearConsoleButton.setOnClickListener(v -> console.clear());

        JSONDialog dialog = new JSONDialog(activity, frontECU);

        JSONToggle.setOnClickListener(v -> dialog.showDialog());
        JSONToggle.setHasToggleMediator(true);
        frontECU.getMessageHandler().onLoadEvent(b -> JSONToggle.post(() -> JSONToggle.setChecked(b)));
        canMsgButton.setOnClickListener(v -> {
            long id = frontECU.getMessageHandler().requestMsgID("[Fault Check]", "[ LOG ] MC0 Fault: DC Bus Voltage Low");
            byte[] raw = ByteBuffer.allocate(Long.SIZE / Byte.SIZE).order(ByteOrder.LITTLE_ENDIAN).putLong(id).array();
            frontECU.postPayload(raw);
        });

        // TODO: New nearby API?
        //nearbyAPISwitch.setOnCheckedChangeListener((buttonView, isChecked) -> frontECU.enableNearbyAPI(isChecked));

        frontECU.setConnectionListener(status -> {
            boolean opened = (status & SerialCom.Opened) == SerialCom.Opened;
            boolean attached = (status & SerialCom.Attached) == SerialCom.Attached;

            Log.getLogger().info(opened ? "Serial Connected" : "Serial Disconnected");
            if (!opened) {
                console.setStatus(ConsoleWidget.Status.Disconnected);
            }

            StringBuilder msg = new StringBuilder("ECU ");

            if (attached) {
                msg.append("attached and ");
            } else {
                msg.append("detached and ");
            }

            if (opened) {
                msg.append("opened");
            } else {
                msg.append("closed");
            }

            Log.toast(msg.toString(), ToastLevel.INFO, false, Gravity.END);

            connToggle.post(() -> connToggle.setChecked(opened));
            console.setStatus(opened ? ConsoleWidget.Status.Connected : (attached ? ConsoleWidget.Status.Attached : ConsoleWidget.Status.Disconnected));
            if (!opened) {
                dashboard.reset();
                liveDataPage.reset();
            }
        });

        connToggle.setOnClickListener(v -> {
            if (frontECU.isOpen()) {
                frontECU.close();
            } else {
                frontECU.open();
            }
        });
        connToggle.setHasToggleMediator(true);
        chargeToggle.setHasToggleMediator(true);
        reverseSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            frontECU.issueCommand(ECU.Command.TOGGLE_REVERSE); // TODO: Use discrete ON / OFF, instead of a toggle
        });
        chargeToggle.setOnClickListener(v -> frontECU.issueCommand(ECU.Command.CHARGE));
    }

    public void onLayoutChange() {
        consoleAnim.reloadAutoSize();
        sidePanelDrawerAnim.reloadAutoSize();
    }
}
