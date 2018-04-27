package com.loc8r.seattle.activities.base;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.loc8r.seattle.models.Collection;
import com.loc8r.seattle.models.POI;
import com.loc8r.seattle.models.Stamp;
import com.loc8r.seattle.utils.StateManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class FirebaseBaseActivity extends AppCompatActivity {

    private static final String TAG = FirebaseBaseActivity.class.getSimpleName();

    // An arbitrary request code value
    // see https://github.com/firebase/FirebaseUI-Android/issues/434
    private static final int RC_SIGN_IN = 1221;
    FirebaseFirestore db;
    FirebaseUser user;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        db = FirebaseFirestore.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();
        mAuth = FirebaseAuth.getInstance();

        // Add a Authentication listener
        mAuth.addAuthStateListener(new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                // on all Auth changes clear data
                // StateManager.getInstance().resetAll();

                // if the user IS logged in, then get new data
                // This is where we initially get our data, once the user is logged in and authenticated
                if (firebaseAuth.getCurrentUser() != null && stateManagerIsEmpty()) {
                    getInitialDataCacheFromDB();
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Start sign in if necessary
        if (!isUserSignedIn()) {
            startSignIn();
        }

    }

    private boolean isUserSignedIn() {
        return (FirebaseAuth.getInstance().getCurrentUser() != null);
    }

    private void getInitialDataCacheFromDB(){
        // If this is the first time, grab the list of POIs and save them to the StateManager
        if (stateManagerIsEmpty()) {
            fetchAllToStateManager();
        }

        // Get extra information about the user, separate from FirebaseUser




    }

    private boolean stateManagerIsEmpty(){
        return StateManager
                .getInstance()
                .getPOIs()
                .size() == 0 && !StateManager.getInstance().isGettingPOIs();
    }

    private void fetchAllToStateManager() {
        StateManager.getInstance().setGettingPOIs(true); // tracking the process

        try {
            GetAllPOIs();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void startSignIn() {
        // Sign in with FirebaseUI
        List<AuthUI.IdpConfig> providers = Arrays.asList(
                new AuthUI.IdpConfig.Builder(AuthUI.EMAIL_PROVIDER).build(),
                new AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build(),
                new AuthUI.IdpConfig.Builder(AuthUI.FACEBOOK_PROVIDER).build());

        // Create and launch sign-in intent
        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(providers)
                        .build(),
                RC_SIGN_IN);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            if (resultCode == RESULT_OK) {
                // Successfully signed in
                // Better get some content if we don't have it
                getInitialDataCacheFromDB();
                user = FirebaseAuth.getInstance().getCurrentUser();
                // ...
            } else {
                // Sign in failed, check response for error code
                // ...
            }
        }
    }

    public void signOutUser(){
        AuthUI.getInstance()
                .signOut(this)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    public void onComplete(@NonNull Task<Void> task) {
                        // Go back to the start
                        startSignIn();
                    }
                });

        //Clear the StateManager data
        StateManager.getInstance().resetAll();

        // TODO Delete this method in Base Activity
        // showLogoutDialog(this);
    }



    public void GetAllPOIs() throws IOException {
        // mResponseListener = (POIsRequester.FireBasePOIResponse) listeningActivity;

        Log.d("STZ", "GetAllPOI method started ");
        db.collection("pois")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            Log.d("STZ", "Getting POIs task completed successfully, now converting to POI class ");
                            ArrayList<POI> results = new ArrayList<>();
                            for (DocumentSnapshot document : task.getResult()) {
                                POI sentPOI = document.toObject(POI.class);
                                sentPOI.setId(document.getId());
                                results.add(sentPOI);
                                // Log.d(TAG, document.getId() + " => " + document.getData());
                            }

                            // Send results to SM
                            onPOIsReceived(results);
                            Log.d("STZ", "onComplete: ");

                        } else {
                            Log.d(TAG, "Error getting POIs. ", task.getException());
                        }
                    }
                });
        // [END get_multiple_all]
    }

    public void onPOIsReceived (final ArrayList<POI> POIsSent) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                StateManager.getInstance().setPOIs(POIsSent);
                StateManager.getInstance().setGettingPOIs(false);

                // Now get Stamps for this collection
                StateManager.getInstance().setGettingStamps(true);
                try {
                    GetUserStamps();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void GetUserStamps() throws IOException {
        Log.d("STZ", "Get User Stamps method started ");
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
                            onStampsReceived(results);
                            Log.d("STZ", "onComplete: ");

                        } else {
                            Log.d(TAG, "Error getting POIs. ", task.getException());
                        }
                    }
                });
    }

    public void onStampsReceived(final ArrayList<Stamp> Stamps) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                for (Stamp stamp: Stamps) {
                    // Works because I overrode equals in POI model
                    for (POI poi: StateManager.getInstance().getPOIs()) {
                        if(poi.getId().equals(stamp.getPoiId())){
                            poi.setStamp(stamp);
                            break;
                        }
                    }
                }

                StateManager.getInstance().setGettingStamps(false);
                GetCollections();
//                try {
//                    GetCollections();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }


                Log.d(TAG, "We got it all FOLKS!");
                //onPOIsAndStampsInStateManager();
            }
        });

    }

    private void GetCollections() {
        StateManager.getInstance().setGettingCollections(true);
        Log.d("STZ", "Get Collections method started ");
        db.collection("collections")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            Log.d("STZ", "Getting Collections task completed successfully, now converting to Collection class ");
                            HashMap<String, Collection> collectionResults = new HashMap<>();
                            for (DocumentSnapshot document : task.getResult()) {
                                Collection sentCollection = document.toObject(Collection.class);

                                collectionResults.put(sentCollection.getId(),sentCollection);
                                // Log.d(TAG, document.getId() + " => " + document.getData());
                            }

                            // Send results to SM
                            onCollectionsReceived(collectionResults);
                            Log.d("STZ", "Collections got ");

                        } else {
                            Log.d(TAG, "Error getting POIs. ", task.getException());
                        }
                    }
                });
        // [END get_multiple_all]
    }

    public void onCollectionsReceived (final HashMap<String, Collection> collectionsSent) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                StateManager.getInstance().setCollections(collectionsSent);
                StateManager.getInstance().setGettingCollections(false);
            }
        });
    }

    public void GetUserRoles() throws IOException {
        Log.d("STZ", "GetAll method started ");
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
                            onStampsReceived(results);
                            Log.d("STZ", "onComplete: ");

                        } else {
                            Log.d(TAG, "Error getting POIs. ", task.getException());
                        }
                    }
                });
    }


//    // callback to sub-classes when POIs and Stamps have been added to StateManager
//    public void onPOIsAndStampsInStateManager(){
//
//    }

}
