package com.iit.dashboard2022;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.iit.dashboard2022.ECU.ECU;
import com.iit.dashboard2022.page.CarDashboard;
import com.iit.dashboard2022.page.LiveData;
import com.iit.dashboard2022.page.Logs;
import com.iit.dashboard2022.page.Pager;
import com.iit.dashboard2022.ui.SidePanel;
import com.iit.dashboard2022.ui.anim.TranslationAnim;
import com.iit.dashboard2022.ui.widget.SettingsButton;
import com.iit.dashboard2022.util.Toaster;

public class MainActivity extends AppCompatActivity {

    Pager mainPager;
    ECU frontECU;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setupUI();

        /* INITIALIZE */
        frontECU = new ECU(this);
        Toaster.setContext(this);

        /* PAGER */

        mainPager = new Pager(this);

        CarDashboard cdPage = (CarDashboard) mainPager.getPage(0);
        Logs logPage = (Logs) mainPager.getPage(1);
        LiveData ldPage = (LiveData) mainPager.getPage(2);

        /* SIDE PANEL */

        SidePanel sidePanel = findViewById(R.id.sidePanel);
        sidePanel.attachConsole(this, findViewById(R.id.console), frontECU);

        /* SETTINGS BUTTON */

        SettingsButton settingsBtn = findViewById(R.id.settingsBtn);
        TranslationAnim sidePanelDrawerAnim = new TranslationAnim(sidePanel, TranslationAnim.X_AXIS, TranslationAnim.ANIM_BACKWARD);
        sidePanelDrawerAnim.startWhenReady();
        mainPager.setOnTouchCallback(settingsBtn::performClick);
        settingsBtn.setCallbacks(
                () -> mainPager.setMargin(Pager.RIGHT, (int) -sidePanelDrawerAnim.reverse()),
                () -> {
                    mainPager.setMargin(Pager.RIGHT, (int) -sidePanelDrawerAnim.start());
                    sidePanel.consoleSwitch.setActionedCheck(false);
                },
                locked -> {
                    mainPager.setUserInputEnabled(!locked);
                    sidePanel.consoleSwitch.setActionedCheck(false);
                }
        );

        /* FINAL CALLS */

        if (!frontECU.loadJSONFromSystem()) {
            Toaster.showToast("No JSON is currently loaded", Toaster.WARNING);
        } else if (frontECU.open()) {
            Toaster.showToast("ECU Connected", Toaster.INFO, Toast.LENGTH_SHORT, Gravity.START);
        }
    }

    @Override // TODO: use registerForActivityResult?
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        frontECU.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        hideUI();
    }

    private void setupUI() {
        // Account for notches in newer phones
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            getWindow().getAttributes().layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
        }
        hideUI();
    }

    private void hideUI() {
        // Hide Action bar and navigation
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            getWindow().getInsetsController().hide(WindowInsets.Type.statusBars() | WindowInsets.Type.navigationBars());
        } else {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    }

}