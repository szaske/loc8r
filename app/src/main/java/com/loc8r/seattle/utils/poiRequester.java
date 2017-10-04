package com.loc8r.seattle.utils;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.loc8r.seattle.models.POI;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

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
    private static final String BASE_URL = "http://www.reddit.com/r/aww/hot.json?";
    private static final String COUNT_PARAMETER = "count=";
    private static final String COUNT_VALUE = "25";
    private static final String AFTER_PARAMETER = "&after=";
    private boolean mLoadingData;


    //Non API Firebase Access
    private static final String TAG= "DataFromFB";
    private FirebaseDatabase database;
    private DatabaseReference mFBDataReference = null;

    public boolean isLoadingData() {
        return mLoadingData;
    }

    // Constructor
    public poiRequester(Activity listeningActivity) {
        mResponseListener = (poiRequesterResponse) listeningActivity;
        mContext = listeningActivity.getApplicationContext();
        mClient = new OkHttpClient();
        mLoadingData = false;
    }

    public void getPOIs() throws IOException {

        // Instantiate the FB references
        database= FirebaseDatabase.getInstance();
        mFBDataReference=database.getReference("pois");

        // Create a FB event listener
        mFBDataReference.addValueEventListener(new ValueEventListener(){

            /**
             * This method is called once with the initial value and again whenever data at this location is updated
             *
             * @param dataSnapshot The data returned from Firebase
             */
            @Override
            public void onDataChange(DataSnapshot dataSnapshot){
                long value=dataSnapshot.getChildrenCount();
                Log.d(TAG,"no of children: "+value);

                ArrayList<POI> poiList = new ArrayList<POI>();
                for (DataSnapshot child: dataSnapshot.getChildren()) {
                    poiList.add(child.getValue(POI.class));
                }

                //This is where the new Awws are passed back to the Activity
                mResponseListener.receivedNewPOIs(poiList);
            }

            @Override
            public void onCancelled(DatabaseError error){
                // Failed to read value
                Log.w(TAG,"Failed to read value.",error.toException());
            }
        });



        String urlRequest = Constants.FIREBASE_JSON_URL;
        Request request = new Request.Builder().url(urlRequest).build();
        mLoadingData = true;

//        mClient.newCall(request).enqueue(new Callback() {
//
//            @Override
//            public void onFailure(Call call, IOException e) {
//                mLoadingData = false;
//                e.printStackTrace();
//            }
//
//            @Override
//            public void onResponse(Call call, Response response) throws IOException {
//                // An Array of POIs
//                ArrayList<POI> POIs = new ArrayList<>();
//                try {
//
//                    String jsonData = response.body().string();
//                    if(response.isSuccessful()){
//                        JSONObject tmpJSON = new JSONObject(jsonData);
//                        JSONArray poisArrayJSON = tmpJSON.getJSONArray("pois");
//                        for(int i = 0; i < poisArrayJSON.length(); i++){
//
//                            JSONObject thisPOI = poisArrayJSON.getJSONObject(i);
//
//                                // Using JSON access, I had to put in this check
//                                if(thisPOI != null){
//                                    String name = thisPOI.getString("name");
//                                    // String description = thisPOI.getString("description");
//                                    Double lat = thisPOI.getDouble("latitude");
//                                    Double lng = thisPOI.getDouble("longitude");
//                                    String desc = thisPOI.getString("description");
//
//                                    POIs.add(new POI(name, lat, lng, desc ));
//                                }
//                        }
//                    }
//
//                    //This is where the new Awws are passed back to the Activity
//                    mResponseListener.receivedNewPOIs(POIs);
//                    mLoadingData = false;
//
//                } catch (JSONException e) {
//                    mLoadingData = false;
//                    e.printStackTrace();
//                }
//            }
//        });  // End of enqueue
    } // End of GetPOIs
}
