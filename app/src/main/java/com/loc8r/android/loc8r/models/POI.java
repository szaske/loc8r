package com.loc8r.android.loc8r.models;

import com.mapbox.mapboxsdk.geometry.LatLng;

/**
 * A POJO Class for a POI (Point Of Interest)
 */

public class POI {
    private String name;
    private String address;
    private LatLng location;
    private String description;

    public POI(String name, String address, LatLng location, String description) {
        this.name = name;
        this.address = address;
        this.location = location;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public LatLng getLocation() {
        return location;
    }

    public String getDescription() {
        return description;
    }
}
