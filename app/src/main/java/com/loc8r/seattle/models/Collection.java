package com.loc8r.seattle.models;

import android.support.annotation.Keep;

import com.google.firebase.firestore.Exclude;

import org.parceler.Parcel;

/**
 * A POJO Class for a Collection Item
 */

@Keep
@Parcel
public class Collection {
    //String id;
    String name; // Limited to 19 characters
    String date;
    String color;
    String textColor;
    String description;

    public Collection(){}

//    public Collection(String name,
//                      String color){
//        this.name = name;
//        this.color = color;
//        // this.textColor = "#ffffff"; // default to white text
//    }

    // Getters
    // @Exclude public String getId() { return id; }
    public String getName() { return name; }
    public String getDate() { return date; }
    public String getColor() { return (color!=null) ? "#" + color : "#7f7f7f"; } // Handles null so not code tests are needed
    public String getTextColor() { return (textColor!=null) ? "#" + textColor : "#DDDDDD"; }
    public String getDescription() { return description; }

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
    public void setTextColor(String textColor) { this.textColor = textColor; }
    public void setDescription(String desc) { this.description = desc; }
}
