package com.iit.dashboard2022.ui.anim;

import android.app.Activity;
import android.content.res.ColorStateList;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageButton;

import androidx.interpolator.view.animation.FastOutSlowInInterpolator;

import com.iit.dashboard2022.R;

public class SettingsBtnAnim {

    private static final int ANIM_DURATION = 300;
    private static final int ANIM_DEGREES = 60;

    private Runnable animCloseStart, animOpenStart, animCloseEnd, animOpenEnd;
    private final RotateAnimation close, open;
    private final ColorAnim colorAnim;

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

        colorAnim = new ColorAnim(activity, R.color.backgroundText, R.color.colorAccent, color -> settingsBtn.setImageTintList(ColorStateList.valueOf(color)));

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
