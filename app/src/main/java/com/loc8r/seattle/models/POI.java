package com.loc8r.seattle.models;


import java.util.ArrayList;

/**
 * A POJO Class for a POI (Point Of Interest)
 */

public class POI {
    private String name;
    private Double latitude;
    private Double longitude;
    private String description;
    private ArrayList<String> tags;

    public POI(String name, Double latitude, Double longitude, String description) {
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.description = description;
    }

    public String getName() { return name; }

    public Double getLatitude() { return latitude; }

    public Double getLongitude() { return longitude; }

    public String getDescription() {
        return description;
    }

    public ArrayList<String> getTags() { return tags; }
}
