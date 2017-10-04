package com.loc8r.seattle.tests;

import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.loc8r.android.loc8r.R;
import com.loc8r.seattle.models.POI;
import com.loc8r.seattle.utils.poiRequester;
import com.loc8r.seattle.utils.poiRequester.poiRequesterResponse;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.plugins.locationlayer.LocationLayerMode;
import com.mapbox.mapboxsdk.plugins.locationlayer.LocationLayerPlugin;
import com.mapbox.services.android.location.LostLocationEngine;
import com.mapbox.services.android.telemetry.location.LocationEngine;
import com.mapbox.services.android.telemetry.location.LocationEngineListener;
import com.mapbox.services.android.telemetry.location.LocationEnginePriority;
import com.mapbox.services.android.telemetry.permissions.PermissionsListener;
import com.mapbox.services.android.telemetry.permissions.PermissionsManager;
import com.mapbox.services.commons.geojson.FeatureCollection;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class mapDataFromFirebase extends AppCompatActivity implements poiRequesterResponse, LocationEngineListener, PermissionsListener {

    private FeatureCollection featureCollection;
    private MapView mapView;
    LatLngBounds.Builder latLngBoundsBuilder;

    private ArrayList<POI> mPOIs; // What's being tracked
    private poiRequester mPOIsRequester; //helper class

    // We might want to save a copy of Map
    private PermissionsManager permissionsManager;
    private LocationLayerPlugin locationPlugin;
    private LocationEngine locationEngine;
    private MapboxMap mapboxMap;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Configure the Mapbox access token. Configuration can either be called in your application
        // class or in the same activity which contains the mapview.
        Mapbox.getInstance(this, getString(R.string.mapbox_access_token));

        // Hide the status bar for the map to fill the entire screen
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // Inflate the layout with the the MapView. Always inflate this after the Mapbox access token is configured.
        setContentView(R.layout.test_activity_map_data_from_fb);

        //Instantiate the POIS list
        mPOIs = new ArrayList<>();

        //This is the object that fetches POI data
        mPOIsRequester = new poiRequester(this);

        // Set up the Mapbox map
        mapView = (MapView) findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);


        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(final MapboxMap mapboxMap) {

                // Setting the returned mapboxMap object (directly above) equal to the "globally declared" one
                mapDataFromFirebase.this.mapboxMap = mapboxMap;

                //Show our location
                enableLocationPlugin();

                // Better get some content
                requestMorePOIs();
            }
        });
    }


    //the mapView's lifecycle to the activity's lifecycle methods
    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    private void requestMorePOIs() {

        try {
            mPOIsRequester.getPOIs();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void receivedNewPOIs(final ArrayList<POI> newPOIs) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                // Delete the list and markers
                mPOIs.clear();
                mapboxMap.clear();

                mPOIs.addAll(newPOIs); // This adds a new item to the list
                //mAdapter.notifyItemInserted(mAwwsList.size());  //This tells the adapter to reset and redraw

                // Create a variable to track camera view
                latLngBoundsBuilder = new LatLngBounds.Builder();

                for (POI poi : mPOIs) {

                    // Add the location's marker to the map
                    mapboxMap.addMarker(new MarkerOptions()
                            .position(new LatLng(poi.getLatitude(), poi.getLongitude()))
                            .title(poi.getName()));

                    //Add location to camera view
                    latLngBoundsBuilder.include(new LatLng(poi.getLatitude(), poi.getLongitude()));
                }
                // moveCamera alters the camera
                // CameraUpdateFactory creates a camera update
                // newLatLngBounds creates a new bounded view
                mapboxMap.moveCamera(CameraUpdateFactory.newLatLngBounds(latLngBoundsBuilder.build(),100));
            }
        });
    }

    @SuppressWarnings( {"MissingPermission"})
    private void enableLocationPlugin() {
        // Check if permissions are enabled and if not request
        if (PermissionsManager.areLocationPermissionsGranted(this)) {
            // Create an instance of LOST location engine
            initializeLocationEngine();

            locationPlugin = new LocationLayerPlugin(mapView, mapboxMap, locationEngine);
            locationPlugin.setLocationLayerEnabled(LocationLayerMode.TRACKING);
        } else {
            permissionsManager = new PermissionsManager(this);
            permissionsManager.requestLocationPermissions(this);
        }
    }

    @SuppressWarnings( {"MissingPermission"})
    private void initializeLocationEngine() {
        locationEngine = new LostLocationEngine(mapDataFromFirebase.this);
        locationEngine.setPriority(LocationEnginePriority.HIGH_ACCURACY);
        locationEngine.activate();

        Location lastLocation = locationEngine.getLastLocation();
        if (lastLocation != null) {
            setCameraPosition(lastLocation);
        } else {
            locationEngine.addLocationEngineListener(this);
        }
    }

    private void setCameraPosition(Location location) {
        mapboxMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                new LatLng(location.getLatitude(), location.getLongitude()), 16));
    }

    @Override
    @SuppressWarnings( {"MissingPermission"})
    public void onConnected() {
        locationEngine.requestLocationUpdates();
    }

    //
    @Override
    public void onLocationChanged(Location location) {
        Toast.makeText(this, "location changed", Toast.LENGTH_LONG).show();
        if (location != null) {
            setCameraPosition(location);
            locationEngine.removeLocationEngineListener(this);
        }
    }

    @Override
    public void onExplanationNeeded(List<String> permissionsToExplain) {

    }

    @Override
    public void onPermissionResult(boolean granted) {
        if (granted) {
            enableLocationPlugin();
        } else {
            Toast.makeText(this, "You didn't grant location permissions.", Toast.LENGTH_LONG).show();
            finish();
        }
    }
}
