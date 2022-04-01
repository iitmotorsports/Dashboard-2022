package com.iit.dashboard2022.ui.widget.gauge;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;
import androidx.annotation.StyleableRes;
import com.iit.dashboard2022.R;
import com.iit.dashboard2022.ui.widget.WidgetUpdater;

public class LinearGauge extends View implements WidgetUpdater.Widget {
    private final Paint paint, bgPaint, topTextPaint, bottomTextPaint, valueTextPaint;
    private final Rect mainBar;
    private final String topText;
    private final boolean flipped, vertical;
    private final int[] colors;
    private RectF dst;
    private String bottomText;
    private String unit;
    private String output;
    private int value = 0;

    private float altX, percent = 0, oldPercent = 0;
    private int width = 0, height = 0;
    private int textOffset = 0, topTextY = 0;

    public LinearGauge(Context context) {
        this(context, null);
    }

    public LinearGauge(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LinearGauge(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mainBar = new Rect();

        int[] set = {
                android.R.attr.backgroundTint,
                R.attr.colorHigh,
                R.attr.colorLow,
                R.attr.colorMid,
                R.attr.flipped,
                R.attr.unit,
                R.attr.vertical,
                android.R.attr.textSize,
                android.R.attr.textColor,
                android.R.attr.text,
                android.R.attr.title
        };

        final TypedArray a = context.obtainStyledAttributes(attrs, set);

        @StyleableRes
        int i = 0;
        int BGColor = a.getColor(i++, Color.BLACK);
        int colorHigh = a.getColor(i++, Color.GREEN);
        int colorLow = a.getColor(i++, Color.WHITE);
        int colorMid = a.getColor(i++, 0);
        flipped = a.getBoolean(i++, false);
        unit = (String) a.getText(i++);
        vertical = a.getBoolean(i++, false);
        int textSize = a.getDimensionPixelSize(i++, 14);
        int textColor = a.getColor(i++, Color.WHITE);

        bottomText = (String) a.getText(i++);
        topText = (String) a.getText(i);

        if (unit != null) {
            unit = String.format(unit, 0);
        }

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

        topTextPaint = new Paint();
        topTextPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        topTextPaint.setStyle(Paint.Style.FILL);
        topTextPaint.setColor(textColor);
        topTextPaint.setAntiAlias(true);
        topTextPaint.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, textSize, context.getResources().getDisplayMetrics()));

        bottomTextPaint = new Paint(topTextPaint);
        bottomTextPaint.setTextSize(topTextPaint.getTextSize() / 1.2f);

        valueTextPaint = new Paint(topTextPaint);
        valueTextPaint.setTextAlign(Paint.Align.RIGHT);

        WidgetUpdater.add(this);
    }

    public void setValue(int value) {
        this.value = value;
        WidgetUpdater.post();
    }

    public void setPercent(float percent) {
        this.percent = Math.max(Math.min(percent, 1f), 0f);
        WidgetUpdater.post();
    }

    private void setSize(int x, int y) {
        if (vertical) {
            width = y;
            height = x;
        } else {
            width = x;
            height = y;
        }

        textOffset = (int) (height - height * 0.125f);
        if (topText != null) {
            topTextY = (int) (topTextPaint.getTextSize() + height * 0.125f / 2);
            float[] widths = new float[topText.length()];
            topTextPaint.getTextWidths(topText, widths);
        }
        valueTextPaint.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, height / 1.2f, getResources().getDisplayMetrics()));
        dst = new RectF(0, 0, x, y);
        altX = textOffset / 8f;
        updateValueString();
    }

    private void updateValueString() {
        if (unit != null) {
            output = value + unit;
        } else {
            output = Integer.toString(value);
        }
    }

    protected void onDraw(Canvas canvas) {
        canvas.drawRect(dst, bgPaint);
        if (vertical) {
            canvas.rotate(-90);
            canvas.translate(-width, 0);
        }
        canvas.drawRect(mainBar, paint);
        if (topText != null) {
            canvas.drawText(topText, altX, topTextY, topTextPaint);
        }
        if (bottomText != null) {
            canvas.drawText(bottomText, altX, textOffset, bottomTextPaint);
        }
        canvas.drawText(output, width - altX, textOffset, valueTextPaint);
    }

    protected void onSizeChanged(int x, int y, int ox, int oy) {
        if (x <= 0 || y <= 0) {
            return;
        }
        setSize(x, y);
    }

    @ColorInt
    private int getColor(float ratio) {
        if (ratio <= 0 || colors.length == 1) {
            return colors[0];
        }
        if (ratio >= 1) {
            return colors[colors.length - 1];
        }

        // Calc the sector
        float position = ((colors.length - 1) * ratio);
        int sector = (int) position;
        ratio = position - sector;

        // Get the color to mix
        int sColor = colors[sector];
        int eColor = colors[sector + 1];

        // Manage the transparent case
        if (sColor == Color.TRANSPARENT) {
            sColor = Color.argb(0, Color.red(eColor), Color.green(eColor), Color.blue(eColor));
        }
        if (eColor == Color.TRANSPARENT) {
            eColor = Color.argb(0, Color.red(sColor), Color.green(sColor), Color.blue(sColor));
        }

        // Calculate the result color
        int alpha = (int) (Color.alpha(eColor) * ratio + Color.alpha(sColor) * (1 - ratio));
        int red = (int) (Color.red(eColor) * ratio + Color.red(sColor) * (1 - ratio));
        int green = (int) (Color.green(eColor) * ratio + Color.green(sColor) * (1 - ratio));
        int blue = (int) (Color.blue(eColor) * ratio + Color.blue(sColor) * (1 - ratio));

        // Get the color
        return Color.argb(alpha, red, green, blue);
    }

    public void setBottomText(String bottomText) {
        this.bottomText = bottomText;
    }

    @Override
    protected void finalize() {
        WidgetUpdater.remove(this);
    }

    @Override
    public void onWidgetUpdate() {
        boolean invalid = false;
        if (oldPercent != percent) {
            float dv = WidgetUpdater.truncate((percent - oldPercent) * WidgetUpdater.DV(percent)) - 0.001f;
            oldPercent += dv;
            oldPercent = WidgetUpdater.truncate(oldPercent);

            if (flipped) {
                mainBar.set((int) (width * (1.0f - oldPercent)), 0, width, height);
            } else {
                mainBar.set(0, 0, (int) (width * oldPercent), height);
            }
            paint.setColor(getColor(oldPercent));
            invalid = true;
        }
        updateValueString();
        if (invalid) {
            postInvalidate();
        }
    }
}
