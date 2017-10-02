package com.loc8r.android.loc8r;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.annotations.MarkerViewOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;

public class MainActivity extends AppCompatActivity {
    private MapView mapView;
    private LatLng myHouse;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        myHouse = new LatLng(47.658365, -122.328074);

        super.onCreate(savedInstanceState);

        //This is my Mapbox API access token, and needs to be called before setContentView
        Mapbox.getInstance(this, getString(R.string.mapbox_access_token));

        setContentView(R.layout.activity_main);
        mapView = (MapView) findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(MapboxMap mapboxMap) {
                mapboxMap.addMarker(new MarkerViewOptions()
                        .position(myHouse)
                        .title(getString(R.string.draw_marker_options_title))
                        .snippet(getString(R.string.draw_marker_options_snippet)));

                CameraPosition position = new CameraPosition.Builder()
                        .target(myHouse) // Sets the new camera position
                        .zoom(17) // Sets the zoom to level 10
                        .tilt(20) // Set the camera tilt to 20 degrees
                        .build(); // Builds the CameraPosition object from the builder
                mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(position),5000);
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        mapView.onStop();
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
}