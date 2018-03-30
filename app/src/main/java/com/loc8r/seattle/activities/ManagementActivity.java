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
    private FirebaseFirestore mFirestore;
    private Button mAddPOIsButton;
    private Button mAddSinglePOI;
    private Button mPOIPlaceholder;
    private Map<String, Integer> mCollectionCounterMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_management);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Firestore
        mFirestore = FirebaseFirestore.getInstance();

        mAddPOIsButton = findViewById(R.id.addPoisBTN);
        mAddPOIsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Add POIs button pressed ");
                CreateFakePOIs(1);
            }
        });

        mAddSinglePOI = findViewById(R.id.addSinglePOIBTN);
        mAddSinglePOI.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Add POI button pressed ");
                Intent intent = new Intent(ManagementActivity.this, AddPOIActivity.class);
                startActivity(intent);
            }
        });

        mPOIPlaceholder = findViewById(R.id.bn_POIPlaceholder);
        mPOIPlaceholder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Add POI button pressed ");
                Intent intent = new Intent(ManagementActivity.this, CreatePOIPlaceholderActivity.class);
                startActivity(intent);
            }
        });

        mCollectionCounterMap = new HashMap<String, Integer>();
    }

    private void CreateFakePOIs(int start) {
        // Add a bunch of random restaurants
        WriteBatch batch = mFirestore.batch();

        for (int i = start; i < start+30; i++) {

            // Create random POI
            POI randomPOI = TestPOIMakerUtil.getRandom();
            randomPOI.setCollectionPosition(NextCollectionCount(randomPOI));
            randomPOI.setId(randomPOI.getCollection().substring(0,3).toUpperCase()+String.format("%03d", randomPOI.getCollectionPosition()));

            DocumentReference restRef = mFirestore.collection("pois").document(randomPOI.getId());

            // Add POI to batch
            batch.set(restRef, randomPOI);

        }

        batch.commit().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Log.d(TAG, "Write batch succeeded.");
                } else {
                    Log.w(TAG, "write batch failed.", task.getException());
                }
            }
        });
    }

    private int NextCollectionCount(POI poi){
        Integer count = mCollectionCounterMap.get(poi.getCollection());
        if(count == null) {
            mCollectionCounterMap.put(poi.getCollection(), 1);
        }
        else {
            mCollectionCounterMap.put(poi.getCollection(), count + 1);
        }

        return mCollectionCounterMap.get(poi.getCollection());
    }


}
