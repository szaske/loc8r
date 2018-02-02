package com.loc8r.seattle.models;

import org.parceler.Parcel;

/**
 * A POJO Class for a Collection Item
 */

@Parcel
public class Collection {
    int id;
    String name;

    public Collection(){}

    public Collection(int id,
                      String name){
        this.id = id;
        this.name = name;
    }

    // Getters
    public int getId() { return id; }
    public String getName() { return name; }

    // Setters
    public void setId(int id) {
        this.id = id;
    }
    public void setName(String name) {
        this.name = name;
    }

}
