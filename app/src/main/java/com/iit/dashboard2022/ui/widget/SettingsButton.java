package com.iit.dashboard2022.ui.widget;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.ColorStateList;
import android.util.AttributeSet;
import android.view.animation.Animation;
import android.view.animation.AnticipateOvershootInterpolator;
import android.view.animation.BounceInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.RotateAnimation;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.iit.dashboard2022.R;
import com.iit.dashboard2022.ui.anim.AnimSetting;
import com.iit.dashboard2022.ui.anim.ColorAnim;

public class SettingsButton extends androidx.appcompat.widget.AppCompatImageButton {
    private static final int ANIM_DEGREES = 60;

    private RotateAnimation close, open, lockSpin, jiggle;
    private ColorAnim spinColorAnim, lockedColorAnim;
    private LockCallback lockCallback;
    private Runnable callbackOpen, callbackClose;
    private boolean callbackSet = false;

    private boolean isOpen = false;
    private boolean locked = false;
    private boolean running = false;

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
        callbackSet = true;
    }

    public void init() {
        setClickable(true);
        setLongClickable(true);

        close = new RotateAnimation(0, ANIM_DEGREES, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        open = new RotateAnimation(0, -ANIM_DEGREES, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        close.setInterpolator(AnimSetting.ANIM_DEFAULT_INTERPOLATOR);
        open.setInterpolator(AnimSetting.ANIM_DEFAULT_INTERPOLATOR);
        close.setDuration(AnimSetting.ANIM_DURATION);
        open.setDuration(AnimSetting.ANIM_DURATION);

        lockSpin = new RotateAnimation(0, ANIM_DEGREES * 2, Animation.RELATIVE_TO_SELF, 1f, Animation.RELATIVE_TO_SELF, 1f);
        lockSpin.setInterpolator(new AnticipateOvershootInterpolator());
        lockSpin.setDuration(AnimSetting.ANIM_DURATION * 2);

        jiggle = new RotateAnimation(0, ANIM_DEGREES, Animation.RELATIVE_TO_SELF, 1f, Animation.RELATIVE_TO_SELF, 1f);
        jiggle.setInterpolator(new BounceInterpolator());
        jiggle.setDuration(AnimSetting.ANIM_DURATION);

        close.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                running = true;
                callbackClose.run();
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                running = false;
                if (locked) {
                    lockedColorAnim.start();
                    translator.start();
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        open.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                running = true;
                callbackOpen.run();
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                running = false;
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        spinColorAnim = new ColorAnim(getContext(), R.color.foreground, R.color.primary, color -> setImageTintList(ColorStateList.valueOf(color)));
        lockedColorAnim = new ColorAnim(getContext(), R.color.foreground, R.color.midground, color -> setImageTintList(ColorStateList.valueOf(color)));
        translator = ValueAnimator.ofFloat(0, 1);
        translator.setDuration(AnimSetting.ANIM_DURATION);
        translator.addUpdateListener(animation -> {
            float fraction = animation.getAnimatedFraction();
            if (fraction == 1.0f) {
                if (locked)
                    startAnimation(lockSpin);
                moving = false;
            }
            fraction = AnimSetting.ANIM_DEFAULT_INTERPOLATOR.getInterpolation(fraction);
            setTranslationX(getWidth() / 2.0f * fraction);
            setTranslationY(getHeight() / 2.0f * fraction);
        });
    }

    boolean moving = false;
    ValueAnimator translator;

    public void lock(boolean locked) {
        if (this.locked != locked) {
            if (locked) {
                if (isOpen) {
                    performClick();
                } else {
                    lockedColorAnim.start();
                    translator.start();
                }
                moving = true;
                this.locked = true;
            } else {
                this.locked = false;
                lockedColorAnim.reverse();
                translator.reverse();
            }

            lockCallback.run(locked);
        }
    }

    @Override
    public boolean performClick() {
        if (moving || !callbackSet)
            return false;
        if (locked) {
            startAnimation(jiggle);
            return false;
        }
        if (running)
            return false;
        if (isOpen) {
            spinColorAnim.reverse();
            startAnimation(close);
        } else {
            spinColorAnim.start();
            startAnimation(open);
        }
        isOpen = !isOpen;
        return super.performClick();
    }

    @Override
    public boolean performLongClick() {
        if (!callbackSet)
            return false;
        lock(!locked);
        return true;
    }
}
