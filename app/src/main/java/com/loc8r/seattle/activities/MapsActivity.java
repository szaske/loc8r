package com.loc8r.seattle.activities;

import android.annotation.SuppressLint;
import android.location.Location;
import android.support.annotation.Nullable;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SnapHelper;

import android.transition.Slide;
import android.transition.TransitionManager;
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
import com.loc8r.seattle.adapters.POIMapRecyclerViewAdapter;
import com.loc8r.seattle.interfaces.LocationListener;
import com.loc8r.seattle.models.POI;
import com.loc8r.seattle.utils.StateManager;
import com.mancj.slideup.SlideUp;
import com.mancj.slideup.SlideUpBuilder;

import android.view.View;

import java.util.ArrayList;

public class MapsActivity extends LoggedInActivity implements
        POIMapRecyclerViewAdapter.ClickListener,
        GoogleMap.OnMarkerClickListener,
        OnMapReadyCallback {

    private static final String TAG = MapsActivity.class.getSimpleName();
    private GoogleMap mMap;
    private Marker mDeviceMarker;
    private Location mCurrentLocation;
    private ArrayList<POI> listOfPOIs;
    private ArrayList<Marker> listOfPOIMarkers = new ArrayList<Marker>();
    private RecyclerView locationsRecyclerView;
    private ViewPager mViewPager;
    // private CardPagerAdapter mCardAdapter;
    private POIMapRecyclerViewAdapter mapRVAdapter;
    public ViewGroup transitionsContainer;
    private View AnimeView;
    private Animation slide_up_animation;

    private int drawerClosedYDistance;
    private SlideUp drawer;
    private View drawerView;
    private View hideImageView;
    private Button detailButton;
    private POI selectedPoi;

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
        listOfPOIs = CreateDummyPOIList();

        // Create RV outside of Map
        // setUpRecyclerViewOfLocationCards();

        // Drawer Setup
        DrawerSetup();

        Log.d(TAG, "transitionContainer child count is " + String.valueOf(transitionsContainer.getChildCount()));

        // Change text in drawer to screen height
        setHeightText();

        detailButton = findViewById(R.id.poi_details_button);
        detailButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast toast = Toast.makeText(MapsActivity.this, "Get details on", Toast.LENGTH_SHORT);
                toast.show();
            }
        });
    }

    public void CreateAnimation(){
        // Load the animation from XML
        slide_up_animation = AnimationUtils.loadAnimation(MapsActivity.this, R.anim.slide_up);

        // set the animation listener
        slide_up_animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {

                if (animation == slide_up_animation) {
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
        //assign the drawer view
        drawerView = findViewById(R.id.drawerView);

        //create the drawer object
        drawer = new SlideUpBuilder(drawerView)
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

        hideImageView = findViewById(R.id.arrow_down_imageView);
        hideImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawer.hide();
            }
        });

        // Name the drawer
        transitionsContainer = findViewById(R.id.mapDrawer);
        // View slideView = findViewById(R.id.slideView);

        //move it below the screen

        transitionsContainer.setTranslationY((getScreenHeight()-50));
    }

    private void setHeightText(){
        TextView tv = findViewById(R.id.heightTV);
        tv.setText(String.valueOf(getScreenHeight()));
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

    public ArrayList<POI> CreateDummyPOIList(){
        ArrayList<POI> tempPOIList = new ArrayList<POI>();

        tempPOIList.add(new POI("Dick's",CreateLocation(47.661116, -122.327877),"Dicks", "www.zaske.com", "street art", "StampIS1","Stamp Text 1"));
        tempPOIList.add(new POI("Sea Monster Lounge",CreateLocation(47.661542, -122.332299),"Sea Monster", "www.zaske2.com", "street art", "StampIS2","Stamp Text 2"));
        tempPOIList.add(new POI("Library",CreateLocation(47.661173, -122.338994),"Library", "www.zaske3.com", "street art", "StampIS3","Stamp Text 3"));
        tempPOIList.add(new POI("Portage Bay Cafe",CreateLocation(47.657846, -122.317634),"Famous eatery with a great waffle bar", "www.zaske3.com", "street art", "StampIS4","Stamp Text 4"));
        tempPOIList.add(new POI("University Barbershop",CreateLocation(47.658945, -122.313323),"Seattle's best barbershop", "www.zaske3.com", "street art", "StampIS5","Stamp Text 5"));
        tempPOIList.add(new POI("Burke Museum",CreateLocation(47.660704, -122.310510),"Famous natural history museum", "www.zaske3.com", "street art", "StampIS6","Stamp Text 6"));
        return tempPOIList;
    }

    // Create a location given a Lat & Long
    public Location CreateLocation(Double latitude, Double longitude){
        Location tempLocation = new Location("");
        tempLocation.setLongitude(longitude);
        tempLocation.setLatitude(latitude);
        return tempLocation;
    }

    private void CreateListOfMarkers(){
        for (int x = 0; x < listOfPOIs.size(); x++) {
            POI singlePOI = listOfPOIs.get(x);

            // Add the location's marker to the map
            LatLng poiLatLng = new LatLng(singlePOI.getLatitude(), singlePOI.getLongitude());

            Marker tempMarker = mMap.addMarker(new MarkerOptions()
            .position(poiLatLng)
            .title(singlePOI.getName()));

            // Create a list of markers so we can address them when clicked (so we have an index of current markers)
            listOfPOIMarkers.add(tempMarker);
        }
    }

    private LatLng locationToLatLong(Location location){
        return new LatLng(location.getLatitude(),location.getLongitude());
    }

    private void setUpRecyclerViewOfLocationCards() {
        // Initialize the recyclerview of location cards and a custom class for automatic card scrolling
        locationsRecyclerView = findViewById(R.id.map_rv);
        locationsRecyclerView.setHasFixedSize(true);


        // locationsRecyclerView.setLayoutManager(new LinearLayoutManagerWithSmoothScroller(this));
        //locationsRecyclerView.setLayoutManager(new GridLayoutManager(this,2,GridLayoutManager.HORIZONTAL, false));
        locationsRecyclerView.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false));
        mapRVAdapter = new POIMapRecyclerViewAdapter(listOfPOIs,
                getApplicationContext(), this, 3);
        locationsRecyclerView.setAdapter(mapRVAdapter);
        SnapHelper snapHelper = new LinearSnapHelper();

        snapHelper.attachToRecyclerView(locationsRecyclerView);

