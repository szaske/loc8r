package com.loc8r.seattle.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.loc8r.seattle.R;
import com.loc8r.seattle.models.POI;
import com.loc8r.seattle.models.Stamp;
import com.loc8r.seattle.utils.POIsRequester;
import com.loc8r.seattle.utils.StampsRequester;
import com.loc8r.seattle.utils.StateManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainListActivity extends LocationBase_Activity
{

    private static final int RC_SIGN_IN = 123;
    private static final String TAG = MainListActivity.class.getSimpleName();
    private Button mExploreButton;
    private Button mPassportButton;
    private Button mSettingsButton;

    FirebaseFirestore db;
    FirebaseAuth mAuth;
    FirebaseUser user;

    //private POIsRequester mPOIsRequester; //helper class
    // private StampsRequester mStampsRequester;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_list);

        Toolbar myToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);

        mExploreButton = findViewById(R.id.explore_Button);
        mExploreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Explore button pressed ");
                Intent intent = new Intent(MainListActivity.this, MapActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_from_bottom, R.anim.slide_out_to_top);
            }
        });

        mPassportButton = findViewById(R.id.my_passport_Button);
        mPassportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Passport button pressed ");
                Intent intent = new Intent(MainListActivity.this, PassportActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left);

            }
        });

        mSettingsButton = findViewById(R.id.settings_Button);
        mSettingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Settings button pressed ");
            }
        });

        // Requester objects to get the list of POI's in the list & the related stamps
//        mPOIsRequester = new POIsRequester();
//        mStampsRequester = new StampsRequester(this);

        // Firebase init
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();

        // Add a Authentication listener
        mAuth.addAuthStateListener(new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                // on all Auth changes clear data
                StateManager.getInstance().resetAll();

                // if the user IS logged in, then get new data
                if (firebaseAuth.getCurrentUser() != null) {
                    getInitialDataCacheFromDB();
                }
            }
        });


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
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
        if (stateManagerIsEmpty()) {
            fetchAllToStateManager();
        }

    }

    private boolean stateManagerIsEmpty(){
        return StateManager
                .getInstance()
                .getPOIs()
                .size() == 0 && !StateManager.getInstance().isGettingPOIs();
    }


    private void startSignIn() {
        // Sign in with FirebaseUI

        // Choose authentication providers
//        List<AuthUI.IdpConfig> providers = Arrays.asList(
//                new AuthUI.IdpConfig.Builder(AuthUI.EMAIL_PROVIDER).build());

        List<AuthUI.IdpConfig> providers = Arrays.asList(
                new AuthUI.IdpConfig.Builder(AuthUI.EMAIL_PROVIDER).build(),
                new AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build(),
                new AuthUI.IdpConfig.Builder(AuthUI.FACEBOOK_PROVIDER).build());

//        List<AuthUI.IdpConfig> providers = Arrays.asList(
//                new AuthUI.IdpConfig.Builder(AuthUI.EMAIL_PROVIDER).build(),
//                new AuthUI.IdpConfig.Builder(AuthUI.PHONE_VERIFICATION_PROVIDER).build(),
//                new AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build(),
//                new AuthUI.IdpConfig.Builder(AuthUI.FACEBOOK_PROVIDER).build(),
//                new AuthUI.IdpConfig.Builder(AuthUI.TWITTER_PROVIDER).build());



        // Create and launch sign-in intent
        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(providers)
                        .build(),
                RC_SIGN_IN);



//        Intent intent = AuthUI.getInstance().createSignInIntentBuilder()
//                .setAvailableProviders(Collections.singletonList(
//                        new AuthUI.IdpConfig.Builder(AuthUI.EMAIL_PROVIDER).build()))
//                .setIsSmartLockEnabled(false)
//                .build();
//
//        startActivityForResult(intent, RC_SIGN_IN);

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
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                // ...
            } else {
                // Sign in failed, check response for error code
                // ...
            }
        }
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.menu_log_out:
                signOutUser();
                break;
            case R.id.menu_admin:
                Log.d(TAG, "Admin item selected");
                Intent intent = new Intent(MainListActivity.this, ManagementActivity.class);
                startActivity(intent);
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void signOutUser(){
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

    private void fetchAllToStateManager() {
        StateManager.getInstance().setGettingPOIs(true); // tracking the process

        try {
            GetAllPOIs();
        } catch (IOException e) {
            e.printStackTrace();
        }

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
                Log.d(TAG, "We got it all FOLKS!");
                onPOIsAndStampsInStateManager();
            }
        });

    }

}
