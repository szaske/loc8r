package com.loc8r.seattle.models;

import org.parceler.Parcel;

/**
 * A POJO Class for a Collection Item
 */

@Parcel
public class Collection {
    int id;
    String name; // Limited to 19 characters

    public Collection(){}

    public Collection(int id,
                      String name){
        this.id = id;
        this.name = name;
    }

    // Getters
    public int getId() { return id; }
    public String getName() { return name; }
    public  String getIconName(){
        return "icon_" + name.substring(0,3).toLowerCase();
    }

    // Setters
    public void setId(int id) {
        this.id = id;
    }
    public void setName(String name) {
        this.name = name;
    }

}
