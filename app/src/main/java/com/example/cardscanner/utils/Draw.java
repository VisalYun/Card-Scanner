package com.example.cardscanner.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.View;

@SuppressLint("ViewConstructor")
public class Draw extends View {

    Rect rect;
    String text;
    Paint boundaryPaint;
    Paint textPaint;

    public Draw(Context context, Rect rect, String text) {
        super(context);
        this.rect = rect;
        this.text = text;
    }

    {
        init();
    }

    private void init() {
        boundaryPaint = new  Paint();
        boundaryPaint.setColor(Color.RED);
        boundaryPaint.setStrokeWidth(10f);
        boundaryPaint.setStyle(Paint.Style.STROKE);

        textPaint = new Paint();
        textPaint.setColor(Color.RED);
        textPaint.setTextSize(50f);
        textPaint.setStyle(Paint.Style.FILL);
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawText(text, (float) rect.centerX(), (float) rect.centerY(), textPaint);
        canvas.drawRect((float) rect.left, (float) rect.top, (float) rect.right, (float) rect.bottom, boundaryPaint);

    }
}
