package com.loc8r.seattle.utils;

import android.location.Location;

import com.google.firebase.auth.FirebaseUser;
import com.loc8r.seattle.models.POI;
import com.loc8r.seattle.models.Stamp;

import java.util.ArrayList;

public class StateManager {
    private static final StateManager ourInstance = new StateManager();
    private Location currentLocation; // The users current location
    private ArrayList<Stamp> mStamps;
    private ArrayList<POI> mPOIs;
    public static StateManager getInstance() {
        return ourInstance;
    }

    private StateManager() {
    }

    public Location getCurrentLocation() {
        return currentLocation;
    }

    public void setCurrentLocation(Location currentLocation) {
        this.currentLocation = currentLocation;
    }

    public ArrayList<Stamp> getStamps() { return mStamps; }
    public void setStamps(ArrayList<Stamp> stamps) {
        this.mStamps = stamps;
    }

    public ArrayList<POI> getPOIs() { return mPOIs;}
    public void setPOIs(ArrayList<POI> pois){ this.mPOIs = pois; }
}
