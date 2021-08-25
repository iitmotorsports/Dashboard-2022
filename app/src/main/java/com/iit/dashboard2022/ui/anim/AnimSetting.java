package com.iit.dashboard2022.ui.anim;

import android.view.animation.Interpolator;

import androidx.interpolator.view.animation.FastOutSlowInInterpolator;

public class AnimSetting {
    public static final int ANIM_DURATION = 300;
    public static final int ANIM_UPDATE_MILLIS = 20;
    public static final Interpolator ANIM_DEFAULT_INTERPOLATOR = new FastOutSlowInInterpolator();
}
