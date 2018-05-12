package com.loc8r.seattle.activities;

import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Rect;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v4.view.animation.PathInterpolatorCompat;
import android.util.Log;
import android.view.TouchDelegate;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.Interpolator;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.github.chrisbanes.photoview.PhotoView;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.loc8r.seattle.R;
import com.loc8r.seattle.activities.base.LocationBase_Activity;
import com.loc8r.seattle.interfaces.LocationListener;
import com.loc8r.seattle.interfaces.QueryListener;
import com.loc8r.seattle.models.POI;
import com.loc8r.seattle.models.Stamp;
// import com.loc8r.seattle.mongodb.MongoDBManager;
import com.loc8r.seattle.models.User;
import com.loc8r.seattle.utils.FocusedCropTransform;
import com.loc8r.seattle.utils.ProgressDialog;
import com.loc8r.seattle.utils.StampView;
import com.loc8r.seattle.utils.StateManager;

import org.parceler.Parcels;
import com.squareup.picasso.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import butterknife.BindView;
import butterknife.*;
import butterknife.OnClick;

public class POIDetailActivity extends LocationBase_Activity {

    // Variables
    private static final String TAG = LocationBase_Activity.class.getSimpleName();
    @BindView(R.id.iv_poiImage) ImageView mIV_PoiImage;
    @BindView(R.id.tv_poi_name) TextView mTV_PoiName;
    @BindView(R.id.tv_poi_description) TextView mTV_PoiDescription;
    @BindView(R.id.tv_distance) TextView mTV_PoiDistance;
    @BindView(R.id.tv_collection) TextView mTV_PoiCollection;
    @BindView(R.id.photoLayout) ConstraintLayout mPhotoLayout;
    @BindView(R.id.stampView) StampView mStampView;
    @BindView(R.id.bt_getStamp) Button mStampBtn;
    @BindView(R.id.bt_back_arrow) ImageButton mBackArrow;
    @BindView(R.id.photo_view) PhotoView mPhotoView;
    @BindView(R.id.progressBar) ProgressBar mProgressBar;

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

        // mPhotoView.setImageResource(R.drawable.image);
        initPhotoView();


        //testing enlarged touch delegate
        changeTouchableAreaOfView(mBackArrow,220);

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
                Picasso.get()
                        .load(detailedPoi.getImg_url())
                        .transform(new FocusedCropTransform(mIV_PoiImage.getMeasuredWidth(),
                                mIV_PoiImage.getMeasuredHeight(),
                                mIV_PoiImage.getId(),
                                detailedPoi.getImgFocalpointX(),
                                detailedPoi.getImgFocalpointY()))
                        .into(mIV_PoiImage, new Callback() {

                            @Override
                            public void onSuccess() {
                                mIV_PoiImage.setVisibility(View.VISIBLE);
                                mProgressBar.setVisibility(View.INVISIBLE);
                            }

                            // Need to handle errors
                            @Override public void onError(Exception e) {
                                mProgressBar.setVisibility(View.VISIBLE);
                                mIV_PoiImage.setVisibility(View.INVISIBLE);
                            }
                        });

                Log.d(TAG, "STZ _ onPreDraw: Width is " + mIV_PoiImage.getMeasuredWidth() + " - Height:"+ mIV_PoiImage.getMeasuredHeight());

                return true;
            }
        });

        Log.d(TAG, "Detail view " + detailedPoi.getName());

        //Create a the Passport Stamp view
        initStampCreation();

        // Configure Page content
        mTV_PoiName.setText(detailedPoi.getName());
        mTV_PoiDescription.setText(detailedPoi.getDescription());
        mTV_PoiCollection.setText(detailedPoi.getCollection()+" #"+String.valueOf(detailedPoi.getCollectionPosition()));

        // Access a Cloud Firestore instance from your Activity
        db = FirebaseFirestore.getInstance();

        //        //Get the Firebase user
        user = FirebaseAuth.getInstance().getCurrentUser();

    }

    private void initPhotoView() {

        // Make sure the view starts invisible
        mPhotoLayout.setVisibility(View.INVISIBLE);

        // Load the image into the full screen view
        Picasso.get()
                .load(detailedPoi.getImg_url())
                .into(mPhotoView);

    }

    private void initStampCreation() {

        mStampView.setPlaceholder(getResources().getDrawable(R.drawable.stamp_placeholder_darker));
        mStampView.constructStampViewFromPOI(detailedPoi);

        if(detailedPoi.isStamped()){ // We have a stamp
            return;
        } else {
            mStampView.setPlaceholderText("GET CLOSER");
            mStampView.setOnClickListener(new StampPlaceholderClicked());

            // By default the listener is set, but disabled and only available
            mStampView.setClickable(false);
        }

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
                    mTV_PoiDistance.setText(String.valueOf(detailedPoi.distanceToUser())+"m");
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

    class StampPlaceholderClicked implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    final Stamp newStamp = CreateStamp(); // Create a new stamp

                    // See what happens with animation in separate thread.
                    animateGettingStamp();

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

                                AddStampToDB(newStamp);
                            }
                        }
                    });
                }
            });
        }
    }


    // @OnClick(R.id.bt_getStamp)
    public void onStampButtonClick() {

        //TODO Add a check to determine if we're within a constant amount of meters from the POI.  If close enough then enable the button

    }

    /**
     *  Click listener for when the user clicks on the back arrow
     */
    @OnClick(R.id.bt_back_arrow)
    public void onBackArrowClick(){
        finish();
    }

    /**
     *  Click listener for when the user clicks on the full screen icon in the Photo View
     */
    @OnClick(R.id.iv_fullScreen)
    public void onClickFullScreenIcon(){
        hideSystemUI();
        mPhotoLayout.setVisibility(View.VISIBLE);
    }

    /**
     *  Click listener for when the user clicks on the exit full screen icon in the Photo View
     */
    @OnClick(R.id.iv_exitFullScreen)
    public void onClickExitFullScreenIcon(){
        showSystemUI();
        mPhotoLayout.setVisibility(View.INVISIBLE);
    }


    // This snippet hides the system bars.
    private void hideSystemUI() {
        // Set the IMMERSIVE flag.
        // Set the content to appear under the system bars so that the content
        // doesn't resize when the system bars hide and show.
        mPhotoLayout.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                        | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                        | View.SYSTEM_UI_FLAG_IMMERSIVE);
    }

    // This snippet shows the system bars. It does this by removing all the flags
