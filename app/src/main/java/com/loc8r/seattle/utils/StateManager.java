package com.loc8r.seattle.utils;

import android.location.Location;
import com.loc8r.seattle.models.Collection;
import com.loc8r.seattle.models.POI;
import com.loc8r.seattle.models.Stamp;

import java.util.ArrayList;

public class StateManager {
    private static final StateManager ourInstance = new StateManager();
    private Location currentLocation; // The users current location
    private String user;
    private ArrayList<POI> mPOIs;
    private ArrayList<Collection> mCollections;
    private ArrayList<Stamp> mStamps;
    private Boolean gettingPOIs;
    private Boolean gettingStamps;

    public static StateManager getInstance() {
        return ourInstance;
    }

    private StateManager() {
        mPOIs = new ArrayList<>();
        mStamps = new ArrayList<>();
        // mCollections = new ArrayList<>();
        gettingPOIs = false;
        gettingStamps = false;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public Location getCurrentLocation() {
        return currentLocation;
    }

    public void setCurrentLocation(Location currentLocation) {
        this.currentLocation = currentLocation;
    }

    public ArrayList<POI> getPOIs() { return mPOIs;}
    public void setPOIs(ArrayList<POI> pois){ this.mPOIs = pois; }

    public ArrayList<Collection> getCollections() {
        return mCollections;
    }

    public void setCollections(ArrayList<Collection> mCollections) {
        this.mCollections = mCollections;
    }

    public ArrayList<Stamp> getStamps() {
        return mStamps;
    }

    public void setStamps(ArrayList<Stamp> mStamps) {
        this.mStamps = mStamps;
    }

    public Boolean isGettingPOIs() {
        return gettingPOIs;
    }

    public void setGettingPOIs(Boolean gettingPOIs) {
        this.gettingPOIs = gettingPOIs;
    }

    public Boolean isGettingStamps() {
        return gettingStamps;
    }

    public void setGettingStamps(Boolean gettingStamps) {
        this.gettingStamps = gettingStamps;
    }

    public void resetAll(){
        mPOIs.clear();
        mStamps.clear();
        gettingPOIs = false;
        gettingStamps = false;
        currentLocation = null;
    }
}
