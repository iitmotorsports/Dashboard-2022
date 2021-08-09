package com.iit.dashboard2022.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.iit.dashboard2022.R;

public class SidePanel extends ConstraintLayout {
    public SidePanel(Context context) {
        super(context);
        init();
    }

    public SidePanel(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SidePanel(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        View.inflate(getContext(), R.layout.side_panel_layout, this);
    }
}
