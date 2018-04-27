package com.loc8r.seattle.activities;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.loc8r.seattle.BuildConfig;
import com.loc8r.seattle.R;
import com.loc8r.seattle.activities.base.FirebaseBaseActivity;
import com.loc8r.seattle.activities.base.LocationBase_Activity;
import com.loc8r.seattle.utils.FocusedCropTransform;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class MainListActivity extends LocationBase_Activity
{

    private static final String TAG = MainListActivity.class.getSimpleName();
    private Button mExploreButton;
    private Button mPassportButton;
    private TextView mTitle;
    private ImageView mBackgroundImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_list);

        Toolbar myToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);

        mBackgroundImage = findViewById(R.id.iv_background_image);

        fetchImages();

        ViewTreeObserver vto = mBackgroundImage.getViewTreeObserver();
        vto.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            public boolean onPreDraw() {
                mBackgroundImage.getViewTreeObserver().removeOnPreDrawListener(this);
                // Should add
                // .error(R.drawable.error)
                // .placeholder(R.drawable.blank_img)
                //
                //
                // Also should add a progressbar
                // here is a good example : https://stackoverflow.com/questions/22143157/android-picasso-placeholder-and-error-image-styling
                Picasso.get()
                        .load(R.drawable.main_menu_bg)
                        .transform(new FocusedCropTransform(mBackgroundImage.getMeasuredWidth(),mBackgroundImage.getMeasuredHeight(), 400,600))
                        .into(mBackgroundImage);

                Log.d(TAG, "STZ _ onPreDraw: Width is " + mBackgroundImage.getMeasuredWidth() + " - Height:"+ mBackgroundImage.getMeasuredHeight());

                return true;
            }
        });

        mExploreButton = findViewById(R.id.explore_Button);
        mExploreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Explore button pressed ");
                Intent intent = new Intent(MainListActivity.this, MapActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_from_bottom, R.anim.slide_out_to_top);
            }
        });

        mPassportButton = findViewById(R.id.my_passport_Button);
        mPassportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Passport button pressed ");
                Intent intent = new Intent(MainListActivity.this, PassportActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left);

            }
        });

        Typeface mainTypeface = Typeface.createFromAsset(getApplicationContext().getAssets(), "fonts/norwester.otf");
        mTitle = findViewById(R.id.tv_main_title);
        mTitle.setTypeface(mainTypeface);
        mTitle.setShadowLayer(15, 0, 0, Color.BLACK );
    }

    private void fetchImages() {
//        if (BuildConfig.DEBUG) {
//            Picasso.get().setIndicatorsEnabled(true);
//            Picasso.get().setLoggingEnabled(true);
//        }
//        Picasso.get()
//                .load(R.drawable.backg_ryanhenryward2018)
//                .fetch();
//        Picasso.get()
//                .load(R.drawable.backg_streetart2018)
//                .fetch();
//        Picasso.get()
//                .load(R.drawable.backg_timecapsule2018)
//                .fetch();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.menu_log_out:
                signOutUser();
                break;
            case R.id.menu_admin:
                Log.d(TAG, "Admin item selected");
                Intent intent = new Intent(MainListActivity.this, ManagementActivity.class);
                startActivity(intent);
                break;
        }

        return super.onOptionsItemSelected(item);
    }


//    @Override public void onPOIsAndStampsInStateManager() {
//        super.onPOIsAndStampsInStateManager();
//        Log.d(TAG, "onPOIsAndStampsInStateManager: EVERYTHING IS IN STATEMANAGER");
//    }
}
