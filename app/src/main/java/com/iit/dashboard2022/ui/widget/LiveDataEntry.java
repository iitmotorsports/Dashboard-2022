package com.iit.dashboard2022.ui.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.annotation.UiThread;

import com.iit.dashboard2022.R;
import com.iit.dashboard2022.ui.anim.AnimSetting;

public class LiveDataEntry extends View implements WidgetUpdater.Widget {

    private static final Paint bgPaint = new Paint();
    private static final Paint titlePaint = new Paint();
    private static final Paint valuePaint = new Paint();

    private final float border, radius;
    private float width = 0, height = 0;
    private String title = "Nil Title";
    private String value = "0    H:0 L:0 A:0";
    private boolean update = false;

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

        float textSize = 16;

        bgPaint.setColor(context.getColor(R.color.midground));
        setActive(false);

        titlePaint.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, textSize, context.getResources().getDisplayMetrics()));
        titlePaint.setColor(context.getColor(R.color.foreground));
        titlePaint.setTextAlign(Paint.Align.LEFT);
        titlePaint.setAntiAlias(true);

        valuePaint.set(titlePaint);
        valuePaint.setTextAlign(Paint.Align.RIGHT);

        titlePaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));

        border = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, context.getResources().getDisplayMetrics());
        radius = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, context.getResources().getDisplayMetrics());
        setMinimumHeight((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, (border + textSize) * 2, context.getResources().getDisplayMetrics()));

        WidgetUpdater.add(this);
    }

    @UiThread
    private void setActive(boolean active) {
        bgPaint.setAlpha(active ? 255 : 64);
        update = true;
    }

    @UiThread
    private void unActivate() {
        setActive(false);
    }

    @UiThread
    public void setTitle(String title) {
        this.title = title;
        Rect bounds = new Rect();
        titlePaint.getTextBounds(title, 0, title.length(), bounds);
        setMinimumWidth(bounds.width() * 2);
        update = true;
    }

    long currentAvg = 0;
    long currentValue = 0;
    long currentLow = Long.MAX_VALUE;
    long currentHigh = Long.MIN_VALUE;

    private void updateValue() {
        this.value = currentValue + "    H:" + currentHigh + " L:" + currentLow + " A:" + currentAvg;
    }

    public void setValue(long value) {
        currentAvg = (currentValue + value) / 2;
        currentValue = value;
        if (value < currentLow)
            currentLow = value;
        if (value > currentHigh)
            currentHigh = value;

        updateValue();
        setActive(true);
        postDelayed(this::unActivate, AnimSetting.ANIM_DURATION);
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
        canvas.drawText(value, width - height / 4f, yPos, valuePaint);
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
