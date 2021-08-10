package com.iit.dashboard2022;

import android.animation.ObjectAnimator;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

import com.iit.dashboard2022.ui.Pager;
import com.iit.dashboard2022.ui.SidePanel;
import com.iit.dashboard2022.ui.anim.SettingsBtnAnim;
import com.iit.dashboard2022.ui.anim.SidePanelDrawerAnim;
import com.iit.dashboard2022.ui.pages.CarDashboard;
import com.iit.dashboard2022.ui.pages.LiveData;
import com.iit.dashboard2022.ui.pages.Logs;

public class MainActivity extends AppCompatActivity {

    Pager mainPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setupUI();

        mainPager = new Pager(this);

        SidePanel sidePanel = findViewById(R.id.sidePanel);
        ImageButton settingsBtn = findViewById(R.id.settingsBtn);

        CarDashboard cdPage = (CarDashboard) mainPager.pageManager.getPage(0);
        Logs logPage = (Logs) mainPager.pageManager.getPage(1);
        LiveData ldPage = (LiveData) mainPager.pageManager.getPage(2);

        ObjectAnimator sidePanelDrawerAnim = SidePanelDrawerAnim.create(sidePanel);
        new SettingsBtnAnim(settingsBtn, sidePanelDrawerAnim::reverse, sidePanelDrawerAnim::start, locked -> {
        });

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