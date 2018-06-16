package com.loc8r.seattle.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;
import com.loc8r.seattle.R;
import com.loc8r.seattle.models.POI;
import com.loc8r.seattle.utils.TestPOIMakerUtil;

import java.util.HashMap;
import java.util.Map;

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
