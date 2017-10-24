package com.loc8r.seattle.models;

import android.location.Location;

import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.Date;

/**
 * A POJO Class for a Stamp
 */

public class Stamp {
    ObjectId id;
    String name;
    String category;
    Date date;
    String ownerId;
    String poiId;
    String stampText;

    public Stamp(){}

    public  Stamp(String name,
                  String category,
                  Date date,
                  String ownerId,
                  String poiId,
                  String stampText){
        this.name = name;
        this.category = category;
        this.date = date;
        this.ownerId = ownerId;
        this.poiId = poiId;
        this.stampText = stampText;
    }

    /**
     * Helper class to keep all the field names in one place
     **/
    public class Field
    {
        public static final String ID = "_id";
        public static final String NAME = "name";
        static final String CAT = "category";
        static final String DATE = "date";
        static final String OWNER_ID = "ownerId";
        static final String POI_ID = "poiId";
        static final String STAMP_TEXT = "stampText";
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
            stamp.name = document.getString(Field.NAME);
            stamp.category = document.getString(Field.CAT);
            stamp.date = document.getDate(Field.DATE);
            stamp.poiId = document.getString(Field.POI_ID);
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
    public String getName() { return name; }
    public String getCategory() { return category; }
    public Date getDate() { return date; }
    public String getPoiID() { return poiId; }
    public String getStampText() { return stampText; }
}
