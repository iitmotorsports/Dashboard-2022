package com.iit.dashboard2022.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.button.MaterialButton;
import com.iit.dashboard2022.R;

public class SideButton extends MaterialButton {

    public SideButton(@NonNull Context context) {
        super(context, null, R.attr.sideButtonStyle);
    }

    public SideButton(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs, R.attr.sideButtonStyle);
    }

    public SideButton(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, R.attr.sideButtonStyle);
    }
}