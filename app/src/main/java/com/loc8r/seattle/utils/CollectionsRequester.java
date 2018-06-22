package com.loc8r.seattle.utils;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.loc8r.seattle.models.Collection;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by steve on 1/30/2018.
 */

public class CollectionsRequester {

    public interface FireBaseCollectionsResponse {
        void onCollectionsListReceived(ArrayList<Collection> collections);
    }

    private static final String TAG = CollectionsRequester.class.getSimpleName();
    FirebaseFirestore db;
    private FireBaseCollectionsResponse mResponseListener;
    public ArrayList<Collection> listOfCollections;

    public CollectionsRequester(Activity listeningActivity) {
        mResponseListener = (FireBaseCollectionsResponse) listeningActivity;
        db = FirebaseFirestore.getInstance();
    }

    public void GetAllCollections() throws IOException {
        Log.d("STZ", "GetAllCollections method started ");
        db.collection("collections")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            Log.d("STZ", "Getting Collections task completed successfully, now converting to POI class ");
                            ArrayList<Collection> results = new ArrayList<>();
                            for (DocumentSnapshot document : task.getResult()) {
                                Collection receivedCollection = document.toObject(Collection.class);
                                results.add(receivedCollection);
                                // Log.d(TAG, document.getId() + " => " + document.getData());
                            }

                            // Send results back to host activity
                            mResponseListener.onCollectionsListReceived(results);
                            Log.d("STZ", "onComplete: All Collections received ");
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
