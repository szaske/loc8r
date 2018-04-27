package com.loc8r.seattle.models;

import com.google.firebase.firestore.Exclude;

import org.parceler.Parcel;

/**
 * A POJO Class for a Collection Item
 */

@Parcel
public class Collection {
    //String id;
    String name; // Limited to 19 characters
    String date;
    String color;

    public Collection(){}

    public Collection(String name,
                      String color){
        this.name = name;
        this.color = color;
    }

    // Getters
    // @Exclude public String getId() { return id; }
    public String getName() { return name; }
    public String getDate() { return date; }
    public String getColor() { return color; }
    @Exclude public String getId(){
        String results = name + date;
        return results.replaceAll("[^a-zA-Z0-9]+","")
                .toLowerCase();
    }

    // Setters
//    public void setId(String id) {
//        this.id = id;
//    }
    public void setName(String name) {
        this.name = name;
    }
    public void setDate(String date) {
        this.date = date;
    }
    public void setColor(String color) { this.color = color; }
}
