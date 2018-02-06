package com.loc8r.seattle.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
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

import java.util.List;

public class ManagementActivity extends AppCompatActivity {

    private static final String TAG = "ManagementActivity";
    private FirebaseFirestore mFirestore;
    private Button mAddPOIsButton;

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
                CreateFakePOIs(0);
                CreateFakePOIs(200);
                CreateFakePOIs(400);
                CreateFakePOIs(600);
                CreateFakePOIs(800);
            }
        });
    }

    private void CreateFakePOIs(int start) {
        // Add a bunch of random restaurants
        WriteBatch batch = mFirestore.batch();

        for (int i = start; i < start+200; i++) {
            DocumentReference restRef = mFirestore.collection("pois").document(String.valueOf(i));

            // Create random POI
            POI randomPOI = TestPOIMakerUtil.getRandom();
            randomPOI.setId(Integer.toString(i));
            randomPOI.setCollectionPosition(i);

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


}
