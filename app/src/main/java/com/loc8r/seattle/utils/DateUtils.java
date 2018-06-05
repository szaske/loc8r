package com.loc8r.seattle.utils;

import android.util.Log;

import com.loc8r.seattle.models.DateInterval;

import java.util.Date;
import java.util.List;

public class DateUtils {

    public static boolean contains(List<DateInterval> intervals, Date query) {
        try {
            for (DateInterval in : intervals) {
                if (in.contains(query)) {
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            Log.e("Exception Error", "Oops we had a failure: " + query + "," + intervals + " the exception was:",e );
            return false;
        }
    }
}
