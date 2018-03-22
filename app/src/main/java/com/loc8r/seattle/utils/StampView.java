package com.loc8r.seattle.utils;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewOutlineProvider;

import com.loc8r.seattle.R;

/**
 * Created by steve on 3/10/2018.
 */

public class StampView extends View {

    private int measuredWidth;
    private int measuredHeight;

    private static final int DEFAULT_COLOR = Color.BLACK;
    private static final Float STROKEWIDTH_PERCENTAGE = .12f;
    private Paint mTextPaint, CircPaint, arcPaint, mShadowPaint;

    // View customizations
    private String mStampTitleText, mStampTimeStampText;
    private int mIconId;
    private boolean stamped;

    private int mWidth;
    private int mHeight;
    int[] mShadowOffsetXY;
    private Path mTopArc, mLowerArc;
    private Drawable mIcon;
    private Rect bounds, mOuterCircleRect, mInnerCircleRect;

    public StampView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        if(mStampTitleText==null){
            mStampTitleText = "";
        }
        if(mStampTimeStampText==null){
            mStampTimeStampText = "";
        }



        // Configure Outer Circle
        CircPaint = new Paint();
        CircPaint.setColor(Color.GREEN);
        CircPaint.setStyle(Paint.Style.STROKE);
        CircPaint.setAntiAlias(true);

        // Configure Inner circle
        //mInnerCircleRect = getBounds();
        //mInnerCircleRect.inset(STROKEWIDTH_PERCENTAGE,STROKEWIDTH_PERCENTAGE);

        // remember passed parameters (for some reason)
        mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        mTextPaint.setTextSize(100);
        mTextPaint.setTextScaleX(1.0f);
        mTextPaint.setTypeface(Typeface.DEFAULT_BOLD);
        mTextPaint.setLetterSpacing(0.1f);


        mTextPaint.setColor(DEFAULT_COLOR);
        mTextPaint.setTextAlign(Paint.Align.CENTER);
        mTextPaint.setStyle(Paint.Style.FILL);

        // configure an Top Arc
        mTopArc = new Path();

        PathMeasure test = new PathMeasure(mTopArc,false);
        Log.d("STZ", "The Top Arc length: " + String.valueOf(test.getLength()));
        Log.d("STZ", "Text:" + mStampTitleText + " length: " + String.valueOf(mTextPaint.measureText(mStampTitleText)));

        // configure the lower arc
        arcPaint = new Paint();
        arcPaint.setColor(Color.RED);
        arcPaint.setStrokeWidth(10);
        arcPaint.setStyle(Paint.Style.STROKE);
        mLowerArc = new Path();

//        // Add a default collection icon to the view
//        Resources res =  context.getResources();
//        mIcon = res.getDrawable(R.drawable.stamp_placeholder);

        // Configuration dealing with the drop shadow
        BlurMaskFilter blurFilter = new BlurMaskFilter(13, BlurMaskFilter.Blur.OUTER);
        mShadowPaint = new Paint();
        mShadowPaint.setMaskFilter(blurFilter);
        mShadowOffsetXY = new int[14];
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {

            setOutlineProvider(new FancyOutline(w, h));
    }


    // See https://stackoverflow.com/questions/12266899/onmeasure-custom-view-explanation

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int desiredWidth = 500;
        int desiredHeight = 500;

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        // int width;
        // int height;

        //Measure Width
        if (widthMode == MeasureSpec.EXACTLY) {
            //Must be this size
            mWidth = widthSize;
        } else if (widthMode == MeasureSpec.AT_MOST) {
            //Can't be bigger than...
            mWidth = Math.min(desiredWidth, widthSize);
        } else {
            //Be whatever you want
            mWidth = desiredWidth;
        }

        //Measure Height
        if (heightMode == MeasureSpec.EXACTLY) {
            //Must be this size
            mHeight = heightSize;
        } else if (heightMode == MeasureSpec.AT_MOST) {
            //Can't be bigger than...
            mHeight = Math.min(desiredHeight, heightSize);
        } else {
            //Be whatever you want
            mHeight = desiredHeight;
        }

        // Measure must be square
        if(mWidth<=mHeight){
            mHeight=mWidth;
        } else {
            mWidth=mHeight;
        }

        //MUST CALL THIS
        setMeasuredDimension(mWidth, mHeight);
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        Rect bounds = new Rect(0,0,mWidth,mHeight);

