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

public class MainListActivity extends GMS_Activity {

    private static final String TAG = MainListActivity.class.getSimpleName();
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
                Intent intent = new Intent(MainListActivity.this, PassportActivity.class);
                startActivity(intent);

            }
        });

        mSettingsButton = findViewById(R.id.settings_Button);
        mSettingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Settings button pressed ");
            }
        });
    }

//    @Override
//    public boolean onPrepareOptionsMenu(Menu menu)
//    {
//        MenuItem mapItem = menu.findItem(R.id.menu_map);
//
//        return super.onPrepareOptionsMenu(menu);
//    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
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


}
