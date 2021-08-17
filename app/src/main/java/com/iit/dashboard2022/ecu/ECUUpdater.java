package com.iit.dashboard2022.ecu;

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
        ECUMsgHandler.getMessage(ECUMsgHandler.Speedometer).setMessageListener(val -> {
            dashboard.setSpeedValue(val);
            dashboard.setSpeedPercentage(Math.abs(val - lastSpeed) * 0.32f);
            lastSpeed = val;
        });
        ECUMsgHandler.getMessage(ECUMsgHandler.BatteryLife).setMessageListener(val -> {
        });
        ECUMsgHandler.getMessage(ECUMsgHandler.PowerGauge).setMessageListener(val -> {
        });
        /* INDICATORS */
        ECUMsgHandler.getMessage(ECUMsgHandler.Beat).setUpdateMethod(ECUMsg.ON_RECEIVE).setMessageListener(val -> dashboard.setIndicator(Indicators.Indicator.Lag, false));
        ECUMsgHandler.getMessage(ECUMsgHandler.Lag).setUpdateMethod(ECUMsg.ON_RECEIVE).setMessageListener(val -> {
            dashboard.setIndicator(Indicators.Indicator.Lag, true);
            dashboard.setLagTime(val);
        });
        ECUMsgHandler.getMessage(ECUMsgHandler.Fault).setMessageListener(val -> dashboard.setIndicator(Indicators.Indicator.Fault, val > 0));
        ECUMsgHandler.getMessage(ECUMsgHandler.StartLight).setMessageListener(val -> dashboard.setStartLight(val == 1));
        /* State Listener */
        ECUMsgHandler.setGlobalStateListener(state -> {
            dashboard.setState(state.title);
            dashboard.setIndicator(Indicators.Indicator.Waiting, state == ECUMsgHandler.STATE.Idle);
            dashboard.setIndicator(Indicators.Indicator.Charging, state == ECUMsgHandler.STATE.Charging);
            sidePanel.chargeToggle.setChecked(state == ECUMsgHandler.STATE.Charging);
        });
    }
}
