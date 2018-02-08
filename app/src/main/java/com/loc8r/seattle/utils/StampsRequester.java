package com.loc8r.seattle.utils;

import android.app.Activity;
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

/**
 *  Helper Class to receive Stamps from Firestore
 */

public class StampsRequester {

    public interface FireBaseStampResponse {
        void onStampsReceived(ArrayList<Stamp> Stamps);
    }

    private static final String TAG = StampsRequester.class.getSimpleName();
    FirebaseFirestore db;
    FirebaseUser user;
    private FireBaseStampResponse mResponseListener;

    public StampsRequester(Activity listeningActivity) {
        mResponseListener = (FireBaseStampResponse) listeningActivity;
        user = FirebaseAuth.getInstance().getCurrentUser();
        db = FirebaseFirestore.getInstance();
    }

    public void GetUserStamps() throws IOException {
        Log.d("STZ", "GetAllPOI method started ");
        db.collection("users")
            .document(user.getUid())
            .collection("stamps")
            .get()
            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        Log.d("STZ", "Getting POIs task completed successfully, now converting to POI class ");
                        ArrayList<Stamp> results = new ArrayList<>();
                        for (DocumentSnapshot document : task.getResult()) {
                            Stamp sentStamp = document.toObject(Stamp.class);
                            results.add(sentStamp);
                            // Log.d(TAG, document.getId() + " => " + document.getData());
                        }

                        // Send results back to host activity
                        mResponseListener.onStampsReceived(results);
                        Log.d("STZ", "onComplete: ");

                    } else {
                        Log.d(TAG, "Error getting POIs. ", task.getException());
                    }
                }
            });
    }

    public void GetUserStampsByCollection(String collection) throws IOException {
        Log.d("STZ", "GetAllPOI method started ");
        db.collection("users")
                .document(user.getUid())
                .collection("stamps")
                .whereEqualTo("collection",collection)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            Log.d("STZ", "Getting POIs task completed successfully, now converting to POI class ");
                            ArrayList<Stamp> results = new ArrayList<>();
                            for (DocumentSnapshot document : task.getResult()) {
                                Stamp sentStamp = document.toObject(Stamp.class);
                                results.add(sentStamp);
                                // Log.d(TAG, document.getId() + " => " + document.getData());
                            }

                            // Send results back to host activity
                            mResponseListener.onStampsReceived(results);
                            Log.d("STZ", "onComplete: ");

                        } else {
                            Log.d(TAG, "Error getting POIs. ", task.getException());
                        }
                    }
                });
    }


}
