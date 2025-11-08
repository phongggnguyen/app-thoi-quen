package com.example.myhabits.utils;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Color;
import android.text.style.ReplacementSpan;
import androidx.annotation.NonNull;

import com.example.myhabits.R;

public class IconSpan extends ReplacementSpan {
    private final int size;
    private final String color;
    private final Paint paint;
    private final int padding;

    public IconSpan(Context context, String color) {
        this.color = color;
        this.size = context.getResources().getDimensionPixelSize(R.dimen.menu_icon_size);
        this.padding = context.getResources().getDimensionPixelSize(R.dimen.menu_icon_padding);
        this.paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        this.paint.setStyle(Paint.Style.FILL);
    }

    @Override
    public int getSize(@NonNull Paint paint, CharSequence text, int start, int end, Paint.FontMetricsInt fm) {
        return size + padding;
    }

    @Override
    public void draw(@NonNull Canvas canvas, CharSequence text, int start, int end, float x, int top, int y, int bottom, @NonNull Paint paint) {
        this.paint.setColor(Color.parseColor(color));
        float radius = size / 3f;
        float centerY = (top + bottom) / 2f;
        canvas.drawCircle(x + radius, centerY, radius - 2, this.paint);
    }
}