package com.loc8r.seattle.activities;

import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.app.Dialog;
import android.graphics.Rect;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.view.animation.PathInterpolatorCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import com.loc8r.seattle.utils.StampDrawable;
import com.loc8r.seattle.utils.StampView;
import com.loc8r.seattle.utils.StateManager;
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
    @BindView(R.id.iv_poiImage) ImageView mIV_PoiImage;
    @BindView(R.id.iv_stamp_placeholder) ImageView mIV_StampPlaceholder;
    @BindView(R.id.tv_poi_name) TextView mTV_PoiName;
    @BindView(R.id.tv_poi_description) TextView mTV_PoiDescription;
    @BindView(R.id.tv_distance) TextView mTV_PoiDistance;
    @BindView(R.id.tv_collection) TextView mTV_PoiCollection;

    @BindView(R.id.stampView) StampView mStampView;
//    @BindView(R.id.getStampBtn) Button mStampBtn;


    // TODO Should I move currentLocation to the State Manager?
    // private Location mCurrentLocation;
    private POI detailedPoi;
    private FirebaseFirestore db;
    private FirebaseUser user;


    // New Toolbar layout items
//    private CollapsingToolbarLayout collapsingToolbar;
//    private AppBarLayout appBarLayout;
    private boolean appBarExpanded = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_poi_detail);
        ButterKnife.bind(this);
        detailedPoi = Parcels.unwrap(getIntent().getParcelableExtra("poi"));


//        setSupportActionBar(mToolbar);
//        if (getSupportActionBar() != null)
//            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//
//        mCollapsingToolbar.setTitle(detailedPoi.getName());

        // addOnPreDrawListener is required so we can measure the size of the image on this
        // specific phone.  Without the listener code we might not fill the
        // imageview with the POI image.
        // https://stackoverflow.com/questions/4680499/how-to-get-the-width-and-height-of-an-android-widget-imageview
        ViewTreeObserver vto = mIV_PoiImage.getViewTreeObserver();
        vto.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            public boolean onPreDraw() {
                mIV_PoiImage.getViewTreeObserver().removeOnPreDrawListener(this);
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
                        .resize(mIV_PoiImage.getMeasuredWidth(), mIV_PoiImage.getMeasuredHeight())
                        .into(mIV_PoiImage);

                return true;
            }
        });


        Log.d(TAG, "Detail view " + detailedPoi.getName());

//        int width = 60;
//        int height = 60;
//        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width,height);
//        mI.setLayoutParams(params)


        mIV_StampPlaceholder.setImageDrawable(new StampDrawable(this, "JOHN STANFORD - BIRDS"));

        mStampView.setStampTitleText("Steve Zaske - Birds");
        mStampView.setStampTimeStampText("DEC 21, 1968");
        mStampView.invalidate();

        mTV_PoiName.setText(detailedPoi.getName());
        mTV_PoiDescription.setText(detailedPoi.getDescription());
        mTV_PoiCollection.setText(detailedPoi.getCollection()+" #"+String.valueOf(detailedPoi.getCollectionPosition()));

        // Access a Cloud Firestore instance from your Activity
        db = FirebaseFirestore.getInstance();

        //        //Get the Firebase user
        user = FirebaseAuth.getInstance().getCurrentUser();

    }


    // TODO Determine if I need to cancel location update onPause or onStop.
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d(TAG, "onConnected() called with: " + "bundle = [" + bundle + "]");
        /**
        * get the last location of the device
        **/
        getContinousLocationUpdates(new LocationListener()
        {
            @Override
            public void onLocationReceived(Location location)
            {
                StateManager.getInstance().setCurrentLocation(location);
                Log.d(TAG, "UI update initiated .............");
                if (null != StateManager.getInstance().getCurrentLocation()) {
                    mTV_PoiDistance.setText(String.valueOf(detailedPoi.distanceToUser())+" meters");
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
    }

    @OnClick(R.id.bt_getStamp)
    public void onStampButtonClick() {

        //Create animation
        /**
         * ValueAnimator to rotate the stamp
         */

        // Create animation value holders
        PropertyValuesHolder RotPropertyHolder = PropertyValuesHolder.ofFloat("Rot", -45,0 );
        PropertyValuesHolder ZoomPropertyHolder = PropertyValuesHolder.ofFloat("Zoom", 30f,4f );
        PropertyValuesHolder XPropertyHolder = PropertyValuesHolder.ofFloat("X", -100f,0f );
        PropertyValuesHolder ElevationPropertyHolder = PropertyValuesHolder.ofFloat("elevation", 30f,4f );
        PropertyValuesHolder TransparencyPropertyHolder = PropertyValuesHolder.ofFloat("Alpha", 0f,1f );


        //Create the animator
        ValueAnimator mTranslationAnimator = ValueAnimator.ofPropertyValuesHolder(RotPropertyHolder,ZoomPropertyHolder,TransparencyPropertyHolder, ElevationPropertyHolder, XPropertyHolder);

        // Create the update listener
        mTranslationAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                // mIV_StampPlaceholder.setTranslationX((float)animation.getAnimatedValue("TranslationX"));
                // mIV_StampPlaceholder.setAlpha((float)animation.getAnimatedValue("Alpha"));
                // mIV_StampPlaceholder.setElevation((float) animation.getAnimatedValue("elevation"));

                // Animate the drawable
                mIV_StampPlaceholder.setRotation((float)animation.getAnimatedValue("Rot"));
                mIV_StampPlaceholder.setTranslationZ((float) animation.getAnimatedValue("Zoom"));
                mIV_StampPlaceholder.setTranslationX((float) animation.getAnimatedValue("X"));
                mIV_StampPlaceholder.requestLayout();

                // Animate the custom view
                mStampView.setRotation((float)animation.getAnimatedValue("Rot"));
                mStampView.setTranslationZ((float) animation.getAnimatedValue("Zoom"));
                mStampView.setTranslationX((float) animation.getAnimatedValue("X"));
                mStampView.requestLayout();






            }
        });

        //Create a time inter
        Interpolator customInterpolator = PathInterpolatorCompat.create(0.790f, 0.000f, 1.000f, 1.000f);

        //Start the animation
        mTranslationAnimator.setInterpolator(customInterpolator);
        mTranslationAnimator.setDuration(1550);
        mTranslationAnimator.start();

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

    @OnClick(R.id.bt_back_arrow)
    public void onBackArrowClick(){
        finish();
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
