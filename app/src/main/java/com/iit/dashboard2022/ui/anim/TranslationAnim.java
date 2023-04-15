package com.iit.dashboard2022.ui.anim;

import android.animation.ObjectAnimator;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.Interpolator;
import com.iit.dashboard2022.util.Constants;

public class TranslationAnim {
    public static final boolean X_AXIS = true;
    public static final boolean Y_AXIS = false;
    public static final boolean ANIM_BACKWARD = false;
    public static final boolean ANIM_FORWARD = true;

    private final boolean autoSize, direction;
    View view;
    boolean state;
    ViewTreeObserver.OnGlobalLayoutListener autoSizeListener;
    private float posDX = 0;
    private boolean startWhenRdy = false, reloadingAutoSize = false;
    private Runnable onInitializedListener;
    private ObjectAnimator translator;

    public TranslationAnim(View view, boolean axis, boolean direction) {
        autoSize = true;
        this.direction = direction;
        setup(view, axis, 0, 0, Constants.ANIM_DEFAULT_INTERPOLATOR);
    }

    private void setup(View view, boolean axis, float from, float to, Interpolator interpolator) {
        this.view = view;
        state = direction;
        if (autoSize) {
            autoSizeListener = new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    view.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    autoSizeInit(view, axis, from, interpolator);
                    if (onInitializedListener != null) {
                        onInitializedListener.run();
                    }
                    if (!reloadingAutoSize && startWhenRdy) {
                        start();
                    } else {
                        if (state == ANIM_FORWARD) {
                            start();
                        } else if (state == ANIM_BACKWARD) {
                            reverse();
                        }
                    }
                    reloadingAutoSize = false;
                }
            };
            view.getViewTreeObserver().addOnGlobalLayoutListener(autoSizeListener);
        } else {
            init(view, axis, from, to, interpolator);
            if (onInitializedListener != null) {
                onInitializedListener.run();
            }
        }
    }

    private void autoSizeInit(View view, boolean axis, float from, Interpolator interpolator) {
        float to = axis ? view.getMeasuredWidth() : view.getMeasuredHeight();
        init(view, axis, from, to * (direction ? -1 : 1), interpolator);
    }

    private void init(View view, boolean axis, float from, float to, Interpolator interpolator) {
        posDX = to - from;
        translator = ObjectAnimator.ofFloat(view, axis ? View.TRANSLATION_X : View.TRANSLATION_Y, from, to);
        translator.setInterpolator(interpolator);
        translator.setDuration(Constants.ANIM_DURATION);
    }

    public void setOnInitializedListener(Runnable callback) {
        this.onInitializedListener = callback;
    }

    public void reloadAutoSize() {
        if (autoSizeListener != null && !reloadingAutoSize) {
            startWhenRdy = true;
            reloadingAutoSize = true;
            view.getViewTreeObserver().addOnGlobalLayoutListener(autoSizeListener);
        }
    }

    public void startWhenReady() {
        if (!startWhenRdy) {
            startWhenRdy = true;
            if (!autoSize) {
                start();
            }
        }
    }

    public float start() {
        if (translator != null) {
            translator.start();
        }
        state = ANIM_FORWARD;
        return posDX;
    }

    public float reverse() {
        if (translator != null) {
            translator.reverse();
        }
        state = ANIM_BACKWARD;
        return -posDX;
    }

    public float getPositionDelta() {
        return posDX;
    }

}
