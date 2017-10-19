package com.loc8r.seattle.utils;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.loc8r.seattle.models.POI;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static android.R.attr.value;

/**
 * Created by Guest on 10/3/17.
 */

public class poiRequester {
    public interface poiRequesterResponse {
        void receivedNewPOIs(ArrayList<POI> POIs);
    }

    private poiRequesterResponse mResponseListener;
    private Context mContext;
    private OkHttpClient mClient;
    private boolean mLoadingData;
    private boolean mAllDataLoaded;


    //Non API Firebase Access
    private static final String TAG= "DataFromFB";
    private FirebaseDatabase database;
    private DatabaseReference mFBDataReference = null;
    private DatabaseReference mFBLocationReference = null;
    private ArrayList<POI> poiFullList = new ArrayList<>();
    private ArrayList<POI> poiTestResults = new ArrayList<>();
    private GeoFire geofire;
    private static final int POIS_PER_PAGE = 5;
    private String LastKeySent;
    private Query poiQuery;

    public boolean isLoadingData() {
        return mLoadingData;
    }

    public boolean isAllDataLoaded() { return mAllDataLoaded; }

    // Constructor
    public poiRequester(Activity listeningActivity) {
        mResponseListener = (poiRequesterResponse) listeningActivity;
        mContext = listeningActivity.getApplicationContext();
        mClient = new OkHttpClient();
        mLoadingData = false;
        mAllDataLoaded = false;
    }

    public void getPOIs() throws IOException {

        mLoadingData = true; // We're currently busy loading data

        // Instantiate the FB references
        database= FirebaseDatabase.getInstance();
        mFBDataReference=database.getReference("pois");

        // Create a different reference to store location data
        mFBLocationReference= database.getReference("locs");

        // DatabaseReference jokesRef = FirebaseDatabase.getInstance().getReference().child("hindi-jokes");


        // ENDED CODING HERE
        //
        //
        //

        // If it's the first page of data
        if(LastKeySent==null){
            poiQuery = mFBDataReference.orderByKey().limitToFirst(POIS_PER_PAGE);
        } else {
            poiQuery = mFBDataReference.orderByKey().startAt(LastKeySent).limitToFirst(POIS_PER_PAGE);
        }

        // final GeoFire geofire = new GeoFire(mFBLocationReference);

        // Create a FB event listener
        poiQuery.addValueEventListener(new ValueEventListener(){

            /**
             * This method is called once with the initial value and again whenever data at this location is updated
             *
             * @param dataSnapshot The data returned from Firebase
             */
            @Override
            public void onDataChange(DataSnapshot dataSnapshot){
                ArrayList<POI> poiResults = new ArrayList<>();

                long value=dataSnapshot.getChildrenCount();
                Log.d(TAG,"no of children: "+value);

                for (DataSnapshot child: dataSnapshot.getChildren()) {
                    POI poiChild = child.getValue(POI.class);
                    poiChild.setId(child.getKey()); //record the PushID for each item
                    poiResults.add(poiChild);
                    Log.d("Item sent: ", poiChild.toString());

                    //Write locations in Geofires proprietary format
                    // geofire.setLocation(poiChild.getId(), new GeoLocation(poiChild.getLatitude(), poiChild.getLongitude()));
                }

                //Remember the PushID for the last item queried
                LastKeySent = poiResults.get(poiResults.size()-1).getId();
                Log.d("Last Item sent: ", LastKeySent);

                //Check to see if we're on the last paged
                if(dataSnapshot.getChildrenCount()<POIS_PER_PAGE){
                    mAllDataLoaded = true;
                } else {
                    // Removes the last item from the array to resolve an
                    // ugly design, that Firebase doesn't have a StartAfter method
                    // Might want to try FireStore since it has a StartAfter method.
                    // I placed this in the if because we don't want to remove the
                    // very last list item, when on the last page
                    poiResults.remove(poiResults.size()-1);
                }

                // Query from around Epicodus
//                GeoQuery geoQuery = geofire.queryAtLocation(new GeoLocation(47.607273, -122.336075), 0.6);
//
//                geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
//                    @Override
//                    public void onKeyEntered(String key, GeoLocation location) {
//                        System.out.println(String.format("Key %s entered the search area at [%f,%f]", key, location.latitude, location.longitude));
//                    }
//
//                    @Override
//                    public void onKeyExited(String key) {
//                        System.out.println(String.format("Key %s is no longer in the search area", key));
//                    }
//
//                    @Override
//                    public void onKeyMoved(String key, GeoLocation location) {
//                        System.out.println(String.format("Key %s moved within the search area to [%f,%f]", key, location.latitude, location.longitude));
//                    }
//
//                    @Override
//                    public void onGeoQueryReady() {
//                        System.out.println("All initial data has been loaded and events have been fired!");
//                    }
//
//                    @Override
//                    public void onGeoQueryError(DatabaseError error) {
//                        System.err.println("There was an error with this query: " + error);
//                    }
//                });

                //This is where the new Awws are passed back to the Activity
                mResponseListener.receivedNewPOIs(poiResults);

                mLoadingData = false; // done
            }

            @Override
            public void onCancelled(DatabaseError error){
                // Failed to read value
                Log.w(TAG,"Failed to read value.",error.toException());
            }
        });


    } // End of GetPOIs

    public double getDistanceToUser(Double userLat, Double userLong, Double poiLat, Double poiLong) {
        return Math.sqrt((userLat-poiLat)*(userLat-poiLat) + (userLong-poiLong)*(userLong-poiLong));
    }
}
