package com.loc8r.seattle.activities;

import android.animation.ObjectAnimator;
import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.os.Bundle;
import android.support.v7.widget.LinearSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SnapHelper;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import com.loc8r.seattle.R;
import com.loc8r.seattle.adapters.POIMapRecyclerViewAdapter;
import com.loc8r.seattle.interfaces.LocationListener;
import com.loc8r.seattle.models.POI;
import com.loc8r.seattle.utils.Constants;
import com.loc8r.seattle.utils.LinearLayoutManagerWithSmoothScroller;
import com.mapbox.androidsdk.plugins.building.BuildingPlugin;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.annotations.PolylineOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.services.api.directions.v5.models.DirectionsRoute;
import com.mapbox.services.commons.geojson.LineString;
import com.mapbox.services.commons.models.Position;

import java.util.ArrayList;
import java.util.List;

import static com.mapbox.services.Constants.PRECISION_6;

public class MapActivity extends LoggedInActivity implements POIMapRecyclerViewAdapter.ClickListener {


    //private static final LatLngBounds LOCKED_MAP_CAMERA_BOUNDS = new LatLngBounds.Builder()
//            .include(new LatLng(40.87096725853152, -74.08277394720501))
//            .include(new LatLng(40.67035340371385,
//                    -73.87063900287112)).build();
    private static final LatLng MOCK_DEVICE_LOCATION_LAT_LNG = new LatLng(47.658331, -122.328474);


    private static final String TAG = LoggedInActivity.class.getSimpleName();

    Location mCurrentLocation;

    // The marker showing our current location
    Marker marker;

    private static final int MAPBOX_LOGO_OPACITY = 75;
    private static final int CAMERA_MOVEMENT_SPEED_IN_MILSECS = 600;
    private static final float NAVIGATION_LINE_WIDTH = 9;
//    private DirectionsRoute currentRoute;
//    private FeatureCollection featureCollection;
    private MapboxMap mapboxMap;
    private MapView mapView;
//    private MapboxDirections directionsApiClient;
    private RecyclerView locationsRecyclerView;


    // Altered in attempt to move to POI class
    // private ArrayList<IndividualLocation> listOfIndividualLocations;
    private ArrayList<POI> listOfPOIs;

    private CustomThemeManager customThemeManager;
    private POIMapRecyclerViewAdapter styleRvAdapter;
    private int chosenTheme = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Configure the Mapbox access token. Configuration can either be called in your application
        // class or in the same activity which contains the mapview.
        Mapbox.getInstance(this, getString(R.string.MAPBOX_ACCESS_TOKEN));

        // Hide the status bar for the map to fill the entire screen
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // Inflate the layout with the the MapView. Always inflate this after the Mapbox access token is configured.
        setContentView(R.layout.activity_map);

        // Create a GeoJSON feature collection from the GeoJSON file in the assets folder.
//        try {
//            getFeatureCollectionFromJson();
//        } catch (Exception exception) {
//            Log.e("MapActivity", "onCreate: " + exception);
//            Toast.makeText(this, R.string.failure_to_load_file, Toast.LENGTH_LONG).show();
//        }

        // Initialize a list of IndividualLocation objects for future use with recyclerview
        //listOfIndividualLocations = new ArrayList<>();
        listOfPOIs = CreateDummyPOIList();

        // Initialize the theme that was selected in the previous activity. The blue theme is set as the backup default.
        //chosenTheme = getIntent().getIntExtra(SELECTED_THEME, R.style.AppTheme_Green);

