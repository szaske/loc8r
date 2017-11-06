package com.loc8r.seattle.mongodb;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.facebook.login.LoginManager;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import com.loc8r.seattle.models.Stamp;
import com.mongodb.stitch.android.PipelineStage;
import com.mongodb.stitch.android.StitchClient;
import com.mongodb.stitch.android.auth.Auth;
import com.mongodb.stitch.android.auth.AvailableAuthProviders;
import com.mongodb.stitch.android.auth.anonymous.AnonymousAuthProvider;
import com.mongodb.stitch.android.auth.anonymous.AnonymousAuthProviderInfo;
import com.mongodb.stitch.android.auth.oauth2.facebook.FacebookAuthProvider;
import com.mongodb.stitch.android.services.mongodb.MongoClient;

import com.loc8r.seattle.interfaces.QueryListener;
import com.loc8r.seattle.models.POI;

import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;


/**
 * Helper class to control all communication with MongoDB in one place.
 * For this sample app we chose to use it as a singleton class
 */

public class MongoDBManager {
    private static final String TAG = MongoDBManager.class.getSimpleName();

    private static MongoDBManager ourInstance;
    private StitchClient mStitchClient;
    private MongoClient mMongoDBClient;
    public  ArrayList<POI> allPOIs = new ArrayList<>();
    public  ArrayList<Stamp> Stamps = new ArrayList<>();

    public String mUserName;


    /*
    * Helper class to keep all the statics
    * */
    private class Statics {

        private static final String APP_ID = "seapass-iqdtw";
        private static final String SERVICE_NAME = "mongodb-atlas";
        private static final String DB_NAME = "SeaPassDB";
    }

    /*
     * Helper class to keep the names of the database collections in one place
     */
    private class DBCollections {
        private static final String POIS = "pois";
        private static final String REVIEWS_RATINGS = "reviewsRatings";
        private static final String STAMPS = "stamps";
    }


    public synchronized static MongoDBManager getInstance(Context context) {
        if (ourInstance == null) {
            ourInstance = new MongoDBManager(context);
        }

        return ourInstance;
    }

    private MongoDBManager(Context context) {
        //initialize the Stitch client and the MongoClient

        mStitchClient = new StitchClient(context, Statics.APP_ID);
        mMongoDBClient = new MongoClient(mStitchClient, Statics.SERVICE_NAME);

        //try to get the user name if we are connected to Facebook
        Profile facebookProfile = Profile.getCurrentProfile();
        if (facebookProfile != null) {
            mUserName = facebookProfile.getName();
        }

        //Get all allPOIs

    }

    /*
    Helper method to reduce the boilerplate code
    * */
    private MongoClient.Database getDatabase() {
        return mMongoDBClient.getDatabase(Statics.DB_NAME);
    }

    /**
     * @return the id of the user that is connected to the Stitch client
     */
    public String getUserId() {
        return mStitchClient.getAuth().getUserId();
    }


    /**
     * @return true if user signed in to app as anonymous, false otherwise
     */
    public boolean isAnonymous() {
        Auth auth = mStitchClient.getAuth();
        String provider = auth != null ? auth.getProvider() : null;


        //check if we used the AnonymousAuthProvider when logging in
        return AnonymousAuthProviderInfo.FQ_NAME.equals(provider);
    }

    public boolean isConnected() {
        Log.i(TAG, "isConnected: fired");
        return mStitchClient.isAuthenticated();
    }

    public void doAnonymousAuthentication(final QueryListener<Void> loginListener) {
        /*
        Log in anonymously.

        1. get authentication providers to know if anonymous authentication is enabled by the service.
        2. if anonymous authentication is enabled, try to login
        * */
        mStitchClient.getAuthProviders().addOnCompleteListener(new OnCompleteListener<AvailableAuthProviders>() {
            @Override
            public void onComplete(@NonNull final Task<AvailableAuthProviders> task) {
                if (!task.isSuccessful()) {
                    Log.e(TAG, "Could not retrieve authentication providers");
                    if (loginListener != null) {
                        loginListener.onError(task.getException());
                    }
                } else {
                    Log.i(TAG, "Retrieved authentication providers");
                    mUserName = null;
                    if (task.getResult().hasAnonymous()) {

                        /*
                        * Service enabled anonymous authentication
                        * */

                        //login anonymously
                        mStitchClient.logInWithProvider(new AnonymousAuthProvider()).continueWith(new Continuation<Auth, Object>() {
                            @Override
                            public Object then(@NonNull final Task<Auth> task) throws Exception {
                                if (task.isSuccessful()) {
                                    //we are logged in anonymously

                                    Log.i(TAG, "User Authenticated as " + mStitchClient.getAuth().getUserId());
                                    if (loginListener != null) {
                                        loginListener.onSuccess(null);
                                    }
                                } else {
                                    //failed

                                    String msg = "Error logging in anonymously";
                                    Log.e(TAG, msg, task.getException());
                                    if (loginListener != null) {
                                        loginListener.onError(task.getException());
                                    }
                                }
                                return null;
                            }
                        });
                    } else {
                        //the service doesn't allow anonymous authentication

                        if (loginListener != null) {
                            loginListener.onError(new Exception("Anonymous not supported"));
                        }
                    }
                }
            }
        });
    }

