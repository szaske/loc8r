package com.loc8r.seattle.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.AttributeSet;
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

    private static final Float STROKEWIDTH_PERCENTAGE = .13f;
    private static final Double OUTSIDE_PERCENTAGE = .06;

    // View State items
    private String stampText, stampTimeStamp, placeholderText;
    private int stampIconId;
    private boolean stamped;
    private int stampBackgroundColor = Constants.DEFAULT_STAMP_BACKGROUND_COLOR;
    private Collection stampCollection;

    // View draw variables
    private int mWidth;
    private int mHeight;
    private int insetAmount;
    private Paint mTextPaint, mShadowPaint, backgroundPaint;
    private Paint boundsPaint;
    private TextPaint mPlaceholderTextPaint;
    public Rect bounds, bgBounds;

    int[] mShadowOffsetXY;
    private Path mTopArc, mLowerArc;
    private Drawable mIcon, stampPlaceholder, mBackground;

    public StampView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        if(stampText ==null){
            stampText = "";
        }
        if(stampTimeStamp ==null){
            stampTimeStamp = "";
        }

        placeholderText = "";

        //Set background shape
        //setBackgroundResource(R.drawable.shadow_shape);

        //Configure temp bounds
        boundsPaint = new Paint();
        boundsPaint.setStyle(Paint.Style.STROKE);
        boundsPaint.setStrokeWidth(1);
        boundsPaint.setColor(Color.RED);

        // Configure Outer Circle
        backgroundPaint = new Paint();
        // I need to optimize the reorg how the background color is set to
        // make this
        backgroundPaint.setColor(stampBackgroundColor);
//        backgroundPaint.setStyle(Paint.Style.FILL);
        backgroundPaint.setStyle(Paint.Style.FILL);
        backgroundPaint.setAntiAlias(true);

