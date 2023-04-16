package com.iit.dashboard2022;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.VideoView;
import androidx.appcompat.app.AppCompatActivity;
import com.iit.dashboard2022.util.HawkUtil;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {
    VideoView videoView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        HawkUtil.setWindowFlags(getWindow());
        setContentView(R.layout.activity_splash);
    }

    @Override
    protected void onStart() {
        super.onStart();
        videoView = findViewById(R.id.videoView);
        Uri video = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.startup);
        videoView.setVideoURI(video);
        videoView.setOnCompletionListener(mp -> {
            startActivity(new Intent(SplashActivity.this, MainActivity.class));
            finish();
        });
        videoView.start();
    }
}