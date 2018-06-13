package com.loc8r.seattle.models;

import android.support.annotation.Keep;

import org.parceler.Parcel;


/**
 * A POJO Class for a POI (Point Of Interest)
 */
@Keep
@Parcel
public class Suggestion {
    String id;
    String name;
    String location;
    String description;
    String img_url;

    public Suggestion(){}

    public Suggestion(String id){
        this.id = id;}

    public Suggestion(String id,
                      String name,
                      String location,
                      String description,
                      String img_url) {
        this.id = id;
        this.name = name;
        this.location = location;
        this.description = description;
        this.img_url = img_url;
    }

    @Override
    public String toString() {
        return "POI{" +
                "id=" + id +
                ",\n img_url='" + img_url + '\'' +
                ",\n name='" + name + '\'' +
                ",\n description='" + description + '\'' +
                '}';
    }

    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public String getLocation() { return location; }
    public String getDescription() { return description; }
    public String getImg_url() { return img_url; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setName(String name) {
        this.name = name;
    }
    public void setLocation(String location) {
        this.location = location;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public void setImg_url(String img_url) {
        this.img_url = img_url;
    }
}
