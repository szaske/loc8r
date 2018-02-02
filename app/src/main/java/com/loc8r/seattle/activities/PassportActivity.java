package com.loc8r.seattle.activities;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import com.loc8r.seattle.R;
import com.loc8r.seattle.adapters.Collections_Adapter;
import com.loc8r.seattle.interfaces.OnCollectionClickListener;
import com.loc8r.seattle.models.Collection;
import com.loc8r.seattle.models.POI;
import com.loc8r.seattle.utils.AtoZComparator;
import com.loc8r.seattle.utils.CollectionsRequester;

import java.io.IOException;
import java.util.ArrayList;
import java.util.SortedSet;
import java.util.TreeSet;

public class PassportActivity extends GMS_Activity implements CollectionsRequester.FireBaseCollectionsResponse, OnCollectionClickListener{
    private static final String TAG = PassportActivity.class.getSimpleName();
    private RecyclerView mRecyclerView;
    private Collections_Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    public ArrayList<POI> mListOfPOIs;
    public ArrayList<Collection> mListOfCollections;
    private CollectionsRequester mCollectionsRequester; //helper class

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

        mListOfCollections = new ArrayList<>(); // Create an empty list for the recyclerView

        mAdapter = new Collections_Adapter(mListOfCollections, this);
        mRecyclerView.setAdapter(mAdapter);

        // mRecyclerView.setAdapter(new Collections_Adapter(POIsRequester.getInstance(getApplicationContext()).mListOfCollections, this));

        //This is the object that can fetch more content
        mCollectionsRequester = new CollectionsRequester(this);
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Better get some content
        if (mListOfCollections.size() == 0) {
            requestCollections();
        }
    }

    private void requestCollections() {

        try {
            mCollectionsRequester.GetAllCollections();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void OnCollectionClick(Collection collection) {
        Log.d(TAG, "OnCollectionClick: Clicked on " + collection.getName());
    }

    @Override public void onCollectionsListReceived(final ArrayList<Collection> collectionsSet) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                mListOfCollections.addAll(collectionsSet); // This adds a new item to the list

                mAdapter.notifyDataSetChanged();  //This tells the adapter to reset and redraw
            }
        });

    }
}
