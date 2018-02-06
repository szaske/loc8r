package com.loc8r.seattle.activities;

import android.app.Dialog;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.loc8r.seattle.R;
import com.loc8r.seattle.interfaces.LocationListener;
import com.loc8r.seattle.interfaces.QueryListener;
import com.loc8r.seattle.models.POI;
import com.loc8r.seattle.models.Stamp;
// import com.loc8r.seattle.mongodb.MongoDBManager;
import com.loc8r.seattle.models.User;
import com.loc8r.seattle.utils.ProgressDialog;
import com.squareup.picasso.Picasso;

import org.parceler.Parcels;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class POIDetailActivity extends GMS_Activity {

    // Variables
    private static final String TAG = GMS_Activity.class.getSimpleName();
    @BindView(R.id.poiImageView) ImageView mImageIV;
    @BindView(R.id.poiNameTextView) TextView mNameTV;
    @BindView(R.id.poiDescriptionTextView) TextView mDescriptionTV;
    @BindView(R.id.poiLocationTextView) TextView mLocationTV;
    @BindView(R.id.currentLocationtextView) TextView mLocTV;
    @BindView(R.id.getStampBtn) Button mStampBtn;


    // TODO Should I move currentLocation to the State Manager?
    private Location mCurrentLocation;
    private POI detailedPoi;
    private FirebaseFirestore db;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_poi_detail);
        ButterKnife.bind(this);
        detailedPoi = Parcels.unwrap(getIntent().getParcelableExtra("poi"));

        // addOnPreDrawListener is required so we can measure the size of the image on this
        // specific phone.  Without the listener code we might
        ViewTreeObserver vto = mImageIV.getViewTreeObserver();
        vto.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            public boolean onPreDraw() {
                mImageIV.getViewTreeObserver().removeOnPreDrawListener(this);
                // Should add
                // .error(R.drawable.error)
                // .placeholder(R.drawable.blank_img)
                //
                //
                // Also should add a progressbar
                // here is a good example : https://stackoverflow.com/questions/22143157/android-picasso-placeholder-and-error-image-styling
                Picasso.with(getApplicationContext())
                        .load(detailedPoi.getImg_url())
                        .centerCrop()
                        .resize(mImageIV.getMeasuredWidth(),mImageIV.getMeasuredHeight())
                        .into(mImageIV);

                return true;
            }
        });

        Log.d(TAG, "Detail view " + detailedPoi.getName() + "Lat =" + detailedPoi.getLatitude());
//        Picasso
//                .with(this)
//                .load(detailedPoi.getImg_url())
//                .into(mImageIV);
        mNameTV.setText(detailedPoi.getName());
        mDescriptionTV.setText(detailedPoi.getDescription());
        mLocationTV.setText(detailedPoi.getLongitude().toString()+","+detailedPoi.getLatitude().toString());

        // Access a Cloud Firestore instance from your Activity
        db = FirebaseFirestore.getInstance();

        //        //Get the Firebase user
        user = FirebaseAuth.getInstance().getCurrentUser();

    }


    // TODO Determine if I need to cancel location update onPause or onStop.
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d(TAG, "onConnected() called with: " + "bundle = [" + bundle + "]");
                /*
        * get the last location of the device
        * */
        getContinousLocationUpdates(new LocationListener()
        {
            @Override
            public void onLocationReceived(Location location)
            {
                mCurrentLocation = location;
                Log.d(TAG, "UI update initiated .............");
                if (null != mCurrentLocation) {
                    String lat = String.valueOf(mCurrentLocation.getLatitude());
                    String lng = String.valueOf(mCurrentLocation.getLongitude());
                    String dist = String.valueOf(detailedPoi.getDistance());
                    String total = "At Time: " + DateFormat.getTimeInstance().format(new Date()) + "\n" +
                            "Latitude: " + lat + "\n" +
                            "Longitude: " + lng + "\n" +
                            "Accuracy: " + String.valueOf(mCurrentLocation.getAccuracy()) + "\n" +
                            "Provider: " + String.valueOf(mCurrentLocation.getProvider()) + "\n" +
                            "Distance to POI: " + dist;
                    mLocTV.setText(total);
                } else {
                    Log.d(TAG, "location is null ...............");
                }
            }
        });
    }

    private Stamp CreateStamp(){

        //Get the current timestamp
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy-hh-mm-ss");
        String timestamp = simpleDateFormat.format(new Date());

        return new Stamp(
                detailedPoi.getId(),
                detailedPoi.getCollection(),
                timestamp,
                detailedPoi.getStampText());
    }

    private void addStamp()
    {
        final Dialog progressDialog = ProgressDialog.getDialog(this, true);
        progressDialog.show();

        // Create a listener
        QueryListener<Stamp> queryListener = new QueryListener<Stamp>()
        {
            @Override
            public void onSuccess(Stamp result)
            {
                progressDialog.dismiss();
                // Do something after we get the stamp
                cancelContinousLocationUpdates();
                Log.e(TAG, "onSuccess: We got ourselves a STAMP here!");
            }

            @Override
            public void onError(Exception e)
            {
                progressDialog.dismiss();
                Log.e(TAG, "onError: ", e);
                Toast.makeText(getApplicationContext(), R.string.stamp_error, Toast.LENGTH_LONG).show();
            }
        };

        //TODO: Add check so user can only create one STAMP per POI
        if (true)
        {
            // MongoDBManager.getInstance(getApplicationContext()).addStamp(detailedPoi, queryListener);
        }
    }

    @OnClick(R.id.getStampBtn)
    public void onStampButtonClick() {

        //TODO Add a check to determine if we're within a constant amount of meters from the POI.  If close enough then enable the button

        //Create a doc reference to this specific stamp
        DocumentReference stampDocRef = db
                .collection("users")
                .document(user.getUid())
                .collection("stamps")
                .document(detailedPoi.getId());

        //Attempt to get the document/object...if it does not exist then get the stamp
        stampDocRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                //City city = documentSnapshot.toObject(City.class);
                if(documentSnapshot.exists()){
                    Log.d(TAG, "the stamp already EXISTS, aborting save");
                } else {
                    Log.d(TAG, "onSuccess: NO Stamp found, lets make one");
                    AddStampToDB();
                }
            }
        });
    }

    private void AddStampToDB(){
        Log.d(TAG, "onClick: fired");
        String uid = user.getUid();
        User ESUser = new User();
        Stamp newStamp = CreateStamp(); // Create a new stamp

        // Create the user with a stamps collection
        db.collection("users")
                .document(uid)
                .collection("stamps")
                .document(newStamp.getPoiId())
                .set(newStamp)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "Stamp added");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error adding document", e);
                    }
                });

        Toast.makeText(getApplicationContext(), "Saved", Toast.LENGTH_SHORT).show();
    }

}
