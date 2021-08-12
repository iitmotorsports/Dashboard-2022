package com.iit.dashboard2022.ui.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;

import androidx.annotation.StyleableRes;

import com.google.android.material.button.MaterialButton;
import com.iit.dashboard2022.R;
import com.iit.dashboard2022.ui.anim.ColorAnim;

public class SideToggle extends MaterialButton implements ActionableCheck {
    private final CharSequence mTextOn;
    private final CharSequence mTextOff;
    private final float mTextOnSize, mTextOffSize;

    private final ColorAnim colorAnim;

    public SideToggle(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, R.attr.sideToggleButtonStyle);
        setCheckable(true);

        colorAnim = new ColorAnim(context, R.color.foregroundText, R.color.colorAccent, this::setBackgroundColor);

        int[] set = {
                android.R.attr.textOn,
                android.R.attr.textOff,
                android.R.attr.checked,
                android.R.attr.text,
                android.R.attr.textSize,
                R.attr.textOffSize,
                R.attr.textOnSize,
        };

        final TypedArray a = context.obtainStyledAttributes(attrs, set);

        @StyleableRes
        int i = 0;

        mTextOn = a.getText(i++);
        mTextOff = a.getText(i++);
        super.setChecked(a.getBoolean(i++, false));
        CharSequence mText = a.getText(i++);
        float textSize = a.getDimension(i++, 12.0f * getResources().getDisplayMetrics().scaledDensity);
        mTextOffSize = a.getDimension(i++, textSize) / getResources().getDisplayMetrics().scaledDensity;
        mTextOnSize = a.getDimension(i, textSize) / getResources().getDisplayMetrics().scaledDensity;

        if (mText != null)
            setText(mText);

        setTextSize(textSize);

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

        colorAnim.cancel();

        if (checked) {
            if (mTextOn != null) {
                setText(mTextOn);
                setTextSize(mTextOnSize);
            }
            colorAnim.start();
        } else {
            if (mTextOff != null) {
                setText(mTextOff);
                setTextSize(mTextOffSize);
            }
            colorAnim.reverse();
        }
    }

    @Override
    public void setActionedCheck(boolean checked) {
        if (isChecked() == checked)
            return;
        performClick();
    }

}