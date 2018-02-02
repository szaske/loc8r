package com.loc8r.seattle.utils;

import android.content.Context;
import android.location.Location;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.loc8r.seattle.models.POI;

import java.util.ArrayList;

/**
 * Created by steve on 1/30/2018.
 */

public class FirebaseManager {

    private static final String TAG = FirebaseManager.class.getSimpleName();
    FirebaseFirestore db;
    FirebaseUser user;
    public ArrayList<POI> listOfPOIs;
    // Static singleton instance
    public static FirebaseManager ourInstance;

    public FirebaseManager(Context context) {
        user = FirebaseAuth.getInstance().getCurrentUser();
        db = FirebaseFirestore.getInstance();

        //Gets the full list of POIs on creation
        CreateDummyPOIList();
    }

    // Method to insure FirebaseManager is a singleton
    public synchronized static FirebaseManager getInstance(Context context) {
        if (ourInstance == null) {
            ourInstance = new FirebaseManager(context);
        }
        return ourInstance;
    }


    public void getAllStamps() {
        // [START get_multiple_all]
        db.collection("users")
                .document(user.getUid())
                .collection("stamps")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (DocumentSnapshot document : task.getResult()) {
                                Log.d(TAG, document.getId() + " => " + document.getData());
                            }
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });
        // [END get_multiple_all]
    }


    public void CreateDummyPOIList(){
        ArrayList<POI> tempPOIList = new ArrayList<POI>();

        tempPOIList.add(new POI("1","Dick's",CreateLocation(47.661116, -122.327877),"Since 1954, original of a chain dishing out burgers, fries & hand-dipped shakes (open late).", "https://www.zaske.com/loc8r/seattle_medal.jpg", "street art", "StampIS1","Stamp Text 1"));
        tempPOIList.add(new POI("2","Sea Monster Lounge",CreateLocation(47.661542, -122.332299),"We feature the very best in Seattle live funk, soul and jazz music, blues/electronic, etc. Tuesday through Sunday.  and we have a covered outdoor patio! it's also a famous spot for musicians getting off gigs elsewhere and wanting to drop in late for a jam session (and some liquor) and you will know why the moment you walk in. Charlie Hunter, Macklemore, Alan Stone, Roy Hargrove, Blake Lewis, Kevin Sawka, etc... 10 NW beers on tap, house liquor infusions, snacks, games, fun!", "https://www.zaske.com/loc8r/seattle_needle.jpg", "street art", "StampIS2","Stamp Text 2"));
        tempPOIList.add(new POI("3","Library",CreateLocation(47.661173, -122.338994),"The Wilmot Memorial Library opened Sept. 10, 1949, in a house donated by Alice Wilmot Dennis in memory of her sister, Florence Wilmot Metcalf. The branch moved to remodeled quarters in a former police and fire station in 1985 and was renamed the Wallingford-Wilmot Library.\n" +
                "\n" +
                "The branch shared its new space with the 45th Street Clinic, which eventually expanded and asked the library to relocate.", "https://www.zaske.com/loc8r/seattle_library.jpg", "street art", "StampIS3","Stamp Text 3"));
        tempPOIList.add(new POI("4","Portage Bay Cafe",CreateLocation(47.657846, -122.317634),"A tasty place to go for an all-American, local/organic/sustainable-focused breakfast or brunch, including cage-free eggs from Olympia, hams and sausages made in-house with Carlton Farms pork, all-organic and local-when-possible veggies, and so forth. Reservations are highly recommended on the weekends.", "https://www.zaske.com/loc8r/seattle_skyline1.jpg", "street art", "StampIS4","Stamp Text 4"));
        tempPOIList.add(new POI("5","University Barbershop",CreateLocation(47.658945, -122.313323),"A great neighborhood barbershop, serving 'The Ave' for over 75 years", "https://www.zaske.com/loc8r/seattle_market.jpg", "street art", "StampIS5","Stamp Text 5"));
        tempPOIList.add(new POI("6","Burke Museum",CreateLocation(47.660704, -122.310510),"The Burke Museum of Natural History and Culture (Burke Museum) is a natural history museum in Seattle, Washington, in the United States. Established in 1899 as the Washington State Museum, it traces its origins to a high school naturalist club formed in 1879. The museum is the oldest natural history museum west of the Mississippi River and boasts a collection of more than 16 million artifacts, including the world's largest collection of spread bird wings. Located on the campus of the University of Washington, the Burke Museum is the official state museum of Washington.", "https://www.zaske.com/loc8r/seattle_library.jpg", "street art", "StampIS6","Stamp Text 6"));

        // Store our POIs in the public variable
        listOfPOIs = tempPOIList;
    }

    // Create a location given a Lat & Long
    public Location CreateLocation(Double latitude, Double longitude){
        Location tempLocation = new Location("");
        tempLocation.setLongitude(longitude);
        tempLocation.setLatitude(latitude);
        return tempLocation;
    }

}