// except for the ones that make the content appear under the system bars.
    private void showSystemUI() {
        mPhotoLayout.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }

    private void animateGettingStamp(){

        // Change StampView to show stamp
        mStampView.setElevation(4f);
        mStampView.setStamped(true);
        // mStampView.invalidate(); // force a redraw

        // Create animation value holders
        PropertyValuesHolder RotPropertyHolder = PropertyValuesHolder.ofFloat("Rot", -45,0 );
        PropertyValuesHolder ZoomPropertyHolder = PropertyValuesHolder.ofFloat("Zoom", 30f,4f );
        PropertyValuesHolder XPropertyHolder = PropertyValuesHolder.ofFloat("X", -100f,0f );
        PropertyValuesHolder ElevationPropertyHolder = PropertyValuesHolder.ofFloat("elevation", 30f,4f );
        PropertyValuesHolder ScalePropertyHolder = PropertyValuesHolder.ofFloat("scale", 2.5f,1f );
        PropertyValuesHolder TransparencyPropertyHolder = PropertyValuesHolder.ofFloat("Alpha", 0f,1f );


        //Create the animator
        ValueAnimator mTranslationAnimator = ValueAnimator.ofPropertyValuesHolder(RotPropertyHolder,
                ZoomPropertyHolder,
                TransparencyPropertyHolder,
                ElevationPropertyHolder,
                ScalePropertyHolder,
                XPropertyHolder);

        // Create the update listener
        mTranslationAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {

                // Animate the custom view
                mStampView.setAlpha((float)animation.getAnimatedValue("Alpha"));
                mStampView.setRotation((float)animation.getAnimatedValue("Rot"));
                mStampView.setTranslationZ((float) animation.getAnimatedValue("Zoom"));
                mStampView.setScaleX((float) animation.getAnimatedValue("scale"));
                mStampView.setScaleY((float) animation.getAnimatedValue("scale"));
                // mStampView.setElevation((float) animation.getAnimatedValue("Zoom"));

                mStampView.setTranslationX((float) animation.getAnimatedValue("X"));
                mStampView.requestLayout();

            }
        });

        //Create a time inter
        Interpolator customInterpolator = PathInterpolatorCompat.create(0.790f, 0.000f, 1.000f, 1.000f);

        //Start the animation
        mTranslationAnimator.setInterpolator(customInterpolator);
        mTranslationAnimator.setDuration(650);
        mTranslationAnimator.start();

    }


    private void AddStampToDB(final Stamp newStamp){
        Log.d(TAG, "onClick: fired");
        String uid = user.getUid();
        User ESUser = new User();

        // Update Firebase adding Stamp to the user stamps collection
        db.collection("users")
                .document(uid)
                .collection("stamps")
                .document(newStamp.getPoiId())
                .set(newStamp)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "Stamp added");
                        addStampToStateManager(newStamp);

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

    /**
     *  Update our local data to show the new stamp
     */
    private void addStampToStateManager(Stamp stamp) {
        //Find the POI in the list
        POI updatedPOI = findPOIinStateManager(detailedPoi.getId());
        updatedPOI.setStamp(stamp);

    }

    private POI findPOIinStateManager(String id) {
        for(POI poi : StateManager.getInstance().getPOIs()) {
            if(poi.getId().equals(id)) {
                return poi;
            }
        }
        return null;
    }

    private void changeTouchableAreaOfView(final View view, final int extraSpace) {

        final View parent = (View) view.getParent();

        parent.post(new Runnable() {
            public void run() {
                // Post in the parent's message queue to make sure the parent
                // lays out its children before we call getHitRect()
                Rect delegateArea = new Rect();
                View delegate = view;
                delegate.getHitRect(delegateArea);
                delegateArea.top -= extraSpace;
                delegateArea.bottom += extraSpace;
                delegateArea.left -= extraSpace;
                delegateArea.right += extraSpace;
                TouchDelegate expandedArea = new TouchDelegate(delegateArea,
                        delegate);
                // give the delegate to an ancestor of the view we're
                // delegating the
                // area to
                if (View.class.isInstance(delegate.getParent())) {
                    ((View) delegate.getParent())
                            .setTouchDelegate(expandedArea);
                }
            }
        });

    }
}
