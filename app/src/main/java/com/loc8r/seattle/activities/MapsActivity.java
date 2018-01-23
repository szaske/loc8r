package com.loc8r.seattle.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SnapHelper;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.loc8r.seattle.R;
import com.loc8r.seattle.adapters.POIMapRecyclerViewAdapter;
import com.loc8r.seattle.interfaces.LocationListener;
import com.loc8r.seattle.models.POI;
import com.loc8r.seattle.utils.LinearLayoutManagerWithSmoothScroller;

import java.util.ArrayList;

public class MapsActivity extends LoggedInActivity implements POIMapRecyclerViewAdapter.ClickListener, OnMapReadyCallback {

    private static final String TAG = MapsActivity.class.getSimpleName();
    private GoogleMap mMap;
    private Marker mDeviceMarker;
    private Location mCurrentLocation;
    private ArrayList<POI> listOfPOIs;
    private RecyclerView locationsRecyclerView;
    private POIMapRecyclerViewAdapter mapRVAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // Obtain the SupportMapFragment and loads layout
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        // Get notified when the map is ready to be used.
        mapFragment.getMapAsync(this);

        // Initialize a list of POI objects for future use with recyclerview
        listOfPOIs = CreateDummyPOIList();

        // Create RV outside of Map
        setUpRecyclerViewOfLocationCards();

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
        AddPOIsToMap();

        //Now we want to zoom into our current location, but we have to do this in our
    }

    public ArrayList<POI> CreateDummyPOIList(){
        ArrayList<POI> POIList = new ArrayList<POI>();
        // dicks 47.661116, -122.327877
        // String name, Location location, String description, String img_url, String category, String stampId, String stampText
        POIList.add(new POI("Dick's",CreateLocation(47.661116, -122.327877),"Dicks", "www.zaske.com", "street art", "StampIS1","Stamp Text 1"));
        POIList.add(new POI("Sea Monster Lounge",CreateLocation(47.661542, -122.332299),"Sea Monster", "www.zaske2.com", "street art", "StampIS2","Stamp Text 2"));
        POIList.add(new POI("Library",CreateLocation(47.661173, -122.338994),"Library", "www.zaske3.com", "street art", "StampIS3","Stamp Text 3"));
        return POIList;
    }

    // Create a location given a Lat & Long
    public Location CreateLocation(Double latitude, Double longitude){
        Location tempLocation = new Location("");
        tempLocation.setLongitude(longitude);
        tempLocation.setLatitude(latitude);
        return tempLocation;
    }

    private void AddPOIsToMap(){
        for (int x = 0; x < listOfPOIs.size(); x++) {
            POI singlePOI = listOfPOIs.get(x);

            // Add the location's marker to the map
            LatLng poiLatLng = new LatLng(singlePOI.getLatitude(), singlePOI.getLongitude());
            mMap.addMarker(new MarkerOptions().position(poiLatLng).title(singlePOI.getName()));
        }
    }

    private LatLng locationToLatLong(Location location){
        return new LatLng(mCurrentLocation.getLatitude(),mCurrentLocation.getLongitude());
    }

    private void setUpRecyclerViewOfLocationCards() {
        // Initialize the recyclerview of location cards and a custom class for automatic card scrolling
        locationsRecyclerView = findViewById(R.id.map_rv);
        locationsRecyclerView.setHasFixedSize(true);
        locationsRecyclerView.setLayoutManager(new LinearLayoutManagerWithSmoothScroller(this));
        mapRVAdapter = new POIMapRecyclerViewAdapter(listOfPOIs,
                getApplicationContext(), this, 3);
        locationsRecyclerView.setAdapter(mapRVAdapter);
        SnapHelper snapHelper = new LinearSnapHelper();
        snapHelper.attachToRecyclerView(locationsRecyclerView);
    }

    @Override public void onItemClick(int position) {

        // Get the selected individual location via its card's position in the recyclerview of cards
        POI selectedPOICard = listOfPOIs.get(position);

        // TODO: update below code to work with Google Maps
        // Retrieve and change the selected card's marker to the selected marker icon
//        Marker markerTiedToSelectedCard = mapboxMap.getMarkers().get(position);
//        adjustMarkerSelectStateIcons(markerTiedToSelectedCard);

        // Reposition the map camera target to the selected marker
//        LatLng selectedPOILatLng = selectedPOICard.getLatLng();
//        repositionMapCamera(selectedPOILatLng);
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
            public void onLocationReceived(Location location)
            {

                // Check if this is the first time location was retrieved
                if(mCurrentLocation==null){
                    Toast.makeText(getApplicationContext(), "Initial location detected", Toast.LENGTH_SHORT).show();
                }

                // Store returned location
                mCurrentLocation = location;

                // Zoom into our current location
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(locationToLatLong(location), 16));

            }
        });
    }



}
