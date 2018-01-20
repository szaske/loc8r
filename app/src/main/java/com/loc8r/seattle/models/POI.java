package com.loc8r.seattle.models;


import android.location.Location;
import android.os.Parcelable;

import com.google.android.gms.nearby.messages.Distance;
import com.loc8r.seattle.utils.StateManager;
import com.mapbox.mapboxsdk.geometry.LatLng;

import org.bson.Document;
import org.bson.types.ObjectId;

import org.parceler.Parcel;

import java.util.ArrayList;
import java.util.StringTokenizer;

/**
 * A POJO Class for a POI (Point Of Interest)
 */
@Parcel
public class POI {
    ObjectId id;
    String name;
    String img_url;
    Location location;
    String category;
    String description;
    String stampId;
    String stampText;
    //double distance;
    boolean isStamped;

    public POI(){}

    public POI(String name, Location location, String description, String img_url, String category, String stampId, String stampText) {
        this.name = name;
        this.location = location;
        this.description = description;
        this.img_url = img_url;
        this.category = category;
        this.stampId = stampId;
        this.stampText = stampText;
        this.isStamped = false; // Set to not stamped by default
    }

    /**
    * Helper class to keep all the field names in one place
    **/
    public class Field
    {
        public static final String ID = "_id";
        public static final String NAME = "name";
        static final String DESC = "description";
        static final String LOC = "location";
        static final String COORD = "coordinates";
        static final String IMG_URL = "img_url";
        static final String DIST = "dist";
        static final String CAT = "category";
        static final String STAMP_ID = "stampId";
        static final String STAMP_TEXT = "stampText";
    }

    /**
    * Parse the POI object from the MongoDB document
    **/
    public static POI fromDocument(Document document)
    {
        POI poi = new POI();
        try
        {
            poi.id = document.getObjectId(Field.ID);
            poi.name = document.getString(Field.NAME);
            poi.description = document.getString(Field.DESC);
            poi.img_url = document.getString(Field.IMG_URL);
            poi.category = document.getString(Field.CAT);
            poi.stampText = document.getString(Field.STAMP_TEXT);
            poi.stampId = document.getString(Field.STAMP_ID);

            Document location = (Document) document.get(Field.LOC);
            ArrayList coords = (ArrayList) location.get(Field.COORD);
            Location tempLocation = new Location("");
            /**
            * note: in MongoDB the longitude is at position 0, and the latitude is in position 1
            **/
            tempLocation.setLongitude((Double) coords.get(0));
            tempLocation.setLatitude((Double) coords.get(1));
            poi.location = tempLocation;
            //poi.distance = getDistance();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return poi;
    }

    @Override
    public String toString() {
        return "POI{" +
                "id=" + id +
                ",\n img_url='" + img_url + '\'' +
                ",\n name='" + name + '\'' +
                ",\n location=" + location +
                ",\n category='" + category + '\'' +
                ",\n description='" + description + '\'' +
                ",\n stampId='" + stampId + '\'' +
                ",\n stampText='" + stampText + '\'' +
                ",\n distance=" + String.valueOf(getDistance()) +
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
        double lat2 = location.getLatitude();
        double lon2 = location.getLongitude();
        double lat1 = StateManager.getInstance().getCurrentLocation().getLatitude();
        double lon1 = StateManager.getInstance().getCurrentLocation().getLongitude();

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

    public LatLng getLatLng(){
        LatLng tempLatLng = new LatLng();
        tempLatLng.setLongitude(this.getLongitude());
        tempLatLng.setLatitude(this.getLatitude());
        return tempLatLng;
    }

    public ObjectId getId() { return id; }
    public void setId(ObjectId id) { this.id = id; }
    public String getImg_url() { return img_url; }
    public String getName() { return name; }
    public String getStampId() { return stampId; }
    public String getStampText() { return stampText; }
    public String getCategory() { return category; }
    public Double getLatitude() { return location.getLatitude(); }
    public Double getLongitude() { return location.getLongitude(); }
    public String getDescription() {
        return description;
    }
    //public double getDistance() { return distance; }
    public boolean isStamped() { return isStamped; }
    public void setStamped(boolean stamped) { isStamped = stamped; }
}
