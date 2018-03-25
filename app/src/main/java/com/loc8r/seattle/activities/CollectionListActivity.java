package com.loc8r.seattle.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.loc8r.seattle.R;
import com.loc8r.seattle.adapters.POI_Adapter;
import com.loc8r.seattle.interfaces.OnPOIClickListener;
import com.loc8r.seattle.models.POI;
import com.loc8r.seattle.models.Stamp;
import com.loc8r.seattle.utils.Constants;
import com.loc8r.seattle.utils.POIsRequester;
import com.loc8r.seattle.utils.StampsRequester;
import com.loc8r.seattle.utils.StateManager;

import org.parceler.Parcels;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;

public class CollectionListActivity extends BaseActivity implements
        StampsRequester.FireBaseStampResponse,
        OnPOIClickListener {
    private static final String TAG = CollectionListActivity.class.getSimpleName();
    private RecyclerView mRecyclerView;
    private POI_Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private ArrayList<POI> mListOfPOIsInCollection;
    public ArrayList<Stamp> mListOfStamps;
    private String mSelectedCollection;

    //private POIsRequester mPOIsRequester; //helper class
    // private StampsRequester mStampsRequester;
    FirebaseFirestore db;
    FirebaseUser user;

    private ConstraintLayout v;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_passport);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        try {
            mSelectedCollection = getIntent().getExtras().getString(Constants.SELECTED_COLLECTION_KEY);
        } catch (Exception e) {
            e.printStackTrace();
        }

        mRecyclerView = (RecyclerView) findViewById(R.id.collectionsRV);

        // use this setting to
        // improve performance if you know that changes
        // in content do not change the layout size
        // of the RecyclerView
        mRecyclerView.setHasFixedSize(true);
        // use a linear layout manager
        //mLayoutManager = new LinearLayoutManager(this);
        mLayoutManager = new GridLayoutManager(this, 2);
        mRecyclerView.setLayoutManager(mLayoutManager);

        mListOfPOIsInCollection = new ArrayList<>(); // Create an empty list for the recyclerView

        mAdapter = new POI_Adapter(mListOfPOIsInCollection, this);
        mRecyclerView.setAdapter(mAdapter);

        // Requester objects to get the list of POI's in the list & the related stamps
        // mPOIsRequester = new POIsRequester();
        //mStampsRequester = new StampsRequester(this);
        db = FirebaseFirestore.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();
    }

    @Override
    protected void onStart() {
        super.onStart();


    }

    @Override protected void onResume() {
        super.onResume();

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                    ArrayList<POI> newPOIs = getPOIsByCollectionFromStateManager(mSelectedCollection);

                    // See https://stackoverflow.com/questions/15422120/notifydatasetchange-not-working-from-custom-adapter
                    mListOfPOIsInCollection.clear();
                    mListOfPOIsInCollection.addAll(newPOIs);

                // Now get Stamps for this collection
                try {
                    GetUserStampsByCollection(mSelectedCollection);
                } catch (IOException e) {
                    e.printStackTrace();
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
                            onStampsReceived(results);
                            Log.d("STZ", "onComplete: ");

                        } else {
                            Log.d(TAG, "Error getting POIs. ", task.getException());
                        }
                    }
                });
    }

    private ArrayList<POI> getPOIsByCollectionFromStateManager(String collection) {
        ArrayList<POI> results = new ArrayList<>();
        for(POI poi: StateManager.getInstance().getPOIs()){
            if(poi.getCollection().equals(collection)){
                results.add(poi);
            }
        }
        return results;
    }

//    private void requestPOICollections() {
//
//        try {
//            // mPOIsRequester.GetPoiByCollection(this,mSelectedCollection);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }

    @Override
    public void OnPOIClick (POI poi) {
        Log.d(TAG, "OnCollectionClick: Clicked on " + poi.getName());

        //let's go to the Details activity
        Intent i = new Intent(this, POIDetailActivity.class);
        Bundle bundle = new Bundle();
        bundle.putParcelable("poi", Parcels.wrap(poi));
        i.putExtras(bundle);
        startActivity(i); // POI is now passed to the new Activity

        overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // Check to see if the user pressed the home arrow
        if (item.getItemId() == android.R.id.home) {
            finish();
            // First passed parameter is the animation to be used for the incoming activity
            // the second parameter is the animatoin to be used by the exiting activity
            overridePendingTransition(R.anim.slide_in_from_left, R.anim.slide_out_to_right);
            return true;
        }
        return false;
    }

//    @Override public void onPOIsCollectionReceived(final ArrayList<POI> POIsSent, final String collection) {
//        runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//
//                mListOfPOIsInCollection.clear(); //not sure clear is needed, but being safe
//                mListOfPOIsInCollection.addAll(POIsSent); // This adds a new item to the list
//
//
//
//            }
//        });
//    }

    public void onStampsReceived(final ArrayList<Stamp> Stamps) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                for (Stamp stamp: Stamps) {
                    // Works because I overrode equals in POI model
                    for (POI poi: mListOfPOIsInCollection) {
                        if(poi.getId().equals(stamp.getPoiId())){
                            poi.setStamp(stamp);
                            break;
                        }
                    }
                }
                mAdapter.notifyDataSetChanged();  //This tells the recyclerview adapter to reset and redraw
            }
        });

    }

}
