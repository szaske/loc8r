package com.loc8r.seattle.activities;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

/**
 *  Base Activity includes code shared across multiple activities
 *
 *  That includes:
 *
 *  Calligraphy - Makes working with fonts easier
 *
 */
public class BaseActivity extends AppCompatActivity {

    // Variables
    //private static final String TAG = BaseActivity.class.getSimpleName();

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }
}
