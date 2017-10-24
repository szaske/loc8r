package com.loc8r.seattle.models;

import android.location.Location;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.parceler.Parcel;

import java.util.ArrayList;
import java.util.Date;

import static android.R.attr.name;

/**
 * A POJO Class for a Stamp
 */

@Parcel
public class Stamp {
    ObjectId id;
    String category;
    Date date;
    String ownerId;
    String poiId;
    String stampId;
    String stampText;

    public Stamp(){}

    public  Stamp(String stampId,
                  String category,
                  Date date,
                  String ownerId,
                  String poiId,
                  String stampText){
        this.category = category;
        this.date = date;
        this.ownerId = ownerId;
        this.poiId = poiId;
        this.stampId = stampId;
        this.stampText = stampText;
    }

    /**
     * Helper class to keep all the field names in one place
     **/
    public class Field
    {
        public static final String ID = "_id";
        public static final String CAT = "category";
        public static final String DATE = "date";
        public static final String OWNER_ID = "ownerId";
        public static final String POI_ID = "poiId";
        public static final String STAMP_TEXT = "stampText";
        public static final String STAMP_ID = "stampId";
    }

    /**
     * Parse the POI object from the MongoDB document
     **/
    public static Stamp fromDocument(Document document)
    {
        Stamp stamp = new Stamp();
        try
        {
            stamp.id = document.getObjectId(Field.ID);
            stamp.category = document.getString(Field.CAT);
            stamp.date = document.getDate(Field.DATE);
            stamp.poiId = document.getString(Field.POI_ID);
            stamp.stampId = document.getString(Field.STAMP_ID);
            stamp.stampText = document.getString(Field.STAMP_TEXT);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return stamp;
    }

    // Getters
    public ObjectId getId() { return id; }
    public String getCategory() { return category; }
    public Date getDate() { return date; }
    public String getPoiID() { return poiId; }
    public String getStampId() { return stampId; }
    public String getStampText() { return stampText; }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public void setPoiId(String poiId) {
        this.poiId = poiId;
    }

    public void setStampId(String stampId) {
        this.stampId = stampId;
    }

    public void setStampText(String stampText) {
        this.stampText = stampText;
    }
}
