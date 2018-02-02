package com.loc8r.seattle.activities;

import android.app.SearchManager;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.loc8r.seattle.R;
import com.loc8r.seattle.models.POI;
import com.loc8r.seattle.utils.FirebaseManager;

import org.parceler.Parcels;

import java.util.ArrayList;

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
