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
import android.view.View;

import androidx.annotation.Nullable;
import androidx.annotation.StyleableRes;

import com.iit.dashboard2022.R;

public class SpeedGauge extends View implements GaugeUpdater.Gauge {
    private Bitmap bitmapBG, bitmaskDraw, bitmaskBuffer;
    private Canvas canvasBuffer;
    private RectF dst;
    private final Paint paint, maskPaint, dstOver;
    private final Rect mask;

    private int height = 0;
    private int bars = 0;
    private float incX = 1;
    private float percent = 0, oldPercent = 0;

    /* User Managed */
    private final float taper;
    private final float minWidth;
    private final int BGColor;
    private final int[] colorWheel = new int[3];

    public SpeedGauge(Context context) {
        this(context, null);
    }

    public SpeedGauge(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SpeedGauge(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        GaugeUpdater.start();

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
        colorWheel[0] = a.getColor(i++, Color.WHITE);
        colorWheel[1] = a.getColor(i++, Color.WHITE);
        minWidth = a.getDimension(i++, 8f);
        taper = a.getFloat(i, 2.5f);

        a.recycle();
        GaugeUpdater.add(this);
    }

    @Override
    protected void finalize() {
        GaugeUpdater.remove(this);
    }

    int getColor(int barCount) {
        if (barCount < bars / 3)
            return colorWheel[2];
        else if (barCount < bars * 2 / 3)
            return colorWheel[1];
        else
            return colorWheel[0];
    }

    void drawBars(int x, int y) {
        bitmapBG = Bitmap.createBitmap(x, y, Bitmap.Config.ARGB_8888);
        bitmaskDraw = Bitmap.createBitmap(x, y, Bitmap.Config.ARGB_8888);
        bitmaskBuffer = Bitmap.createBitmap(x, y, Bitmap.Config.ARGB_8888);

        canvasBuffer = new Canvas(bitmaskBuffer);
        Canvas canvasBG = new Canvas(bitmapBG);
        Canvas canvasDraw = new Canvas(bitmaskDraw);

        bars = (int) ((x / 8f) / getResources().getDisplayMetrics().scaledDensity);
        int count = bars;
        incX = x / (float) count;
        float incY = y / (float) count;

        boolean draw = true;
        int xPos = 0, yPos = (int) (incY * 16);
        float varX = incX / taper;

        while (count > 0) {
            if (draw) {
                paint.setColor(BGColor);
                canvasBG.drawRect(xPos, 0, xPos + incX + varX, yPos, paint);
                paint.setColor(getColor(count));
                canvasDraw.drawRect(xPos, 0, xPos + incX + varX, yPos, paint);
                varX -= incX / (taper * 8.0f);
                if (varX < minWidth - incX) {
                    varX = minWidth - incX;
                }
            } else {
                xPos += varX;
            }

            yPos += incY;
            xPos += incX;
            draw = !draw;
            count--;
        }
    }

    private float DV(float x) {
        return (float) Math.max((0.5 - (Math.pow(x, 2)) / 8), 0.01f);
    }

    private int maskWidth(float percent) { // TODO: memoize
        int count = (int) Math.ceil((bars + 2) * percent);

        int xPos = 0;
        boolean draw = true;
        float varX = incX / taper;

        int seq = 0, seqX = 0;

        // TODO: remove need for while loop to get mask width when drawing
        while (count > 0) {
            if (draw) {
                varX -= incX / (taper * 8.0f);
                if (varX < minWidth - incX) {
                    varX = minWidth - incX;
                }
            } else {
                xPos += varX;
            }
            if (seq % 2 == 0) {
                seqX = xPos;
            }
            xPos += incX;
            draw = !draw;
            count--;
            seq++;
        }
        return (int) (seqX - (minWidth / 2));
    }


    protected void onDraw(Canvas canvas) {
        canvasBuffer.drawBitmap(bitmapBG, null, dst, null);
        canvasBuffer.drawRect(mask, maskPaint);
        canvasBuffer.drawBitmap(bitmaskDraw, null, dst, dstOver);
        canvas.drawBitmap(bitmaskBuffer, null, dst, null);
    }

    protected void onSizeChanged(int x, int y, int ox, int oy) {
        if (x <= 0 || y <= 0)
            return;
        height = y;
        dst = new RectF(0, 0, x, y);
        drawBars(x, y);
    }

    private float truncate(float val) {
        return ((int) Math.ceil(val * 1000)) / 1000.0f;
    }

    public void setPercent(float percent) {
        this.percent = Math.max(Math.min(percent, 1f), 0f);
        GaugeUpdater.post();
    }

    public void update() {
        if (oldPercent != percent) {
            float dv = truncate((percent - oldPercent) * DV(percent));
            oldPercent += dv;
            oldPercent = truncate(oldPercent);

            mask.set(0, 0, maskWidth(oldPercent), height);
            postInvalidate();
        }
    }

}