        // Set up the Mapbox map
        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(final MapboxMap mapboxMap) {

                // Setting the returned mapboxMap object (directly above) equal to the "globally declared" one
                MapActivity.this.mapboxMap = mapboxMap;

                // Initialize the custom class that handles marker icon creation and map styling based on the selected theme
                customThemeManager = new CustomThemeManager(chosenTheme, MapActivity.this, mapView, mapboxMap);
                customThemeManager.initializeTheme();

                // Adjust the opacity of the Mapbox logo in the lower left hand corner of the map
                ImageView logo = mapView.findViewById(R.id.logoView);
                logo.setImageAlpha(MAPBOX_LOGO_OPACITY);

                // Set bounds for the map camera so that the user can't pan the map outside of the NYC area
                // mapboxMap.setLatLngBoundsForCameraTarget(LOCKED_MAP_CAMERA_BOUNDS);

                // Create a list of pois from the feature collection
                //List<Feature> featureList = featureCollection.getFeatures();

                // Loop through the locations to add markers to the map
                for (int x = 0; x < listOfPOIs.size(); x++) {

                    //Feature singleLocation = featureList.get(x);
                    POI singlePOI = listOfPOIs.get(x);

                    // Get the single location's String properties to place in its map marker
                    String singlePOIName = singlePOI.getName();
                    // String singleLocationHours = singleLocation.getStringProperty("hours");
                    String singlePOIDescription = singlePOI.getDescription();
                    // String singleLocationPhoneNum = singleLocation.getStringProperty("phone");

                    // Get the single location's LatLng coordinates
                    //Position singlePOIPosition = (Position) singlePOI.getGeometry().getCoordinates();

                    // Create a new LatLng object with the Position object created above
                    LatLng singlePOILatLng = new LatLng(singlePOI.getLatitude(),
                            singlePOI.getLongitude());

                    // Add the location to the Arraylist of locations for later use in the recyclerview
//                    listOfPOIs.add(new POI(
//                            singleLocationName,
//                            singleLocationDescription,
//                            singleLocationHours,
//                            singleLocationPhoneNum,
//                            singleLocationLatLng
//                    ));

                    // Add the location's marker to the map
                    mapboxMap.addMarker(new MarkerOptions()
                            .position(singlePOILatLng)
                            .title(singlePOIName)
                            .icon(customThemeManager.getUnselectedMarkerIcon()));

                    // Call getInformationFromDirectionsApi() to eventually display the location's
                    // distance from mocked device location
//                    getInformationFromDirectionsApi(singleLocationLatLng.getLatitude(),
//                            singleLocationLatLng.getLongitude(), false, x);
                }

                //Removed as there is a chance we haven't gotten our current location yet.
                // addDeviceLocationMarkerToMap();

                // reset camera to point at current device location
                cameraToCurrentLocation();

                setUpMarkerClickListener();

                // setUpRecyclerViewOfLocationCards(chosenTheme);
            }
        });

        // Create RV outside of Map
        setUpRecyclerViewOfLocationCards(chosenTheme);
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

    //
    public Location CreateLocation(Double latitude, Double longitude){
        Location tempLocation = new Location("");
        tempLocation.setLongitude(longitude);
        tempLocation.setLatitude(latitude);
        return tempLocation;
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
                    Toast.makeText(getApplicationContext(), "location change detected", Toast.LENGTH_SHORT).show();
                }

                mCurrentLocation = location;

                if(mapboxMap != null){

                    //check to see if the current location marker exists
                    if(marker==null){
                        addDeviceLocationMarkerToMap();
                    }else{
                        animateMarkerToCurrentLocation();
                    }

                    Toast.makeText(getApplicationContext(), "location change detected", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onItemClick(int position) {

        // Get the selected individual location via its card's position in the recyclerview of cards
        POI selectedPOICard = listOfPOIs.get(position);

        // Retrieve and change the selected card's marker to the selected marker icon
        Marker markerTiedToSelectedCard = mapboxMap.getMarkers().get(position);
        adjustMarkerSelectStateIcons(markerTiedToSelectedCard);

        // Reposition the map camera target to the selected marker
        LatLng selectedPOILatLng = selectedPOICard.getLatLng();
        repositionMapCamera(selectedPOILatLng);

        // Check for an internet connection before making the call to Mapbox Directions API
//        if (deviceHasInternetConnection()) {
//            // Start call to the Mapbox Directions API
//            getInformationFromDirectionsApi(selectedPOILatLng.getLatitude(),
//                    selectedPOILatLng.getLongitude(), true, null);
//        } else {
//            Toast.makeText(this, R.string.no_internet_message, Toast.LENGTH_LONG).show();
//        }
    }

//    private void getInformationFromDirectionsApi(double destinationLatCoordinate, double destinationLongCoordinate,
//                                                 final boolean fromMarkerClick, @Nullable final Integer listIndex) {
//        // Set up origin and destination coordinates for the call to the Mapbox Directions API
//        Position mockCurrentLocation = Position.fromLngLat(MOCK_DEVICE_LOCATION_LAT_LNG.getLongitude(),
//                MOCK_DEVICE_LOCATION_LAT_LNG.getLatitude());
//        Position destinationMarker = Position.fromLngLat(destinationLongCoordinate, destinationLatCoordinate);
//
//        // Initialize the directionsApiClient object for eventually drawing a navigation route on the map
//        directionsApiClient = new MapboxDirections.Builder()
//                .setOrigin(mockCurrentLocation)
//                .setDestination(destinationMarker)
//                .setOverview(DirectionsCriteria.OVERVIEW_FULL)
//                .setProfile(DirectionsCriteria.PROFILE_DRIVING)
//                .setAccessToken(getString(R.string.access_token))
//                .build();
//
//        directionsApiClient.enqueueCall(new Callback<DirectionsResponse>() {
//            @Override
//            public void onResponse(Call<DirectionsResponse> call, Response<DirectionsResponse> response) {
//                // Check that the response isn't null and that the response has a route
//                if (response.body() == null) {
//                    Log.e("MapActivity", "No routes found, make sure you set the right user and access token.");
//                } else if (response.body().getRoutes().size() < 1) {
//                    Log.e("MapActivity", "No routes found");
//                } else {
//                    if (fromMarkerClick) {
//                        // Retrieve and draw the navigation route on the map
//                        currentRoute = response.body().getRoutes().get(0);
//                        drawNavigationPolylineRoute(currentRoute);
//                    } else {
//                        // Use Mapbox Turf helper method to convert meters to miles and then format the mileage number
//                        DecimalFormat df = new DecimalFormat("#.#");
//                        String finalConvertedFormattedDistance = String.valueOf(df.format(TurfHelpers.convertDistance(
//                                response.body().getRoutes().get(0).getDistance(), "meters", "miles")));
//
//                        // Set the distance for each location object in the list of locations
//                        if (listIndex != null) {
//                            listOfIndividualLocations.get(listIndex).setDistance(finalConvertedFormattedDistance);
//                            // Refresh the displayed recyclerview when the location's distance is set
//                            styleRvAdapter.notifyDataSetChanged();
//                        }
//                    }
//                }
//            }
//
//            @Override
//            public void onFailure(Call<DirectionsResponse> call, Throwable throwable) {
//                Toast.makeText(MapActivity.this, R.string.failure_to_retrieve, Toast.LENGTH_LONG).show();
//            }
//        });
//    }



    private void cameraToCurrentLocation(){
        if(mCurrentLocation != null) {
            repositionMapCamera(new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude()));
        }
    }
    private void repositionMapCamera(LatLng newTarget) {
        CameraPosition newCameraPosition = new CameraPosition.Builder()
                .target(newTarget)
                .build();
        mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(newCameraPosition), CAMERA_MOVEMENT_SPEED_IN_MILSECS);
    }

    // Add the user location marker to the map
    private void addDeviceLocationMarkerToMap() {
        marker = mapboxMap.addMarker(new MarkerOptions()
                .position(new LatLng(mCurrentLocation.getLatitude(),mCurrentLocation.getLongitude()))
                .title(getString(R.string.device_location_title))
                .icon(customThemeManager.getLocationIcon()));
    }

    private void animateMarkerToCurrentLocation(){

        LatLng updatedLocation = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());

        repositionMapCamera(updatedLocation);
        ValueAnimator markerAnimator = ObjectAnimator.ofObject(marker, "position",
                new LatLngEvaluator(), marker.getPosition(), updatedLocation);
        markerAnimator.setDuration(1000);
        markerAnimator.start();
    }

    private LatLng locationToLatLong(Location location){
        return new LatLng(mCurrentLocation.getLatitude(),mCurrentLocation.getLongitude());
    }

    private static class LatLngEvaluator implements TypeEvaluator<LatLng> {
        // Method is used to interpolate the marker animation.

        private LatLng latLng = new LatLng();

        @Override
        public LatLng evaluate(float fraction, LatLng startValue, LatLng endValue) {
            latLng.setLatitude(startValue.getLatitude()
                    + ((endValue.getLatitude() - startValue.getLatitude()) * fraction));
            latLng.setLongitude(startValue.getLongitude()
                    + ((endValue.getLongitude() - startValue.getLongitude()) * fraction));
            return latLng;
        }
    }

