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

public class MainListActivity extends GMS_Activity {

    private static final String TAG = GMS_Activity.class.getSimpleName();
    private Button mExploreButton;
    private Button mPassportButton;
    private Button mSettingsButton;

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
                Intent intent = new Intent(MainListActivity.this, MapsActivity.class);
                startActivity(intent);
            }
        });

        mPassportButton = findViewById(R.id.my_passport_Button);
        mPassportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Passport button pressed ");
            }
        });

        mSettingsButton = findViewById(R.id.settings_Button);
        mSettingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Settings button pressed ");
//                Intent intent = new Intent(MainListActivity.this, NearbyPOIActivity.class);
//                startActivity(intent);
            }
        });

    }


// Removed.  This code can be used as the click event method for a recyclerview

//    private void clicked(ListItem item){
//        switch (item.getTitle()) {
//            case "Explore Seattle":
//                Log.d(TAG, "Explore button pressed ");
//                Intent intent = new Intent(MainListActivity.this, NearbyPOIActivity.class);
//                startActivity(intent);
//                break;
//            case "My Passport":
//                Toast.makeText(getApplicationContext(), "Item " + item.getTitle() + " Clicked", Toast.LENGTH_LONG).show();
//                break;
//            default:
//                Toast.makeText(getApplicationContext(), "Item " + item.getTitle() + " Clicked", Toast.LENGTH_LONG).show();
//                break;
//        }
//    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu)
    {
        MenuItem mapItem = menu.findItem(R.id.menu_map);

        return super.onPrepareOptionsMenu(menu);
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu)
//    {
//        super.onCreateOptionsMenu(menu);
//        getMenuInflater().inflate(R.menu.options_menu, menu);
//        return true;
//    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.menu_filter:
                //showFilterDialog();
                break;
            case R.id.menu_map:
                //openMap();
                break;
            case R.id.menu_log_out:
                showLogoutDialog(this);
                break;
        }

        return super.onOptionsItemSelected(item);
    }


}
