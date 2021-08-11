package com.iit.dashboard2022.ui.widget;

import android.content.Context;
import android.content.res.ColorStateList;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.radiobutton.MaterialRadioButton;
import com.iit.dashboard2022.R;

public class SideRadio extends MaterialRadioButton {
    private static ColorStateList radioOnColorList, radioOffColorList;

    public SideRadio(@NonNull Context context) {
        super(context);
        init();
    }

    public SideRadio(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SideRadio(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        if (radioOffColorList == null || radioOnColorList == null) {
            radioOffColorList = new ColorStateList(
                    new int[][]
                            {
                                    new int[]{-android.R.attr.state_enabled},
                                    new int[]{android.R.attr.state_enabled}
                            },
                    new int[]
                            {
                                    getResources().getColor(R.color.backgroundText, getContext().getTheme()),
                                    getResources().getColor(R.color.foregroundText, getContext().getTheme())
                            }
            );

            radioOnColorList = new ColorStateList(
                    new int[][]
                            {
                                    new int[]{-android.R.attr.state_enabled},
                                    new int[]{android.R.attr.state_enabled}
                            },
                    new int[]
                            {
                                    getResources().getColor(R.color.backgroundText, getContext().getTheme()),
                                    getResources().getColor(R.color.colorAccent, getContext().getTheme())
                            }
            );
        }
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
}
