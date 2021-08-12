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
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.annotation.StyleableRes;

import com.iit.dashboard2022.R;
import com.iit.dashboard2022.ui.anim.AnimSetting;

import java.util.HashSet;
import java.util.Set;

public class SpeedGauge extends View {
    private static final Set<SpeedGauge> gauges = new HashSet<>();
    private static final Handler updater = new Handler();
    private static final Runnable update = new Runnable() {
        @Override
        public void run() {
            for (SpeedGauge sg : gauges) {
                sg.update();
            }
            updater.postDelayed(this, AnimSetting.ANIM_UPDATE_MILLIS);
        }
    };
    private static boolean posted = false;
    private Bitmap bitmapBG, bitmapMask, bitmaskDraw;
    private Canvas canvasMask;
    private Paint paint, maskPaint;
    private RectF dst;
    private Rect mask;
    private int height = 0;
    private int bars = 0;
    private int currentWidth = 0;
    private float incX = 1;
    private float percent = 0, oldPercent = 0;

    /* User Managed */
    private float taper;
    private float minWidth;
    private int BGColor;
    private final int[] colorWheel = new int[3];

    public SpeedGauge(Context context) {
        this(context, null);
    }

    public SpeedGauge(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SpeedGauge(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    void init(Context context, AttributeSet attrs) {
        mask = new Rect();
        paint = new Paint();
        paint.setStyle(Paint.Style.FILL_AND_STROKE);

        maskPaint = new Paint();
        maskPaint.setColor(Color.WHITE);
        maskPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
        maskPaint.setStyle(Paint.Style.FILL_AND_STROKE);

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
        gauges.add(this);
        if (!posted) {
            posted = true;
            updater.post(update);
        }
    }

    @Override
    protected void finalize() {
        gauges.remove(this);
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
        Canvas canvasBG = new Canvas(bitmapBG);
        bitmapMask = Bitmap.createBitmap(x, y, Bitmap.Config.ARGB_8888);
        canvasMask = new Canvas(bitmapMask);
        bitmaskDraw = Bitmap.createBitmap(x, y, Bitmap.Config.ARGB_8888);
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

    private int maskWidth(float percent) {
        int count = (int) Math.ceil((bars + 1) * percent);

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
        mask.set(0, 0, currentWidth, height);
        canvasMask.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        canvasMask.drawRect(mask, paint);

        canvas.drawBitmap(bitmapBG, null, dst, null);

        canvas.saveLayer(dst, null, Canvas.ALL_SAVE_FLAG);
        canvas.drawBitmap(bitmaskDraw, null, dst, null);
        canvas.drawBitmap(bitmapMask, null, dst, maskPaint);
        canvas.restore();
    }

    protected void onSizeChanged(int x, int y, int ox, int oy) {
        if (x <= 0 || y <= 0)
            return;
        height = y;
        dst = new RectF(0, 0, x, y);
        drawBars(x, y);
    }

    public void setPercent(float percent) {
        this.percent = Math.max(Math.min(percent, 1f), 0f);
    }

    private void update() {
        if (oldPercent == percent)
            return;
        oldPercent += (percent - oldPercent) * DV(percent);
        this.currentWidth = maskWidth(oldPercent);
        invalidate();
    }
}