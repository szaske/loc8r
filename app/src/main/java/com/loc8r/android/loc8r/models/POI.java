package com.loc8r.android.loc8r.models;

import com.google.gson.internal.bind.ArrayTypeAdapter;
import com.mapbox.mapboxsdk.geometry.LatLng;

import java.util.ArrayList;

/**
 * A POJO Class for a POI (Point Of Interest)
 */

public class POI {
    private String name;
    private LatLng location;
    private String description;
    private ArrayList<String> tags;

    public POI(String name, LatLng location, String description) {
        this.name = name;
        this.location = location;
        this.description = description;
    }

    public String getName() { return name; }

    public LatLng getLocation() {
        return location;
    }

    public String getDescription() {
        return description;
    }

    public ArrayList<String> getTags() { return tags; }
}
