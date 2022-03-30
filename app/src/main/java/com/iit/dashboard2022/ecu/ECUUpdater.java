package com.iit.dashboard2022.ecu;

import androidx.annotation.NonNull;
import com.iit.dashboard2022.page.CarDashboard;
import com.iit.dashboard2022.page.LiveData;
import com.iit.dashboard2022.ui.SidePanel;

import static com.iit.dashboard2022.util.Constants.Statistics.*;

public class ECUUpdater {

    private long lastSpeed = 0;

    public ECUUpdater(CarDashboard dashboardPage, LiveData liveDataPage, SidePanel sidePanel, @NonNull ECU frontECU) {
        /*
         *  DASHBOARD
         */
        ECUMessageHandler ecuMsgHandler = frontECU.getMessageHandler();

        /* GAUGES */
        ecuMsgHandler.getStatistic(Speedometer).addMessageListener(stat -> {
            dashboardPage.setSpeedValue(stat.get());
            dashboardPage.setSpeedPercentage(Math.abs(stat.get() - lastSpeed) * 0.32f);
            lastSpeed = stat.get();
        });

        ecuMsgHandler.getStatistic(BatteryLife).addMessageListener(stat -> dashboardPage.setBatteryPercentage(Math.max(Math.min(stat.get(), 100), 0) / 100f));
        ecuMsgHandler.getStatistic(PowerGauge).addMessageListener(stat -> { // NOTE: Actual MC power not being used
            long avgMCVolt = (ecuMsgHandler.getStatistic(MC0Voltage).get() + ecuMsgHandler.getStatistic(MC1Voltage).get()) / 2;
            float limit = ecuMsgHandler.getStatistic(BMSVolt).get() * ecuMsgHandler.getStatistic(BMSAmp).get();
            int usage = (int) (avgMCVolt * ecuMsgHandler.getStatistic(BMSDischargeLim).get());

            dashboardPage.setPowerLimit((int) limit);
            if (limit == 0) {
                limit = 1;
            }

            float percent = Math.abs(usage / limit) * 100f;
            dashboardPage.setPowerPercentage(Math.max(Math.min(percent, 100), 0) / 100f);
            dashboardPage.setPowerValue(usage);
        });
        /* INDICATORS */

        //TODO: CRY
        /*
        ecuMsgHandler.getMessage(ECUMsgHandler.Beat).addMessageListener(val -> dashboardPage.setIndicator(Indicators.Indicator.Lag, false), ECUMsg.UpdateMethod.ON_RECEIVE);
        ecuMsgHandler.getMessage(ECUMsgHandler.Lag).addMessageListener(val -> {
            dashboardPage.setIndicator(Indicators.Indicator.Lag, true);
            dashboardPage.setLagTime(val);
        }, ECUMsg.UpdateMethod.ON_RECEIVE);
        ecuMsgHandler.getMessage(ECUMsgHandler.Fault).addMessageListener(val -> dashboardPage.setIndicator(Indicators.Indicator.Fault, val > 0));
        ecuMsgHandler.getMessage(ECUMsgHandler.StartLight).addMessageListener(val -> dashboardPage.setStartLight(val == 1));
         */

        /* State Listener */
        /*
        ecuMsgHandler.setGlobalStateListener(state -> {
            if (state == null) {
                return;
            }
            dashboardPage.setState(state.title);
            dashboardPage.setIndicator(Indicators.Indicator.Waiting, state == ECUMsgHandler.STATE.Idle);
            dashboardPage.setIndicator(Indicators.Indicator.Charging, state == ECUMsgHandler.STATE.Charging);
            sidePanel.chargeToggle.post(() -> sidePanel.chargeToggle.setChecked(state == ECUMsgHandler.STATE.Charging));
        });


         */




        /*
         *  LIVE DATA
         */

        //TODO: CRY
        /*
        ECUMsg[] messages = ecuMsgHandler.getMessageArray();
        String[] titles = new String[messages.length];

        for (int i = 0; i < messages.length; i++) {
            titles[i] = removeMsgTag(messages[i].stringMsg);
        }

        long[] values = liveDataPage.setMessageTitles(titles);

        for (int i = 0; i < messages.length; i++) {
            int finalI = i;
            messages[i].addMessageListener(val -> {
                values[finalI] = val;
                liveDataPage.updateValue(finalI);
            }, ECUMsg.UpdateMethod.ON_RECEIVE);
        }

         */
    }
}
