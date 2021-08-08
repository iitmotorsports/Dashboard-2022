package com.iit.dashboard2022;

import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.iit.dashboard2022.ui.Pager;
import com.iit.dashboard2022.ui.anim.SettingsBtnAnim;

public class MainActivity extends AppCompatActivity {

    Pager mainPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setupUI();

        mainPager = new Pager(this);

//        Snackbar snk = Snackbar.make(findViewById(R.id.tabs), "AHHHH!", Snackbar.LENGTH_SHORT);
//        snk.setActionTextColor(getResources().getColor(R.color.colorAccent, getTheme()));
//        snk.setAction("Yellow", v -> snk.dismiss());

        ImageButton settingsBtn = findViewById(R.id.settingsBtn);
        SettingsBtnAnim settingsBtnAnim = new SettingsBtnAnim(this, settingsBtn);
        settingsBtnAnim.setCallbackOpen(() -> Toast.makeText(this, "Open", Toast.LENGTH_SHORT).show(), false);
        settingsBtnAnim.setCallbackClose(() -> Toast.makeText(this, "Close", Toast.LENGTH_SHORT).show(), true);

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