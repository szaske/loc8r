package com.loc8r.android.loc8r.tests;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import com.loc8r.android.loc8r.R;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.services.commons.geojson.Feature;
import com.mapbox.services.commons.geojson.FeatureCollection;
import com.mapbox.services.commons.models.Position;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class mapFeatureCollection extends AppCompatActivity {

    private FeatureCollection featureCollection;
    private MapView mapView;
    LatLngBounds.Builder latLngBoundsBuilder;

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
        setContentView(R.layout.test_activity_map_feature_collection);

        // Create a GeoJSON feature collection from the GeoJSON file in the assets folder.
        try {
            getFeatureCollectionFromJson();
        } catch (Exception exception) {
            Log.e("MapActivity", "onCreate: " + exception);
            Toast.makeText(this, R.string.JSON_file_failure, Toast.LENGTH_LONG).show();
        }

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
                mapFeatureCollection.this.mapboxMap = mapboxMap;

                // Initialize the custom class that handles marker icon creation and map styling based on the selected theme
//                customThemeManager = new CustomThemeManager(chosenTheme, MapActivity.this, mapView, mapboxMap);
//                customThemeManager.initializeTheme();

                // Adjust the opacity of the Mapbox logo in the lower left hand corner of the map
//                ImageView logo = mapView.findViewById(R.id.logoView);
//                logo.setImageAlpha(MAPBOX_LOGO_OPACITY);

                // Set bounds for the map camera so that the user can't pan the map outside of the NYC area
//                mapboxMap.setLatLngBoundsForCameraTarget(LOCKED_MAP_CAMERA_BOUNDS);

                // Create a variable to track camera view
                latLngBoundsBuilder = new LatLngBounds.Builder();

                // Create a list of features from the feature collection
                List<Feature> featureList = featureCollection.getFeatures();

                // Loop through the locations to add markers to the map
                for (int x = 0; x < featureList.size(); x++) {

                    Feature singleLocation = featureList.get(x);

                    // Get the single location's String properties to place in its map marker
                    String singleLocationName = singleLocation.getStringProperty("name");
                    String singleLocationDescription = singleLocation.getStringProperty("description");


                    // Get the single location's LatLng coordinates
                    Position singleLocationPosition = (Position) singleLocation.getGeometry().getCoordinates();

                    // Create a new LatLng object with the Position object created above
                    LatLng singleLocationLatLng = new LatLng(singleLocationPosition.getLatitude(),
                            singleLocationPosition.getLongitude());

                    //Add location to camera view
                    latLngBoundsBuilder.include(singleLocationLatLng);

                    // Add the location to the Arraylist of locations for later use in the recyclerview
//                    listOfIndividualLocations.add(new IndividualLocation(
//                            singleLocationName,
//                            singleLocationDescription,
//                            singleLocationHours,
//                            singleLocationPhoneNum,
//                            singleLocationLatLng
//                    ));




                    // Add the location's marker to the map
                    mapboxMap.addMarker(new MarkerOptions()
                            .position(singleLocationLatLng)
                            .title(singleLocationName));

                    // Call getInformationFromDirectionsApi() to eventually display the location's
                    // distance from mocked device location
//                    getInformationFromDirectionsApi(singleLocationLatLng.getLatitude(),
//                            singleLocationLatLng.getLongitude(), false, x);
                }

                // moveCamera alters the camera
                // CameraUpdateFactory creates a camera update
                // newLatLngBounds creates a new bounded view
                mapboxMap.moveCamera(CameraUpdateFactory.newLatLngBounds(latLngBoundsBuilder.build(),100));

                // Add the fake device location marker to the map. In a real use case scenario, the Mapbox location layer plugin
                // can be used to easily display the device's location
                //addMockDeviceLocationMarkerToMap();

                //setUpMarkerClickListener();

                //setUpRecyclerViewOfLocationCards(chosenTheme);
            }
        });
    }

    private void getFeatureCollectionFromJson() throws IOException {
        try {
            // Use fromJson() method to convert the GeoJSON file into a usable FeatureCollection object
            featureCollection = FeatureCollection.fromJson(loadGeoJsonFromAsset("test/poiList.geojson"));
        } catch (Exception exception) {
            Log.e("MapActivity", "getFeatureCollectionFromJson: " + exception);
        }
    }

    private String loadGeoJsonFromAsset(String filename) {
        try {
            // Load the GeoJSON file from the local asset folder
            InputStream is = getAssets().open(filename);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            return new String(buffer, "UTF-8");
        } catch (Exception exception) {
            Log.e("MapActivity", "Exception Loading GeoJSON: " + exception.toString());
            exception.printStackTrace();
            return null;
        }
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
}
