package com.loc8r.utils;

import android.app.Activity;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.lifecycle.ActivityLifecycleMonitorRegistry;
import android.support.test.runner.lifecycle.Stage;
import android.util.Log;
import com.squareup.spoon.Spoon;
// import com.jraska.falcon.FalconSpoon;

import java.util.Collection;
import java.util.Iterator;

/**
 * Created by steve on 2/28/2018.
 */

public class TestUtils {
    public static Activity getCurrentActivity() {
        final Activity[] currentActivity = {null};
        InstrumentationRegistry.getInstrumentation().runOnMainSync(new Runnable() {
            public void run() {
                Collection<Activity> resumedActivity = ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(Stage.RESUMED);
                Iterator<Activity> it = resumedActivity.iterator();
                currentActivity[0] = it.next();
            }
        });

        return currentActivity[0];
    }

    /**
     *  Uses the Spoon library to take a screenshot.  Images are saved
     *  at /mnt/sdcard/app_spoon-screenshots
     *
     * @param tag A String used as part of the file name
     */
    public static void screenShot(String tag) {
        Spoon.screenshot(getCurrentActivity(), tag);
        Log.i("asd", "Screenshot taken: " + tag);
    }
}
