package com.loc8r.seattle.activities;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.loc8r.seattle.R;
import com.loc8r.seattle.activities.base.LocationBase_Activity;
import com.loc8r.seattle.utils.FocusedCropTransform;
import com.loc8r.seattle.utils.ProgressIndicator;
import com.loc8r.seattle.utils.StateManager;
import com.squareup.picasso.Picasso;

import org.apache.commons.net.ntp.NTPUDPClient;
import org.apache.commons.net.ntp.TimeInfo;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;

import static com.loc8r.seattle.utils.Constants.TIME_SERVER;

public class MainListActivity extends LocationBase_Activity
{

    private static final String TAG = MainListActivity.class.getSimpleName();
    private Button mExploreButton, mPassportButton, mSuggestButton;
    private TextView mTitle;
    private ImageView mBackgroundImage;
    private ProgressIndicator progressIndicator;
    private Handler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_list);

        Toolbar myToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);

        mBackgroundImage = findViewById(R.id.iv_background_image);

        // Currently is diabled
        fetchImages();

        // This section allows me to fit background image to properly fill the screen
        ViewTreeObserver vto = mBackgroundImage.getViewTreeObserver();
        vto.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            public boolean onPreDraw() {
                mBackgroundImage.getViewTreeObserver().removeOnPreDrawListener(this);

                int width = mBackgroundImage.getMeasuredWidth();
                int height = mBackgroundImage.getMeasuredHeight();

                // Checking for size in rare case that app starts with screen off causes exception
                if(width > 0 && height > 0) {
                    // good example of Picasso builder: https://stackoverflow.com/questions/22143157/android-picasso-placeholder-and-error-image-styling
                    Picasso.get()
                            .load(R.drawable.main_menu_bg)
                            .transform(new FocusedCropTransform(
                                    width,
                                    height,
                                    mBackgroundImage.getId(),
                                    .5,
                                    .5))
                            .into(mBackgroundImage);
                }

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

        mSuggestButton = findViewById(R.id.suggest_Button);
        mSuggestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Suggestion button pressed ");
                Intent intent = new Intent(MainListActivity.this, AddSuggestionActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left);

            }
        });


        Typeface mainTypeface = Typeface.createFromAsset(getApplicationContext().getAssets(), "fonts/Roboto-Regular.ttf");
        mTitle = findViewById(R.id.tv_main_motto);
        mTitle.setTypeface(mainTypeface);
        mTitle.setShadowLayer(15, 0, 0, Color.BLACK );

        //Create Handler object for getting user time
        mHandler = new Handler(Looper.getMainLooper()) {
            @Override public void handleMessage(Message msg) {
                // This is where I can respond to messages with this handler
            }
        };


    }

    private void fetchImages() {
//        if (BuildConfig.DEBUG) {
//            Picasso.get().setIndicatorsEnabled(true);
//            Picasso.get().setLoggingEnabled(true);
//        }
//        Picasso.get()
//                .load(R.drawable.backg_2018besties2018)
//                .fetch();
//        Picasso.get()
//                .load(R.drawable.backg_ryanhenryward2018)
//                .fetch();
//        Picasso.get()
//                .load(R.drawable.backg_hiddengems2018)
//                .fetch();
//        Picasso.get()
//                .load(R.drawable.backg_kylermartz2018)
//                .fetch();
//        Picasso.get()
//                .load(R.drawable.backg_oddities2018)
//                .fetch();
//        Picasso.get()
//                .load(R.drawable.backg_seasonal2018)
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

        //Get the current network time
        getTime();
    }



    public static void getTime(){

        Log.d(TAG, "getTime: Getting Network time");

        Runnable timeRunnable = new Runnable() {
            @Override public void run() {

                // A UDP implementation of a client for the Network Time Protocol (NTP)
                NTPUDPClient timeClient = new NTPUDPClient();
                InetAddress inetAddress = null;
                try {
                    inetAddress = InetAddress.getByName(TIME_SERVER);
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                }
                TimeInfo timeInfo = null;
                try {
                    timeInfo = timeClient.getTime(inetAddress);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                //long returnTime = timeInfo.getReturnTime();   //local device time
                long returnTime = timeInfo.getMessage().getTransmitTimeStamp().getTime();   //server time

                Date time = new Date(returnTime);

                // Send time to StateManager object
                StateManager.getInstance().setDate(time);
                Log.i("Phone time", "Phone time: " + new Date(System.currentTimeMillis()));

                Log.i("Network Time", "Time from " + TIME_SERVER + ": " + time);
                Log.i("Network Time", "TimeInfo time: " + new Date(timeInfo.getReturnTime()));
                Log.i("Network Time", "GetOriginateTimeStamp: " + new Date(timeInfo.getMessage().getOriginateTimeStamp().getTime()));
                Log.i("Network Time", "Time info: " + new Date(timeInfo.getMessage().getReceiveTimeStamp().getTime()));
                Log.i("Network Time", "GetTransmitTimeStamp: " + new Date(timeInfo.getMessage().getTransmitTimeStamp().getTime()));
            }
        };

        Thread timeThread = new Thread(timeRunnable);
        timeThread.start();


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
                Intent intent2 = new Intent(MainListActivity.this, ManagementActivity.class);
                startActivity(intent2);
                break;
        }

        return super.onOptionsItemSelected(item);
    }


//    @Override public void onPOIsAndStampsInStateManager() {
//        super.onPOIsAndStampsInStateManager();
//        Log.d(TAG, "onPOIsAndStampsInStateManager: EVERYTHING IS IN STATEMANAGER");
//    }
}
