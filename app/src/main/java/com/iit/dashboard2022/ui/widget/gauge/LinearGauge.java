package com.iit.dashboard2022.ui.widget.gauge;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;
import androidx.annotation.StyleableRes;

import com.iit.dashboard2022.R;

public class LinearGauge extends View implements GaugeUpdater.Gauge {
    private RectF dst;
    private final Paint paint, bgPaint;
    private final Rect mainBar;

    private boolean flipped;
    private final int[] colors;

    private float percent = 0, oldPercent = 0;
    private int width = 0, height = 0;

    public LinearGauge(Context context) {
        this(context, null);
    }

    public LinearGauge(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LinearGauge(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        GaugeUpdater.start();

        mainBar = new Rect();

        int[] set = {
                android.R.attr.backgroundTint,
                R.attr.colorHigh,
                R.attr.colorLow,
                R.attr.colorMid,
                R.attr.flipped
        };

        final TypedArray a = context.obtainStyledAttributes(attrs, set);

        @StyleableRes
        int i = 0;
        int BGColor = a.getColor(i++, Color.BLACK);
        int colorHigh = a.getColor(i++, Color.GREEN);
        int colorLow = a.getColor(i++, Color.WHITE);
        int colorMid = a.getColor(i++, 0);
        flipped = a.getBoolean(i, false);

        if (colorMid != 0) {
            colors = new int[]{
                    colorLow, colorMid, colorHigh
            };
        } else {
            colors = new int[]{
                    colorLow, colorHigh
            };
        }

        a.recycle();

        paint = new Paint();
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        paint.setColor(colors[0]);

        bgPaint = new Paint();
        bgPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        bgPaint.setColor(BGColor);

        GaugeUpdater.add(this);
    }

    public void setPercent(float percent) {
        this.percent = Math.max(Math.min(percent, 1f), 0f);
        GaugeUpdater.post();
    }

    private void setSize(int x, int y) {
        width = x;
        height = y;

        dst = new RectF(0, 0, x, y);
    }

    protected void onDraw(Canvas canvas) {
        canvas.drawRect(dst, bgPaint);
        canvas.drawRect(mainBar, paint);
    }

    protected void onSizeChanged(int x, int y, int ox, int oy) {
        if (x <= 0 || y <= 0)
            return;
        setSize(x, y);
    }

    @ColorInt
    private int getColor(float ratio, boolean isSmooth) {
        if (ratio <= 0 || colors.length == 1)
            return colors[0];
        if (ratio >= 1)
            return colors[colors.length - 1];

        // Smooth value
        if (isSmooth) {
            // Calc the sector
            float position = ((colors.length - 1) * ratio);
            int sector = (int) position;
            ratio = position - sector;

            // Get the color to mix
            int sColor = colors[sector];
            int eColor = colors[sector + 1];

            // Manage the transparent case
            if (sColor == Color.TRANSPARENT)
                sColor = Color.argb(0, Color.red(eColor), Color.green(eColor), Color.blue(eColor));
            if (eColor == Color.TRANSPARENT)
                eColor = Color.argb(0, Color.red(sColor), Color.green(sColor), Color.blue(sColor));

            // Calculate the result color
            int alpha = (int) (Color.alpha(eColor) * ratio + Color.alpha(sColor) * (1 - ratio));
            int red = (int) (Color.red(eColor) * ratio + Color.red(sColor) * (1 - ratio));
            int green = (int) (Color.green(eColor) * ratio + Color.green(sColor) * (1 - ratio));
            int blue = (int) (Color.blue(eColor) * ratio + Color.blue(sColor) * (1 - ratio));

            // Get the color
            return Color.argb(alpha, red, green, blue);

        } else {
            // Rough value
            int sector = (int) (colors.length * ratio);
            return colors[sector];
        }
    }

    @Override
    protected void finalize() {
        GaugeUpdater.remove(this);
    }

    @Override
    public void update() {
        if (oldPercent != percent) {
            float dv = GaugeUpdater.truncate((percent - oldPercent) * GaugeUpdater.DV(percent));
            oldPercent += dv;
            oldPercent = GaugeUpdater.truncate(oldPercent);

            if (flipped)
                mainBar.set((int) (width * (1.0f - percent)), 0, width, height);
            else
                mainBar.set(0, 0, (int) (width * percent), height);
            paint.setColor(getColor(percent, true));
            postInvalidate();
        }
    }
}
