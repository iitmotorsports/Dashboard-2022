package com.iit.dashboard2022.ui.layout;

import android.content.Context;
import android.content.res.Resources;
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
import androidx.appcompat.content.res.AppCompatResources;

import com.iit.dashboard2022.R;

public class MaskableFrameLayout extends FrameLayout {
    private final Paint paint;
    @Nullable
    private Drawable drawableMask;
    @Nullable
    private Bitmap mask;

    public MaskableFrameLayout(Context context) {
        this(context, null);
    }

    public MaskableFrameLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MaskableFrameLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setAntiAlias(true);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));

        Resources.Theme theme = context.getTheme();
        if (theme != null) {
            TypedArray a = theme.obtainStyledAttributes(attrs, R.styleable.MaskableLayout, 0, 0);
            try {
                final int drawableResId = a.getResourceId(R.styleable.MaskableLayout_mask, -1);
                if (drawableResId != -1) {
                    drawableMask = AppCompatResources.getDrawable(context, drawableResId);
                }
            } finally {
                if (a != null) {
                    a.recycle();
                }
            }
        }
    }

    private void newMask(@Nullable Drawable drawable) {
        if (drawable != null) {
            if (getMeasuredWidth() > 0 && getMeasuredHeight() > 0) {
                Bitmap mask = Bitmap.createBitmap(getMeasuredWidth(), getMeasuredHeight(), Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(mask);
                drawable.setBounds(0, 0, getMeasuredWidth(), getMeasuredHeight());
                drawable.draw(canvas);
                if (this.mask != null) {
                    this.mask.recycle();
                }
                this.mask = mask;
            }
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int ow, int oh) {
        super.onSizeChanged(w, h, ow, oh);
        if (w > 0 && h > 0) {
            newMask(drawableMask);
        }
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        if (mask != null) {
            canvas.drawBitmap(mask, 0, 0, paint);
        }
    }

}
