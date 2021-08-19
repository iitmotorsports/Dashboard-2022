package com.iit.dashboard2022.ecu;

import com.iit.dashboard2022.page.CarDashboard;
import com.iit.dashboard2022.page.LiveData;
import com.iit.dashboard2022.ui.SidePanel;
import com.iit.dashboard2022.ui.widget.Indicators;

public class ECUUpdater {

    private long lastSpeed = 0;

    public ECUUpdater(CarDashboard dashboardPage, LiveData liveDataPage, SidePanel sidePanel, ECU frontECU) {
        /*
         *  DASHBOARD
         */
        ECUMsgHandler ecuMsgHandler = frontECU.getEcuMsgHandler();

        /* GAUGES */
        ecuMsgHandler.getMessage(ECUMsgHandler.Speedometer).setMessageListener(val -> {
            dashboardPage.setSpeedValue(val);
            dashboardPage.setSpeedPercentage(Math.abs(val - lastSpeed) * 0.32f);
            lastSpeed = val;
        });
        ecuMsgHandler.getMessage(ECUMsgHandler.BatteryLife).setMessageListener(val -> dashboardPage.setBatteryPercentage(Math.max(Math.min(val, 100), 0) / 100f));
        ecuMsgHandler.getMessage(ECUMsgHandler.PowerGauge).setMessageListener(val -> { // NOTE: Actual MC power not being used
            long avgMCVolt = (ecuMsgHandler.requestValue(ECUMsgHandler.MC0Voltage) + ecuMsgHandler.requestValue(ECUMsgHandler.MC1Voltage)) / 2;
            float limit = ecuMsgHandler.requestValue(ECUMsgHandler.BMSVolt) * ecuMsgHandler.requestValue(ECUMsgHandler.BMSAmp);
            int usage = (int) (avgMCVolt * ecuMsgHandler.requestValue(ECUMsgHandler.BMSDischargeLim));

            dashboardPage.setPowerLimit((int) limit);
            if (limit == 0)
                limit = 1;

            float percent = Math.abs(usage / limit) * 100f;
            dashboardPage.setPowerPercentage(Math.max(Math.min(percent, 100), 0) / 100f);
            dashboardPage.setPowerValue(usage);
        });
        /* INDICATORS */
        ecuMsgHandler.getMessage(ECUMsgHandler.Beat).setUpdateMethod(ECUMsg.ON_RECEIVE).setMessageListener(val -> dashboardPage.setIndicator(Indicators.Indicator.Lag, false));
        ecuMsgHandler.getMessage(ECUMsgHandler.Lag).setUpdateMethod(ECUMsg.ON_RECEIVE).setMessageListener(val -> {
            dashboardPage.setIndicator(Indicators.Indicator.Lag, true);
            dashboardPage.setLagTime(val);
        });
        ecuMsgHandler.getMessage(ECUMsgHandler.Fault).setMessageListener(val -> dashboardPage.setIndicator(Indicators.Indicator.Fault, val > 0));
        ecuMsgHandler.getMessage(ECUMsgHandler.StartLight).setMessageListener(val -> dashboardPage.setStartLight(val == 1));
        /* State Listener */
        ecuMsgHandler.setGlobalStateListener(state -> {
            dashboardPage.setState(state.title);
            dashboardPage.setIndicator(Indicators.Indicator.Waiting, state == ECUMsgHandler.STATE.Idle);
            dashboardPage.setIndicator(Indicators.Indicator.Charging, state == ECUMsgHandler.STATE.Charging);
            sidePanel.chargeToggle.setChecked(state == ECUMsgHandler.STATE.Charging);
        });
    }

}
