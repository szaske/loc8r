package com.loc8r.seattle.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.loc8r.seattle.R;
import com.loc8r.seattle.adapters.POIStamp_Adapter;
import com.loc8r.seattle.interfaces.OnPOIClickListener;
import com.loc8r.seattle.models.Collection;
import com.loc8r.seattle.models.POI;
import com.loc8r.seattle.models.Stamp;
import com.loc8r.seattle.utils.Constants;
import com.loc8r.seattle.utils.FocusedCropTransform;
import com.loc8r.seattle.utils.StampListDecoration;
import com.loc8r.seattle.utils.StampsRequester;
import com.loc8r.seattle.utils.StateManager;
import com.squareup.picasso.Picasso;

import org.parceler.Parcels;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CollectionListActivity extends AppCompatActivity implements
        StampsRequester.FireBaseStampResponse,
        OnPOIClickListener {
    private static final String TAG = CollectionListActivity.class.getSimpleName();

    @BindView(R.id.collectionsRV) RecyclerView mRecyclerView;
    @BindView(R.id.collapsing_toolbar) CollapsingToolbarLayout mCollapsingToolbar;
    @BindView(R.id.collection_desc) TextView mCollectionDescriptionTV;
    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.collection_image_iv) ImageView mCollectionImageIV;

    int bgResourceId; // used for collapsing toolbar
    private Collection mSelectedCollection;
    private POIStamp_Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private ArrayList<POI> mListOfPOIsInCollection;
    //private String mSelectedCollectionId, mSelectedCollectionName;

    FirebaseFirestore db;
    FirebaseUser user;

    // State variables
    private static final String LIST_STATE_KEY = "collectionLayoutManagerState";
    private static final String COLLECTION_ARRAYLIST_STATE_KEY = "collectionPOIList";
    private Parcelable mListState = null;
    private Parcelable mCollectionList = null;


    protected void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state);

        // Save layoutManager state
        mListState = mLayoutManager.onSaveInstanceState();
        state.putParcelable(LIST_STATE_KEY, mListState);

        mCollectionList = Parcels.wrap(mListOfPOIsInCollection);
        state.putParcelable(COLLECTION_ARRAYLIST_STATE_KEY,mCollectionList);
    }

    protected void onRestoreInstanceState(Bundle state) {
        super.onRestoreInstanceState(state);

        // Retrieve list state and list/item positions
        if(state != null) {
            mListState = state.getParcelable(LIST_STATE_KEY);
            mListOfPOIsInCollection = Parcels.unwrap(state.getParcelable(COLLECTION_ARRAYLIST_STATE_KEY));
        }
    }

    /**
     *  Create items needed for the activity
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collection_list);
        ButterKnife.bind(this);

        // Create the Firebase items
        db = FirebaseFirestore.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();

        // Create and configure the toolbar
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Get the collection we're looking at
        try {
            mSelectedCollection = Parcels.unwrap(getIntent().getParcelableExtra(Constants.SELECTED_COLLECTION));

            //            mSelectedCollectionId = getIntent().getExtras().getString(Constants.SELECTED_COLLECTION_KEY);
//            mSelectedCollectionName = getIntent().getExtras().getString(Constants.PRETTY_COLLECTION_KEY);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Configure the Collapsing toolbar
        // Get the Id for the background image resource
        bgResourceId = getResources().getIdentifier("backg_" + mSelectedCollection.getId(), "drawable", getPackageName());

        if(bgResourceId==0){
            bgResourceId = (int) R.drawable.main_menu_bg;
        }

        // see https://stackoverflow.com/questions/18081001/android-get-width-of-layout-programatically-having-fill-parent-in-its-xml
        ViewTreeObserver vto = mCollectionImageIV.getViewTreeObserver();
        vto.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            public boolean onPreDraw() {
                mCollectionImageIV.getViewTreeObserver().removeOnPreDrawListener(this);
                int rootWidth = mCollectionImageIV.getMeasuredWidth();
                int rootHeight = mCollectionImageIV.getMeasuredHeight();
                Picasso.get()
                        .load(bgResourceId)
                        .transform(new FocusedCropTransform(rootWidth,rootHeight, mCollectionImageIV.getId(), .5,.5))
                        .into(mCollectionImageIV);

                // Log.d("tester-", "STZ _ onPreDraw: Width is " + rootView.getMeasuredWidth() + " - Height:"+ rootView.getMeasuredHeight());
                return true;
            }
        });

        getSupportActionBar().setTitle(mSelectedCollection.getName() + " Collection");
        //collapsingToolbar.setTitle(mSelectedCollectionName + " Collection");

        if (mSelectedCollection.getDescription()!=null){
            mCollectionDescriptionTV.setText(mSelectedCollection.getDescription());
        }


        // Configure the RecyclerView
        //mRecyclerView = (RecyclerView) findViewById(R.id.collectionsRV);

        /** use this setting to improve performance if you know that changes
        in content do not change the layout size of the RecyclerView
         **/
        //mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mListOfPOIsInCollection = new ArrayList<>(); // Create an empty list for the recyclerView
        mAdapter = new POIStamp_Adapter(mListOfPOIsInCollection, this);
        mRecyclerView.setAdapter(mAdapter);
        RecyclerView.ItemDecoration dividerItemDecoration = new StampListDecoration(getResources().getDrawable(R.drawable.collection_rv_divider), this);
        mRecyclerView.addItemDecoration(dividerItemDecoration);

    }

    @Override
    protected void onStart() {
        super.onStart();

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ArrayList<POI> newPOIs = getPOIsByCollectionIdFromStateManager(mSelectedCollection.getId());

                // Sort the POIs
                // See https://stackoverflow.com/questions/4066538/sort-an-arraylist-based-on-an-object-field
                Collections.sort(newPOIs, new Comparator<POI>(){
                    public int compare(POI o1, POI o2){
                        if(o1.getCollectionPosition() == o2.getCollectionPosition())
                            return 0;
                        return o1.getCollectionPosition() < o2.getCollectionPosition() ? -1 : 1;
                    }
                });

                // check to ensure we have an even number of POIs, so that the list lays out correctly
                //check to see if we need to even the list
                if(newPOIs.size()%2!=0){
                    POI blankPoi=new POI();
                    blankPoi.setName("blank");
                    newPOIs.add(blankPoi);
                }

                // See https://stackoverflow.com/questions/15422120/notifydatasetchange-not-working-from-custom-adapter
                mListOfPOIsInCollection.clear();
                mListOfPOIsInCollection.addAll(newPOIs);

                // Now get Stamps for this collection
                try {
                    GetUserStampsByCollection(mSelectedCollection.getId());
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        });
    }

