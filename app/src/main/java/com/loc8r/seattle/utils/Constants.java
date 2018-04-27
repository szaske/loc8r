package com.loc8r.seattle.utils;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.annotation.IntDef;
import android.support.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;

/**
 *
 * A class to keep all constants in one location
 *
 */

public class Constants {
    public static final String PREFERENCES_PREVIOUS_LOCATION_KEY = "previousLocation";
    public static final String PREFERENCES_PREVIOUS_USER_KEY = "previousUser";
    public static final String SELECTED_COLLECTION_KEY = "SELECTED_COLLECTION_KEY";
    public static final String PRETTY_COLLECTION_KEY = "PRETTY_COLLECTION_KEY";
    public static final int DISTANCE_TO_SCAN_MARKERS = 800;

    public static final int DEFAULT_STAMP_BACKGROUND_COLOR = Color.WHITE;
    public static final int DEFAULT_STAMP_TEXT_COLOR = Color.parseColor("#F1F3F3");

    public static final String MARKER = "marker";
    public static final String ICON = "icon";

    // Declare the @IntDef for these constants
    @StringDef({MARKER,ICON})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Loc8rGraphics {}

}
