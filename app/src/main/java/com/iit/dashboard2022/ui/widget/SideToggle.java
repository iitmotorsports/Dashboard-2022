package com.iit.dashboard2022.ui.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.SoundEffectConstants;

import com.google.android.material.button.MaterialButton;
import com.iit.dashboard2022.R;
import com.iit.dashboard2022.ui.anim.ColorAnim;

public class SideToggle extends MaterialButton {
    private CharSequence mTextOn, mTextOff;
    //    private final float mDisabledAlpha;
//    private boolean mChecked;

    private final ColorAnim colorAnim;

    @SuppressLint("ResourceType")
    public SideToggle(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, R.attr.sideToggleButtonStyle);

        colorAnim = new ColorAnim(context, R.color.foregroundText, R.color.colorAccent, this::setBackgroundColor);

        int[] set = {
                android.R.attr.textOn,
                android.R.attr.textOff,
                android.R.attr.disabledAlpha,
                android.R.attr.checked
        };

        final TypedArray a = context.obtainStyledAttributes(attrs, set);
        mTextOn = a.getText(0);
        mTextOff = a.getText(1);
//        mDisabledAlpha = a.getFloat(2, 0.5f);

        setCheckable(true);

        super.setChecked(a.getBoolean(3, false));

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
        super.setChecked(checked);
        syncCheckState();
    }

    private void syncCheckState() {
        boolean checked = isChecked();

        if (checked && mTextOn != null) {
            setText(mTextOn);
            colorAnim.start();
        } else if (!checked && mTextOff != null) {
            setText(mTextOff);
            colorAnim.reverse();
        }
    }

//    @Override
//    public boolean performClick() {
//        boolean handled = super.performClick();
//        if (!handled) {
//            playSoundEffect(SoundEffectConstants.CLICK);
//        }
//        return handled;
//    }

}