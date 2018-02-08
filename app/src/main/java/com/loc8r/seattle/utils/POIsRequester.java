package com.loc8r.seattle.utils;

import android.app.Activity;
import android.location.Location;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.loc8r.seattle.models.POI;
import com.loc8r.seattle.models.Stamp;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Created by steve on 1/30/2018.
 */

public class POIsRequester {

    public interface FireBasePOIResponse {
        void onPOIsReceived(HashMap<String,POI> POIs);
    }

    public interface FireBasePOICollectionResponse {
        void onPOIsCollectionReceived(HashMap<String,POI> POIs, String collection);
    }

    private static final String TAG = POIsRequester.class.getSimpleName();
    FirebaseFirestore db;
    FirebaseUser user;
    private FireBasePOIResponse mResponseListener;
    private FireBasePOICollectionResponse mPOICollectionResponseListener;
    private ArrayList<POI> listOfPOIs;

    public POIsRequester() {
        user = FirebaseAuth.getInstance().getCurrentUser();
        db = FirebaseFirestore.getInstance();
    }

    public void GetAllPOIs(Activity listeningActivity) throws IOException {
        mResponseListener = (FireBasePOIResponse) listeningActivity;

        Log.d("STZ", "GetAllPOI method started ");
        db.collection("pois")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            Log.d("STZ", "Getting POIs task completed successfully, now converting to POI class ");
                            HashMap<String, POI> results = new HashMap<>();
                            for (DocumentSnapshot document : task.getResult()) {
                                POI sentPOI = document.toObject(POI.class);
                                sentPOI.setId(document.getId());
                                results.put(sentPOI.getId(),sentPOI);
                                // Log.d(TAG, document.getId() + " => " + document.getData());
                            }

                            // Send results back to host activity
                            mResponseListener.onPOIsReceived(results);
                            Log.d("STZ", "onComplete: ");

                        } else {
                            Log.d(TAG, "Error getting POIs. ", task.getException());
                        }
                    }
                });
        // [END get_multiple_all]
    }

    public void GetPoiByCollection(Activity listeningActivity, final String collection) throws IOException {
        mPOICollectionResponseListener = (FireBasePOICollectionResponse) listeningActivity;

        Log.d("STZ", "Get POI Collection method started ");
        db.collection("pois")
                .whereEqualTo("collection", collection)
                .orderBy("collectionPosition", Query.Direction.ASCENDING)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            Log.d("STZ", "Getting POIs task completed successfully, now converting to POI class ");

                            // ArrayList<POI> results = new ArrayList<>();
                            HashMap<String, POI> results = new HashMap<>();
                            for (DocumentSnapshot document : task.getResult()) {
                                POI sentPOI = document.toObject(POI.class);
                                //sentPOI.setStamp();//Set Stamp if one exists
                                results.put(sentPOI.getId(),sentPOI);
                                // Log.d(TAG, document.getId() + " => " + document.getData());
                            }

                            // Send results back to host activity
                            mPOICollectionResponseListener.onPOIsCollectionReceived(results,collection);
                            Log.d("STZ", "onComplete: ");

                        } else {
                            Log.d(TAG, "Error getting POIs. ", task.getException());
                        }
                    }
                });
    }
}
