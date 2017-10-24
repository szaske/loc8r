package com.loc8r.seattle.models;


import android.location.Location;
import android.os.Parcelable;

import com.google.android.gms.nearby.messages.Distance;

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
    String img_url;
    String name;
    Location location;
    String category;
    String description;
    String stampText;
    double distance;

    public POI(){}

    public POI(String name, Location location, String description, String img_url, String category, String stampText) {
        this.name = name;
        this.location = location;
        this.description = description;
        this.img_url = img_url;
        this.category = category;
        this.stampText = stampText;
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

            Document location = (Document) document.get(Field.LOC);
            ArrayList coords = (ArrayList) location.get(Field.COORD);
            Location tempLocation = new Location("");
            /**
            * note: in MongoDB the longitude is at position 0, and the latitude is in position 1
            **/
            tempLocation.setLongitude((Double) coords.get(0));
            tempLocation.setLatitude((Double) coords.get(1));
            poi.location = tempLocation;

            // Being extra careful here.  Distance will only exist
            // if we parsed the POI object after a geoNear command,
            // where the distance was calculated
            Number distance = (Number) document.get(Field.DIST);
            if (distance != null)
            {
                poi.distance = distance.doubleValue();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return poi;
    }

    public ObjectId getId() { return id; }
    public void setId(ObjectId id) { this.id = id; }
    public String getImg_url() { return img_url; }
    public String getName() { return name; }
    public String getStampText() { return stampText; }
    public String getCategory() { return category; }
    public Double getLatitude() { return location.getLatitude(); }
    public Double getLongitude() { return location.getLongitude(); }
    public String getDescription() {
        return description;
    }
    public double getDistance() { return distance; }
}
