package com.loc8r.seattle.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewOutlineProvider;

import com.loc8r.seattle.R;
import com.loc8r.seattle.models.Collection;
import com.loc8r.seattle.models.POI;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

/**
 * Created by steve on 3/10/2018.
 */

public class StampView extends View {

    // private static final int DEFAULT_COLOR = ;
    private static final Float STROKEWIDTH_PERCENTAGE = .12f;

    // View State items
    private String stampText, stampTimeStamp;
    private int stampIconId;
    private boolean stamped;
    private int stampBackgroundColor = Constants.DEFAULT_STAMP_BACKGROUND_COLOR;

    // View draw variables
    private int mWidth;
    private int mHeight;
    private Paint mTextPaint, mShadowPaint, backgroundPaint;

    int[] mShadowOffsetXY;
    private Path mTopArc, mLowerArc;
    private Drawable mIcon, stampPlaceholder;

    public StampView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        if(stampText ==null){
            stampText = "";
        }
        if(stampTimeStamp ==null){
            stampTimeStamp = "";
        }

        // Configure Outer Circle
        backgroundPaint = new Paint();
        // I need to optimize the reorg how the background color is set to
        // make this
        backgroundPaint.setColor(stampBackgroundColor);
        backgroundPaint.setStyle(Paint.Style.FILL);
        backgroundPaint.setAntiAlias(true);

        mShadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mShadowPaint.setStyle(Paint.Style.FILL);

        // Configure the default text
        mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setTextSize(100);
        mTextPaint.setTextScaleX(1.0f);
        mTextPaint.setTypeface(Typeface.DEFAULT_BOLD);
        mTextPaint.setLetterSpacing(0.1f);
        mTextPaint.setColor(Constants.DEFAULT_STAMP_TEXT_COLOR);
        mTextPaint.setTextAlign(Paint.Align.CENTER);
        mTextPaint.setStyle(Paint.Style.FILL);

        // configure an Top Arc, used for curved top text
        mTopArc = new Path();

        // configure the lower arc
        mLowerArc = new Path();

        // Configuration dealing with the drop shadow
//        BlurMaskFilter blurFilter = new BlurMaskFilter(13, BlurMaskFilter.Blur.OUTER);
//        mShadowPaint = new Paint();
//        mShadowPaint.setMaskFilter(blurFilter);
//        mShadowOffsetXY = new int[14];

        //Configure the placeholder
        stampPlaceholder = getResources().getDrawable(R.drawable.stamp_placeholder);

        // init background color
        stampBackgroundColor = Color.parseColor("#000000");
        setSaveEnabled(true);
    }

    /**
     * Hook allowing a view to generate a representation of its internal state
     * that can later be used to create a new instance with that same state.
     * This state should only contain information that is not persistent or can
     * not be reconstructed later. For example, you will never store your
     * current position on screen because that will be computed again when a
     * new instance of the view is placed in its view hierarchy.
     * <p>
     * Some examples of things you may store here: the current cursor position
     * in a text view (but usually not the text itself since that is stored in a
     * content provider or other persistent storage), the currently selected
     * item in a list view.
     *
     * See: http://trickyandroid.com/saving-android-view-state-correctly/
     *
     * @return Returns a Parcelable object containing the view's current dynamic
     * state, or null if there is nothing interesting to save.
     * @see #onRestoreInstanceState(Parcelable)
     * @see #saveHierarchyState(SparseArray)
     * @see #dispatchSaveInstanceState(SparseArray)
     * @see #setSaveEnabled(boolean)
     */
    @Override public Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        SavedState ss = new SavedState(superState);

        // We need to save 4 extra things to save state.
        // StampText string
        ss.stampText = stampText;
        // Icon ID int
        ss.stampIconId = stampIconId;

        // Timestamp string
        ss.stampTimestamp = stampTimeStamp;

        // Stamped boolean
        ss.stamped = stamped;

        ss.stampBackgroundColor = stampBackgroundColor;

        // An example
        // ss.state = customState;
        return ss;
    }



    @Override
    public void onRestoreInstanceState(Parcelable state) {
        SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());

        // our custom saved state items
        setStampIcon(ss.stampIconId);
        setStampBackgroundColor(ss.stampBackgroundColor);
        setStampTitleText(ss.stampText);
        setStampTimeStampText(ss.stampTimestamp);
        setStamped(ss.stamped);
    }

    /**
     * Extending the BaseSavedState allows us to extend state saving and save our own custom state variables above.
     *
     **/
    static class SavedState extends BaseSavedState {
        int stampIconId, stampBackgroundColor;
        String stampText, stampTimestamp;
        boolean stamped;

        SavedState(Parcelable superState) {
            super(superState);
        }

        // Reading In from a Parcel
        private SavedState(Parcel in) {
            super(in);
            stampIconId = in.readInt();
            stampBackgroundColor = in.readInt();
            stampText = in.readString();
            stampTimestamp = in.readString();
            stamped = in.readByte() != 0;  //myBoolean == true if byte != 0
        }

        // Writing to a Parcel
        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            // I assume order matters, since we don't seem to be writing the key values
            out.writeInt(stampIconId);
            out.writeInt(stampBackgroundColor);
            out.writeString(stampText);
            out.writeString(stampTimestamp);
            out.writeByte((byte) (stamped ? 1 : 0));     //if myBoolean == true, byte == 1
        }

        public static final Parcelable.Creator<SavedState> CREATOR
                = new Parcelable.Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
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

            //Draw background bounds.  This appears as a circle because we altered the
            // outline

            canvas.drawOval(bounds.left,bounds.top,bounds.right,bounds.bottom, backgroundPaint);

