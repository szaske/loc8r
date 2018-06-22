package com.loc8r.seattle.utils;

import android.graphics.Color;
import android.support.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 *
 * A class to keep all constants in one location
 *
 */

public class Constants {
    public static final String PREFERENCES_PREVIOUS_LOCATION_KEY = "previousLocation";
    public static final String PREFERENCES_PREVIOUS_USER_KEY = "previousUser";
    public static final String SELECTED_COLLECTION_KEY = "SELECTED_COLLECTION_KEY";
    public static final String SELECTED_COLLECTION = "SELECTED_COLLECTION";
    public static final String SELECTED_POI = "SELECTED_POI";
    public static final String PRETTY_COLLECTION_KEY = "PRETTY_COLLECTION_KEY";
    public static final String TIME_SERVER = "time-a.nist.gov";

    /** Map Constants **/
    // This is the distance in meters that we scan on the map
    // default is 800 meters
    public static final int DISTANCE_TO_SCAN_MARKERS = 800;
    public static final int DISTANCE_TO_GET_STAMP = 50;
    public static final int DEFAULT_ZOOM_LEVEL = 16;

    public static final String STAMP_ENABLED_TEXT = "PRESS FOR STAMP";
    public static final String STAMP_DISABLED_TEXT = "GET CLOSER";

    public static final int DEFAULT_STAMP_BACKGROUND_COLOR = Color.WHITE;
    public static final int DEFAULT_STAMP_TEXT_COLOR = Color.parseColor("#F1F3F3");

    public static final String MARKER = "marker";
    public static final String ICON = "icon";

    // Declare the @IntDef for these constants
    @StringDef({MARKER,ICON})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Loc8rGraphics {}

}
