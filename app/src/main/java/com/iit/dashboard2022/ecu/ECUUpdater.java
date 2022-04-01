package com.iit.dashboard2022.ecu;

import androidx.annotation.NonNull;
import com.iit.dashboard2022.page.CarDashboard;
import com.iit.dashboard2022.page.LiveData;
import com.iit.dashboard2022.ui.SidePanel;
import com.iit.dashboard2022.ui.widget.Indicators;

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

        ecuMsgHandler.getStatistic(Beat).addMessageListener(stat -> dashboardPage.setIndicator(Indicators.Indicator.Lag, false), ECUStat.UpdateMethod.ON_RECEIVE);
        ecuMsgHandler.getStatistic(Lag).addMessageListener(stat -> {
            dashboardPage.setIndicator(Indicators.Indicator.Lag, true);
            dashboardPage.setLagTime(stat.get());
        });
        ecuMsgHandler.getStatistic(Fault).addMessageListener(stat -> dashboardPage.setIndicator(Indicators.Indicator.Fault, stat.get() > 0));
        ecuMsgHandler.getStatistic(StartLight).addMessageListener(stat -> dashboardPage.setStartLight(stat.get() == 1));
        ecuMsgHandler.getStatistic(MC0BoardTemp).addMessageListener(stat -> {
            dashboardPage.setLeftTempValue(stat.getAsInt());
            dashboardPage.setLeftTempPercentage(stat.get());
        });
        ecuMsgHandler.getStatistic(MC1BoardTemp).addMessageListener(stat -> {
            dashboardPage.setRightTempValue(stat.getAsInt());
            dashboardPage.setRightTempPercentage(stat.get());
        });
        /* State Listener */
        /*
        ecuMsgHandler.setGlobalStateListener(state -> {
            if (state == null) {
                return;
            }
        });


         */
    }
}
