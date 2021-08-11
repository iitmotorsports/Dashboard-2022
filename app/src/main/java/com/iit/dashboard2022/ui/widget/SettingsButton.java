package com.iit.dashboard2022.ui.widget;

import android.content.Context;
import android.content.res.ColorStateList;
import android.util.AttributeSet;
import android.view.animation.Animation;
import android.view.animation.AnticipateOvershootInterpolator;
import android.view.animation.BounceInterpolator;
import android.view.animation.RotateAnimation;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;

import com.iit.dashboard2022.R;
import com.iit.dashboard2022.ui.anim.AnimSetting;
import com.iit.dashboard2022.ui.anim.ColorAnim;

public class SettingsButton extends androidx.appcompat.widget.AppCompatImageButton {
    private static final int ANIM_DEGREES = 60;

    private RotateAnimation close, open, lockSpin, jiggle;
    private ColorAnim spinColorAnim, lockedColorAnim;
    private LockCallback lockCallback;
    private Runnable callbackOpen, callbackClose;

    private boolean isOpen = false;
    private boolean locked = false;

    public SettingsButton(@NonNull Context context) {
        this(context, null);
    }

    public SettingsButton(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SettingsButton(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public interface LockCallback {
        void run(boolean locked);
    }

    public void setCallbacks(@NonNull Runnable callbackOpen, @NonNull Runnable callbackClose, @NonNull LockCallback lockCallback) {
        this.lockCallback = lockCallback;
        this.callbackOpen = callbackOpen;
        this.callbackClose = callbackClose;
    }

    public void init() {
        setClickable(true);
        setLongClickable(true);

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
                    startAnimation(lockSpin);
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
        spinColorAnim = new ColorAnim(getContext(), R.color.backgroundText, R.color.colorAccent, color -> setImageTintList(ColorStateList.valueOf(color)));
        lockedColorAnim = new ColorAnim(getContext(), R.color.backgroundText, R.color.foregroundText, color -> setImageTintList(ColorStateList.valueOf(color)));
    }

    @Override
    public boolean performClick() {
        if (locked) {
            startAnimation(jiggle);
            return false;
        }
        if (isOpen) {
            spinColorAnim.reverse();
            startAnimation(close);
        } else {
            spinColorAnim.start();
            startAnimation(this.open);
        }
        isOpen = !isOpen;
        return super.performClick();
    }

    @Override
    public boolean performLongClick() {
        lock(!locked);
        return true;
    }

    public void lock(boolean locked) {
        if (this.locked != locked) {
            if (locked) {
                if (isOpen)
                    performClick();
                else
                    startAnimation(lockSpin);
                lockedColorAnim.start();
            } else {
                lockedColorAnim.reverse();
            }
            this.locked = locked;

            lockCallback.run(locked);
        }
    }
}
