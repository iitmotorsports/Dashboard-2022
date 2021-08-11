package com.iit.dashboard2022.ui.anim;

import android.animation.ObjectAnimator;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.Interpolator;

import androidx.interpolator.view.animation.FastOutSlowInInterpolator;

public class TranslationAnim {
    private static final Interpolator defaultInter = new FastOutSlowInInterpolator();
    public static boolean X_AXIS = true;
    public static boolean Y_AXIS = false;
    public static boolean ANIM_BACKWARD = false;
    public static boolean ANIM_FORWARD = true;

    private final boolean autoSize, direction;
    private boolean startWhenRdy = false;
    private ObjectAnimator translator;

    public TranslationAnim(View view, boolean axis, boolean direction) {
        autoSize = true;
        this.direction = direction;
        setup(view, axis, 0, 0, defaultInter);
    }

    public TranslationAnim(View view, boolean axis, float from, float to) {
        autoSize = false;
        this.direction = ANIM_BACKWARD;
        setup(view, axis, from, to, defaultInter);
    }

    public TranslationAnim(View view, boolean axis, boolean direction, Interpolator interpolator) {
        autoSize = true;
        this.direction = direction;
        setup(view, axis, 0, 0, interpolator);
    }

    public TranslationAnim(View view, boolean axis, float from, float to, Interpolator interpolator) {
        autoSize = false;
        this.direction = ANIM_BACKWARD;
        setup(view, axis, from, to, interpolator);
    }

    private void setup(View view, boolean axis, float from, float to, Interpolator interpolator) {
        if (autoSize) {
            view.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    view.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    int to = axis ? view.getMeasuredWidth() : view.getMeasuredHeight();
                    init(view, axis, from, to * (direction ? -1 : 1), interpolator);
                    if (startWhenRdy)
                        start();
                }
            });
        } else {
            init(view, axis, from, to, interpolator);
        }
    }

    private void init(View view, boolean axis, float from, float to, Interpolator interpolator) {
        translator = ObjectAnimator.ofFloat(view, axis ? View.TRANSLATION_X : View.TRANSLATION_Y, from, to);
        translator.setInterpolator(interpolator);
        translator.setDuration(AnimSetting.ANIM_DURATION);
    }

    public void startWhenReady() {
        if (!startWhenRdy) {
            if (!autoSize)
                start();
            startWhenRdy = true;
        }
    }

    public void start() {
        if (translator != null)
            translator.start();
    }

    public void reverse() {
        if (translator != null)
            translator.reverse();
    }

    public void cancel() {
        if (translator != null)
            translator.cancel();
    }
}
