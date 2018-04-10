package com.loc8r.seattle.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.constraint.ConstraintLayout;
import android.util.AttributeSet;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;


public class CollectionLayout extends ConstraintLayout implements Target {
    public CollectionLayout(Context context) {
        super(context);
    }

    public CollectionLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CollectionLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /**
     * Callback when an image has been successfully loaded.
     * <p>
     * <strong>Note:</strong> You must not recycle the bitmap.
     *
     * @param bitmap
     * @param from
     */
    @Override public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
        setBackground(new BitmapDrawable(getResources(), bitmap));
    }

    /**
     * Callback indicating the image could not be successfully loaded.
     * <p>
     * <strong>Note:</strong> The passed {@link Drawable} may be {@code null} if none has been
     * specified via {@link RequestCreator#error(Drawable)}
     * or {@link RequestCreator#error(int)}.
     *
     * @param e
     * @param errorDrawable
     */
    @Override public void onBitmapFailed(Exception e, Drawable errorDrawable) {
        // TODO Set your error drawable
    }

    /**
     * Callback invoked right before your request is submitted.
     * <p>
     * <strong>Note:</strong> The passed {@link Drawable} may be {@code null} if none has been
     * specified via {@link RequestCreator#placeholder(Drawable)}
     * or {@link RequestCreator#placeholder(int)}.
     *
     * @param placeHolderDrawable
     */
    @Override public void onPrepareLoad(Drawable placeHolderDrawable) {
        // TODO Set your error drawable
    }
}
