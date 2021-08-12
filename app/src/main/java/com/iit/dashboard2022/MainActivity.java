package com.iit.dashboard2022;

import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.switchmaterial.SwitchMaterial;
import com.iit.dashboard2022.page.CarDashboard;
import com.iit.dashboard2022.page.LiveData;
import com.iit.dashboard2022.page.Logs;
import com.iit.dashboard2022.page.Pager;
import com.iit.dashboard2022.ui.SidePanel;
import com.iit.dashboard2022.ui.UITester;
import com.iit.dashboard2022.ui.anim.TranslationAnim;
import com.iit.dashboard2022.ui.widget.ConsoleWidget;
import com.iit.dashboard2022.ui.widget.SettingsButton;

public class MainActivity extends AppCompatActivity {

    Pager mainPager;
    SidePanel sidePanel;
    ConsoleWidget console;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setupUI();

        mainPager = new Pager(this);

        console = findViewById(R.id.console);

        sidePanel = findViewById(R.id.sidePanel);
        SettingsButton settingsBtn = findViewById(R.id.settingsBtn);

        CarDashboard cdPage = (CarDashboard) mainPager.getPage(0);
        Logs logPage = (Logs) mainPager.getPage(1);
        LiveData ldPage = (LiveData) mainPager.getPage(2);

        TranslationAnim sidePanelDrawerAnim = new TranslationAnim(sidePanel, TranslationAnim.X_AXIS, TranslationAnim.ANIM_BACKWARD);
        sidePanelDrawerAnim.startWhenReady();
        TranslationAnim consoleAnim = new TranslationAnim(console, TranslationAnim.X_AXIS, TranslationAnim.ANIM_FORWARD);
        consoleAnim.startWhenReady();
        sidePanel.setUiTestSwitchListener(v -> UITester.enable(((SwitchMaterial) v).isChecked()));
        sidePanel.setConsoleSwitchListener(v -> {
            if (((SwitchMaterial) v).isChecked())
                consoleAnim.reverse();
            else
                consoleAnim.start();
        });

        mainPager.setOnTouchCallback(settingsBtn::performClick);

        settingsBtn.setCallbacks(
                () -> mainPager.setMargin(Pager.RIGHT, (int) -sidePanelDrawerAnim.reverse()),
                () -> {
                    mainPager.setMargin(Pager.RIGHT, (int) -sidePanelDrawerAnim.start());
                    sidePanel.setChecked(SidePanel.CheckableWidget.consoleSwitch, false);
                },
                locked -> {
                    mainPager.setUserInputEnabled(!locked);
                    sidePanel.setChecked(SidePanel.CheckableWidget.consoleSwitch, false);
                }
        );
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        setupUI();
    }

    private void setupUI() {
        // Landscape mode
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        // Account for notches in newer phones
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            getWindow().getAttributes().layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
        }

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