package com.loc8r.seattle.utils;

import android.graphics.Bitmap;
import com.squareup.picasso.Transformation;

/**
 * Created by steve on 3/5/2018.
 */

public class FocusedCropTransform implements Transformation {

    private final int maxWidth;
    private final int maxHeight;

    public FocusedCropTransform(int maxWidth, int maxHeight) {
        this.maxWidth = maxWidth;
        this.maxHeight = maxHeight;
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
        int targetWidth, targetHeight;
        double aspectRatio;

        if (source.getWidth() > source.getHeight()) {
            targetWidth = maxWidth;
            aspectRatio = (double) source.getHeight() / (double) source.getWidth();
            targetHeight = (int) (targetWidth * aspectRatio);
        } else {
            targetHeight = maxHeight;
            aspectRatio = (double) source.getWidth() / (double) source.getHeight();
            targetWidth = (int) (targetHeight * aspectRatio);
        }

        Bitmap result = Bitmap.createScaledBitmap(source, targetWidth, targetHeight, false);
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
        return maxWidth + "x" + maxHeight;
    }

}
