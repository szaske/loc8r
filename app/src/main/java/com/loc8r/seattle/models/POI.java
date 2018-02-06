package com.loc8r.seattle.models;


import com.loc8r.seattle.utils.StateManager;

import org.parceler.Parcel;


/**
 * A POJO Class for a POI (Point Of Interest)
 */
@Parcel
public class POI {
    String id;
    String name;
    Double latitude;
    Double longitude;
    String description;
    String img_url;
    String collection;
    int collectionPosition;
    //double distance;
    String stampText;
    boolean isStamped;

    public POI(){}

    public POI(String id,
               String name,
               Double latitude,
               Double longitude,
               String description,
               String img_url,
               String collection,
               int collectionPosition,
               String stampText) {
        this.id = id;
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.description = description;
        this.img_url = img_url;
        this.collection = collection;
        this.collectionPosition = collectionPosition;
        this.stampText = stampText;
        this.isStamped = false; // Set to not stamped by default
    }

    @Override
    public String toString() {
        return "POI{" +
                "id=" + id +
                ",\n img_url='" + img_url + '\'' +
                ",\n name='" + name + '\'' +
                ",\n collection='" + collection + '\'' +
                ",\n description='" + description + '\'' +
                ",\n isStamped=" + isStamped +
                '}';
    }

    /**
     * Calculate distance to this POI from the users current latitude and longitude
     * elevation has been ignored.
     * Uses Haversine method as its base.
     *
     * @return distance in meters
     */
    public int getDistance() {

        final int R = 6371; // Radius of the earth
        double lat2 = getLatitude();
        double lon2 = getLongitude();

        // Defaults to Space Needle location
        double lat1 = 47.620510;
        double lon1 = -122.349312;

        if(StateManager.getInstance().getCurrentLocation()!=null) {
            lat1 = StateManager.getInstance().getCurrentLocation().getLatitude();
            lon1 = StateManager.getInstance().getCurrentLocation().getLongitude();
        }

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c * 1000; // convert to meters

        distance = Math.pow(distance, 2);

        return (int) Math.round(Math.sqrt(distance));
    }

    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public Double getLatitude() { return latitude; }
    public Double getLongitude() { return longitude; }
    public String getDescription() { return description; }
    public String getImg_url() { return img_url; }
    public String getCollection() { return collection; }
    public int getCollectionPosition() { return collectionPosition; }
    public String getStampText() { return stampText; }
    //public double getDistance() { return distance; }
    public boolean isStamped() { return isStamped; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setStamped(boolean stamped) { isStamped = stamped; }
    public void setName(String name) {
        this.name = name;
    }
    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }
    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public void setImg_url(String img_url) {
        this.img_url = img_url;
    }
    public void setCollection(String collection) {
        this.collection = collection;
    }
    public void setCollectionPosition(int collectionPosition) {
        this.collectionPosition = collectionPosition;
    }
    public void setStampText(String stampText) { this.stampText = stampText; }
}
