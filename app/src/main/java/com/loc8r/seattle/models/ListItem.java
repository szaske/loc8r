package com.loc8r.seattle.models;

/**
 * A POJO Class for the main List items
 */
public class ListItem {
    String title;
    String imageURL;

    public ListItem(){}

    public ListItem(String title, String imageURL) {
        this.title = title;
        this.imageURL = imageURL;
    }
}
