package com.iit.dashboard2022.ui.anim;

import android.content.res.ColorStateList;
import android.view.animation.Animation;
import android.view.animation.AnticipateOvershootInterpolator;
import android.view.animation.BounceInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageButton;

import androidx.interpolator.view.animation.FastOutSlowInInterpolator;

import com.iit.dashboard2022.R;

public class SettingsBtnAnim {
    private static final int ANIM_DEGREES = 60;

    private final RotateAnimation close, open, lockSpin, jiggle;
    private final ColorAnim spinColorAnim, lockedColorAnim;
    private final LockCallback lockCallback;

    private final ImageButton settingsBtn;
    private boolean isOpen = false;
    private boolean locked = false;

    public interface LockCallback {
        void run(boolean locked);
    }

    public SettingsBtnAnim(ImageButton settingsBtn, Runnable callbackOpen, Runnable callbackClose, LockCallback lockCallback) {

        close = new RotateAnimation(0, ANIM_DEGREES, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        open = new RotateAnimation(0, -ANIM_DEGREES, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        close.setInterpolator(new FastOutSlowInInterpolator());
        open.setInterpolator(new FastOutSlowInInterpolator());
        close.setDuration(AnimSetting.ANIM_DURATION);
        open.setDuration(AnimSetting.ANIM_DURATION);

        lockSpin = new RotateAnimation(0, 360, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        lockSpin.setInterpolator(new AnticipateOvershootInterpolator());
        lockSpin.setDuration(AnimSetting.ANIM_DURATION * 2);

        jiggle = new RotateAnimation(0, ANIM_DEGREES, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        jiggle.setInterpolator(new BounceInterpolator());
        jiggle.setDuration(AnimSetting.ANIM_DURATION);

        close.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                callbackClose.run();
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if (locked)
                    settingsBtn.startAnimation(lockSpin);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        open.setAnimationListener(new Animation.AnimationListener() {
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
        spinColorAnim = new ColorAnim(settingsBtn.getContext(), R.color.backgroundText, R.color.colorAccent, color -> settingsBtn.setImageTintList(ColorStateList.valueOf(color)));
        lockedColorAnim = new ColorAnim(settingsBtn.getContext(), R.color.backgroundText, R.color.foregroundText, color -> settingsBtn.setImageTintList(ColorStateList.valueOf(color)));

        this.lockCallback = lockCallback;

        this.settingsBtn = settingsBtn;

        settingsBtn.setOnClickListener(v -> {
            if (locked) {
                settingsBtn.startAnimation(jiggle);
                return;
            }
            if (isOpen) {
                spinColorAnim.reverse();
                settingsBtn.startAnimation(close);
            } else {
                spinColorAnim.start();
                settingsBtn.startAnimation(this.open);
            }
            isOpen = !isOpen;
        });

        settingsBtn.setOnLongClickListener(v -> {
            lock(!locked);
            return true;
        });
    }

    public void lock(boolean locked) {
        if (this.locked != locked) {
            if (locked) {
                if (isOpen)
                    settingsBtn.performClick();
                else
                    settingsBtn.startAnimation(lockSpin);
                lockedColorAnim.start();
            } else {
                lockedColorAnim.reverse();
            }
            this.locked = locked;

            lockCallback.run(locked);
        }
    }
}
