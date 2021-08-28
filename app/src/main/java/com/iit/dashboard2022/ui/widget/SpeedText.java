package com.iit.dashboard2022.ui.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import com.iit.dashboard2022.R;

public class SpeedText extends View implements WidgetUpdater.Widget {
    private static final Paint paint = new Paint();
    private String text = "0";
    private int width = 0, height = 0;

    public SpeedText(Context context) {
        this(context, null);
    }

    public SpeedText(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SpeedText(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        paint.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        paint.setColor(context.getColor(R.color.foreground));
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setAntiAlias(true);

        WidgetUpdater.add(this);
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    protected void onSizeChanged(int w, int h, int ow, int oh) {
        width = w;
        height = h;
        paint.setTextSize(h + (h / 5f));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        float yPos = (height / 2f) - ((paint.descent() + paint.ascent()) / 2);
        canvas.drawText(text, width / 2f, yPos, paint);
    }

    @Override
    protected void finalize() throws Throwable {
        WidgetUpdater.remove(this);
        super.finalize();
    }

    @Override
    public void onWidgetUpdate() {
        postInvalidate();
    }
}
