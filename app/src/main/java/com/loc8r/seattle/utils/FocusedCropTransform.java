package com.loc8r.seattle.utils;

import android.graphics.Bitmap;
import android.util.Log;

import com.squareup.picasso.Transformation;

/**
 *  A Transform on an image that prioritizes a specific location in the source image.  This
 *  transform will attempt to include the focal point in the returned crop.
 *
 *  Aspect ratio is always determined as width/height
 */

public class FocusedCropTransform implements Transformation {

    private String resultKey;
    private final int neededWidth;
    private final int neededHeight;

    private int focalpointX;
    private int focalpointY;

    private double focalXpercent;
    private double focalYpercent;

    /**
     *  A Transform on an image that prioritizes a specific location in the source image.  This
     *  transform will attempt to include the focal point in the returned crop
     *
     * @param neededWidth The width in pixels of the image needed
     * @param neededHeight The height in pixels of the image needed
     * @param Id A unique integer Id for the the transformation
     * @param percentX The position entered as a percentage on the X axis where the crop should prioritize/focus
     * @param percentY The position entered as a percentage on the Y axis where the crop should prioritize/focus
     */
    public FocusedCropTransform(int neededWidth, int neededHeight, int Id, double percentX, double percentY) {
        this.neededWidth = neededWidth;
        this.neededHeight = neededHeight;

        this.resultKey = String.valueOf(Id);

        //this removes the integer (left side) portion of the float,
        // insuring that these are percentages
        this.focalXpercent = percentX - (int)percentX;
        this.focalYpercent = percentY - (int)percentY;
    }

    /**
     * Transform the source bitmap into a new bitmap. If you create a new bitmap instance, you must
     * call {@link android.graphics.Bitmap#recycle()} on {@code source}. You may return the original
     * if no transformation is required.
     *
     * @param source the original bitmap
     * @return a transformed bitmap
     */
    @Override
    public Bitmap transform(Bitmap source) {
        int resultsWidth, resultsHeight;
        int sourceWidth = source.getWidth();
        int sourceHeight = source.getHeight();

        int newStartX = 0;
        int newStartY = 0;
        double sourceAspectRatio = (double) sourceWidth / sourceHeight;
        double neededAspectRatio = (double) neededWidth / (double) neededHeight;

        // Determine focal coordinates from percentages
        focalpointX = (int) Math.floor(sourceWidth * focalXpercent);
        focalpointY = (int) Math.floor(sourceHeight * focalYpercent);

        // Determine if we need to use width or height as our base.  Aspect ratio will be respected.
        if (sourceAspectRatio >= neededAspectRatio) {
            resultsHeight = source.getHeight();
            resultsWidth = (int) Math.floor(resultsHeight*neededAspectRatio);

            // Now lets slide the inner image on the X axis
            // within the source to create an image focused on the focalpoint X axis
            int potentialX = Math.round(focalpointX - (resultsWidth/2));
            if(potentialX <= 0){  // checks to insure we cannot break left side of source X axis
                newStartX = 0;
            } else {
                if (potentialX+resultsWidth>source.getWidth()){ // check to see if we break the sources max width
                    newStartX = source.getWidth()- resultsWidth;
                } else {
                    //we can slide the width to both the left and right without breaking the bounds of the source image
                    newStartX = potentialX;
                }
            }

        } else {
            resultsWidth = source.getWidth();
            resultsHeight = (int) Math.floor(resultsWidth/neededAspectRatio);

            //Now lets slide the inner image within the source to create an image focused on the focalpoint Y plane
            int potentialY = Math.round(focalpointY - (resultsHeight/2));
            if(potentialY <= 0){  // checks to insure we cannot break left side of source
                newStartY = 0;
            } else {
                if (potentialY+resultsHeight>source.getHeight()){ // check to see if we break the sources max width
                    newStartY = source.getHeight() - resultsHeight;
                } else {
                    //we can slide the wdith to both the left and right without breaking the bounds of the source image
                    newStartY = potentialY;
                }
            }
        }

        // passed parameters in pixels not dp
        // see https://stackoverflow.com/questions/18027054/use-density-independent-pixels-for-width-and-height-when-creating-a-bitmap
        Bitmap result = Bitmap.createBitmap(source,newStartX,newStartY,resultsWidth,resultsHeight);
        Log.d("FocusedCropTransform:", "source: W:"+ source.getWidth()+"dp, H:" + source.getHeight() + "dp, resized to W:" + result.getWidth() + "dp, H:" + result.getHeight()+"dp");

        if (result != source) {
            source.recycle();
        }
        return result;
    }

    /**
     * Returns a unique key for the transformation, used for caching purposes. If the transformation
     * has parameters (e.g. size, scale factor, etc) then these should be part of the key.
     *
     * @return Returns a unique key for the transformation
     */
    @Override
    public String key() {
        return resultKey+neededWidth+"x"+neededHeight;
    }

}