//        //Setup ViewPager
//        mViewPager = findViewById(R.id.viewPager);
//
//        // Create the Card Adapter
//        mCardAdapter = new CardPagerAdapter();
//
//        for (int x = 0; x < listOfPOIs.size(); x++) {
//            mCardAdapter.addCardItem(listOfPOIs.get(x));
//        }
//
//        //Assign the adapter
//        mViewPager.setAdapter(mCardAdapter);

    }

    // Click event listener for the RV
    @Override public void onItemClick(int position) {

        // Get the selected individual location via its card's position in the recyclerview of cards
        POI selectedPOICard = listOfPOIs.get(position);
        Marker selectedPOIMarker = listOfPOIMarkers.get(position);

        Toast.makeText(getApplicationContext(), "Card #" + position+ " clicked", Toast.LENGTH_SHORT).show();

        // Zoom in to the linked marker
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(selectedPOIMarker.getPosition(), 16));

        //scroll the recycler view
        locationsRecyclerView.smoothScrollToPosition(position);

        TransitionManager.beginDelayedTransition(transitionsContainer, new Slide(Gravity.BOTTOM));

        // TODO: update below code to work with Google Maps
        // Retrieve and change the selected card's marker to the selected marker icon
//        Marker markerTiedToSelectedCard = mapboxMap.getMarkers().get(position);
//        adjustMarkerSelectStateIcons(markerTiedToSelectedCard);

        // Reposition the map camera target to the selected marker
//        LatLng selectedPOILatLng = selectedPOICard.getLatLng();
//        repositionMapCamera(selectedPOILatLng);
    }

    @Override
    public boolean onMarkerClick(final Marker marker) {

        // Assign the selected POI by marker
        selectedPoi = listOfPOIs.get((int)marker.getTag());

        Log.d(TAG, "onMarkerClick: You selected marker " + selectedPoi.getName() );
        //Bring up the details drawer
        drawer.show();

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

                        // Store returned location in the state Manager
                        StateManager.getInstance().setCurrentLocation(location);

                        // clear all current markers on map
                        mMap.clear();

                        // Step through all POI's and show markers for close ones (400 meters)
                        for (POI poi : listOfPOIs) {
                            // if the POI is within 800 meters
                            if (poi.getDistance() < 800) {
                                // Add the location's marker to the map
                                LatLng poiLatLng = new LatLng(poi.getLatitude(), poi.getLongitude());

                                Marker tempMarker = mMap.addMarker(new MarkerOptions()
                                        .position(poiLatLng)
                                        .title(poi.getName()));
                                tempMarker.setTag(listOfPOIs.indexOf(poi));

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
