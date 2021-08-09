package com.iit.dashboard2022.ui.anim;

import android.app.Activity;
import android.content.res.ColorStateList;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageButton;

import androidx.interpolator.view.animation.FastOutSlowInInterpolator;

import com.iit.dashboard2022.R;

public class SettingsBtnAnim {
    private static final int ANIM_DEGREES = 60;

    private final RotateAnimation close, open;
    private final ColorAnim colorAnim;

    public SettingsBtnAnim(ImageButton settingsBtn, Runnable callbackOpen, Runnable callbackClose) {
        final boolean[] open = {false};

        close = new RotateAnimation(0, ANIM_DEGREES, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        this.open = new RotateAnimation(0, -ANIM_DEGREES, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        close.setInterpolator(new FastOutSlowInInterpolator());
        this.open.setInterpolator(new FastOutSlowInInterpolator());
        close.setDuration(AnimSetting.ANIM_DURATION);
        this.open.setDuration(AnimSetting.ANIM_DURATION);

        close.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                callbackClose.run();
            }

            @Override
            public void onAnimationEnd(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        this.open.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                callbackOpen.run();
            }

            @Override
            public void onAnimationEnd(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        colorAnim = new ColorAnim(settingsBtn.getContext(), R.color.backgroundText, R.color.colorAccent, color -> settingsBtn.setImageTintList(ColorStateList.valueOf(color)));

        settingsBtn.setOnClickListener(v -> {
            if (open[0]) {
                colorAnim.reverse();
                settingsBtn.startAnimation(close);
            } else {
                colorAnim.start();
                settingsBtn.startAnimation(this.open);
            }
            open[0] = !open[0];
        });
    }
}
