package com.loc8r.seattle.models;

import android.location.Location;
import android.support.annotation.Keep;

import com.google.firebase.firestore.Exclude;

import java.util.ArrayList;

/**
 * Created by steve on 1/30/2018.
 */
@Keep
public class User {
    String UserId;
    // ArrayList<Stamp> stamps;
    private boolean isAdmin;
    private Location currentLocation; // The users current location

    public User(){}

    // Getters
    public boolean isAdmin(){ return isAdmin; }
    // public ArrayList<Stamp> getStamps() { return stamps; }
    @Exclude public Location getCurrentLocation() {
        return currentLocation;
    }
    @Exclude public String getUserId() { return UserId; }

    //Setters
    public void setisAdmin(boolean admin){ isAdmin = admin; }
    @Exclude public void setUserId(String userId) { UserId = userId; }
    // public void setStamps(ArrayList<Stamp> stamps) { this.stamps = stamps; }
    @Exclude public void setCurrentLocation(Location currentLocation) { this.currentLocation = currentLocation; }

}
