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
    private Bitmap overlay;

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

        if (drawableResId != -1) {
            drawableMask = AppCompatResources.getDrawable(context, drawableResId);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int ow, int oh) {
        super.onSizeChanged(w, h, ow, oh);
        if (w > 0 && h > 0 && drawableMask != null) {
            Bitmap mask = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(mask);
            drawableMask.setBounds(0, 0, w, h);
            drawableMask.draw(canvas);

            Bitmap overlay = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            canvas = new Canvas(overlay);

            canvas.drawPaint(dstOver);
            canvas.drawBitmap(mask, 0, 0, paint);

            if (this.overlay != null) {
                this.overlay.recycle();
            }
            this.overlay = overlay;
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
