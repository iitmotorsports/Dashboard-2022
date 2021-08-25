package com.iit.dashboard2022.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.AttrRes;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;

import com.iit.dashboard2022.R;

import java.util.Locale;

public class Indicators extends FrameLayout implements WidgetUpdater.Widget {
    private final RadioButton lagRadio, faultRadio, waitRadio, chargeRadio;
    private final TextView lagTimer;
    private final LinearLayout indicatorLayout;
    private final String lagTimerMSFormat, lagTimerSFormat;
    private final float lagTimerLarge, LagTimerSmall;
    private String currentLagTime = "";

    public enum Indicator {
        Lag,
        Fault,
        Waiting,
        Charging;

        boolean on = false;
    }

    public Indicators(Context context) {
        this(context, null);
    }

    public Indicators(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public Indicators(Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        View.inflate(context, R.layout.widget_indicators, this);

        indicatorLayout = findViewById(R.id.indicatorLayout);
        lagRadio = findViewById(R.id.lagRadio);
        faultRadio = findViewById(R.id.faultRadio);
        waitRadio = findViewById(R.id.waitRadio);
        chargeRadio = findViewById(R.id.chargeRadio);
        lagTimer = findViewById(R.id.lagTimer);

        lagTimerLarge = lagTimer.getTextSize() / getResources().getDisplayMetrics().scaledDensity;
        LagTimerSmall = lagTimerLarge / 1.2f;

        lagTimerMSFormat = context.getString(R.string.indicators_lag_ms_format);
        lagTimerSFormat = context.getString(R.string.indicators_lag_s_format);

        indicatorLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                indicatorLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                ViewGroup.LayoutParams lp = indicatorLayout.getLayoutParams();
                lp.width = indicatorLayout.getMeasuredWidth();
                lp.height = indicatorLayout.getMeasuredHeight();
                indicatorLayout.setLayoutParams(lp);
                updateLagTime();

                for (Indicator i : Indicator.values()) {
                    setIndicator(i, false);
                }
            }
        });

        WidgetUpdater.add(this);
    }

    @UiThread
    private void updateIndicators() {
        lagRadio.setChecked(Indicator.Lag.on);
        int v = Indicator.Lag.on ? View.VISIBLE : View.GONE;
        lagRadio.setVisibility(v);
        lagTimer.setVisibility(v);
        if (v == View.VISIBLE)
            updateLagTime();

        faultRadio.setChecked(Indicator.Fault.on);
        faultRadio.setVisibility(Indicator.Fault.on ? View.VISIBLE : View.GONE);

        waitRadio.setChecked(Indicator.Waiting.on);
        waitRadio.setVisibility(Indicator.Waiting.on ? View.VISIBLE : View.GONE);

        chargeRadio.setChecked(Indicator.Charging.on);
        chargeRadio.setVisibility(Indicator.Charging.on ? View.VISIBLE : View.GONE);
    }

    @UiThread
    private void updateLagTime() {
        if (currentLagTime.length() >= 5)
            lagTimer.setTextSize(LagTimerSmall);
        else
            lagTimer.setTextSize(lagTimerLarge);
        lagTimer.setText(currentLagTime);
    }

    public void setIndicator(Indicator indicator, boolean enabled) {
        switch (indicator) {
            case Lag:
                Indicator.Lag.on = enabled;
                break;
            case Fault:
                Indicator.Fault.on = enabled;
                break;
            case Waiting:
                Indicator.Waiting.on = enabled;
                break;
            case Charging:
                Indicator.Charging.on = enabled;
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + indicator);
        }
        WidgetUpdater.post();
    }

    public void setLagTime(long ms) {
        if (ms == 0) {
            currentLagTime = "";
        } else {
            if (ms >= 1000)
                currentLagTime = String.format(Locale.US, lagTimerSFormat, ms / 1000.0f);
            else
                currentLagTime = String.format(Locale.US, lagTimerMSFormat, ms);
        }
        WidgetUpdater.post();
    }

    @Override
    protected void finalize() throws Throwable {
        WidgetUpdater.remove(this);
        super.finalize();
    }

    @Override
    public void onWidgetUpdate() {
        post(this::updateIndicators);
    }
}
