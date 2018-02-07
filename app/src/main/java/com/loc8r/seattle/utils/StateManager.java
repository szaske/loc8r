package com.loc8r.seattle.utils;

import android.location.Location;

import com.google.firebase.auth.FirebaseUser;
import com.loc8r.seattle.models.POI;
import com.loc8r.seattle.models.Stamp;

import java.util.ArrayList;
import java.util.HashMap;

public class StateManager {
    private static final StateManager ourInstance = new StateManager();
    private Location currentLocation; // The users current location
    private HashMap<String,POI> mPOIs;
    private boolean mPOIsHaveBeenStamped;

    public static StateManager getInstance() {
        return ourInstance;
    }

    private StateManager() {
        mPOIs = new HashMap<>();
    }

    public Location getCurrentLocation() {
        return currentLocation;
    }

    public void setCurrentLocation(Location currentLocation) {
        this.currentLocation = currentLocation;
    }

//    public ArrayList<Stamp> getStamps() { return mStamps; }
//    public void setStamps(ArrayList<Stamp> stamps) {
//        this.mStamps = stamps;
//    }

    public HashMap<String,POI> getPOIs() { return mPOIs;}
    public void setPOIs(HashMap<String,POI> pois){ this.mPOIs = pois; }

    public void setPOIsHaveBeenStamped(boolean stampStatus) {
        this.mPOIsHaveBeenStamped = stampStatus;
    }

    private boolean POIsStamped(){
        return mPOIsHaveBeenStamped;
    }
}
