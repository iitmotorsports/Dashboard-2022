package com.iit.dashboard2022.ui.layout;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;
import androidx.annotation.StyleableRes;
import androidx.appcompat.content.res.AppCompatResources;

import com.iit.dashboard2022.R;

public class OverlayFrameLayout extends FrameLayout {
    private final Paint paint, dstOver;
    @Nullable
    private Drawable drawableMask;
    @Nullable
    private final Bitmap overlay, mask;
    Canvas canvas;

    public OverlayFrameLayout(Context context) {
        this(context, null);
    }

    public OverlayFrameLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public OverlayFrameLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        int[] set = {
                R.attr.mask,
                android.R.attr.color,
        };

        final TypedArray a = context.obtainStyledAttributes(attrs, set);

        @StyleableRes
        int i = 0;
        int drawableResId = a.getResourceId(i++, -1);
        int color = a.getColor(i, context.getResources().getColor(R.color.background, context.getTheme()));
        a.recycle();

        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OUT));

        dstOver = new Paint();
        dstOver.setColor(color);
        dstOver.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OVER));

        DisplayMetrics dm = context.getResources().getDisplayMetrics();

        mask = Bitmap.createBitmap(dm.widthPixels, dm.heightPixels, Bitmap.Config.ARGB_8888);
        overlay = Bitmap.createBitmap(dm.widthPixels, dm.heightPixels, Bitmap.Config.ARGB_8888);
        canvas = new Canvas();

        if (drawableResId != -1) {
            drawableMask = AppCompatResources.getDrawable(context, drawableResId);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int ow, int oh) {
        super.onSizeChanged(w, h, ow, oh);
        if (w > 0 && h > 0 && drawableMask != null && mask != null && overlay != null) {
            mask.reconfigure(w, h, Bitmap.Config.ARGB_8888);
            canvas.setBitmap(mask);
            mask.eraseColor(0);

            drawableMask.setBounds(0, 0, w, h);
            drawableMask.draw(canvas);

            overlay.reconfigure(w, h, Bitmap.Config.ARGB_8888);
            canvas.setBitmap(overlay);
            overlay.eraseColor(0);

            canvas.drawPaint(dstOver);
            canvas.drawBitmap(mask, 0, 0, paint);
        }
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        if (overlay != null) {
            canvas.drawBitmap(overlay, 0, 0, null);
        }
    }

}