    public void doFacebookAuthentication(final String accessToken, final QueryListener<Void> listener) {

        Log.i(TAG, "doFacebookAuthentication: ");
        /**
        Log in with Facebook.

        1. get authentication providers to know if Facebook authentication is enabled by the service.
        2. if Facebook authentication is enabled, try to login
        **/
        mStitchClient.getAuthProviders().addOnCompleteListener(new OnCompleteListener<AvailableAuthProviders>() {
            @Override
            public void onComplete(@NonNull final Task<AvailableAuthProviders> task) {
                if (!task.isSuccessful()) {
                    Log.e(TAG, "Could not retrieve authentication providers");
                    if (listener != null) {
                        listener.onError(task.getException());
                    }
                } else {
                    Log.i(TAG, "Retrieved authentication providers");

                    if (task.getResult().hasFacebook()) {

                        /*
                        * Facebook authentication is enabled by the service,
                        * try to login using the access token we got from Facebook SDK
                        * */
                        mStitchClient.logInWithProvider(FacebookAuthProvider.fromAccessToken(accessToken)).continueWith(new Continuation<Auth, Object>() {
                            private ProfileTracker mProfileTracker;

                            @Override
                            public Object then(@NonNull final Task<Auth> task) throws Exception {
                                if (task.isSuccessful()) {
                                    //we are logged in with Facebook

                                    Log.i(TAG, "User Authenticated as " + mStitchClient.getAuth().getUserId());

                                    //try to get the user name from Facebook SDK
                                    if (Profile.getCurrentProfile() == null) {
                                        mProfileTracker = new ProfileTracker() {
                                            @Override
                                            protected void onCurrentProfileChanged(Profile oldProfile, Profile currentProfile) {
                                                mUserName = currentProfile.getName();
                                                mProfileTracker.stopTracking();
                                            }
                                        };
                                    } else {
                                        mUserName = Profile.getCurrentProfile().getName();
                                    }


                                    if (listener != null) {
                                        listener.onSuccess(null);
                                    }
                                } else {
                                    String msg = "Error logging with facebook";
                                    Log.e(TAG, msg, task.getException());
                                    if (listener != null) {
                                        listener.onError(task.getException());
                                    }

                                }
                                return null;
                            }
                        });
                    } else {
                        /*
                        * The service does not support Facebook authentication
                        * */
                        listener.onError(new Exception("Facebook not supported"));
                    }
                }
            }
        });
    }


    /*
    * Logout from MongoDB & Facebook SDK
    * */
    public void logout(final QueryListener<Void> listener) {
        Log.i(TAG, "logout: ");
        LoginManager.getInstance().logOut(); //logout from Facebook
        mUserName = null;

        //logout from MongoDB
        mStitchClient.logout().continueWith(new Continuation<Void, Object>() {
            @Override
            public Object then(@NonNull Task<Void> task) throws Exception {
                if (task.isSuccessful()) {
                    Log.d(TAG, "then: logged out");
                    if (listener != null) {
                        listener.onSuccess(null);
                    }
                } else {
                    if (listener != null) {
                        listener.onError(task.getException());
                    }
                }
                return null;
            }
        });
    }

