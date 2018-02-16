package com.loc8r.seattle.utils;

import android.location.Location;
import com.loc8r.seattle.models.Collection;
import com.loc8r.seattle.models.POI;

import java.util.ArrayList;

public class StateManager {
    private static final StateManager ourInstance = new StateManager();
    private Location currentLocation; // The users current location
    private ArrayList<POI> mPOIs;
    private ArrayList<Collection> mCollections;

    public static StateManager getInstance() {
        return ourInstance;
    }

    private StateManager() {
        mPOIs = new ArrayList<>();
        mCollections = new ArrayList<>();
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
}
