package com.iit.dashboard2022.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.textview.MaterialTextView;
import com.iit.dashboard2022.R;

public class SideLabel extends MaterialTextView {

    public SideLabel(@NonNull Context context) {
        super(context, null, R.attr.sideLabelStyle);
    }

    public SideLabel(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs, R.attr.sideLabelStyle);
    }

    public SideLabel(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, R.attr.sideLabelStyle);
    }
}