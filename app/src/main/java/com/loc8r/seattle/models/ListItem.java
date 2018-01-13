package com.loc8r.seattle.models;

/**
 * A POJO Class for the main List items
 */
public class ListItem {
    String title;
    int imageId;

    public ListItem(){}

    public ListItem(String title, int imageURL) {
        this.title = title;
        this.imageId = imageURL;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getImageURL() {
        return imageId;
    }

    public void setImageURL(int imageId) {
        this.imageId = imageId;
    }
}