//            Bitmap originalBitmap = drawableToBitmap(mIcon);
//
//                Bitmap convertedBM = applyFadedEffect(originalBitmap);
//                canvas.drawBitmap(convertedBM,innerBounds.left,innerBounds.top,mShadowPaint);
            //canvas.drawBitmap(originalBitmap,innerBounds.left,innerBounds.top,mShadowPaint);

            // make the text 10% of the height
            Float targetFontSize = bounds.height() * .1f;

            // Get Text arc radius
            Float StrokeWidth = bounds.width() * STROKEWIDTH_PERCENTAGE;

            // The text arc is drawn 1 half the Strokewitdth inside of the bounds,
            // so that the stroke width does not get clipped by the view edge.
            // Float textArcRadius = (bounds.width()/2)-StrokeWidth/2;
            Float textArcRadius = (bounds.width()-StrokeWidth)/2;

            // Draw the circle
            // CircPaint.setStrokeWidth(StrokeWidth);
            //canvas.drawCircle(bounds.centerX(),bounds.centerY(),textArcRadius,CircPaint);


            // Draw the top text
            mTopArc.addArc(bounds.left,bounds.top,bounds.right,bounds.bottom, -180, 180);
            mTextPaint.setTextSize(targetFontSize);
            canvas.drawTextOnPath(stampText.toUpperCase(), mTopArc, 0, targetFontSize, mTextPaint);

            // Draw bottom text
            mLowerArc.addArc(bounds.left,bounds.top,bounds.right,bounds.bottom, -180, -180);
            canvas.drawTextOnPath(stampTimeStamp, mLowerArc, 0, -(StrokeWidth-targetFontSize), mTextPaint);


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

//                mIcon.setBounds(innerBounds);
//                mIcon.draw(canvas);

                //Draw the filtered icon
                Bitmap originalBitmap = drawableToBitmap(mIcon);
//
                Bitmap convertedBM = applyFadedEffect(originalBitmap);
                canvas.drawBitmap(convertedBM,innerBounds.left,innerBounds.top,mShadowPaint);
                //canvas.drawBitmap(originalBitmap,innerBounds.left,innerBounds.top,mShadowPaint);

//                Bitmap shadowImage = originalBitmap.extractAlpha(mShadowPaint, mShadowOffsetXY);
//                Bitmap shadowImage32 = shadowImage.copy(Bitmap.Config.ARGB_8888, true);


            } else { //if not draw nothing

            }

        } else {
            // Draw a placeholder instead
            stampPlaceholder.setBounds(bounds);
            stampPlaceholder.draw(canvas);

        }

    }

    private String timestampConversion(String dbTimeStampString) {

        SimpleDateFormat oldFormat = new SimpleDateFormat("dd-MM-yyyy-hh-mm-ss");
        SimpleDateFormat newFormat = new SimpleDateFormat("MMM dd, yyyy");

        Date date = null;

        try {
            date = oldFormat.parse(dbTimeStampString);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        //Convert to a new nice looking format
        return newFormat.format(date);

    }


    public void constructStampViewFromPOI(POI poi){

        // We always create a stamp, because the This section of code is for items that we always create
        Context context = getContext();
        setStampTitleText(poi.getStampText());

        // Set Collection color
        //if we can get the collection from the POI
        if(StateManager
                .getInstance()
                .getCollections()
                .get(poi.getCollection())!=null){
            Collection collection = StateManager
                    .getInstance()
                    .getCollections()
                    .get(poi.getCollection());
            if(collection.getColor()!=null){
                setStampBackgroundColor(Color.parseColor(collection.getColor()));
                backgroundPaint.setColor(Color.parseColor(collection.getColor()));
            } else {
                setStampBackgroundColor(Constants.DEFAULT_STAMP_BACKGROUND_COLOR);
                backgroundPaint.setColor(Constants.DEFAULT_STAMP_BACKGROUND_COLOR);

            }
        } else {
            //if not set a default color
            setStampBackgroundColor(Constants.DEFAULT_STAMP_BACKGROUND_COLOR);
            backgroundPaint.setColor(Constants.DEFAULT_STAMP_BACKGROUND_COLOR);
        }

        setStampIcon(context.getResources()
                .getIdentifier(poi.getIconName(),
                        "drawable",
                        context.getPackageName()));

        if(poi.isStamped()){ // We have a stamp
            setStampTimeStampText(timeStampStringConversion(poi.getStamp().getTimestamp()));
            setStamped(true); //setStamped automatically invalidates the view, so none is needed.

        } else {
            // Create a default timestamp
            SimpleDateFormat defaultDateFormat = new SimpleDateFormat("MMM dd, yyyy");
            String defaultTimestamp = defaultDateFormat.format(new Date());
            setStampTimeStampText(defaultTimestamp);
        }
    }

    /**
     *  Public Stamp title setter
     *
     * @param titleString the string to be used
     */
    private void setStampTitleText (String titleString){
        this.stampText = titleString;
    }

    private void setStampTimeStampText (String tsString){
        this.stampTimeStamp = tsString;
    }

    private String timeStampStringConversion(String dbTimeStampString) {

        SimpleDateFormat oldFormat = new SimpleDateFormat("dd-MM-yyyy-hh-mm-ss");
        SimpleDateFormat newFormat = new SimpleDateFormat("MMM dd, yyyy");

        Date date = null;

        try {
            date = oldFormat.parse(dbTimeStampString);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        //Convert to a new nice looking format
        return newFormat.format(date);

    }

    private void setStampIcon(int collectionIconId) {

        this.stampIconId = collectionIconId;

        // Get and display the correct icon for each collection

        if ( stampIconId != 0 ) {  // the resource exists...
            mIcon = getContext().getResources().getDrawable(stampIconId);
        }
        else {  // checkExistence == 0  // the resource does NOT exist!!
            mIcon = getContext().getDrawable(R.drawable.stamp_placeholder);
        }

    }

    private void setStampBackgroundColor(int color){
        this.stampBackgroundColor = color;
    }

    public void setStamped(boolean bool) {
        this.stamped = bool;
        // this.invalidate();
    }

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

            //Outline is a circle
            outline.setOval(0,0,mOutlineWidth, mOutlineHeight);
            // outline.setRoundRect(15, 15, mOutlineWidth, mOutlineHeight, mOutlineWidth/2);

        }
    }

