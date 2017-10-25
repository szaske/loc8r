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
//import com.mongodb.platespace.model.Attributes;
//import com.mongodb.platespace.model.Poi;
//import com.mongodb.platespace.model.Review;
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
import com.loc8r.seattle.models.Stamp;

import org.bson.BsonElement;
import org.bson.BsonString;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.R.attr.category;
import static android.R.attr.name;
import static android.R.id.list;


/**
 * Helper class to control all communication with MongoDB in one place.
 * For this sample app we chose to use it as a singleton class
 */

public class MongoDBManager {
    private static final String TAG = MongoDBManager.class.getSimpleName();

    private static MongoDBManager ourInstance;

    private StitchClient mStitchClient;
    private MongoClient mMongoDBClient;

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
        /*
        Log in with Facebook.

        1. get authentication providers to know if Facebook authentication is enabled by the service.
        2. if Facebook authentication is enabled, try to login
        * */
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


    // Method for getting nearby POIs

    /**
     * @param keyword A searched for limit of the POIs
     * @param latitude The current Latitude of the device
     * @param longitude The current longitude of the device
     * @param farthestPOI
     * @param limit A limit size to the number of results we return
     * @param listener The callback listener
     */
    @SuppressWarnings("unchecked")
    public void geoNear(@Nullable String keyword,
                        double latitude,
                        double longitude,
                        @Nullable POI farthestPOI,
                        int limit,
                        final QueryListener<List<POI>> listener)
    {

        /**
        * Get a list of poi, sorted by descending distance from a current location.
        *
        * The list has a limit, attributed, and possibly a keyword
        **/

        Document query = new Document();

        if (keyword != null)
        {
            /*
            User searches for similar poi names.
            * We implement this search using the $regex operator, with case insensitivity to match upper and lower cases.
            *
            *
            * https://docs.mongodb.com/manual/reference/operator/query/regex/
            * */
            query.put(POI.Field.NAME, new Document("$regex", keyword).append("$options", "i"));
        }

        if (farthestPOI != null)
        {
            /*
            * We already have the farthest poi, so we don't wanna get it the next iteration.
            * Therefor, get the pois which id != farthestPOI
            * */

            query.put(POI.Field.ID, new Document("$ne", farthestPOI.getId()));
        }

        List<Document> items = new ArrayList<>();
        items.add(new Document("result", "%%vars.geo_matches")); //bind the arguments to our named pipeline required parameters


        /*
        The pagination is implemented in the following way:

        * 1. to get the first page, we sort the distances with a minimum distance of 0.
        * This will return the closest poi first, and the farthest last (according to our geoNear named pipeline).
        *
        * 2. Once we finished step 1, we keep the distance of the farthest poi (i.e 1000 meters).
        * For the next page will will set the min distance to the farthest distance, so that the results received will only have
        * a distance >= farthest poi (I.e, the second stage will return pois with a minimum distance of 1000 meters).
        *
        * Note: make sure to exclude the farthest poi so we don't have duplicates (once in stage 1, and second time in stage 2)
        *
        * 3. Keep iterating over step 2 until the result list size is smaller than the limit given.
        * That means there is no more data.
        *
        * */

        double minDistance = farthestPOI == null ? 0 : farthestPOI.getDistance();


        Document argsMap = new Document()
                .append("latitude", latitude) //the current phone latitude
                .append("longitude", longitude) //the current phone longitude
                .append("query", query) //query will contain any additional parameters
                .append("minDistance", minDistance) //pagination parameter
                .append("limit", limit); //pagination limit

        Document pipelineMap = new Document()
                .append("name", "geoNear") //our named pipeline in the service
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
                    Log.d(TAG, "then isSuccessful: ");

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


//    @SuppressWarnings("unchecked")
//    public void updateRatings(final POI poi, final QueryListener<Void> listener)
//    {
//
//        /*
//        This named pipeline defined in the service goes over every
//        rating that is attached to a certain poi, collects the average rating and updates it
//        in the specific poi
//        */
//
//        List<Document> items = new ArrayList<>();
//        items.add(new Document("result", "%%vars.updateRatings"));
//
//
//        Document pipelineMap = new Document()
//                .append("name", "updateRatings") //our named pipeline
//                .append("args", new Document("poiId", poi.getId())); //the arguments for our named pipeline
//
//
//        PipelineStage literalStage = new PipelineStage("literal",
//                new Document("items", items),
//                new Document("updateRatings", new Document("%pipeline", pipelineMap)));
//
//        mStitchClient.executePipeline(literalStage).continueWith(new Continuation<List<Object>, Object>()
//        {
//            @Override
//            public Object then(@NonNull Task<List<Object>> task) throws Exception
//            {
//                if (task.isSuccessful())
//                {
//                    /*
//                    * In our named pipeline, we return 'false' or 'true' to know if the query was a success
//                    * */
//
//                    Log.d(TAG, "then: isSuccessful");
//                    Map<String, Object> result = (Map<String, Object>) task.getResult().get(0);
//                    boolean boolResult = (boolean) result.get("result");
//
//                    if (listener != null)
//                    {
//                        if (boolResult)
//                        {
//                            listener.onSuccess(null);
//                        }
//                        else
//                        {
//                            listener.onError(new Exception("Update failed"));
//                        }
//                    }
//                }
//                else
//                {
//                    Log.e(TAG, "then: ", task.getException());
//                    if (listener != null)
//                    {
//                        listener.onError(task.getException());
//                    }
//                }
//                return null;
//            }
//        });
//    }

    public void getCatsPipe(final QueryListener<List<String>> listener) {
        /*
        * Get the reviews of a poi, except for the review of the user that is logged in.
        * */

        //no
        List<Document> pipeline = new ArrayList<>();
        pipeline.add(new Document("$group", new Document("_id","$category")));


        Document args = new Document();
        args.put("database", Statics.DB_NAME);
        args.put("collection", DBCollections.POIS);
        args.put("pipeline", pipeline);

        Log.i(TAG, "getCatsPipe: " + pipeline.toString());

        Log.i(TAG, "getCatsArgs: " + args.toString());

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

    public void getCategories(final QueryListener<ArrayList<String>> listener)
    {
        Document query = new Document()
                .append("distinct", "pois")
                .append("key", "category");


        getDatabase().getCollection(DBCollections.POIS).find(query).continueWith(new Continuation<List<Document>, Object>()
        {
            @Override
            public Object then(@NonNull final Task<List<Document>> task) throws Exception
            {
                // convert to array
                ArrayList cats = new ArrayList();
                cats.add("tested");

                if (!task.isSuccessful())
                {
                    Log.e(TAG, "Failed to execute category list query");
                }
                else
                {
                    List<Document> result = task.getResult();
                    if (result == null || result.isEmpty())
                    {
                        Log.d(TAG, "No results found");
                    }
                    else
                    {
                        Log.i(TAG, "then: we got catagories");
                    }

                    if (listener != null)
                    {
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

} // end of Manager class