//    private void getFeatureCollectionFromJson() throws IOException {
//        try {
//            // Use fromJson() method to convert the GeoJSON file into a usable FeatureCollection object
//            featureCollection = FeatureCollection.fromJson(loadGeoJsonFromAsset("list_of_pois.geojson"));
//        } catch (Exception exception) {
//            Log.e("MapActivity", "getFeatureCollectionFromJson: " + exception);
//        }
//    }
//
//    private String loadGeoJsonFromAsset(String filename) {
//        try {
//            // Load the GeoJSON file from the local asset folder
//            InputStream is = getAssets().open(filename);
//            int size = is.available();
//            byte[] buffer = new byte[size];
//            is.read(buffer);
//            is.close();
//            return new String(buffer, "UTF-8");
//        } catch (Exception exception) {
//            Log.e("MapActivity", "Exception Loading GeoJSON: " + exception.toString());
//            exception.printStackTrace();
//            return null;
//        }
//    }

    private void setUpRecyclerViewOfLocationCards(int chosenTheme) {
        // Initialize the recyclerview of location cards and a custom class for automatic card scrolling
        locationsRecyclerView = findViewById(R.id.map_layout_rv);
        locationsRecyclerView.setHasFixedSize(true);
        locationsRecyclerView.setLayoutManager(new LinearLayoutManagerWithSmoothScroller(this));
        styleRvAdapter = new POIMapRecyclerViewAdapter(listOfPOIs,
                getApplicationContext(), this, chosenTheme);
        locationsRecyclerView.setAdapter(styleRvAdapter);
        SnapHelper snapHelper = new LinearSnapHelper();
        snapHelper.attachToRecyclerView(locationsRecyclerView);
    }

    //Creates click listener that
    private void setUpMarkerClickListener() {
        mapboxMap.setOnMarkerClickListener(new MapboxMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(@NonNull Marker marker) {

                // Get the position of the selected marker
                LatLng positionOfSelectedMarker = marker.getPosition();

                // Check that the selected marker isn't the mock device location marker
                if (!marker.getPosition().equals(MOCK_DEVICE_LOCATION_LAT_LNG)) {

                    for (int x = 0; x < mapboxMap.getMarkers().size(); x++) {
                        if (mapboxMap.getMarkers().get(x).getPosition() == positionOfSelectedMarker) {
                            // Scroll the recyclerview to the selected marker's card. It's "x-1" below because
                            // the mock device location marker is part of the marker list but doesn't have its own card
                            // in the actual recyclerview.
                            locationsRecyclerView.smoothScrollToPosition(x);
                        }
                    }
                    adjustMarkerSelectStateIcons(marker);
                }
                // Return true so that the selected marker's info window doesn't pop up
                return true;
            }
        });
    }

    private void adjustMarkerSelectStateIcons(Marker marker) {
        // Set all of the markers' icons to the unselected marker icon
        for (Marker singleMarker : mapboxMap.getMarkers()) {
            if (!singleMarker.getTitle().equals(getString(R.string.device_location_title))) {
                singleMarker.setIcon(customThemeManager.getUnselectedMarkerIcon());
            }
        }

        // Change the selected marker's icon to a selected state marker except if the mock device location marker is selected
        if (!marker.getIcon().equals(customThemeManager.getLocationIcon())) {
            marker.setIcon(customThemeManager.getSelectedMarkerIcon());
        }

        // Get the directionsApiClient route to the selected marker except if the mock device location marker is selected
//        if (!marker.getIcon().equals(customThemeManager.getLocationIcon())) {
//            // Check for an internet connection before making the call to Mapbox Directions API
//            if (deviceHasInternetConnection()) {
//                // Start the call to the Mapbox Directions API
//                getInformationFromDirectionsApi(marker.getPosition().getLatitude(),
//                        marker.getPosition().getLongitude(), true, null);
//            } else {
//                Toast.makeText(this, R.string.no_internet_message, Toast.LENGTH_LONG).show();
//            }
//        }
    }

    private void drawNavigationPolylineRoute(DirectionsRoute route) {
        // Check for and remove a previously-drawn navigation route polyline before drawing the new one
        if (mapboxMap.getPolylines().size() > 0) {
            mapboxMap.removePolyline(mapboxMap.getPolylines().get(0));
        }

        // Convert LineString coordinates into a LatLng[]
        LineString lineString = LineString.fromPolyline(route.getGeometry(), PRECISION_6);
        List<Position> coordinates = lineString.getCoordinates();
        LatLng[] polylineDirectionsPoints = new LatLng[coordinates.size()];
        for (int i = 0; i < coordinates.size(); i++) {
            polylineDirectionsPoints[i] = new LatLng(
                    coordinates.get(i).getLatitude(),
                    coordinates.get(i).getLongitude());
        }

        // Draw the navigation route polyline on to the map
        mapboxMap.addPolyline(new PolylineOptions()
                .add(polylineDirectionsPoints)
                .color(customThemeManager.getNavigationLineColor())
                .width(NAVIGATION_LINE_WIDTH));
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

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    private boolean deviceHasInternetConnection() {
        ConnectivityManager connectivityManager = (ConnectivityManager)
                getApplicationContext().getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnected();
    }

    /**
     * Custom class which creates marker icons and colors based on the selected theme
     */
    class CustomThemeManager {
        private static final String BUILDING_EXTRUSION_COLOR = "#c4dbed";
        private static final float BUILDING_EXTRUSION_OPACITY = .8f;
        private int selectedTheme;
        private Context context;
        private Icon unselectedMarkerIcon;
        private Icon selectedMarkerIcon;
        private Icon LocationIcon;
        private int navigationLineColor;
        private MapboxMap mapboxMap;
        private MapView mapView;

        CustomThemeManager(int selectedTheme, Context context,
                           MapView mapView, MapboxMap mapboxMap) {
            this.selectedTheme = selectedTheme;
            this.context = context;
            this.mapboxMap = mapboxMap;
            this.mapView = mapView;
        }

        private void initializeTheme() {

            mapboxMap.setStyle(getString(R.string.terminal_map_style));
            navigationLineColor = getResources().getColor(R.color.navigationRouteLine_green);
            unselectedMarkerIcon = IconFactory.getInstance(context).fromResource(R.drawable.green_unselected_seattle);
            selectedMarkerIcon = IconFactory.getInstance(context).fromResource(R.drawable.green_selected_seattle);
            LocationIcon = IconFactory.getInstance(context).fromResource(R.drawable.green_user_location);

        }

        private void showBuildingExtrusions() {
            // Use the Mapbox building plugin to display and customize the opacity/color of building extrusions
            BuildingPlugin buildingPlugin = new BuildingPlugin(mapView, mapboxMap);
            buildingPlugin.setVisibility(true);
            buildingPlugin.setOpacity(BUILDING_EXTRUSION_OPACITY);
            buildingPlugin.setColor(Color.parseColor(BUILDING_EXTRUSION_COLOR));
        }

        Icon getUnselectedMarkerIcon() {
            return unselectedMarkerIcon;
        }

        Icon getSelectedMarkerIcon() {
            return selectedMarkerIcon;
        }

        Icon getLocationIcon() {
            return LocationIcon;
        }

        int getNavigationLineColor() {
            return navigationLineColor;
        }
    }
}
