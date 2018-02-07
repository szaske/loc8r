package com.loc8r.seattle.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;

import com.loc8r.seattle.R;
import com.loc8r.seattle.adapters.POI_Adapter;
import com.loc8r.seattle.interfaces.OnPOIClickListener;
import com.loc8r.seattle.models.POI;
import com.loc8r.seattle.models.Stamp;
import com.loc8r.seattle.utils.Constants;
import com.loc8r.seattle.utils.POIsRequester;
import com.loc8r.seattle.utils.StampsRequester;

import org.parceler.Parcels;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class CollectionListActivity extends GMS_Activity implements
        POIsRequester.FireBasePOICollectionResponse,
        StampsRequester.FireBaseStampResponse,
        OnPOIClickListener {
    private static final String TAG = CollectionListActivity.class.getSimpleName();
    private RecyclerView mRecyclerView;
    private POI_Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private ArrayList<POI> mListOfPOIsInCollection;
    public ArrayList<Stamp> mListOfStamps;
    private String mSelectedCollection;

    private POIsRequester mPOIsRequester; //helper class
    private StampsRequester mStampsRequester;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_passport);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Get restaurant ID from extras

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
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        mListOfPOIsInCollection = new ArrayList<>(); // Create an empty list for the recyclerView

        mAdapter = new POI_Adapter(mListOfPOIsInCollection, this);
        mRecyclerView.setAdapter(mAdapter);

        // mRecyclerView.setAdapter(new Collections_Adapter(POIsRequester.getInstance(getApplicationContext()).mListOfCollections, this));

        //This is the object that can fetch more content
        mPOIsRequester = new POIsRequester();
        mStampsRequester = new StampsRequester(this);
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Better get some content if we don't have it already
        if (mListOfPOIsInCollection.size() == 0) {
            requestPOICollections();
        }
    }

    private void requestPOICollections() {

        try {
            mPOIsRequester.GetPoiByCollection(this,mSelectedCollection);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

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

        if (item.getItemId() == android.R.id.home) {
            finish();
            overridePendingTransition(R.anim.slide_out_to_right, R.anim.slide_in_from_left);
            return true;
        }
        return false;
    }

    @Override public void onPOIsCollectionReceived(final HashMap<String, POI> POIsSent, final String collection) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                //Convert to an Arraylist because we use Arraylist location as key to finding it
                ArrayList<POI> Step2Results = new ArrayList<>();
                Step2Results.addAll(POIsSent.values());

                mListOfPOIsInCollection.addAll(Step2Results); // This adds a new item to the list

                // Now get Stamps for this collection
                try {
                    mStampsRequester.GetUserStampsByCollection(collection);
                } catch (IOException e) {
                    e.printStackTrace();
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
                    for (POI poi: mListOfPOIsInCollection) {
                        if(poi.getId().equals(stamp.getPoiId())){
                            poi.setStamp(stamp);
                            break;
                        }
                    }
                }
                mAdapter.notifyDataSetChanged();  //This tells the adapter to reset and redraw
            }
        });

    }

}
