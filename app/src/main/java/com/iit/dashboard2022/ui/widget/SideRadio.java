package com.iit.dashboard2022.ui.widget;

import android.content.Context;
import android.content.res.ColorStateList;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.radiobutton.MaterialRadioButton;
import com.iit.dashboard2022.R;

public class SideRadio extends MaterialRadioButton implements ActionableCheck {
    private static ColorStateList radioOnColorList, radioOffColorList;

    public SideRadio(@NonNull Context context) {
        super(context);
        init(context);
    }

    public SideRadio(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public SideRadio(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        radioOffColorList = context.getColorStateList(R.color.foreground);
        radioOnColorList = context.getColorStateList(R.color.primary);
    }

    @Override
    public void setChecked(boolean checked) {
        super.setChecked(checked);
        if (checked) {
            setButtonTintList(radioOnColorList);
        } else {
            setButtonTintList(radioOffColorList);
        }
    }

    @Override
    public void setActionedCheck(boolean checked) {
        if (isChecked() == checked)
            return;
        performClick();
    }
}
