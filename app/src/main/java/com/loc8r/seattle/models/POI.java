package com.loc8r.seattle.models;


import android.location.Location;
import android.widget.SimpleCursorTreeAdapter;

import com.loc8r.seattle.utils.StateManager;

import org.parceler.Parcel;


/**
 * A POJO Class for a POI (Point Of Interest)
 */
@Parcel
public class POI {
    String id;
    String name;
    int release;
    Double latitude;
    Double longitude;
    String description;
    String img_url;
    String collection;
    int collectionPosition;
    String stampText;
    boolean stampChecked; // Has the POI been checked to be stamped?
    Stamp stamp;

    public POI(){}

    public POI(String id){
        this.id = id;}

    public POI(String id,
               String name,
               int release,
               Double latitude,
               Double longitude,
               String description,
               String img_url,
               String collection,
               int collectionPosition,
               String stampText) {
        this.id = id;
        this.name = name;
        this.release = release;
        this.latitude = latitude;
        this.longitude = longitude;
        this.description = description;
        this.img_url = img_url;
        this.collection = collection;
        this.collectionPosition = collectionPosition;
        this.stampText = stampText;
        this.stampChecked = false;
    }

    @Override
    public String toString() {
        return "POI{" +
                "id=" + id +
                ",\n img_url='" + img_url + '\'' +
                ",\n name='" + name + '\'' +
                ",\n collection='" + collection + '\'' +
                ",\n description='" + description + '\'' +
                '}';
    }

    /**
     *  Calculates the distance between a POI and the user, in meters
     *
     * @return the distance between a POI and the user, in meters or ZERO if the users location is not known.
     */
    public int distanceToUser(){

        if(StateManager.getInstance().getCurrentLocation()!=null){

            float[] distance = new float[1];

            Location.distanceBetween(
                    StateManager.getInstance().getCurrentLocation().getLatitude(),
                    StateManager.getInstance().getCurrentLocation().getLongitude(),
                    latitude,
                    longitude,
                    distance);
            return Math.round(distance[0]);
        } else {
            return 0;
        }
    }

    /**
     *  Checks to see if this POI has a stamp
     *
     * @return boolean, true if stamp variable is of type Stamp
     */
    public boolean isStamped(){
     return (stamp instanceof Stamp);
    }

    public Location location(){
        Location poiLocation = new Location("Seattle Passport"); //provider name is unnecessary
        poiLocation.setLatitude(latitude);
        poiLocation.setLongitude(longitude);
        return poiLocation;
    }

    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public int getRelease() { return release; }
    public Double getLatitude() { return latitude; }
    public Double getLongitude() { return longitude; }
    public String getDescription() { return description; }
    public String getImg_url() { return img_url; }
    public String getCollection() { return collection; }
    public int getCollectionPosition() { return collectionPosition; }
    public String getStampText() { return stampText; }
    public Stamp getStamp() { return stamp; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setName(String name) {
        this.name = name;
    }
    public void setRelease(int release) { this.release = release; }
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
    public void setStamp(Stamp stamp){
        this.stamp = stamp;
    }

}
