package com.iit.dashboard2022.ui.anim;

import android.animation.ObjectAnimator;
import android.view.View;

import androidx.interpolator.view.animation.FastOutSlowInInterpolator;

import com.iit.dashboard2022.ui.SidePanel;

public class SidePanelDrawerAnim {

    public static ObjectAnimator create(SidePanel sidePanel) {
        ObjectAnimator transAnimation = ObjectAnimator.ofFloat(sidePanel, View.TRANSLATION_X, 0, 200);
        transAnimation.setInterpolator(new FastOutSlowInInterpolator());
        transAnimation.setDuration(AnimSetting.ANIM_DURATION);
        transAnimation.start();
        return transAnimation;
    }
}
