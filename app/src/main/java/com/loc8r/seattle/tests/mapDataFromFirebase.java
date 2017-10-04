package com.loc8r.seattle.tests;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;
import android.view.WindowManager;

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
import com.mapbox.services.commons.geojson.FeatureCollection;

import java.io.IOException;
import java.util.ArrayList;

public class mapDataFromFirebase extends AppCompatActivity implements poiRequesterResponse {

    private FeatureCollection featureCollection;
    private MapView mapView;
    LatLngBounds.Builder latLngBoundsBuilder;

    private ArrayList<POI> mPOIs; // What's being tracked
    private poiRequester mPOIsRequester; //helper class

    // We might want to save a copy of Map
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


//        // Create a GeoJSON feature collection from the GeoJSON file in the assets folder.
//        try {
//            getFeatureCollectionFromJson();
//        } catch (Exception exception) {
//            Log.e("MapActivity", "onCreate: " + exception);
//            Toast.makeText(this, R.string.JSON_file_failure, Toast.LENGTH_LONG).show();
//        }

        // Initialize a list of IndividualLocation objects for future use with recyclerview
//        listOfIndividualLocations = new ArrayList<>();

        // Initialize the theme that was selected in the previous activity. The blue theme is set as the backup default.
//        chosenTheme = getIntent().getIntExtra(SELECTED_THEME, R.style.AppTheme_Blue);

        // Set up the Mapbox map
        mapView = (MapView) findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);


        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(final MapboxMap mapboxMap) {

                // Setting the returned mapboxMap object (directly above) equal to the "globally declared" one
                mapDataFromFirebase.this.mapboxMap = mapboxMap;

                // Initialize the custom class that handles marker icon creation and map styling based on the selected theme
//                customThemeManager = new CustomThemeManager(chosenTheme, MapActivity.this, mapView, mapboxMap);
//                customThemeManager.initializeTheme();

                // Adjust the opacity of the Mapbox logo in the lower left hand corner of the map
//                ImageView logo = mapView.findViewById(R.id.logoView);
//                logo.setImageAlpha(MAPBOX_LOGO_OPACITY);

                // Set bounds for the map camera so that the user can't pan the map outside of the NYC area
//                mapboxMap.setLatLngBoundsForCameraTarget(LOCKED_MAP_CAMERA_BOUNDS);



                // Add the fake device location marker to the map. In a real use case scenario, the Mapbox location layer plugin
                // can be used to easily display the device's location
                //addMockDeviceLocationMarkerToMap();

                //setUpMarkerClickListener();

                //setUpRecyclerViewOfLocationCards(chosenTheme);

                // Better get some content
                    requestMorePOIs();
            }
        });
    }


    // Add the mapView's lifecycle to the activity's lifecycle methods
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
}
