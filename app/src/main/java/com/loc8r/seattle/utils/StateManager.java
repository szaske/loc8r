package com.loc8r.seattle.utils;

import android.location.Location;

import com.google.firebase.auth.FirebaseUser;
import com.loc8r.seattle.models.Stamp;

import java.util.ArrayList;

public class StateManager {
    private static final StateManager ourInstance = new StateManager();
    private Location currentLocation; // The users current location
    private ArrayList<Stamp> stamps;

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

    public ArrayList<Stamp> getStamps() {
        return stamps;
    }

    public void setStamps(ArrayList<Stamp> stamps) {
        this.stamps = stamps;
    }
}
