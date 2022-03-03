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
import com.iit.dashboard2022.ecu.ECUUpdater;
import com.iit.dashboard2022.page.CarDashboard;
import com.iit.dashboard2022.page.Commander;
import com.iit.dashboard2022.page.Errors;
import com.iit.dashboard2022.page.LiveData;
import com.iit.dashboard2022.page.Logs;
import com.iit.dashboard2022.page.PageManager;
import com.iit.dashboard2022.page.Pager;
import com.iit.dashboard2022.telemetry.TelemetryHandler;
import com.iit.dashboard2022.ui.SidePanel;
import com.iit.dashboard2022.ui.widget.SettingsButton;
import com.iit.dashboard2022.ui.widget.WidgetUpdater;
import com.iit.dashboard2022.ui.widget.console.ConsoleWidget;
import com.iit.dashboard2022.util.HawkUtil;
import com.iit.dashboard2022.util.Toaster;

import java.net.InetSocketAddress;

public class MainActivity extends AppCompatActivity {

    SidePanel sidePanel;
    Pager mainPager;

    ECU frontECU;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        startActivity(new Intent(this, SplashActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED));
        Toaster.setEnabled(false);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setupUI();

        /* INITIALIZE */
        frontECU = new ECU(this);
        mainPager = new Pager(this);
        Toaster.setContext(this);
        new Thread(() -> {

            Toaster.showToast("Starting");
            //192.168.137.1
            TelemetryHandler.getInstance().start(new InetSocketAddress("192.168.137.1", 50051));
        }).start();
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        ConsoleWidget console = findViewById(R.id.console);

        /* PAGER */
        CarDashboard cdPage = (CarDashboard) mainPager.getPage(PageManager.DASHBOARD);
        LiveData ldPage = (LiveData) mainPager.getPage(PageManager.LIVEDATA);
        Errors errorsPage = (Errors) mainPager.getPage(PageManager.ERRORS);
        Commander commandPage = (Commander) mainPager.getPage(PageManager.COMMANDER);
        commandPage.setECU(frontECU);
        cdPage.setECU(frontECU);
        Logs logPage = (Logs) mainPager.getPage(PageManager.LOGS);

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
            // TODO: INSTANCE?
            ECUUpdater ecuUpdater = new ECUUpdater(cdPage, ldPage, sidePanel, frontECU);
            frontECU.setJSONLoadListener(() -> logPage.displayFiles(frontECU.getLocalLogs()));

            cdPage.reset();

            logPage.attachConsole(console, () -> settingsBtn.post(() -> {
                if (settingsBtn.isLocked()) {
                    settingsBtn.performLongClick();
                }
                settingsBtn.setActionedCheck(true);
                sidePanel.consoleSwitch.setActionedCheck(true);
            }));
            WidgetUpdater.start();

            if (!frontECU.loadJSONFromSystem()) {
                Toaster.showToast("No JSON is currently loaded", Toaster.Status.WARNING);
                logPage.displayFiles(frontECU.getLocalLogs());
            } else {
                frontECU.open();
            }

            Toaster.setEnabled(true);
        }, 500);
        super.onPostCreate(savedInstanceState);
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