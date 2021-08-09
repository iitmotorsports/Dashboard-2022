package com.iit.dashboard2022.ui.widget;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.SoundEffectConstants;
import android.view.ViewDebug;

import androidx.annotation.InspectableProperty;

import com.google.android.material.button.MaterialButton;
import com.iit.dashboard2022.R;

public class SideToggle extends MaterialButton {
    private CharSequence mTextOn, mTextOff;
    private final float mDisabledAlpha;
    private boolean mChecked;
    private final Context mContext;

    final float[] anim_from = new float[3], anim_to = new float[3];
    final float[] anim_hsv = new float[3];
    ValueAnimator anim;

    @SuppressLint("ResourceType")
    public SideToggle(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, R.attr.sideToggleButtonStyle);

        Color.colorToHSV(getResources().getColor(R.color.foregroundText, context.getTheme()), anim_from);
        Color.colorToHSV(getResources().getColor(R.color.colorAccent, context.getTheme()), anim_to);

        anim = ValueAnimator.ofFloat(0, 1);
        anim.setDuration(150);

        anim.addUpdateListener(animation -> {
            anim_hsv[0] = anim_from[0] + (anim_to[0] - anim_from[0]) * (4 * animation.getAnimatedFraction());
            anim_hsv[1] = anim_from[1] + (anim_to[1] - anim_from[1]) * animation.getAnimatedFraction();
            anim_hsv[2] = anim_from[2] + (anim_to[2] - anim_from[2]) * animation.getAnimatedFraction();
            setBackgroundColor(Color.HSVToColor(anim_hsv));
        });

        int[] set = {
                android.R.attr.textOn,
                android.R.attr.textOff,
                android.R.attr.disabledAlpha,
                android.R.attr.checked
        };

        final TypedArray a = context.obtainStyledAttributes(attrs, set);
        mTextOn = a.getText(0);
        mTextOff = a.getText(1);
        mDisabledAlpha = a.getFloat(2, 0.5f);
        mChecked = a.getBoolean(3, false);
        mContext = context;
        if (mTextOn == null || mTextOff == null) {
            mTextOn = "ON";
            mTextOff = "OFF";
        }
        a.recycle();

        syncCheckState();
    }

    public SideToggle(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SideToggle(Context context) {
        this(context, null);
    }

    @Override
    public void setChecked(boolean checked) {
        if (mChecked != checked) {
            mChecked = checked;
            refreshDrawableState();
            syncCheckState();
        }
    }

    private void syncCheckState() {
        boolean checked = isChecked();

        if (checked && mTextOn != null) {
            setText(mTextOn);
            anim.start();
        } else if (!checked && mTextOff != null) {
            setText(mTextOff);
            anim.reverse();
        }
    }

    @Override
    public void toggle() {
        setChecked(!mChecked);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean performClick() {
        toggle();

        playSoundEffect(SoundEffectConstants.CLICK);

        return true;
    }

    @InspectableProperty
    @ViewDebug.ExportedProperty
    @Override
    public boolean isChecked() {
        return mChecked;
    }

}