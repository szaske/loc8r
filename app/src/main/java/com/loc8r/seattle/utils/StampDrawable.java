package com.loc8r.seattle.utils;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.loc8r.seattle.R;


/**
 *   To be deleted.  Created a StampView instead of a drawable
 *   because a view can have a drop shadow.  Leaving the code here
 *   in case i want to return to a drawable for another reason
 */


public class StampDrawable extends Drawable {
    private static final int DEFAULT_COLOR = Color.BLACK;
    private static final Float STROKEWIDTH_PERCENTAGE = .12f;
    private Paint mTextPaint, CircPaint, arcPaint;
    private String mText;
    private int mIntrinsicWidth = 500;
    private int mIntrinsicHeight = 500;
    private Path mTopArc, mLowerArc;
    private Drawable mIcon;
    private Rect bounds, mOuterCircleRect, mInnerCircleRect;

    public StampDrawable(Context context, String text) {

        // Configure Outer Circle
        CircPaint = new Paint();
        CircPaint.setColor(Color.GREEN);
        CircPaint.setStyle(Paint.Style.STROKE);


        // Configure Inner circle
        //mInnerCircleRect = getBounds();
        //mInnerCircleRect.inset(STROKEWIDTH_PERCENTAGE,STROKEWIDTH_PERCENTAGE);

        // remember passed parameters (for some reason)
        mText = text;
        mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        mTextPaint.setTextSize(100);
        mTextPaint.setTextScaleX(1.0f);
        mTextPaint.setTypeface(Typeface.DEFAULT_BOLD);

        mTextPaint.setColor(DEFAULT_COLOR);
        mTextPaint.setTextAlign(Align.CENTER);
        mTextPaint.setStyle(Paint.Style.FILL);



        // Initialize the paint object for the text
//        float textSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,
//                DEFAULT_TEXTSIZE, res.getDisplayMetrics());
        // float textSize = 50f;
        // mPaint.setTextSize(textSize);
//        mIntrinsicWidth = (int) (mPaint.measureText(mText, 0, mText.length()) + .5);
//        mIntrinsicHeight = mPaint.getFontMetricsInt(null);

        // RectF oval = new RectF(50,50,450,450);

        // configure an Top Arc
        mTopArc = new Path();

        PathMeasure test = new PathMeasure(mTopArc,false);
        Log.d("STZ", "The Top Arc length: " + String.valueOf(test.getLength()));
        Log.d("STZ", "Text:" + mText + " length: " + String.valueOf(mTextPaint.measureText(mText)));

        // configure the lower arc
        arcPaint = new Paint();
        arcPaint.setColor(Color.RED);
        arcPaint.setStrokeWidth(10);
        arcPaint.setStyle(Paint.Style.STROKE);

        mLowerArc = new Path();
        // mLowerArc.addArc(oval, -180, -180);

        Resources res =  context.getResources();
        mIcon = res.getDrawable(R.drawable.art_marker);

    }

    @Override
    public void draw(Canvas canvas) {
        Rect bounds = getBounds();


        // make the text 10% of the height
        Float targetFontSize = bounds.height() * .1f;

        // Get Text arc radius
        Float StrokeWidth = bounds.width() * STROKEWIDTH_PERCENTAGE;
        Float textArcRadius = (bounds.width()/2)-StrokeWidth/2;



        // Draw the icon
        Rect innerBounds = copyBounds();
        innerBounds.inset(Math.round(StrokeWidth*2),Math.round(StrokeWidth*2));
        mIcon.setBounds(innerBounds);
        mIcon.draw(canvas);

        Log.d("STZ", "draw: Bounds are L:" + String.valueOf(bounds.left) + " - T:" + String.valueOf(bounds.top)+ " - R:" + String.valueOf(bounds.right)+ " - B:" + String.valueOf(bounds.bottom));
        // draw the rect
        // canvas.drawRect(bounds.left,bounds.top,bounds.right,bounds.bottom,mRectPaint);
        // int width = bounds.right - bounds.left;

        // Draw the circle
        CircPaint.setStrokeWidth(StrokeWidth);
        canvas.drawCircle(bounds.centerX(),bounds.centerY(),textArcRadius,CircPaint);

        // Draw the top text
        mTopArc.addArc(bounds.left,bounds.top,bounds.right,bounds.bottom, -180, 180);
        mTextPaint.setTextSize(targetFontSize);
        canvas.drawTextOnPath(mText, mTopArc, 0, targetFontSize, mTextPaint);

        // Draw bottom text
        mLowerArc.addArc(bounds.left,bounds.top,bounds.right,bounds.bottom, -180, -180);
        canvas.drawTextOnPath("SEPT 4, 2018", mLowerArc, 0, -(StrokeWidth-targetFontSize), mTextPaint);

    }

    @Override
    public int getOpacity() {
        return mTextPaint.getAlpha();
    }


    /**
     *  When a view asks this drawable it's width this is the method called.
     *  see: https://stackoverflow.com/questions/13756910/what-does-android-getintrinsicheight-and-getintrinsicwidth-mean
     *
     * @return the width in pixels (I assume its pixels)
     */
    @Override
    public int getIntrinsicWidth() {
        return mIntrinsicWidth;
    }

    /**
     *  When a view asks this drawable it's height this is the method called.
     *  see: https://stackoverflow.com/questions/13756910/what-does-android-getintrinsicheight-and-getintrinsicwidth-mean
     *
     * @return the height in pixels (I assume its pixels)
     */
    @Override
    public int getIntrinsicHeight() {
        return mIntrinsicHeight;
    }
    @Override
    public void setAlpha(int alpha) {
        mTextPaint.setAlpha(alpha);
    }
    @Override
    public void setColorFilter(ColorFilter filter) {
        mTextPaint.setColorFilter(filter);
    }

}