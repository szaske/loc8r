package com.loc8r.seattle.models;

import android.location.Location;

import java.util.ArrayList;

/**
 * Created by steve on 1/30/2018.
 */

public class User {
    String UserId;
    ArrayList<Stamp> stamps;
    private boolean role_editor;
    private boolean role_admin;
    private Location currentLocation; // The users current location

    // Getters
    boolean isEditor(){ return role_editor; }
    boolean isAdmin(){ return role_admin; }
    public ArrayList<Stamp> getStamps() { return stamps; }
    public Location getCurrentLocation() {
        return currentLocation;
    }
    public String getUserId() { return UserId; }

    //Setters
    void setEditor(boolean option){ role_editor = option; }
    void setAdmin(boolean option){ role_admin = option; }
    public void setUserId(String userId) { UserId = userId; }
    public void setStamps(ArrayList<Stamp> stamps) {
        this.stamps = stamps;
    }
    public void setCurrentLocation(Location currentLocation) { this.currentLocation = currentLocation; }

}
