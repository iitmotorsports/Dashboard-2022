package com.iit.dashboard2022.ECU;

import com.iit.dashboard2022.page.CarDashboard;
import com.iit.dashboard2022.ui.SidePanel;
import com.iit.dashboard2022.ui.widget.Indicators;

public class ECUUpdater {

    private long lastSpeed = 0;

    public ECUUpdater(CarDashboard dashboard, SidePanel sidePanel) {
        /*
         *  DASHBOARD
         */

        /* GAUGES */
        ECUMsg.getMessage(ECUMsg.Speedometer).setMessageListener(val -> {
            dashboard.setSpeedValue(val);
            dashboard.setSpeedPercentage(Math.abs(val - lastSpeed) * 0.32f);
            lastSpeed = val;
        });
        ECUMsg.getMessage(ECUMsg.BatteryLife).setMessageListener(val -> {
        });
        ECUMsg.getMessage(ECUMsg.PowerGauge).setMessageListener(val -> {
        });
        /* INDICATORS */
        ECUMsg.getMessage(ECUMsg.Beat).setUpdateMethod(ECUMsg.ON_RECEIVE).setMessageListener(val -> dashboard.setIndicator(Indicators.Indicator.Lag, false));
        ECUMsg.getMessage(ECUMsg.Lag).setUpdateMethod(ECUMsg.ON_RECEIVE).setMessageListener(val -> {
            dashboard.setIndicator(Indicators.Indicator.Lag, true);
            dashboard.setLagTime(val);
        });
        ECUMsg.getMessage(ECUMsg.Fault).setMessageListener(val -> dashboard.setIndicator(Indicators.Indicator.Fault, val > 0));
        ECUMsg.getMessage(ECUMsg.StartLight).setMessageListener(val -> dashboard.setStartLight(val == 1));
        /* State Listener */
        ECUMsg.setGlobalStateListener(state -> {
            dashboard.setState(state.title);
            dashboard.setIndicator(Indicators.Indicator.Waiting, state == ECUMsg.STATE.Idle);
            dashboard.setIndicator(Indicators.Indicator.Charging, state == ECUMsg.STATE.Charging);
            sidePanel.chargeToggle.setChecked(state == ECUMsg.STATE.Charging);
        });
    }
}
