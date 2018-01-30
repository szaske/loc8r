package com.loc8r.seattle.models;

import org.parceler.Parcel;

import java.util.Date;

/**
 * A POJO Class for a Stamp
 */

@Parcel
public class Stamp {
    String category;
    String timestamp;
    String ownerId;
    String poiId;
    String stampId;
    String stampText;

    public Stamp(){}

    public  Stamp(String stampId,
                  String category,
                  String timestamp,
                  String poiId,
                  String stampText){
        this.stampId = stampId;
        this.category = category;
        this.timestamp = timestamp;
        this.poiId = poiId;
        this.stampText = stampText;
    }

    /**
     * Helper class to keep all the field names in one place
     **/
//    public class Field
//    {
//        public static final String ID = "_id";
//        public static final String CAT = "category";
//        public static final String DATE = "timestamp";
//        public static final String OWNER_ID = "ownerId";
//        public static final String POI_ID = "poiId";
//        public static final String STAMP_TEXT = "stampText";
//        public static final String STAMP_ID = "stampId";
//    }

    /**
     * Parse the POI object from the MongoDB document
     **/
//    public static Stamp fromDocument(Document document)
//    {
//        Stamp stamp = new Stamp();
//        try
//        {
//            stamp.id = document.getObjectId(Field.ID);
//            stamp.category = document.getString(Field.CAT);
//            stamp.timestamp = document.getTimestamp(Field.DATE);
//            stamp.poiId = document.getString(Field.POI_ID);
//            stamp.stampId = document.getString(Field.STAMP_ID);
//            stamp.stampText = document.getString(Field.STAMP_TEXT);
//        }
//        catch (Exception e)
//        {
//            e.printStackTrace();
//        }
//        return stamp;
//    }

    @Override
    public String toString() {
        return "Stamp{" +
                "id=" + stampId +
                ",\n timestamp='" + timestamp +
                '}';
    }

    // Getters
    public String getCategory() { return category; }
    public String getTimestamp() { return timestamp; }
    public String getPoiId() { return poiId; }
    public String getStampId() { return stampId; }
    public String getStampText() { return stampText; }

    // Setters
    public void setCategory(String category) {
        this.category = category;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public void setPoiId(String pid) {
        this.poiId = pid;
    }

    public void setStampId(String stampId) {
        this.stampId = stampId;
    }

    public void setStampText(String stampText) {
        this.stampText = stampText;
    }

}
