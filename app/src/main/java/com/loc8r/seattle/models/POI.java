package com.loc8r.seattle.models;


import java.util.ArrayList;
import java.util.StringTokenizer;

/**
 * A POJO Class for a POI (Point Of Interest)
 */

public class POI {
    private String id;
    private String img_url;
    private String name;
    private Double latitude;
    private Double longitude;
    private String description;
    private ArrayList<String> tags;

    public POI(){}

    public POI(String name, Double latitude, Double longitude, String description) {
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.description = description;
    }

    public String getId() { return id; }

    public String getImg_url() { return img_url; }

    public String getName() { return name; }

    public Double getLatitude() { return latitude; }

    public Double getLongitude() { return longitude; }

    public String getDescription() {
        return description;
    }

    public ArrayList<String> getTags() { return tags; }
}