//    public static final int COLOR_MIN = 0x00;
//    public static final int COLOR_MAX = 0xFF;
    public static final int COLOR_MIN = 10;
    public static final int COLOR_MAX = 80;
    public static final int COLOR_MID = 255;

    public static Bitmap applyFadedEffect(Bitmap source) {
        // get image size
        int width = source.getWidth();
        int height = source.getHeight();
        int[] pixels = new int[width * height];
        // get pixel array from source
        source.getPixels(pixels, 0, width, 0, 0, width, height);
        // a random object
        Random random = new Random();

        int index = 0;
        int a,r,g,b = 0;

        // iteration through pixels
        for(int y = 0; y < height; ++y) {
            for(int x = 0; x < width; ++x) {
                // get current index in 2D-matrix
                index = y * width + x;
                // get random number
                int randChange = random.nextInt(COLOR_MAX + 1 -COLOR_MIN) + COLOR_MIN;
                int numChange = (int) Math.floor(255 * (float) randChange/COLOR_MID);


                // see: https://www.dyclassroom.com/image-processing-project/how-to-get-and-set-pixel-value-in-java
                //get alpha
                a = (pixels[index]>>24) & 0xff;

                if(a==0){
                    // if the pixel is invisible skip it
                    continue;
                } else {
                    //get red
                    r = (pixels[index]>>16) & 0xff;

                    //get green
                    g = (pixels[index]>>8) & 0xff;

                    //get blue
                    b = pixels[index] & 0xff;

                    //Log.d("filter", "applyFadedEffect: Row:" + String.valueOf(y) + " Column:" + String.valueOf(x) + " ARGB:" + String.valueOf(a) + "-" + String.valueOf(r) + "-"+ String.valueOf(g) + "-"+ String.valueOf(b));

                    if (random.nextBoolean()) {
                        // Were getting darker
                        r = Math.max(0, r - numChange);
                        g = Math.max(0, g - numChange);
                        b = Math.max(0, b - numChange);
                    } else {
                        // Were getting brighter
                        r = Math.min(255, r + numChange);
                        g = Math.min(255, g + numChange);
                        b = Math.min(255, b + numChange);
                    }

                    // now we combine the data back into a single number
                    //Log.d("filter", "applyFadedEffect before: " + String.valueOf(pixels[index]));
                    int newCol = Color.argb(a,r,g,b);
                    //.rgb(pixelChange,pixelChange,pixelChange);
                    //pixels[index] = (a<<24) | (r<<16) | (g<<8) | b;
                    pixels[index] = newCol;
                    // Log.d("filter", "applyFadedEffect after: " + String.valueOf(pixels[index]));


                }



                //int randColor = Color.argb() .rgb(pixelChange,pixelChange,pixelChange);
                // OR
                // pixels[index] |= randColor;
            }
        }
        // output bitmap
        Bitmap bmOut = Bitmap.createBitmap(width, height, source.getConfig());
        bmOut.setPixels(pixels, 0, width, 0, 0, width, height);
        return bmOut;
    }

}
