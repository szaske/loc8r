package com.loc8r.seattle.utils;

import android.location.Location;

public class StateManager {
    private static final StateManager ourInstance = new StateManager();

    private Location currentLocation; // The users current location

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

}
