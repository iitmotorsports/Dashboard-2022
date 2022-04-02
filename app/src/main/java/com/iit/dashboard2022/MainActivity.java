package com.iit.dashboard2022;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.WindowManager;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.iit.dashboard2022.ecu.ECU;
import com.iit.dashboard2022.ecu.ECUMessageHandler;
import com.iit.dashboard2022.ecu.ECUStat;
import com.iit.dashboard2022.logging.Log;
import com.iit.dashboard2022.logging.StringAppender;
import com.iit.dashboard2022.logging.ToastLevel;
import com.iit.dashboard2022.page.CarDashboard;
import com.iit.dashboard2022.page.Commander;
import com.iit.dashboard2022.page.Errors;
import com.iit.dashboard2022.page.LiveData;
import com.iit.dashboard2022.page.Logs;
import com.iit.dashboard2022.page.PageManager;
import com.iit.dashboard2022.page.Pager;
import com.iit.dashboard2022.ui.SidePanel;
import com.iit.dashboard2022.ui.widget.Indicators;
import com.iit.dashboard2022.ui.widget.SettingsButton;
import com.iit.dashboard2022.ui.widget.WidgetUpdater;
import com.iit.dashboard2022.ui.widget.console.ConsoleWidget;
import com.iit.dashboard2022.util.HawkUtil;
import com.iit.dashboard2022.util.mapping.JsonFileSelectorHandler;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicLong;

import static com.iit.dashboard2022.util.Constants.Statistics.*;

public class MainActivity extends AppCompatActivity {

    // Don't use this variable. It will have unintended consequences and will likely end up in the app crashing
    public static AppCompatActivity GLOBAL_CONTEXT = null;

    SidePanel sidePanel;
    Pager mainPager;
    ECU frontECU;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        GLOBAL_CONTEXT = this;
        startActivity(new Intent(this, SplashActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED));
        Log.setEnabled(false);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setupUI();

        /* INITIALIZE */
        frontECU = new ECU(this);

        mainPager = new Pager(this);
        Log.setContext(this);
        Log.getInstance().loadLogs();

        ((JsonFileSelectorHandler) ECUMessageHandler.MapHandler.SELECTOR.get()).init(this);
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        ConsoleWidget console = findViewById(R.id.console);
        /* PAGER */
        CarDashboard cdPage = (CarDashboard) mainPager.getPage(PageManager.DASHBOARD);
        LiveData ldPage = (LiveData) mainPager.getPage(PageManager.LIVEDATA);
        ldPage.setEcu(frontECU);
        Errors errorsPage = (Errors) mainPager.getPage(PageManager.ERRORS);
        Commander commandPage = (Commander) mainPager.getPage(PageManager.COMMANDER);
        commandPage.setECU(frontECU);
        cdPage.setECU(frontECU);
        Logs logPage = (Logs) mainPager.getPage(PageManager.LOGS);

        StringAppender.register(console);
        StringAppender.register(errorsPage);

        frontECU.onStateChangeEvent(state -> {
            cdPage.setState(state.name());
            cdPage.setIndicator(Indicators.Indicator.Waiting, state == ECU.State.IDLE);
            cdPage.setIndicator(Indicators.Indicator.Charging, state == ECU.State.CHARGING);
            sidePanel.chargeToggle.post(() -> sidePanel.chargeToggle.setChecked(state == ECU.State.CHARGING));
        });

