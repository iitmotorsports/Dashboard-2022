package com.iit.dashboard2022.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import com.iit.dashboard2022.R;
import com.iit.dashboard2022.ecu.ECU;
import com.iit.dashboard2022.logging.Log;
import com.iit.dashboard2022.logging.ToastLevel;
import com.iit.dashboard2022.page.CarDashboard;
import com.iit.dashboard2022.page.LiveData;
import com.iit.dashboard2022.ui.anim.TranslationAnim;
import com.iit.dashboard2022.ui.widget.SideSwitch;
import com.iit.dashboard2022.ui.widget.SideToggle;
import com.iit.dashboard2022.util.USBSerial;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SidePanel extends ConstraintLayout {
    public final SideSwitch uiTestSwitch, reverseSwitch;
    public final SideToggle connToggle;
    public final TranslationAnim sidePanelDrawerAnim;

    public SidePanel(Context context) {
        this(context, null);
    }

    public SidePanel(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SidePanel(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        View.inflate(getContext(), R.layout.side_panel_layout, this);

        connToggle = findViewById(R.id.connToggle);

        uiTestSwitch = findViewById(R.id.uiTestSwitch);
        reverseSwitch = findViewById(R.id.reverseSwitch);


        uiTestSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> UITester.enable(isChecked));

        sidePanelDrawerAnim = new TranslationAnim(this, TranslationAnim.X_AXIS, TranslationAnim.ANIM_BACKWARD);
        sidePanelDrawerAnim.startWhenReady();
    }

    public void attach(CarDashboard dashboard, LiveData liveDataPage, ECU frontECU) {
        frontECU.setConnectionListener(status -> {
            boolean opened = (status & USBSerial.Opened) == USBSerial.Opened;
            boolean attached = (status & USBSerial.Attached) == USBSerial.Attached;

            log.info(opened ? "Serial Connected" : "Serial Disconnected");

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
        reverseSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            frontECU.issueCommand(ECU.Command.TOGGLE_REVERSE); // TODO: Use discrete ON / OFF, instead of a toggle
        });
    }

    public void onLayoutChange() {
        sidePanelDrawerAnim.reloadAutoSize();
    }
}
