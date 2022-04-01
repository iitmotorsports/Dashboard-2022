package com.iit.dashboard2022.ui.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;
import com.iit.dashboard2022.R;
import com.iit.dashboard2022.ui.anim.AnimSetting;

public class LiveDataEntry extends View implements WidgetUpdater.Widget {

    private final Paint bgPaint = new Paint();
    private final Paint titlePaint = new Paint();
    private final Paint valuePaint = new Paint();

    private final float border, radius;
    private float width = 0, height = 0;
    private String title = "Nil Title";
    private String value = "0    H:0 L:0 A:0";
    private boolean active = true;
    private boolean update = false;
    private boolean enableValue = true;
    private double currentAvg = 0;
    private double currentValue = 0;
    private double currentLow = Long.MAX_VALUE;
    private double currentHigh = Long.MIN_VALUE;

    public LiveDataEntry(String title, Context context) {
        this(context);
        setTitle(title);
    }

    public LiveDataEntry(Context context) {
        this(context, null);
    }

    public LiveDataEntry(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LiveDataEntry(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        float textSize = 10;

        bgPaint.setColor(context.getColor(R.color.midground));
        setActive(false);

        titlePaint.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, textSize, context.getResources().getDisplayMetrics()));
        titlePaint.setColor(context.getColor(R.color.foregroundSecondary));
        titlePaint.setTextAlign(Paint.Align.LEFT);
        titlePaint.setAntiAlias(true);

        valuePaint.set(titlePaint);
        valuePaint.setColor(context.getColor(R.color.foreground));
        valuePaint.setTextAlign(Paint.Align.RIGHT);

        titlePaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));

        border = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, context.getResources().getDisplayMetrics());
        radius = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4f, context.getResources().getDisplayMetrics());
        setMinimumHeight((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (border + textSize) * 2, context.getResources().getDisplayMetrics()));

        WidgetUpdater.add(this);
    }

    @UiThread
    public void setActive(boolean active) {
        if (this.active == active) {
            return;
        }
        this.active = active;
        bgPaint.setAlpha(active ? 255 : 64);
        update = true;
    }

    @UiThread
    public void unActivate() {
        setActive(false);
    }

    public String getTitle() {
        return this.title;
    }

    @UiThread
    public void setTitle(String title) {
        this.title = title;
        update = true;
    }

    public void setEnableValue(boolean enable) {
        enableValue = enable;
        update = true;
    }

    @UiThread
    public void updateValue() {
        this.value = currentValue + "    H:" + currentHigh + " L:" + currentLow + " A:" + currentAvg;
        invalidate();
    }

    public void setRawValue(double value) {
        currentValue = value;
    }

    public void setRawStats(double avg, double low, double high) {
        currentAvg = avg;
        currentLow = low;
        currentHigh = high;
    }

    public double[] getValues() {
        return new double[]{ currentValue, currentAvg, currentLow, currentHigh };
    }

    public void setValue(double value) {
        currentAvg = (currentValue + value) / 2;
        currentValue = value;
        if (value < currentLow) {
            currentLow = value;
        }
        if (value > currentHigh) {
            currentHigh = value;
        }

        updateValue();
        setActive(true);
        postDelayed(this::unActivate, AnimSetting.ANIM_DURATION);
        update = true;
    }

    public void clear() {
        currentAvg = 0;
        currentValue = 0;
        currentLow = 0;
        currentHigh = 0;
        updateValue();
        currentLow = Long.MAX_VALUE;
        currentHigh = Long.MIN_VALUE;
        setActive(false);
        onWidgetUpdate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int ow, int oh) {
        width = w;
        height = h;
        update = true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawRoundRect(border, border, width - border, height - border, radius, radius, bgPaint);
        float yPos = (height / 2) - ((titlePaint.descent() + titlePaint.ascent()) / 2);
        canvas.drawText(title, height / 4f, yPos, titlePaint);
        if (enableValue) {
            canvas.drawText(value, width - height / 4f, yPos, valuePaint);
        }
    }

    @Override
    protected void finalize() throws Throwable {
        WidgetUpdater.remove(this);
        super.finalize();
    }

    @Override
    public void onWidgetUpdate() {
        if (update) {
            postInvalidate();
            update = false;
        }
    }
}
