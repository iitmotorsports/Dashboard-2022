package com.iit.dashboard2022.ui.widget.gauge;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.annotation.StyleableRes;

import com.iit.dashboard2022.R;
import com.iit.dashboard2022.ui.widget.WidgetUpdater;

import java.util.ArrayList;
import java.util.List;

public class SpeedGauge extends View implements WidgetUpdater.Widget {
    private final Bitmap bitmapBG, bitmaskDraw, bitmaskBuffer;
    private final Canvas canvasBG, canvasDraw, canvasBuffer;
    private final Paint paint, maskPaint, dstOver;
    private final Rect mask;
    private final RectF ovalCutout = new RectF(), arcCutout = new RectF();
    float arcSweep = 0f;
    /* User Managed */
    private final float minWidth;
    private final int BGColor;
    private final int[] colorWheel = new int[3];
    private RectF dst;
    private int width = 0, height = 0;
    private int bars = 0;
    private float taper, oldTaper = 0;
    private float percent = 0, oldPercent = 0;
    private int[] maskWidths;

    public SpeedGauge(Context context) {
        this(context, null);
    }

    public SpeedGauge(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SpeedGauge(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mask = new Rect();

        paint = new Paint();
        paint.setStyle(Paint.Style.FILL_AND_STROKE);

        dstOver = new Paint();
        dstOver.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OVER));

        maskPaint = new Paint();
        maskPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        maskPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));

        int[] set = {
                android.R.attr.backgroundTint,
                R.attr.colorHigh,
                R.attr.colorLow,
                R.attr.colorMid,
                android.R.attr.minWidth,
                R.attr.taper,
        };

        final TypedArray a = context.obtainStyledAttributes(attrs, set);

        @StyleableRes
        int i = 0;
        BGColor = a.getColor(i++, Color.BLACK);
        colorWheel[2] = a.getColor(i++, Color.WHITE);
        colorWheel[0] = a.getColor(i++, Color.DKGRAY);
        colorWheel[1] = a.getColor(i++, Color.LTGRAY);
        minWidth = a.getDimension(i++, 8f);
        taper = a.getFloat(i, 0.5f);

        DisplayMetrics dm = context.getResources().getDisplayMetrics();

        int x = dm.widthPixels;
        int y = dm.heightPixels;

        bitmapBG = Bitmap.createBitmap(x, y, Bitmap.Config.ARGB_8888);
        bitmaskDraw = Bitmap.createBitmap(x, y, Bitmap.Config.ARGB_8888);
        bitmaskBuffer = Bitmap.createBitmap(x, y, Bitmap.Config.ARGB_8888);

        canvasBuffer = new Canvas();
        canvasBG = new Canvas();
        canvasDraw = new Canvas();

        a.recycle();
        WidgetUpdater.add(this);
    }

    @Override
    protected void finalize() {
        WidgetUpdater.remove(this);
    }

    int getColor(float percent) {
        if (percent > 2 / 3f)
            return colorWheel[2];
        else if (percent > 1 / 3f)
            return colorWheel[1];
        else
            return colorWheel[0];
    }

    void drawBars(int x, int y) {
        bitmapBG.reconfigure(x, y, Bitmap.Config.ARGB_8888);
        bitmaskDraw.reconfigure(x, y, Bitmap.Config.ARGB_8888);
        bitmaskBuffer.reconfigure(x, y, Bitmap.Config.ARGB_8888);

        canvasBG.setBitmap(bitmapBG);
        canvasDraw.setBitmap(bitmaskDraw);
        canvasBuffer.setBitmap(bitmaskBuffer);

        bitmapBG.eraseColor(0);
        bitmaskDraw.eraseColor(0);
        bitmaskBuffer.eraseColor(0);

        int count = (int) (((x) / 8f));
        float incX = x / (float) count;

        float sd = getResources().getDisplayMetrics().scaledDensity;
        boolean draw = true;
        int xPos = 0;
        float varX = incX * sd;

        List<Float> widths = new ArrayList<>();

        widths.add(0f);

        while (xPos <= x) {

            if (draw) {
                paint.setColor(BGColor);
                canvasBG.drawRect(xPos, 0, xPos + incX + varX, height, paint);
                paint.setColor(getColor((xPos + incX) / width));
                canvasDraw.drawRect(xPos, 0, xPos + incX + varX, height, paint);
                widths.add(xPos + incX + varX);
                varX -= incX / 8;
                if (varX < minWidth - incX) {
                    varX = minWidth - incX;
                }
            } else {
                xPos += varX;
            }

            xPos += incX;
            draw = !draw;
        }
        widths.remove(widths.size() - 1);
        widths.add((float) width);

        bars = widths.size();
        maskWidths = new int[widths.size()];

        int c = 0;
        for (Float i : widths) {
            maskWidths[c++] = (int) (i.intValue() + minWidth / 4);
        }

        maskWidths[0] = 0;

        WidgetUpdater.post();
    }

    private int getCount(float percent) {
        return Math.round((bars - 1) * percent);
    }

    private int getMaskWidth(float percent) {
        if (maskWidths != null) {
            int gc = getCount(percent);
            return maskWidths[gc];
        }
        return 0;
    }

    protected void onDraw(Canvas canvas) {
        canvasBuffer.drawBitmap(bitmapBG, null, dst, null);
        canvasBuffer.drawRect(mask, maskPaint);
        canvasBuffer.drawBitmap(bitmaskDraw, null, dst, dstOver);
        if (oldTaper > 0.75f)
            canvasBuffer.drawArc(arcCutout, 180, arcSweep, true, maskPaint);
        canvasBuffer.drawOval(ovalCutout, maskPaint);
        canvas.drawBitmap(bitmaskBuffer, null, dst, null);
    }

    public void setTaper(float percent) {
        this.taper = percent;
        WidgetUpdater.post();
    }

    protected void onSizeChanged(int x, int y, int ox, int oy) {
        if (x <= 0 || y <= 0)
            return;
        width = x;
        height = y;
        dst = new RectF(0, 0, x, y);
        arcCutout.set(-width, -height * 2, width * 3, height * 2);
        drawBars(x, y);
    }

    public void setPercent(float percent) {
        this.percent = Math.max(Math.min(percent, 1f), 0f);
        WidgetUpdater.post();
    }

    public void onWidgetUpdate() {
        boolean invalid = false;

        if (oldPercent != percent) {
            float dv = WidgetUpdater.truncate((percent - oldPercent) * WidgetUpdater.DV(percent));
            oldPercent += dv;
            oldPercent = WidgetUpdater.truncate(oldPercent);
            invalid = true;
        }

        if (oldTaper != taper) {
            float dv = WidgetUpdater.truncate((taper - oldTaper) * WidgetUpdater.DV(taper));
            oldTaper += dv;
            oldTaper = WidgetUpdater.truncate(oldTaper);
            invalid = true;
        }

        mask.set(0, 0, getMaskWidth(oldPercent), height);

        float OHeight = height / 4f;

        if (oldTaper > 0.75f) {
            float _oldTaper = oldTaper - .75f;
            OHeight -= _oldTaper * height / 4f;
            arcSweep = _oldTaper * -10;
        }
        ovalCutout.set((-oldTaper * width) - width / 2f, OHeight, (oldTaper * width) + width / 2f, height * 4f);

        if (invalid)
            postInvalidate();
    }

}