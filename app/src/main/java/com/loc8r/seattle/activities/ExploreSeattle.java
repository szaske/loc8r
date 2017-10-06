package com.loc8r.seattle.activities;

import android.content.res.Configuration;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.loc8r.seattle.R;
import com.loc8r.seattle.adapters.POIsPassportRecyclerAdapter;
import com.loc8r.seattle.models.POI;
import com.loc8r.seattle.utils.poiRequester;
import com.loc8r.seattle.utils.poiRequester.poiRequesterResponse;

import java.io.IOException;
import java.util.ArrayList;

public class ExploreSeattle extends AppCompatActivity implements poiRequesterResponse {

    // My variables
    private RecyclerView mRecyclerView; // To connect to my view object
    private LinearLayoutManager mLinearLayoutManager; // This tracks what views are where in the Rview
    private ArrayList<POI> mPOIsList; // What's being tracked
    private poiRequester mPOIRequester; //helper class
    private POIsPassportRecyclerAdapter mAdapter; //The 'data source' for the recyclerview
    private GridLayoutManager mGridLayoutManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_explore_seattle);

        //  Attaching the Rview and assigning a layoutManager
        mRecyclerView = (RecyclerView) findViewById(R.id.poiRecyclerView);

        // Adjust grid according to orientation
        if(this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT){
            mGridLayoutManager = new GridLayoutManager(this, 1);
        }
        else{
            mGridLayoutManager = new GridLayoutManager(this, 1);
        }

        mRecyclerView.setLayoutManager(mGridLayoutManager);

        mPOIsList = new ArrayList<>(); // An empty arrayList for the items in your list
        mAdapter = new POIsPassportRecyclerAdapter(mPOIsList);
        mRecyclerView.setAdapter(mAdapter); //this attaches your empty list to the view
        setRecyclerViewScrollListener(); // This sets the scroll listener

        //This is the object that can fetch more content
        mPOIRequester = new poiRequester(this);
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Better get some content
        if (mPOIsList.size() == 0) {
            requestMorePOIs();
        }
    }

    // This method asks the layoutManager what's the last visible position
    private int getLastVisibleItemPosition() {
        return mGridLayoutManager.findLastVisibleItemPosition();
    }

    // This creates a Scroll Listener to the RecyclerView
    private void setRecyclerViewScrollListener() {
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                int totalItemCount = mRecyclerView.getLayoutManager().getItemCount();

                Log.d("Scrolled", "TotalItemCount:" + totalItemCount + "LastVisItemPos:" + getLastVisibleItemPosition()  );

                //If we're not already loading data
                if (!mPOIRequester.isLoadingData() && !mPOIRequester.isAllDataLoaded() && getLastVisibleItemPosition() >= (totalItemCount -1)) {
                    requestMorePOIs();
                }
            }
        });
    }

    private void requestMorePOIs() {

        try {
            mPOIRequester.getPOIs();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void receivedNewPOIs(final ArrayList<POI> Pois) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mPOIsList.addAll(Pois); // This adds a new item to the list
                mAdapter.notifyItemInserted(mPOIsList.size());  //This tells the adapter to reset and redraw
            }
        });
    }
}
