package com.loc8r.seattle.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.location.Location;
import android.support.annotation.Nullable;
import android.os.Bundle;

import android.animation.ValueAnimator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import android.util.Log;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.util.DisplayMetrics;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.loc8r.seattle.R;
import com.loc8r.seattle.interfaces.LocationListener;
import com.loc8r.seattle.models.POI;
import com.loc8r.seattle.mongodb.MongoDBManager;
import com.loc8r.seattle.utils.StateManager;
import com.mancj.slideup.SlideUp;
import com.mancj.slideup.SlideUpBuilder;

import android.view.View;

import org.parceler.Parcels;

import java.util.ArrayList;

public class MapsActivity extends LoggedInActivity implements
        GoogleMap.OnMarkerClickListener,
        OnMapReadyCallback {

    private static final String TAG = MapsActivity.class.getSimpleName();
    private GoogleMap mMap;
    private ArrayList<POI> mListOfPOIs;
    private ArrayList<Marker> mlistOfPOIMarkers = new ArrayList<Marker>();
    public ViewGroup mDrawerContainer;
    private Animation mDrawer_up_animation;

    private SlideUp mDrawer;
    private View mDrawerView;
    private View mHideImageView;
    private TextView mDrawerTitleTV;
    private TextView mDrawerDescTV;
    private Button mDrawerDetailButton;
    private Location mCurrentLocation;

    // The selected POI
    private POI mSelectedPOI;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // Obtain the SupportMapFragment and loads layout
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        // Get notified when the map is ready to be used.
        mapFragment.getMapAsync(this);

        // Get and remember the full list of POI objects
        mListOfPOIs = MongoDBManager.getInstance(getApplicationContext()).CreateDummyPOIList();

        // Create RV outside of Map
        // setUpRecyclerViewOfLocationCards();

        // Drawer Setup
        DrawerSetup();

        // Change text in mDrawer to screen height
        //setHeightText();


    }

    public void CreateAnimation(){
        // Load the animation from XML
        mDrawer_up_animation = AnimationUtils.loadAnimation(MapsActivity.this, R.anim.slide_up);

        // set the animation listener
        mDrawer_up_animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {

                if (animation == mDrawer_up_animation) {
                    Toast.makeText(MapsActivity.this, "Animation Stopped", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

    }

    private int getScreenHeight(){
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        return metrics.heightPixels;
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
                Intent i = new Intent(MapsActivity.this, POIDetailActivity.class);
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

    public static void move(final ViewGroup viewG){
        ValueAnimator va = ValueAnimator.ofFloat(0f, 300f);
        int mDuration = 2000; //in millis
        va.setDuration(mDuration);
        va.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            public void onAnimationStart(ValueAnimator animation)
            {
                // This is the key...
                //set the coordinates for the bounds (left, top, right, bottom) based on the offset value (50px) in a resource XML
                viewG.setTranslationY(300f);
            }


            public void onAnimationUpdate(ValueAnimator animation) {
                viewG.setTranslationY((float)animation.getAnimatedValue());
            }

            public void onAnimationEnd(ValueAnimator animation){
                viewG.clearAnimation();
            }
        });
        // va.setRepeatCount(5);
        va.start();
    }



    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @SuppressLint("MissingPermission") @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Allow the map to see the devices location, this should ALREADY have permission to get location from LoggedInActivity
        mMap.setMyLocationEnabled(true);

        // mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(AUSTRALIA.getCenter(), 10));

        // Add markers for all nearby POIs
        //CreateListOfMarkers();

        // Set listeners for marker events.  See the bottom of this class for their behavior.
        mMap.setOnMarkerClickListener(this);
    }

//    public ArrayList<POI> CreateDummyPOIList(){
//        ArrayList<POI> tempPOIList = new ArrayList<POI>();
//
//        tempPOIList.add(new POI("Dick's",CreateLocation(47.661116, -122.327877),"Dicks", "www.zaske.com", "street art", "StampIS1","Stamp Text 1"));
//        tempPOIList.add(new POI("Sea Monster Lounge",CreateLocation(47.661542, -122.332299),"Sea Monster", "www.zaske2.com", "street art", "StampIS2","Stamp Text 2"));
//        tempPOIList.add(new POI("Library",CreateLocation(47.661173, -122.338994),"Library", "www.zaske3.com", "street art", "StampIS3","Stamp Text 3"));
//        tempPOIList.add(new POI("Portage Bay Cafe",CreateLocation(47.657846, -122.317634),"Famous eatery with a great waffle bar", "www.zaske3.com", "street art", "StampIS4","Stamp Text 4"));
//        tempPOIList.add(new POI("University Barbershop",CreateLocation(47.658945, -122.313323),"Seattle's best barbershop", "www.zaske3.com", "street art", "StampIS5","Stamp Text 5"));
//        tempPOIList.add(new POI("Burke Museum",CreateLocation(47.660704, -122.310510),"Famous natural history museum", "www.zaske3.com", "street art", "StampIS6","Stamp Text 6"));
//        return tempPOIList;
//    }

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
        mSelectedPOI = mListOfPOIs.get((int)marker.getTag());

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

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d(TAG, "onConnected() called with: " + "bundle = [" + bundle + "]");
                /*
        * get the last location of the device
        * */
        getContinousLocationUpdates(new LocationListener()
        {
            @Override
            public void onLocationReceived(final Location location)
            {

                // Spawn a new thread to redraw nearby markers
                MapsActivity.this.runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        // Check if this is the first time location was retrieved
                        if(mCurrentLocation==null){
                            Toast.makeText(getApplicationContext(), "Initial location detected", Toast.LENGTH_SHORT).show();
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(locationToLatLong(location), 16));
                        }

                        // Store returned location in the state Manager and member variable
                        StateManager.getInstance().setCurrentLocation(location);
                        mCurrentLocation = location;

                        // clear all current markers on map
                        mMap.clear();

                        // Step through all POI's and show markers for close ones (400 meters)
                        for (POI poi : mListOfPOIs) {
                            // if the POI is within 800 meters
                            if (poi.getDistance() < 800) {
                                // Add the location's marker to the map
                                LatLng poiLatLng = new LatLng(poi.getLatitude(), poi.getLongitude());

                                Marker tempMarker = mMap.addMarker(new MarkerOptions()
                                        .position(poiLatLng)
                                        .title(poi.getName()));
                                tempMarker.setTag(mListOfPOIs.indexOf(poi));

                                //log that the marker is displayed
                                Log.d(TAG, "showing marker "+ poi.getName());
                            }
                        }

                    }

                });


            }
        });
    }



}