        if(stamped){
            // Draw the stamp

            // make the text 10% of the height
            Float targetFontSize = bounds.height() * .1f;

            // Get Text arc radius
            Float StrokeWidth = bounds.width() * STROKEWIDTH_PERCENTAGE;

            // The text arc is drawn 1 half the Strokewitdth inside of the bounds,
            // so that the stroke with does not get clipped by the view edge.
            // Float textArcRadius = (bounds.width()/2)-StrokeWidth/2;
            Float textArcRadius = (bounds.width()-StrokeWidth)/2;

            // Draw the circle
            CircPaint.setStrokeWidth(StrokeWidth);
            canvas.drawCircle(bounds.centerX(),bounds.centerY(),textArcRadius,CircPaint);

            // Draw the top text
            mTopArc.addArc(bounds.left,bounds.top,bounds.right,bounds.bottom, -180, 180);
            mTextPaint.setTextSize(targetFontSize);
            canvas.drawTextOnPath(mStampTitleText, mTopArc, 20, targetFontSize, mTextPaint);

            // Draw bottom text
            mLowerArc.addArc(bounds.left,bounds.top,bounds.right,bounds.bottom, -180, -180);
            canvas.drawTextOnPath(mStampTimeStampText, mLowerArc, 0, -(StrokeWidth-targetFontSize), mTextPaint);


            /**
             * Draw the icon.  First we need to create an area inside the band,
             * And create a bounds rectangle.  The bounds need to be in the correct aspect ratio
             * or the icon will not be drawn correctly.
             *
             * *** This assumes that only the width needs to change.  I'm not sure this will work correctly
             * if height needs to be adjusted.***
             *
             **/

            if(mIcon!=null){  //Check if icon exists, if it does, draw it
                // See https://stackoverflow.com/questions/4931892/why-does-the-division-of-two-integers-return-0-0-in-java
                float iconAspectRatio = ((float) mIcon.getIntrinsicWidth()) / mIcon.getIntrinsicHeight();

                Rect innerBounds = new Rect(0,0,mWidth,mHeight);

                innerBounds.inset(Math.round(StrokeWidth),Math.round(StrokeWidth));
                int newWidth = innerBounds.width();
                int neededWidth = Math.round(newWidth*iconAspectRatio);
                int WidthInsetAmountforRatio = Math.round((newWidth-neededWidth)/2);
                innerBounds.inset(WidthInsetAmountforRatio,0);

                mIcon.setBounds(innerBounds);
                mIcon.draw(canvas);

                //Draw the drop shadow
//                Bitmap originalBitmap = drawableToBitmap(mIcon);
//                Bitmap shadowImage = originalBitmap.extractAlpha(mShadowPaint, mShadowOffsetXY);
//                Bitmap shadowImage32 = shadowImage.copy(Bitmap.Config.ARGB_8888, true);
//                canvas.drawBitmap(shadowImage32,innerBounds.left,innerBounds.top,mShadowPaint);

            } else { //if not draw nothing

            }

        } else {
            // Draw a placeholder instead
        }

    }

//    private void getStampText(Context context, AttributeSet attrs) {
//        TypedArray typedArray = context.getTheme().obtainStyledAttributes(attrs, R.styleable.StampView, 0, 0);
//
//        try {
//            mStampTitleText = typedArray.getString(R.styleable.StampView_stampTitle);
//            mStampTimeStampText = typedArray.getString(R.styleable.StampView_stampTimeStamp);
//        } finally {
//            typedArray.recycle();
//        }
//    }

    /**
     *  Public Stamp title setter
     *
     * @param titleString the string to be used
     */
    public void setStampTitleText (String titleString){
        this.mStampTitleText = titleString;
    }

    public void setStampTimeStampText (String tsString){
        this.mStampTimeStampText = tsString;
    }

    public void setStampIcon(Context context, int collectionIconId) {
        this.mIconId = collectionIconId;

        // Get and display the correct icon for each collection

        if ( mIconId != 0 ) {  // the resource exists...
            mIcon = context.getResources().getDrawable(mIconId);
        }
        else {  // checkExistence == 0  // the resource does NOT exist!!
            mIcon = context.getDrawable(R.drawable.stamp_placeholder);
        }

    }

    public void setStamped(boolean bool) { this.stamped = bool; }
    public boolean isStamped() { return this.stamped; }

    public static Bitmap drawableToBitmap (Drawable drawable) {

        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable)drawable).getBitmap();
        }

        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }




    // See https://stackoverflow.com/questions/27497987/android-elevation-is-not-showing-a-shadow-under-a-customview
    public class FancyOutline extends ViewOutlineProvider {

        int mOutlineWidth;
        int mOutlineHeight;

        FancyOutline(int width, int height) {
            this.mOutlineWidth = width;
            this.mOutlineHeight = height;
        }

        @Override
        public void getOutline(View view, Outline outline) {
            outline.setRoundRect(0, 0, mOutlineWidth, mOutlineHeight, mOutlineWidth/2);
        }
    }

}
