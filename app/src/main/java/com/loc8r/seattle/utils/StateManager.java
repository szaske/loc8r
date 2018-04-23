package com.loc8r.seattle.utils;

import android.location.Location;
import com.loc8r.seattle.models.Collection;
import com.loc8r.seattle.models.POI;
import com.loc8r.seattle.models.Stamp;
import com.loc8r.seattle.models.User;

import java.util.ArrayList;

public class StateManager {
    private static final StateManager ourInstance = new StateManager();
    private User mUser;
    // private Location currentLocation; // The users current location
   // private String userId;
    private ArrayList<POI> mPOIs;
    private ArrayList<Collection> mCollections;
    private ArrayList<Stamp> mStamps;
    private Boolean gettingPOIs;
    private Boolean gettingStamps;

    public static StateManager getInstance() {
        return ourInstance;
    }

    private StateManager() {
        mUser = new User();
        mPOIs = new ArrayList<>();
        mStamps = new ArrayList<>();
        mCollections = new ArrayList<>();
        gettingPOIs = false;
        gettingStamps = false;
    }

    // Getters
    public String getUser() {
        return mUser.getUserId();
    }
    public Location getCurrentLocation() { return mUser.getCurrentLocation(); }
    public ArrayList<POI> getPOIs() { return mPOIs;}
    public ArrayList<Collection> getCollections() {
        return mCollections;
    }
    public ArrayList<Stamp> getStamps() {
        return mStamps;
    }
    public Boolean isGettingPOIs() {
        return gettingPOIs;
    }
    public Boolean isGettingStamps() {
        return gettingStamps;
    }

    // Setters
    public void setUser(String userId) { mUser.setUserId(userId); }
    public void setCurrentLocation(Location currentLocation) { mUser.setCurrentLocation(currentLocation); }
    public void setPOIs(ArrayList<POI> pois){ this.mPOIs = pois; }
    public void setCollections(ArrayList<Collection> mCollections) { this.mCollections = mCollections; }
    public void setStamps(ArrayList<Stamp> mStamps) {
        this.mStamps = mStamps;
    }
    public void setGettingPOIs(Boolean gettingPOIs) {
        this.gettingPOIs = gettingPOIs;
    }
    public void setGettingStamps(Boolean gettingStamps) {
        this.gettingStamps = gettingStamps;
    }

    /**
     *  Method clears all State Manager info, including user.
     */
    public void resetAll(){
        mPOIs.clear();
        mStamps.clear();
        gettingPOIs = false;
        gettingStamps = false;
        mUser = new User();
    }
}