//    @Override
//    public void onPause() {
//        // Save ListView state @ onPause
//        Log.d(TAG, "saving listview state @ onPause");
//        mCollectionListState = listView.onSaveInstanceState();
//        super.onPause();
//    }

    @Override protected void onResume() {
        super.onResume();

        if (mListState != null) {
            mLayoutManager.onRestoreInstanceState(mListState);
        }
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

    private ArrayList<POI> getPOIsByCollectionIdFromStateManager(String collectionId) {
        ArrayList<POI> results = new ArrayList<>();
        for(POI poi: StateManager.getInstance().getPOIs()){
            if(poi.getCollectionId().equals(collectionId)){
                results.add(poi);
            }
        }
        return results;
    }

    /**
     *  Click Listener for when a user clicks on a POI in the recyclerview
     *
     * @param poi The POI class object passed when a user clicks on the view
     */
    @Override
    public void OnPOIClick (POI poi) {
        Log.d(TAG, "OnCollectionClick: Clicked on " + poi.getName());

        if(poi.isStamped() || StateManager.getInstance().userIsAdmin()){
            //let's go to the Details activity
            Intent i = new Intent(this, POIDetailActivity.class);
            Bundle bundle = new Bundle();
            bundle.putParcelable(Constants.SELECTED_POI, Parcels.wrap(poi));
            i.putExtras(bundle);
            startActivity(i); // POI is now passed to the new Activity

            overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // Check to see if the user pressed the home arrow
        if (item.getItemId() == android.R.id.home) {
            finish();
            // First passed parameter is the animation to be used for the incoming activity
            // the second parameter is the animation to be used by the exiting activity
            overridePendingTransition(R.anim.slide_in_from_left, R.anim.slide_out_to_right);
            return true;
        }
        return false;
    }

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
