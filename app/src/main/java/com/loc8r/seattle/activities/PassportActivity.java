package com.loc8r.seattle.activities;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import com.loc8r.seattle.R;
import com.loc8r.seattle.adapters.Collections_Adapter;
import com.loc8r.seattle.interfaces.OnCollectionClickListener;
import com.loc8r.seattle.models.ListItem;
import com.loc8r.seattle.models.POI;

public class PassportActivity extends GMS_Activity implements OnCollectionClickListener{
    private static final String TAG = PassportActivity.class.getSimpleName();
    private RecyclerView mRecyclerView;
    private Collections_Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

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

        //mAdapter = new Collections_Adapter(listOfAllPOIs(), this);
        //mRecyclerView.setAdapter(mAdapter);

        mRecyclerView.setAdapter(new Collections_Adapter(listOfAllPOIs(), this));

        // mAdapter.setClickListener(this); // Bind the listener
    }

    @Override
    public void OnCollectionClick(POI poi) {
        Log.d(TAG, "OnCollectionClick: Clicked on " + poi.getName());
    }
}
