package com.loc8r.seattle.utils;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by steve on 2/8/2018.
 */

public class CurvedView extends View {
    private static final String YOUR_TEXT = "something cool";
    private Path _arc;

    private Paint _paintText;

    public CurvedView(Context context) {
        super(context);
        init(context);
    }

    public CurvedView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public CurvedView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        //do stuff that was in your original constructor...
        _arc = new Path();
        RectF oval = new RectF(50,100,200,250);;
        _arc.addArc(oval, -180, 200);
        _paintText = new Paint(Paint.ANTI_ALIAS_FLAG);
        _paintText.setStyle(Paint.Style.FILL_AND_STROKE);
        _paintText.setColor(Color.WHITE);
        _paintText.setTextSize(16f);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawTextOnPath(YOUR_TEXT, _arc, 0, 20, _paintText);
        invalidate();
    }
}
