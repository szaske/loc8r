package com.loc8r.seattle.models;

import org.parceler.Parcel;

/**
 * A POJO Class for a Stamp
 */

@Parcel
public class Stamp {
    String collection;
    String timestamp;
    String userId;
    String poiId;
    String stampText;

    public Stamp(){}

    public  Stamp(String poiId,
                  String collection,
                  String timestamp,
                  String stampText){
        this.collection = collection;
        this.timestamp = timestamp;
        this.poiId = poiId;
        this.stampText = stampText;
    }

    @Override
    public String toString() {
        return "Stamp{" +
                "id=" + poiId +
                ",\n timestamp='" + timestamp +
                '}';
    }

    // Getters
    public String getCollection() { return collection; }
    public String getTimestamp() { return timestamp; }
    public String getPoiId() { return poiId; }
    public String getStampText() { return stampText; }

    // Setters
    public void setCollection(String collection) {
        this.collection = collection;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public void setPoiId(String pid) {
        this.poiId = pid;
    }

    public void setStampText(String stampText) {
        this.stampText = stampText;
    }

}