        /* SIDE PANEL */
        sidePanel = findViewById(R.id.sidePanel);
        sidePanel.attach(this, console, cdPage, ldPage, errorsPage, frontECU);

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
                    sidePanel.consoleSwitch.setActionedCheck(false);
                    WidgetUpdater.post();
                },
                locked -> {
                    mainPager.setUserInputEnabled(!locked);
                    sidePanel.consoleSwitch.setActionedCheck(false);
                    WidgetUpdater.post();
                }
        );

        new Handler(Looper.myLooper()).postDelayed(() -> {
            /* FINAL CALLS */
            setupStatistics(cdPage);
            frontECU.getMessageHandler().onLoadEvent(b -> logPage.displayFiles(Log.getInstance().getLogs().values()));
            cdPage.reset();

            logPage.attachConsole(console, () -> settingsBtn.post(() -> {
                if (settingsBtn.isLocked()) {
                    settingsBtn.performLongClick();
                }
                settingsBtn.setActionedCheck(true);
                sidePanel.consoleSwitch.setActionedCheck(true);
            }));
            WidgetUpdater.start();

            Log.setEnabled(true);
            try {
                if (!frontECU.getMessageHandler().load().get()) {
                    Log.toast("No JSON is currently loaded", ToastLevel.WARNING);
                    logPage.displayFiles(Log.getInstance().getLogs().values());
                } else {
                    frontECU.open();
                }
            } catch (ExecutionException | InterruptedException e) {
                Log.getLogger().error("Error while loading ECU");
            }
        }, 500);
        super.onPostCreate(savedInstanceState);
    }

    /**
     * Sets up events for incoming statistics.
     *
     * @param dashboard The {@link CarDashboard} page.
     */
    private void setupStatistics(CarDashboard dashboard) {
        AtomicLong lastSpeed = new AtomicLong();
        ECUMessageHandler ecuMsgHandler = frontECU.getMessageHandler();

        /* GAUGES */
        ecuMsgHandler.getStatistic(Speedometer).addMessageListener(stat -> {
            dashboard.setSpeedValue(stat.get());
            dashboard.setSpeedPercentage(Math.abs(stat.get() - lastSpeed.get()) * 0.32f);
            lastSpeed.set(stat.get());
        });

        ecuMsgHandler.getStatistic(BatteryLife).addMessageListener(stat -> dashboard.setBatteryPercentage(Math.max(Math.min(stat.get(), 100), 0) / 100f));
        ecuMsgHandler.getStatistic(PowerGauge).addMessageListener(stat -> { // NOTE: Actual MC power not being used
            long avgMCVolt = (ecuMsgHandler.getStatistic(MC0Voltage).get() + ecuMsgHandler.getStatistic(MC1Voltage).get()) / 2;
            float limit = ecuMsgHandler.getStatistic(BMSVolt).get() * ecuMsgHandler.getStatistic(BMSAmp).get();
            int usage = (int) (avgMCVolt * ecuMsgHandler.getStatistic(BMSDischargeLim).get());

            dashboard.setPowerLimit((int) limit);
            if (limit == 0) {
                limit = 1;
            }

            float percent = Math.abs(usage / limit) * 100f;
            dashboard.setPowerPercentage(Math.max(Math.min(percent, 100), 0) / 100f);
            dashboard.setPowerValue(usage);
        });

        /* INDICATORS */

        ecuMsgHandler.getStatistic(Beat).addMessageListener(stat -> dashboard.setIndicator(Indicators.Indicator.Lag, false), ECUStat.UpdateMethod.ON_RECEIVE);
        ecuMsgHandler.getStatistic(Lag).addMessageListener(stat -> {
            dashboard.setIndicator(Indicators.Indicator.Lag, true);
            dashboard.setLagTime(stat.get());
        });
        ecuMsgHandler.getStatistic(Fault).addMessageListener(stat -> dashboard.setIndicator(Indicators.Indicator.Fault, stat.get() > 0));
        ecuMsgHandler.getStatistic(StartLight).addMessageListener(stat -> dashboard.setStartLight(stat.get() == 1));
        ecuMsgHandler.getStatistic(MC0BoardTemp).addMessageListener(stat -> {
            dashboard.setLeftTempValue(stat.getAsInt());
            dashboard.setLeftTempPercentage(stat.get());
        });
        ecuMsgHandler.getStatistic(MC1BoardTemp).addMessageListener(stat -> {
            dashboard.setRightTempValue(stat.getAsInt());
            dashboard.setRightTempPercentage(stat.get());
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

    private void setupUI() {
        // Account for notches in newer phones
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            getWindow().getAttributes().layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
        }
        HawkUtil.setWindowFlags(getWindow());
    }

}