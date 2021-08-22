package com.iit.dashboard2022.ui.widget;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
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

import com.iit.dashboard2022.R;
import com.iit.dashboard2022.ui.UITester;

import java.util.Locale;

public class Indicators extends FrameLayout implements UITester.TestUI {
    private static final Handler uiHandle = new Handler(Looper.getMainLooper());

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
        Charging,
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

        UITester.addTest(this);
    }

    public void setIndicator(Indicator indicator, boolean enabled) {
        RadioButton rb;
        int visibility = enabled ? View.VISIBLE : View.GONE;
        switch (indicator) {
            case Lag:
                rb = lagRadio;
                lagTimer.postDelayed(() -> lagTimer.setVisibility(visibility), 10);
                break;
            case Fault:
                rb = faultRadio;
                break;
            case Waiting:
                rb = waitRadio;
                break;
            case Charging:
                rb = chargeRadio;
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + indicator);
        }
        rb.postDelayed(() -> {
            rb.setChecked(enabled);
            rb.setVisibility(visibility);
        }, 10);
    }

    private void updateLagTime() {
        if (currentLagTime.length() >= 5)
            lagTimer.setTextSize(LagTimerSmall);
        else
            lagTimer.setTextSize(lagTimerLarge);
        lagTimer.setText(currentLagTime);
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
        uiHandle.post(this::updateLagTime);
    }

    @Override
    protected void finalize() throws Throwable {
        UITester.removeTest(this);
        super.finalize();
    }

    @Override
    public void testUI(float percent) {
        setLagTime((long) (percent * percent * 10000));
        if (percent == 0) {
            for (Indicator i : Indicator.values()) {
                setIndicator(i, false);
            }
        } else {
            for (Indicator i : Indicator.values()) {
                if (i != Indicator.Lag && UITester.Rnd.nextFloat() > 0.9)
                    setIndicator(i, percent > 0.5);
            }
            setIndicator(Indicator.Lag, true);
            setLagTime((long) (percent * 5000));
        }
    }
}
