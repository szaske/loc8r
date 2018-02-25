package com.loc8r.seattle.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.support.annotation.Nullable;
import android.os.Bundle;

import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Gravity;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.util.DisplayMetrics;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.loc8r.seattle.R;
import com.loc8r.seattle.interfaces.LocationListener;
import com.loc8r.seattle.models.POI;
import com.loc8r.seattle.utils.Constants;
import com.loc8r.seattle.utils.POIsRequester;
import com.loc8r.seattle.utils.StateManager;
import com.mancj.slideup.SlideUp;
import com.mancj.slideup.SlideUpBuilder;

import android.view.View;

import org.parceler.Parcels;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MapActivity extends GMS_Activity implements
        POIsRequester.FireBasePOIResponse,
        GoogleMap.OnMarkerClickListener,
        OnMapReadyCallback {

    private static final String TAG = MapActivity.class.getSimpleName();
    private GoogleMap mMap;

    // private ArrayList<POI> mListOfPOIs;
    private POIsRequester mPOIsRequester; //helper class

    private SlideUp mDrawer;
    private View mDrawerView;
    private View mHideImageView;
    private TextView mDrawerTitleTV;
    private TextView mDrawerDescTV;
    private Button mDrawerDetailButton;
    private Location mCurrentLocation;
    private Context context;

    private List<Integer> mExistingPoiMarkers;

    @BindView(R.id.map_fab) FloatingActionButton mFAB;

    // The selected POI
    private POI mSelectedPOI;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        ButterKnife.bind(this);

        // Obtain the SupportMapFragment and loads layout
        SupportMapFragment mMapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        // Get notified when the map is ready to be used.
        mMapFragment.getMapAsync(this);

        // Drawer Setup
        DrawerSetup();

        //mListOfPOIs = new ArrayList<>(); // Create an empty list to hold POIs
        //This is the object that can fetch more content
        mPOIsRequester = new POIsRequester();
        context = this; // Set context so we can use inside the runnable below

        mExistingPoiMarkers = new ArrayList<>();
    }

    @OnClick(R.id.map_fab)
    public void onFABClick(View view) {
        Log.d(TAG, "onFABClick: yup it was");
    }

    // Creates a Bitmap marker from vector art
    // From https://stackoverflow.com/questions/42365658/custom-marker-in-google-maps-in-android-with-vector-asset-icon
    private BitmapDescriptor bitmapDescriptorFromVector(Context context, int vectorResId) {
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResId);
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    // A callback method, which is invoked on configuration is changed
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // Adding the pointList arraylist to Bundle
        // Saved in baseActivity instead
        // outState.putParcelable("pois", Parcels.wrap(mListOfPOIs));

        //remember drawer state
        if (mDrawer!=null) {
            outState.putParcelable("drawerIsVisible", Parcels.wrap(mDrawer.isVisible()));
        }

        // Saving the bundle
        super.onSaveInstanceState(outState);
    }

    private void DrawerSetup(){
        //assign the mDrawer view and other contained views
        mDrawerView = findViewById(R.id.drawerView);
        mDrawerTitleTV = findViewById(R.id.title_textView);
        mDrawerDescTV = findViewById(R.id.desc_textView);

        // Configure the details button and click listener
        mDrawerDetailButton = findViewById(R.id.poi_details_button);
        mDrawerDetailButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //let's go to the Details activity
                Intent i = new Intent(MapActivity.this, POIDetailActivity.class);
                Bundle bundle = new Bundle();
                bundle.putParcelable("poi", Parcels.wrap(mSelectedPOI));
                i.putExtras(bundle);
                startActivity(i); // POI is now passed to the new Activity
            }
        });

        //create the mDrawer SlideUp object
        mDrawer = new SlideUpBuilder(mDrawerView)
                .withListeners(new SlideUp.Listener.Events() {
                    @Override
                    public void onSlide(float percent) {
//                        dim.setAlpha(1 - (percent / 100));
//                        if (fab.isShown() && percent < 100) {
//                            fab.hide();
//                        }
                    }

                    @Override
                    public void onVisibilityChanged(int visibility) {
//                        if (visibility == View.GONE){
//                            fab.show();
//                        }
                    }
                })
                .withStartGravity(Gravity.BOTTOM)
                .withLoggingEnabled(true)
                .withGesturesEnabled(true)
                .withStartState(SlideUp.State.HIDDEN)
                .withSlideFromOtherView(findViewById(R.id.rootView))
                .build();

        mHideImageView = findViewById(R.id.arrow_down_imageView);
        mHideImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDrawer.hide();
            }
        });

    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     */
    @SuppressLint("MissingPermission") @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;

        try {
            // Customise the styling of the base map using a JSON object defined
            // in a raw resource file.
            boolean success = mMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            this, R.raw.map_style));

            if (!success) {
                Log.e(TAG, "Style parsing failed.");
            }
        } catch (Resources.NotFoundException e) {
            Log.e(TAG, "Can't find style. Error: ", e);
        }



        // Allow the map to see the devices location, this should ALREADY have permission to get location from GMS_Activity
        mMap.setMyLocationEnabled(true);

        // Set listeners for marker events.  See the bottom of this class for their behavior.
        mMap.setOnMarkerClickListener(this);

    }

    // Create a location given a Lat & Long
    public Location CreateLocation(Double latitude, Double longitude){
        Location tempLocation = new Location("");
        tempLocation.setLongitude(longitude);
        tempLocation.setLatitude(latitude);
        return tempLocation;
    }

    private LatLng locationToLatLong(Location location){
        return new LatLng(location.getLatitude(),location.getLongitude());
    }

    @Override
    public boolean onMarkerClick(final Marker marker) {

        // Assign the selected POI by marker
        mSelectedPOI = StateManager.getInstance().getPOIs().get((int)marker.getTag());

        //Set draw info to selected POI
        mDrawerTitleTV.setText(mSelectedPOI.getName());
        mDrawerDescTV.setText(mSelectedPOI.getDescription());

        Log.d(TAG, "onMarkerClick: You selected marker " + mSelectedPOI.getName() );
        //Bring up the details mDrawer
        mDrawer.show();

        // We return false to indicate that we have not consumed the event and that we wish
        // for the default behavior to occur (which is for the camera to move such that the
        // marker is centered and for the marker's info window to open, if it has one).
        return false;
    }

    private void DrawNearbyMarkers(){
        // Step through all POI's and show markers for close ones (800 meters)

        for(int i = 0;i<StateManager.getInstance().getPOIs().size();i++){

            if(!mExistingPoiMarkers.contains(i)) {
                POI poi = StateManager.getInstance().getPOIs().get(i);

                if (poi.distanceToUser() < Constants.DISTANCE_TO_SCAN_MARKERS) {

                    // Add the location's marker to the map
                    LatLng poiLatLng = new LatLng(poi.getLatitude(), poi.getLongitude());

                    Marker tempMarker = mMap.addMarker(new MarkerOptions()
                            .position(poiLatLng)
                            .icon(bitmapDescriptorFromVector(this, R.drawable.marker_art))
                            .title(poi.getName()));
                    tempMarker.setTag(StateManager.getInstance().getPOIs().indexOf(poi)); //Tag is set to POI Index in full SM Arraylist

                    // Keep track of existing POI markers, so we don't need to redraw them
                    mExistingPoiMarkers.add(StateManager.getInstance().getPOIs().indexOf(poi));

                    //log that the marker is displayed
                    Log.d(TAG, "showing marker " + poi.getName());
                }
            } else {
                //We might , in the future, consider putting code here to remove markers from the map
            }
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d(TAG, "onConnected() called with: " + "bundle = [" + bundle + "]");

        /**
        * get the last location of the device
        * */
        getContinousLocationUpdates(new LocationListener()
        {
            @Override
            public void onLocationReceived(final Location location)
            {

                // Spawn a new thread to redraw nearby markers
                MapActivity.this.runOnUiThread(new Runnable() {

                    @Override
                    public void run() {

                        // Check if this is the first time location was retrieved
                        if(mCurrentLocation==null){
                            Toast.makeText(getApplicationContext(), "Initial location detected", Toast.LENGTH_SHORT).show();
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(locationToLatLong(location), 18));
                        }

                        // Store returned location in the state Manager and member variable
                        StateManager.getInstance().setCurrentLocation(location);
                        mCurrentLocation = location;

                        // clear all current markers on map
                        // mMap.clear();

                        // If we don't have POIs yes, lets get them
                        if (StateManager.getInstance().getPOIs().size() == 0) {
                            try {
                                mPOIsRequester.GetAllPOIs((Activity) context);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        } else {
                            // Step through all POI's and show markers for close ones (800 meters)
                            DrawNearbyMarkers();
                        }

                    }

                });

            }
        });
    }

    /**
     *  Returned POIs Event listener method
     *
     * @param POIs
     */
    public void onPOIsReceived(ArrayList<POI> POIs) {
        StateManager.getInstance().setPOIs(POIs);
    }
}
