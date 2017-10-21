package com.loc8r.seattle.models;


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
    Double latitude;
    Double longitude;
    String description;
    ArrayList<String> tags;
    double distance;

    public POI(){}

    public POI(String name, Double latitude, Double longitude, String description) {
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.description = description;
    }

    /*
    * Helper class to keep all the field names in one place
    * */
    public class Field
    {
        public static final String ID = "_id";
        public static final String NAME = "name";
        static final String DESC = "description";
        static final String LAT = "latitude";
        static final String LONG = "longitude";
        static final String IMG_URL = "img_url";
        static final String DIST = "dist";
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
            // How to get longitude from MongoDG Document
            // object.get("location").get("coordinates").get(0)
            poi.latitude = document.getDouble(Field.LAT);
            poi.longitude = document.getDouble(Field.LONG);
            poi.img_url = document.getString(Field.IMG_URL);

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

    public Double getLatitude() { return latitude; }

    public Double getLongitude() { return longitude; }

    public String getDescription() {
        return description;
    }

    public ArrayList<String> getTags() { return tags; }

    public double getDistance() { return distance; }
}
