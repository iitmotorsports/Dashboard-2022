package com.iit.dashboard2022;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.WindowManager;
import androidx.appcompat.app.AppCompatActivity;
import com.iit.dashboard2022.ecu.ECU;
import com.iit.dashboard2022.ecu.Metric;
import com.iit.dashboard2022.ecu.State;
import com.iit.dashboard2022.logging.Log;
import com.iit.dashboard2022.page.CarDashboard;
import com.iit.dashboard2022.page.Commander;
import com.iit.dashboard2022.page.LiveData;
import com.iit.dashboard2022.page.Logs;
import com.iit.dashboard2022.page.PageManager;
import com.iit.dashboard2022.page.Pager;
import com.iit.dashboard2022.ui.SidePanel;
import com.iit.dashboard2022.ui.widget.Indicators;
import com.iit.dashboard2022.ui.widget.SettingsButton;
import com.iit.dashboard2022.ui.widget.WidgetUpdater;
import com.iit.dashboard2022.util.HawkUtil;

import java.util.concurrent.atomic.AtomicLong;

public final class MainActivity extends AppCompatActivity {

    // Don't use this variable. It will have unintended consequences and will likely end up in the app crashing
    public static AppCompatActivity GLOBAL_CONTEXT = null;

    SidePanel sidePanel;
    Pager mainPager;
    ECU frontECU;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        GLOBAL_CONTEXT = this;


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        HawkUtil.setWindowFlags(getWindow());

        Log.setEnabled(false);
        frontECU = new ECU(this);

        mainPager = new Pager(this);
        Log.setContext(this);
        Log.getInstance().loadLogs();
    }

    @Override
    protected void onStart() {
        /* INITIALIZE */

        /* PAGER */
        CarDashboard cdPage = (CarDashboard) mainPager.getPage(PageManager.DASHBOARD);
        LiveData ldPage = (LiveData) mainPager.getPage(PageManager.LIVEDATA);
        Commander commandPage = (Commander) mainPager.getPage(PageManager.COMMANDER);
        commandPage.setECU(frontECU);
        cdPage.setECU(frontECU);
        Logs logPage = (Logs) mainPager.getPage(PageManager.LOGS);

        frontECU.onStateChangeEvent(state -> {
            cdPage.setState(state.name());
            cdPage.setIndicator(Indicators.Indicator.Waiting, state == State.IDLE);
            cdPage.setIndicator(Indicators.Indicator.Charging, state == State.CHARGING);
        });

        /* SIDE PANEL */
        sidePanel = findViewById(R.id.sidePanel);
        sidePanel.attach(cdPage, ldPage, frontECU);

        /* SETTINGS BUTTON */
        SettingsButton settingsBtn = findViewById(R.id.settingsBtn);

        mainPager.setOnTouchCallback(settingsBtn::performClick);
        settingsBtn.setCallbacks(
                () -> {
                    mainPager.pushMargin(Pager.RIGHT, (int) -sidePanel.sidePanelDrawerAnim.reverse());
                    WidgetUpdater.post();
                },
                () -> {
                    mainPager.pushMargin(Pager.RIGHT, (int) -sidePanel.sidePanelDrawerAnim.start());
                    WidgetUpdater.post();
                },
                locked -> {
                    mainPager.setUserInputEnabled(!locked);
                    WidgetUpdater.post();
                }
        );
        cdPage.reset();
        WidgetUpdater.start();

        new Handler(Looper.myLooper()).post(() -> {
            /* FINAL CALLS */
            Log.setEnabled(true);
            Log.getInstance().newLog(Metric.getMetricsAsMap());
            logPage.displayFiles(Log.getInstance().getLogs().values());
            setupStatistics(cdPage);

            frontECU.open();
        });
        super.onStart();

    }

    /**
     * Sets up events for incoming statistics.
     *
     * @param dashboard The {@link CarDashboard} page.
     */
    private void setupStatistics(CarDashboard dashboard) {
        AtomicLong lastSpeed = new AtomicLong();

        /* GAUGES */
        Metric.SPEEDOMETER.addMessageListener(stat -> {
            dashboard.setSpeedValue(stat.getValue());
            dashboard.setSpeedPercentage(Math.abs(stat.getValue() - lastSpeed.get()) * 0.32f);
            lastSpeed.set(stat.getValue());
        });

        Metric.SOC.addMessageListener(stat -> dashboard.setBatteryPercentage(Math.max(Math.min(stat.getValue(), 100), 0) / 100f));
        Metric.POWER_GAUGE.addMessageListener(stat -> { // NOTE: Actual MC power not being used
            long avgMCVolt = (Metric.MC0_VOLTAGE.getValue() + Metric.MC1_VOLTAGE.getValue()) / 2;
            float limit = Metric.STACK_VOLTAGE.getValue() * Metric.STACK_CURRENT.getValue();
            int usage = (int) (avgMCVolt * Metric.BMS_DISCHARGE_LIM.getValue());

            dashboard.setPowerLimit((int) limit);
            if (limit == 0) {
                limit = 1;
            }

            float percent = Math.abs(usage / limit) * 100f;
            dashboard.setPowerPercentage(Math.max(Math.min(percent, 100), 0) / 100f);
            dashboard.setPowerValue(usage);
        });

        /* INDICATORS */

        Metric.BEAT.addMessageListener(stat -> dashboard.setIndicator(Indicators.Indicator.Lag, false), Metric.UpdateMethod.ON_RECEIVE);
        Metric.LAG.addMessageListener(stat -> {
            dashboard.setIndicator(Indicators.Indicator.Lag, true);
            dashboard.setLagTime(stat.getValue());
        });
        Metric.FAULT.addMessageListener(stat -> dashboard.setIndicator(Indicators.Indicator.Fault, stat.getValue() > 0));
        Metric.START_LIGHT.addMessageListener(stat -> dashboard.setStartLight(stat.getValue() == 1));
        Metric.MC0_BOARD_TEMP.addMessageListener(stat -> {
            dashboard.setLeftTempValue(stat.getValue());
            dashboard.setLeftTempPercentage(stat.getValue() / 100f);
        });
        Metric.MC1_BOARD_TEMP.addMessageListener(stat -> {
            dashboard.setRightTempValue(stat.getValue());
            dashboard.setRightTempPercentage(stat.getValue() / 100f);
        });
    }

    @Override
    public void onWindowAttributesChanged(WindowManager.LayoutParams params) {
        super.onWindowAttributesChanged(params);
        if (sidePanel != null) {
            sidePanel.onLayoutChange();
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        HawkUtil.setWindowFlags(getWindow());
    }
}