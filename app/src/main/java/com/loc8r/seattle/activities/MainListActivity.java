package com.loc8r.seattle.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.loc8r.seattle.R;
import com.loc8r.seattle.models.POI;
import com.loc8r.seattle.models.Stamp;
import com.loc8r.seattle.utils.POIsRequester;
import com.loc8r.seattle.utils.StampsRequester;
import com.loc8r.seattle.utils.StateManager;

import java.io.IOException;
import java.util.ArrayList;

public class MainListActivity extends GMS_Activity implements
    POIsRequester.FireBasePOIResponse,
    StampsRequester.FireBaseStampResponse
{

    private static final String TAG = MainListActivity.class.getSimpleName();
    private Button mExploreButton;
    private Button mPassportButton;
    private Button mSettingsButton;

    private POIsRequester mPOIsRequester; //helper class
    private StampsRequester mStampsRequester;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_list);

        Toolbar myToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);

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

        mSettingsButton = findViewById(R.id.settings_Button);
        mSettingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Settings button pressed ");
            }
        });

        // Requester objects to get the list of POI's in the list & the related stamps
        mPOIsRequester = new POIsRequester();
        mStampsRequester = new StampsRequester(this);


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    protected void onStart() {

        // Better get some content if we don't have it already
        if (StateManager.getInstance().getPOIs().size() == 0 && !StateManager.getInstance().isGettingPOIs()) {
            fetchAllToStateManager();
        }


        super.onStart();
    }

    private void fetchAllToStateManager() {
            StateManager.getInstance().setGettingPOIs(true); // tracking the process

            try {
                mPOIsRequester.GetAllPOIs(this);
            } catch (IOException e) {
                e.printStackTrace();
            }

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.menu_log_out:
                showLogoutDialog(this);
                break;
            case R.id.menu_admin:
                Log.d(TAG, "Admin item selected");
                Intent intent = new Intent(MainListActivity.this, ManagementActivity.class);
                startActivity(intent);
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override public void onPOIsReceived (final ArrayList<POI> POIsSent) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                StateManager.getInstance().setPOIs(POIsSent);
                StateManager.getInstance().setGettingPOIs(false);

                // Now get Stamps for this collection
                StateManager.getInstance().setGettingStamps(true);
                try {
                    mStampsRequester.GetUserStamps();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void onStampsReceived(final ArrayList<Stamp> Stamps) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                for (Stamp stamp: Stamps) {
                    // Works because I overrode equals in POI model
                    for (POI poi: StateManager.getInstance().getPOIs()) {
                        if(poi.getId().equals(stamp.getPoiId())){
                            poi.setStamp(stamp);
                            break;
                        }
                    }
                }

                StateManager.getInstance().setGettingStamps(false);
                Log.d(TAG, "We got it all FOLKS!");
            }
        });

    }

}
