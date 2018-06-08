package com.loc8r.seattle.utils;

import android.location.Location;
import com.loc8r.seattle.models.Collection;
import com.loc8r.seattle.models.POI;
import com.loc8r.seattle.models.Stamp;
import com.loc8r.seattle.models.User;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class StateManager {
    private static final StateManager ourInstance = new StateManager();
    private User mUser;
    // private Location currentLocation; // The users current location
   // private String userId;
    private ArrayList<POI> mPOIs;
    private HashMap<String, Collection> mCollections;
    private ArrayList<Stamp> mStamps;
    private Date mDate;
    private Boolean gettingPOIs;
    private Boolean gettingStamps;
    private Boolean gettingCollections;

    public static StateManager getInstance() {
        return ourInstance;
    }

    private StateManager() {
        mUser = new User();
        mPOIs = new ArrayList<>();
        mStamps = new ArrayList<>();
        mCollections = new HashMap<>();
        gettingPOIs = false;
        gettingStamps = false;
        gettingCollections = false;
    }

    // Getters
    public boolean userIsAdmin() {
        return mUser.isAdmin();
    }
    public Location getCurrentLocation() {
        return mUser.getCurrentLocation(); }
    public ArrayList<POI> getPOIs() { return mPOIs;}
    public HashMap<String, Collection> getCollections() {
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
    public Boolean isGettingCollections() { return gettingCollections; }
    public Date getDate(){return mDate;}

    // Setters
    public void setUser(String userId) { mUser.setUserId(userId); }
    public void setUserIsAdmin(boolean bool) { mUser.setisAdmin(bool); }
    public void setCurrentLocation(Location currentLocation) { mUser.setCurrentLocation(currentLocation); }
    public void setPOIs(ArrayList<POI> pois){ this.mPOIs = pois; }
    public void setCollections(HashMap<String, Collection> mCollections) { this.mCollections = mCollections; }
    public void setStamps(ArrayList<Stamp> mStamps) {
        this.mStamps = mStamps;
    }
    public void setGettingPOIs(Boolean gettingPOIs) {
        this.gettingPOIs = gettingPOIs;
    }
    public void setGettingStamps(Boolean gettingStamps) {
        this.gettingStamps = gettingStamps;
    }
    public void setGettingCollections(Boolean gettingCollections ) { this.gettingCollections = gettingCollections; }
    public void setDate(Date date){ this.mDate = date;}

    /**
     *  Method clears all State Manager info, including user.
     */
    public void resetAll(){
        mPOIs.clear();
        mStamps.clear();
        gettingPOIs = false;
        gettingStamps = false;
        gettingCollections = false;
        mUser = new User();
    }
}
