package com.loc8r.seattle.activities;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import com.loc8r.seattle.R;
import com.loc8r.seattle.adapters.POI_Adapter;
import com.loc8r.seattle.interfaces.OnCollectionClickListener;
import com.loc8r.seattle.interfaces.OnPOIClickListener;
import com.loc8r.seattle.models.Collection;
import com.loc8r.seattle.models.POI;
import com.loc8r.seattle.utils.CollectionsRequester;
import com.loc8r.seattle.utils.POIsRequester;

import java.io.IOException;
import java.util.ArrayList;

public class CollectionListActivity extends GMS_Activity implements POIsRequester.FireBasePOIResponse, OnPOIClickListener {
    private static final String TAG = CollectionListActivity.class.getSimpleName();
    private RecyclerView mRecyclerView;
    private POI_Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    public ArrayList<POI> mListOfPOIs;
    // public ArrayList<Collection> mListOfCollections;
    private POIsRequester mPOIsRequester; //helper class

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_passport);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


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
            mPOIsRequester.GetPoiCollection("Film");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void OnPOIClick (POI poi) {
        Log.d(TAG, "OnCollectionClick: Clicked on " + poi.getName());
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
}
