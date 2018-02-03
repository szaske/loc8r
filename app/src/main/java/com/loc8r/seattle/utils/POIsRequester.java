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

import java.io.IOException;
import java.util.ArrayList;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Created by steve on 1/30/2018.
 */

public class POIsRequester {

    public interface FireBasePOIResponse {
        void onPOIsReceived(ArrayList<POI> POIs);
    }

    private static final String TAG = POIsRequester.class.getSimpleName();
    FirebaseFirestore db;
    FirebaseUser user;
    private FireBasePOIResponse mResponseListener;
    public ArrayList<POI> listOfPOIs;
    // Static singleton instance
    // public static POIsRequester ourInstance;

//    public POIsRequester(Context context) {
//        user = FirebaseAuth.getInstance().getCurrentUser();
//        db = FirebaseFirestore.getInstance();
//    }

    public POIsRequester(Activity listeningActivity) {
        mResponseListener = (FireBasePOIResponse) listeningActivity;
        user = FirebaseAuth.getInstance().getCurrentUser();
        db = FirebaseFirestore.getInstance();
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

    public void GetPoiCollection(String category) throws IOException {
        Log.d("STZ", "GetAllPOI method started ");
        db.collection("pois").whereEqualTo("category", category)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            Log.d("STZ", "Getting POIs task completed successfully, now converting to POI class ");
                            ArrayList<POI> results = new ArrayList<>();
                            for (DocumentSnapshot document : task.getResult()) {
                                POI sentPOI = document.toObject(POI.class);
                                results.add(sentPOI);
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

//    public ArrayList<POI> GetPOICategory(){
//        //TODO next
//    }

}
