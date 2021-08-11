package com.iit.dashboard2022.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.iit.dashboard2022.R;
import com.iit.dashboard2022.ui.widget.SideRadio;

public class SidePanel extends ConstraintLayout {
    private final SideRadio asciiRButton, hexRButton, rawRButton;

    public SidePanel(Context context) {
        this(context, null);
    }

    public SidePanel(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SidePanel(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        View.inflate(getContext(), R.layout.side_panel_layout, this);

        asciiRButton = findViewById(R.id.asciiRButton);
        hexRButton = findViewById(R.id.hexRButton);
        rawRButton = findViewById(R.id.rawRButton);

    }
}
