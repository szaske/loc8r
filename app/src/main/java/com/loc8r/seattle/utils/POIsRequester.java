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

    private static final String TAG = POIsRequester.class.getSimpleName();
    FirebaseFirestore db;
    FirebaseUser user;
    private FireBasePOIResponse mResponseListener;
    private ArrayList<POI> listOfPOIs;

    public POIsRequester(Activity listeningActivity) {
        mResponseListener = (FireBasePOIResponse) listeningActivity;
        user = FirebaseAuth.getInstance().getCurrentUser();
        db = FirebaseFirestore.getInstance();
    }

    public void GetAllPOIs() throws IOException {
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


//    public void GetAllStamps() {
//        db.collection("users")
//                .document(user.getUid())
//                .collection("stamps")
//                .get()
//                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
//                    @Override
//                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
//                        if (task.isSuccessful()) {
//                            for (DocumentSnapshot document : task.getResult()) {
//                                // Log.d(TAG, document.getId() + " => " + document.getData());
//                            }
//                        } else {
//                            Log.d(TAG, "Error getting documents: ", task.getException());
//                        }
//                    }
//                });
//    }

//    public void GetPoiCollection(String collection) throws IOException {
//        Log.d("STZ", "Get POI Collection method started ");
//        db.collection("pois").whereEqualTo("collection", collection)
//                .get()
//                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
//                    @Override
//                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
//                        if (task.isSuccessful()) {
//                            Log.d("STZ", "Getting POIs task completed successfully, now converting to POI class ");
//
//                            // ArrayList<POI> results = new ArrayList<>();
//                            HashMap<String, POI> results = new HashMap<>();
//                            for (DocumentSnapshot document : task.getResult()) {
//                                POI sentPOI = document.toObject(POI.class);
//                                //sentPOI.setStamp();//Set Stamp if one exists
//                                results.put(sentPOI.getId(),sentPOI);
//                                // Log.d(TAG, document.getId() + " => " + document.getData());
//                            }
//
//                            // Send results back to host activity
//                            mResponseListener.onPOIsReceived(results);
//                            Log.d("STZ", "onComplete: ");
//
//                        } else {
//                            Log.d(TAG, "Error getting POIs. ", task.getException());
//                        }
//                    }
//                });
//    }

    public void GetPoiCollection(String collection) throws IOException {
        Log.d("STZ", "Get POI Collection method started ");


        HashMap<String, POI> results = new HashMap<>();

        //HashMap<String, HashMap> selects = new HashMap<String, HashMap>();

        // magic from https://stackoverflow.com/questions/4234985/how-to-for-each-the-hashmap
        for(Map.Entry<String, POI> entry : StateManager.getInstance().getPOIs().entrySet()) {
            String key = entry.getKey();
            POI poi = entry.getValue();

            // Get POI from a specific collection
            if(poi.getCollection().equals(collection)){
                results.put(key,poi);
            }
        }

        // Now Stamp the POIs in results if needed
        for (Stamp stamp: StateManager.getInstance().getStamps()) {
            if(results.containsKey(stamp.getPoiId())){
                results.get(stamp.getPoiId()).setStamp(stamp);
            }
        }

        // Send results back to host activity
        mResponseListener.onPOIsReceived(results);
        Log.d("STZ", "POI Collection retrieval completed.");
    }


//    public ArrayList<POI> GetPOICategory(){
//        //TODO next
//    }

}