    /**
     *  Method for getting nearby allPOIs
     *
     * @param latitude The current Latitude of the device
     * @param longitude The current longitude of the device
     * @param listener The callback listener
     */
    @SuppressWarnings("unchecked")
    public void geoNear(double latitude,
                        double longitude,
                        final QueryListener<List<POI>> listener)
    {

        /**
        * Get a list of poi, sorted by descending distance from a current location.
        *
        * The list has a limit, attributed, and possibly a keyword
        **/

        Log.i(TAG, "geoNear: method fired, allPois count is" + String.valueOf(allPOIs.size()));

        Document query = new Document();

        List<Document> items = new ArrayList<>();
        items.add(new Document("result", "%%vars.geo_matches")); //bind the arguments to our named pipeline required parameters

        Document argsMap = new Document()
                .append("latitude", latitude) //the current phone latitude
                .append("longitude", longitude); //the current phone longitude
//                .append("query", query) //query will contain any additional parameters
//                .append("minDistance", minDistance) //pagination parameter
//                .append("limit", limit); //pagination limit

        Document pipelineMap = new Document()
                .append("name", "getNearbyPOIs") //our named pipeline in the service
                .append("args", argsMap); //required parameters for the named pipeline

        /*
        * To execute the named pipeline we need to use the pipeline stage
        *
        * TODO Insert link to Mongo Named pipeline documentation
        * */
        PipelineStage literalStage = new PipelineStage("literal", new Document("items", items),
                new Document("geo_matches", new Document("%pipeline", pipelineMap)));

        mStitchClient.executePipeline(literalStage).continueWith(new Continuation<List<Object>, Object>()
        {
            @Override
            public Object then(@NonNull Task<List<Object>> task) throws Exception
            {
                if (task.isSuccessful())
                {
                    Log.d(TAG, " GeoNear promise came back successful ");

                    List<POI> list = new ArrayList<>();
                    Map<String, Object> map = (Map<String, Object>) task.getResult().get(0);


                    //get the list of results from the query
                    List<Object> resultList = (List<Object>) map.get("result");
                    for (Object object : resultList)
                    {
                        //parse document object to a POI model object
                        list.add(POI.fromDocument((Document) object));
                    }

                    if (listener != null)
                    {
                        listener.onSuccess(list);
                    }
                }
                else
                {
                    Log.e(TAG, "then: ", task.getException());
                    if (listener != null)
                    {
                        listener.onError(task.getException());
                    }

                }

                return null;
            }
        });

    }

    public void refreshPOI(final POI poi, final QueryListener<POI> listener) {
        /*
        * Find a specific poi within the DB (using the unique poi id).
        * */

        Document query = new Document(POI.Field.ID, poi.getId());
        getDatabase().getCollection(DBCollections.POIS).find(query).continueWith(new Continuation<List<Document>, Object>() {
            @Override
            public Object then(@NonNull Task<List<Document>> task) throws Exception {
                if (task.isSuccessful()) {
                    List<Document> result = task.getResult();

                    //we know each poi has its own unique id, so we consider an empty list as an error.
                    if (result.isEmpty()) {
                        if (listener != null) {
                            listener.onError(new Exception("Unable to refresh poi"));
                        }
                    } else {
                        POI refreshedPOI = POI.fromDocument(result.get(0));
                        if (listener != null) {
                            listener.onSuccess(refreshedPOI);
                        }
                    }
                } else {
                    if (listener != null) {
                        listener.onError(task.getException());
                    }
                }
                return null;
            }
        });
    }

    public void getPOIs(final QueryListener<List<POI>> listener) {

        Log.i(TAG, "getPOIs: method started ");
        Document args = new Document();
        args.put("database", Statics.DB_NAME);
        args.put("collection", DBCollections.POIS);

        mStitchClient.executePipeline(new PipelineStage("find", Statics.SERVICE_NAME, args)).continueWith(new Continuation<List<Object>, Object>() {
            @Override
            public Object then(@NonNull Task<List<Object>> task) throws Exception {

                if (!task.isSuccessful()) {
                    Log.e(TAG, "Failed to get allPOIs");
                    if (listener != null) {
                        listener.onError(task.getException());
                    }
                } else {
                    Log.i(TAG, "getPOIS method promise returned (then) ");
                    ArrayList<POI> pois = new ArrayList<>();
                    List<Object> result = task.getResult();
                    if (result == null || result.isEmpty()) {
                        Log.d(TAG, "No allPOIs found");
                    } else {
                        for (Object object : result) {
                            //parse the POI model object from the result
                            allPOIs.add(POI.fromDocument((Document) object));
                        }
                    }
                    //on to step #2
                    getStamps(listener);
//                    if (listener != null)
//                    {
//                        listener.onSuccess(allPOIs);
//                    }
                }
                return null;
            }
        });
    }

    // Gets a list of all Stamps from the DB
    // And sets the stamped property on those allPOIs
    // the user has already visited

