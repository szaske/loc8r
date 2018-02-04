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
import com.loc8r.seattle.interfaces.OnCollectionClickListener;
import com.loc8r.seattle.interfaces.OnPOIClickListener;
import com.loc8r.seattle.models.Collection;
import com.loc8r.seattle.models.POI;
import com.loc8r.seattle.utils.CollectionsRequester;
import com.loc8r.seattle.utils.Constants;
import com.loc8r.seattle.utils.POIsRequester;

import java.io.IOException;
import java.util.ArrayList;

public class CollectionListActivity extends GMS_Activity implements POIsRequester.FireBasePOIResponse, OnPOIClickListener {
    private static final String TAG = CollectionListActivity.class.getSimpleName();
    private RecyclerView mRecyclerView;
    private POI_Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private ArrayList<POI> mListOfPOIs;
    private String mSelectedCollection;
    // public ArrayList<Collection> mListOfCollections;
    private POIsRequester mPOIsRequester; //helper class

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

        mListOfPOIs = new ArrayList<>(); // Create an empty list for the recyclerView

        mAdapter = new POI_Adapter(mListOfPOIs, this);
        mRecyclerView.setAdapter(mAdapter);

        // mRecyclerView.setAdapter(new Collections_Adapter(POIsRequester.getInstance(getApplicationContext()).mListOfCollections, this));

        //This is the object that can fetch more content
        mPOIsRequester = new POIsRequester(this);
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Better get some content
        if (mListOfPOIs.size() == 0) {
            requestPOICollections();
        }
    }

    private void requestPOICollections() {

        try {
            mPOIsRequester.GetPoiCollection(mSelectedCollection);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void OnPOIClick (POI poi) {
        Log.d(TAG, "OnCollectionClick: Clicked on " + poi.getName());

//        // Go to the details page for the selected restaurant
//        Intent intent = new Intent(this, PassportActivity.class);
//        intent.putExtra(RestaurantDetailActivity.KEY_RESTAURANT_ID, restaurant.getId());
//
//        startActivity(intent);
//        overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left);
    }

    @Override public void onPOIsReceived(final ArrayList<POI> POIsSet) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                mListOfPOIs.addAll(POIsSet); // This adds a new item to the list

                mAdapter.notifyDataSetChanged();  //This tells the adapter to reset and redraw
            }
        });

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

}
