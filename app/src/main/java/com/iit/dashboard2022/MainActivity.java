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
import com.iit.dashboard2022.ui.widget.SettingsButton;
import com.iit.dashboard2022.ui.widget.console.ConsoleWidget;

public class MainActivity extends AppCompatActivity {

    Pager mainPager;
    SidePanel sidePanel;
    ConsoleWidget console;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setupUI();

        /* PAGER */

        mainPager = new Pager(this);

        CarDashboard cdPage = (CarDashboard) mainPager.getPage(0);
        Logs logPage = (Logs) mainPager.getPage(1);
        LiveData ldPage = (LiveData) mainPager.getPage(2);

        /* SIDE PANEL */

        console = findViewById(R.id.console);
        sidePanel = findViewById(R.id.sidePanel);

        TranslationAnim sidePanelDrawerAnim = new TranslationAnim(sidePanel, TranslationAnim.X_AXIS, TranslationAnim.ANIM_BACKWARD);
        sidePanelDrawerAnim.startWhenReady();
        TranslationAnim consoleAnim = new TranslationAnim(console, TranslationAnim.X_AXIS, TranslationAnim.ANIM_FORWARD);
        consoleAnim.startWhenReady();

        sidePanel.consoleSwitch.setOnClickListener(v -> {
            if (((SwitchMaterial) v).isChecked()) {
                consoleAnim.reverse();
                console.enable(true);
            } else {
                consoleAnim.start();
                console.enable(false);
            }
        });

        sidePanel.consoleRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.asciiRButton) {
                console.setMode("Ascii");
            } else if (checkedId == R.id.hexRButton) {
                console.setMode("Hex");
            } else if (checkedId == R.id.rawRButton) {
                console.setMode("Raw");
            }
        });
        sidePanel.asciiRadio.setChecked(true);

        sidePanel.clearConsoleButton.setOnClickListener(v -> console.clear());
        sidePanel.uiTestSwitch.setOnClickListener(v -> UITester.enable(((SwitchMaterial) v).isChecked()));

        SettingsButton settingsBtn = findViewById(R.id.settingsBtn);
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