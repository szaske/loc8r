package com.loc8r.seattle.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.loc8r.seattle.R;

public class ManagementActivity extends AppCompatActivity {

    private static final String TAG = "ManagementActivity";
    private Button mPOIPlaceholder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_management);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mPOIPlaceholder = findViewById(R.id.bn_POIPlaceholder);
        mPOIPlaceholder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Add POI Placeholder button pressed ");
                Intent intent = new Intent(ManagementActivity.this, CreatePOIPlaceholderActivity.class);
                startActivity(intent);
            }
        });

    }

}
