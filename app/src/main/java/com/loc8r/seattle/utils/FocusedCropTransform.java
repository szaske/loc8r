package com.loc8r.seattle.utils;

import android.graphics.Bitmap;
import com.squareup.picasso.Transformation;

/**
 *  Aspect ratio is always determined as width/height
 */

public class FocusedCropTransform implements Transformation {

    private final int neededWidth;
    private final int neededHeight;

    private final int focalpointX;
    private final int focalpointY;

    public FocusedCropTransform(int neededWidth, int neededHeight, int focalpointX, int focalpointY) {
        this.neededWidth = neededWidth;
        this.neededHeight = neededHeight;
        this.focalpointX = focalpointX;
        this.focalpointY = focalpointY;
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

        int newStartX = 0;
        int newStartY = 0;
        double sourceAspectRatio = (double) source.getWidth() / (double) source.getHeight();
        double neededAspectRatio = (double) neededWidth / (double) neededHeight;

        if (sourceAspectRatio > neededAspectRatio) {
            resultsHeight = source.getHeight();
            resultsWidth = (int) Math.floor(resultsHeight*neededAspectRatio);

        } else {
            resultsWidth = source.getWidth();
            resultsHeight = (int) Math.floor(resultsWidth/neededAspectRatio);
        }

        //Now lets slide the inner image within the source to create an image focused on the focalpoint X plane
        int potentialX = Math.round(focalpointX - (resultsWidth/2));
        if(potentialX <= 0){  // checks to insure we cannot break left side of source
            newStartX = 0;
        } else {
            if (potentialX+resultsWidth>source.getWidth()){ // check to see if we break the sources max width
                newStartX = source.getWidth()- resultsWidth;
            } else {
                //we can slide the wdith to both the left and right without breaking the bounds of the source image
                newStartX = potentialX;
            }
        }

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

        Bitmap result = Bitmap.createBitmap(source,newStartX,newStartY,resultsWidth,resultsHeight);
        if (result != source) {
            source.recycle();
        }
        return result;
    }


    // Old transform
//    public Bitmap transform(Bitmap source) {
//        int targetWidth, targetHeight;
//        double sourceAspectRatio = (double) source.getWidth() / (double) source.getHeight();
//        double neededAspectRatio = (double) neededWidth / (double) neededHeight;
//
//        if (source.getWidth() > source.getHeight()) {
//            targetWidth = maxWidth;
//            aspectRatio = (double) source.getHeight() / (double) source.getWidth();
//            targetHeight = (int) (targetWidth * aspectRatio);
//        } else {
//            targetHeight = maxHeight;
//            aspectRatio = (double) source.getWidth() / (double) source.getHeight();
//            targetWidth = (int) (targetHeight * aspectRatio);
//        }
//
//        Bitmap result = Bitmap.createScaledBitmap(source, targetWidth, targetHeight, false);
//        if (result != source) {
//            source.recycle();
//        }
//        return result;
//    }



    /**
     * Returns a unique key for the transformation, used for caching purposes. If the transformation
     * has parameters (e.g. size, scale factor, etc) then these should be part of the key.
     *
     * @return Returns a unique key for the transformation
     */
    @Override
    public String key() {
        return neededWidth + "x" + neededHeight;
    }

}