    public void getStamps(final QueryListener<List<POI>> listener ) {

        Document args = new Document();
        args.put("database", Statics.DB_NAME);
        args.put("collection", DBCollections.STAMPS);

        mStitchClient.executePipeline(new PipelineStage("find", Statics.SERVICE_NAME, args)).continueWith(new Continuation<List<Object>, Object>() {
            @Override
            public Object then(@NonNull Task<List<Object>> task) throws Exception {
                if (!task.isSuccessful()) {
                    Log.e(TAG, "Failed to get Stamps");
                } else {
                    ArrayList<Stamp> stamps = new ArrayList<>();
                    List<Object> result = task.getResult();
                    if (result == null || result.isEmpty()) {
                        Log.d(TAG, "No Stamps found");
                    } else {
                        for (Object object : result) {
                            // Check Stamps against the full list of allPOIs
                            String stampedPoiId = ((Document) object).getString("poiId");
                            StampPOI(stampedPoiId);
                        }
                    }
                    if (listener != null)
                    {
                        listener.onSuccess(allPOIs);
                    }
                }
                return null;
            }
        });
    }

    // This method stamps a
    private void StampPOI(String stampedPoiId){
        for(POI poi: allPOIs){
            Log.d(TAG, "StampPOI: Comparing StampID: " + stampedPoiId + " to local POIiD:" + poi.getId().toString());
            if(poi.getId().toString().equals(stampedPoiId) ){
                poi.setStamped(true);
                Log.i(TAG, "StampPOI: Set POI id " + poi.getId().toString() + "to stamped");
                break;
            }
        }
    }

    public void getCategories(final QueryListener<List<String>> listener) {
        List<Document> pipeline = new ArrayList<>();
        pipeline.add(new Document("$group", new Document("_id","$category")));

        Document args = new Document();
        args.put("database", Statics.DB_NAME);
        args.put("collection", DBCollections.POIS);
        args.put("pipeline", pipeline);

        mStitchClient.executePipeline(new PipelineStage("aggregate", Statics.SERVICE_NAME, args)).continueWith(new Continuation<List<Object>, Object>() {
            @Override
            public Object then(@NonNull Task<List<Object>> task) throws Exception {
                if (!task.isSuccessful()) {
                    Log.e(TAG, "Failed to execute query");
                    if (listener != null) {
                        listener.onError(task.getException());
                    }
                } else {
                    List<String> cats = new ArrayList<>();
                    List<Object> result = task.getResult();
                    if (result == null || result.isEmpty()) {
                        Log.d(TAG, "No results found");
                    } else {
                        for (Object object : result) {
                            //Extract the categories into array
                            Document tempDoc = (Document) object;
                            cats.add(tempDoc.getString("_id"));
                        }
                    }

                    if (listener != null) {
                        listener.onSuccess(cats);
                    }
                }
                return null;
            }
        });
    }

    /**
     *  Add a stamp for a specific poi
     *
     * @param poi
     * @param listener
     */
    public void addStamp(@NonNull final POI poi, final QueryListener<Stamp> listener)
    {

        /**
        *   We init the object id so that we already have it when the query finishes.
        *   If we didn't initialize it here, it would have been initialize automatically
        **/
        final ObjectId id = new ObjectId();
        final Date date = new Date();

        Document query = new Document(Stamp.Field.ID, id)
                .append(Stamp.Field.STAMP_ID, poi.getStampId())
                .append(Stamp.Field.CAT, poi.getCategory())
                .append(Stamp.Field.DATE, date)
                .append(Stamp.Field.OWNER_ID, getUserId())
                .append(Stamp.Field.POI_ID, poi.getId().toString());

        getDatabase().getCollection(DBCollections.STAMPS).insertOne(query).continueWith(new Continuation<Void, Object>()
        {
            @Override
            public Object then(@NonNull Task<Void> task) throws Exception
            {
                if (!task.isSuccessful())
                {
                    Log.e(TAG, "Failed to execute query");
                    if (listener != null)
                    {
                        listener.onError(task.getException());
                    }
                }
                else
                {
                    //construct the newly added stamp
                    if (listener != null)
                    {
                        Stamp stamp = new Stamp();
                        stamp.setId(id);
                        stamp.setDate(date);
                        stamp.setCategory(poi.getCategory());
                        stamp.setOwnerId(getUserId());
                        stamp.setPoiId(poi.getId().toString());
                        listener.onSuccess(stamp);
                    }
                }

                return null;
            }
        });

    }

    public ArrayList<POI> getAllPOIs() {
        return allPOIs;
    }

    @Override
    public String toString(){
        String AllString = "";
        for(POI poi: allPOIs){
            AllString+=poi.toString();
        }
        return AllString;
    }

} // end of Manager class
