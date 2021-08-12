package com.iit.dashboard2022.ui.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.StyleableRes;

import com.google.android.material.switchmaterial.SwitchMaterial;
import com.iit.dashboard2022.R;

public class SideSwitch extends RelativeLayout {
    private SwitchMaterial widget_switch;

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
                    android.R.attr.text,
                    R.attr.enableText,
            };

            @StyleableRes
            int i = 0;

            TypedArray a = context.obtainStyledAttributes(attrs, set);
            CharSequence t = a.getText(i++);
            boolean enableText = a.getBoolean(i, true);
            a.recycle();

            TextView tv = findViewById(R.id.SwitchWidgetLabel);
            widget_switch = findViewById(R.id.widget_switch);

            if (!enableText) {
                tv.setVisibility(View.GONE);
                ViewGroup.MarginLayoutParams params = (MarginLayoutParams) widget_switch.getLayoutParams();
                params.setMargins(0, 0, 0, 0);
                widget_switch.setLayoutParams(params);
            }

            if (t != null)
                tv.setText(t);
        }
    }

    public boolean isChecked() {
        return widget_switch.isChecked();
    }

    public void setChecked(boolean checked) {
        widget_switch.setChecked(checked);
    }

    public void setOnCheckedChangeListener(@Nullable CompoundButton.OnCheckedChangeListener listener) {
        widget_switch.setOnCheckedChangeListener(listener);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean performClick() {
        return widget_switch.performClick();
    }

    @Override
    public void setOnClickListener(@Nullable View.OnClickListener listener) {
        widget_switch.setOnClickListener(listener);
    }

}