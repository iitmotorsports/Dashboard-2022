package com.iit.dashboard2022.ui.anim;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageButton;

import androidx.interpolator.view.animation.FastOutSlowInInterpolator;

import com.iit.dashboard2022.R;

public class SettingsBtnAnim {

    private static final int ANIM_DURATION = 150;
    private static final int ANIM_DEGREES = 60;

    private Runnable animCloseStart, animOpenStart, animCloseEnd, animOpenEnd;
    private final RotateAnimation close, open;

    public SettingsBtnAnim(Activity activity, ImageButton settingsBtn) {
        final boolean[] open = {false};

        close = new RotateAnimation(0, ANIM_DEGREES, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        this.open = new RotateAnimation(0, -ANIM_DEGREES, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        close.setInterpolator(new FastOutSlowInInterpolator());
        this.open.setInterpolator(new FastOutSlowInInterpolator());
        close.setDuration(ANIM_DURATION);
        this.open.setDuration(ANIM_DURATION);

        close.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                if (animCloseStart != null) {
                    animCloseStart.run();
                }
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if (animCloseEnd != null) {
                    animCloseEnd.run();
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        this.open.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                if (animOpenStart != null) {
                    animOpenStart.run();
                }
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if (animOpenEnd != null) {
                    animOpenEnd.run();
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        final float[] from = new float[3], to = new float[3];

        Color.colorToHSV(activity.getResources().getColor(R.color.backgroundText, activity.getTheme()), from);
        Color.colorToHSV(activity.getResources().getColor(R.color.colorAccent, activity.getTheme()), to);

        ValueAnimator anim = ValueAnimator.ofFloat(0, 1);
        anim.setDuration(150);

        final float[] hsv = new float[3];

        anim.addUpdateListener(animation -> {
            hsv[0] = from[0] + (to[0] - from[0]) * (4 * animation.getAnimatedFraction());
            hsv[1] = from[1] + (to[1] - from[1]) * animation.getAnimatedFraction();
            hsv[2] = from[2] + (to[2] - from[2]) * animation.getAnimatedFraction();
            settingsBtn.setImageTintList(ColorStateList.valueOf(Color.HSVToColor(hsv)));
        });

        settingsBtn.setOnClickListener(v -> {
            if (open[0]) {
                anim.reverse();
                settingsBtn.startAnimation(close);
            } else {
                anim.start();
                settingsBtn.startAnimation(this.open);
            }
            open[0] = !open[0];
        });
    }

    public void setCallbackOpen(Runnable callback, boolean onEnd) {
        if (onEnd) {
            animOpenEnd = callback;
        } else {
            animOpenStart = callback;
        }
    }

    public void setCallbackClose(Runnable callback, boolean onEnd) {
        if (onEnd) {
            animCloseEnd = callback;
        } else {
            animCloseStart = callback;
        }
    }
}
