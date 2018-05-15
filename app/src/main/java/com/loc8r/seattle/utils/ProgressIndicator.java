package com.loc8r.seattle.utils;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

// Not currently used

// See: https://medium.com/@sreekumar_av/how-to-create-your-own-progressbar-in-android-511419293158
public class ProgressIndicator extends View {
    private int mSize;
    private Paint mPaint;
    private RectF mRect;

    // Called when it is created Programatically
    public ProgressIndicator(Context context) {
        super(context);
        init();
    }

    // Called when view in inflated via XML. param attrs which contain collection
    // of your attributes you passed via XML.
    public ProgressIndicator(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    // this invoked manually to apply any default style you want to apply for your widget.
    public ProgressIndicator(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(Color.RED);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(8f);
        mPaint.setStrokeCap(Paint.Cap.BUTT);
        mRect = new RectF(200,200,800,800);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawRect(mRect,mPaint);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int xPad = getPaddingLeft() + getPaddingRight();
        int yPad = getPaddingTop() + getPaddingBottom();
        int width = getMeasuredWidth() - xPad;
        int height = getMeasuredHeight() - yPad;
        mSize = (width < height) ? width : height;

        // Must call this
        setMeasuredDimension(mSize + xPad, mSize + yPad);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }
}