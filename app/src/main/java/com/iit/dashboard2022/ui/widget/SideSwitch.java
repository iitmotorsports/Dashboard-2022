package com.iit.dashboard2022.ui.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.iit.dashboard2022.R;

public class SideSwitch extends RelativeLayout {

    public SideSwitch(Context context) {
        super(context, null, R.attr.sideSwitchWidgetStyle);
        init(context, null);
    }

    public SideSwitch(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs, R.attr.sideSwitchWidgetStyle);
        init(context, attrs);
    }

    public SideSwitch(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, R.attr.sideSwitchWidgetStyle);
        init(context, attrs);
    }

    private void init(Context context, @Nullable AttributeSet attrs) {
        View.inflate(context, R.layout.widget_side_switch, this);
        if (attrs != null) {
            int[] set = {
                    android.R.attr.text
            };
            TypedArray a = context.obtainStyledAttributes(attrs, set);
            CharSequence t = a.getText(0);
            a.recycle();
            TextView tv = findViewById(R.id.SwitchWidgetLabel);
            if (t != null)
                tv.setText(t);
        }
    }

}