//        mShadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
//        mShadowPaint.setStyle(Paint.Style.FILL);

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

        //Configure the placeholder
        stampPlaceholder = getResources().getDrawable(R.drawable.stamp_placeholder);
        mPlaceholderTextPaint = new TextPaint();
        mPlaceholderTextPaint.setAntiAlias(true);
        float textSize = 14 * getResources().getDisplayMetrics().density;
        mPlaceholderTextPaint.setTextSize(textSize);
        // mPlaceholderTextPaint.setTypeface(Typeface.DEFAULT_BOLD);
        mPlaceholderTextPaint.setColor(0xFFDDDDDD);

        // set the shadowLayer
        mPlaceholderTextPaint.setShadowLayer(
                textSize * .05f,
                2f, // blurX
                2f, // blurY
                Color.argb(255, 0, 0, 0) // shadow color
        );

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

        int desiredWidth = 1000;
        int desiredHeight = 1000;

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
            mWidth=mHeight;
        } else {
            mHeight=mWidth;
        }

        //MUST CALL THIS
        setMeasuredDimension(mWidth, mHeight);
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);

        // The full drawable bounds
        bounds = new Rect(0,0,mWidth,mHeight);

        // The background circle bounds, 10 percent smaller on each side
        // This allows us to draw outside of the circle with the logo if we prefer
        insetAmount = (int) Math.floor(mWidth*OUTSIDE_PERCENTAGE);
        bgBounds = new Rect(bounds);
        bgBounds.inset(insetAmount,insetAmount);

        if(stamped){
            // Draw the stamp

            //Draw the Stamp background circle
            canvas.drawOval(bgBounds.left,bgBounds.top,bgBounds.right,bgBounds.bottom, backgroundPaint);


            // Draw the icon
            if(mIcon!=null){  //Check if icon exists, if it does, draw it

                mIcon.setBounds(bounds);
                mIcon.draw(canvas);

            } else { //if not draw nothing

            }

            // make the text 10% of the height
            Float targetFontSize = bgBounds.height() * .1f;

            // Get Text arc radius
            Float StrokeWidth = bgBounds.width() * STROKEWIDTH_PERCENTAGE;

            // The text arc is drawn 1 half the Strokewitdth inside of the bounds,
            // so that the stroke width does not get clipped by the view edge.
            // Float textArcRadius = (bounds.width()/2)-StrokeWidth/2;
            Float textArcRadius = (bgBounds.width()-StrokeWidth)/2;

            // Draw the top text
            mTopArc.addArc(bgBounds.left,bgBounds.top,bgBounds.right,bgBounds.bottom, -180, 180);
            mTextPaint.setTextSize(targetFontSize);
            canvas.drawTextOnPath(stampText.toUpperCase(), mTopArc, 0, targetFontSize, mTextPaint);

            // Draw bottom text
            mLowerArc.addArc(bgBounds.left,bgBounds.top,bgBounds.right,bgBounds.bottom, -180, -180);
            canvas.drawTextOnPath(stampTimeStamp, mLowerArc, 0, -(StrokeWidth-targetFontSize), mTextPaint);



        } else {
            // Draw a placeholder instead
            stampPlaceholder.setBounds(bounds);
            stampPlaceholder.draw(canvas);

            StaticLayout mTextLayout = new StaticLayout(placeholderText, mPlaceholderTextPaint, getWidth()/2 - getPaddingLeft() - getPaddingRight(), Layout.Alignment.ALIGN_CENTER, 1.0f, 0.0f, false);

            canvas.save();

            canvas.translate (bounds.centerX () / 2, bounds.centerY () - mTextLayout.getHeight () / 2); // find center of bounds
            mTextLayout.draw(canvas);
            canvas.restore();
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

    public void setPlaceholderText(String placeholderText){
        this.placeholderText = placeholderText;
    }

    public void setPlaceholder(Drawable placeholder){
        this.stampPlaceholder = placeholder;
    }

    public void constructStampViewFromPOI(POI poi){

        // We always create a stamp, because the This section of code is for items that we always create
        Context context = getContext();
        setStampTitleText(poi.getStampText());

        // Set Collection and Text colors
        //if we can get the collection from the POI
        if(StateManager
                .getInstance()
                .getCollections()
                .get(poi.getCollectionId())!=null){
            stampCollection = StateManager
                    .getInstance()
                    .getCollections()
                    .get(poi.getCollectionId());

            // no null check needed, it's checked in the model
            backgroundPaint.setColor(Color.parseColor(stampCollection.getColor()));

            // no null check needed, it's checked in the model
            mTextPaint.setColor(Color.parseColor(stampCollection.getTextColor()));
        }

        // Need to put a check in here
        // This call will crash if a POI appears without a proper Release #
        setStampIcon(context.getResources()
                .getIdentifier("icon_" +  stampCollection.getId(),
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
        SimpleDateFormat newFormat = new SimpleDateFormat("MMM dd - yyyy");

        Date date = null;

        try {
            date = oldFormat.parse(dbTimeStampString);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        //Convert to a new nice looking format
        return newFormat.format(date).toUpperCase();

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

    public String placeholderText() {
        return this.placeholderText;
    }

    public void setStamped(boolean bool) {
        this.stamped = bool;
        // this.invalidate();
    }

    public void setPlaceholderTextColor(int colorAsInt){
        mPlaceholderTextPaint.setColor(colorAsInt);
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

        int index;
        int a,r,g,b;

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

    // See https://stackoverflow.com/questions/27497987/android-elevation-is-not-showing-a-shadow-under-a-customview
    public class FancyOutline extends ViewOutlineProvider {

        int offset;

        FancyOutline(int width, int height) {
            offset = (int) Math.floor(width*OUTSIDE_PERCENTAGE);
        }

        @Override
        public void getOutline(View view, Outline outline) {

            //Outline is a circle
            outline.setOval(offset,offset,view.getWidth()-offset, view.getHeight()-offset);

        }
    } // end of FancyOutline class
}
