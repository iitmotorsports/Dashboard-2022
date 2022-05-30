package com.iit.dashboard2022.ui.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.TypedValue;
import androidx.annotation.StyleableRes;
import com.google.android.material.button.MaterialButton;
import com.iit.dashboard2022.R;
import com.iit.dashboard2022.ui.anim.ColorAnim;

public class SideToggle extends MaterialButton implements ActionableCheck {
    private final CharSequence mTextOn;
    private final CharSequence mTextOff;
    private final float mTextOnSize, mTextOffSize;

    private final ColorAnim colorAnim;
    private boolean toggleMediator = false;

    public SideToggle(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, R.attr.sideToggleButtonStyle);
        setCheckable(true);

        colorAnim = new ColorAnim(context, R.color.foreground, R.color.primary, this::setBackgroundColor);

        @SuppressLint("ResourceType") @StyleableRes
        int[] set = {
                android.R.attr.textOn,
                android.R.attr.textOff,
                android.R.attr.checked,
                android.R.attr.text,
                android.R.attr.textSize,
                R.attr.textOffSize,
                R.attr.textOnSize,
        };

        final TypedArray a = context.obtainStyledAttributes(attrs, set, R.attr.sideToggleButtonStyle, R.style.SideToggleButton);

        @StyleableRes
        int i = 0;

        mTextOn = a.getText(i++);
        mTextOff = a.getText(i++);
        super.setChecked(a.getBoolean(i++, false));
        CharSequence mText = a.getText(i++);
        int textSize = a.getDimensionPixelSize(i++, 24);
        mTextOffSize = a.getDimensionPixelSize(i++, textSize);
        mTextOnSize = a.getDimensionPixelSize(i, textSize);

        if (mText != null) {
            setText(mText);
        }

        setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);

        a.recycle();
        syncCheckState();
    }

    public SideToggle(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SideToggle(Context context) {
        this(context, null);
    }

    private void syncCheckState() {
        boolean checked = isChecked();

        colorAnim.cancel();

        if (checked) {
            if (mTextOn != null) {
                setText(mTextOn);
                setTextSize(TypedValue.COMPLEX_UNIT_PX, mTextOnSize);
            }
            colorAnim.start();
        } else {
            if (mTextOff != null) {
                setText(mTextOff);
                setTextSize(TypedValue.COMPLEX_UNIT_PX, mTextOffSize);
            }
            colorAnim.reverse();
        }
    }

    public void setHasToggleMediator(boolean toggleMediator) {
        this.toggleMediator = toggleMediator;
    }

    @Override
    public boolean performClick() {
        if (toggleMediator) {
            setCheckable(false);
            super.performClick();
            setCheckable(true);
            return false;
        }
        return super.performClick();
    }

    @Override
    public void setChecked(boolean checked) {
        super.setChecked(checked);
        syncCheckState();
    }

    @Override
    public void setActionedCheck(boolean checked) {
        if (isChecked() == checked) {
            return;
        }
        performClick();
    }

}