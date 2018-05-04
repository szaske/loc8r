package com.loc8r.seattle.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.support.annotation.Nullable;
import android.os.Bundle;

import android.support.constraint.ConstraintLayout;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.TouchDelegate;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.loc8r.seattle.R;
import com.loc8r.seattle.activities.base.LocationBase_Activity;
import com.loc8r.seattle.interfaces.LocationListener;
import com.loc8r.seattle.models.POI;
import com.loc8r.seattle.utils.Constants;
import com.loc8r.seattle.utils.Constants.Loc8rGraphics;
import com.loc8r.seattle.utils.StateManager;
import com.mancj.slideup.SlideUp;
import com.mancj.slideup.SlideUpBuilder;

import android.view.View;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.loc8r.seattle.utils.Constants.MARKER;

public class MapActivity extends LocationBase_Activity implements
        GoogleMap.OnMarkerClickListener,
        OnMapReadyCallback {

    private static final String TAG = MapActivity.class.getSimpleName();
    private GoogleMap mMap;

    // Drawer UI
    private SlideUp mDrawer;
    private View mDrawerView;
    private View mHideImageView;
    private TextView mDrawerTitleTV;
    private TextView mDrawerDescTV;
    private ImageView mDrawerIconIV;
    private Button mDrawerDetailButton;
    // private Location mCurrentLocation;
    private Context context;

    private List<Integer> mExistingPoiMarkers;
    private ConstraintLayout mDraggableDrawer;

    private Boolean haveNotDoneInitialZoomIn;
    SupportMapFragment mMapFragment;

    @BindView(R.id.bt_map_back_arrow) ImageButton mBackArrow;

    // The selected POI
    private POI mSelectedPOI;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        ButterKnife.bind(this);

        // Obtain the SupportMapFragment and loads layout
        mMapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        // Setup for the drawer UI.
        DrawerSetup();

        context = this; // Set context so we can use inside the runnable below

        // Our list of map markers.  An arraylist of ints, the indexs of POIs in the State manager.
        // I use this list to remember what POIs are currently being shown in the map.
        mExistingPoiMarkers = new ArrayList<>();

        //testing enlarged touch delegate
        changeTouchableAreaOfView(mBackArrow,220);

    }

    @OnClick(R.id.bt_map_back_arrow)
    public void onBackArrowClick(){
        finish();
    }


    //

    /**
     *  Creates a Bitmap marker from vector art
     *  See https://stackoverflow.com/questions/42365658/custom-marker-in-google-maps-in-android-with-vector-asset-icon
     *
     * @param context The current activity
     * @param vectorResId The ID
     * @return
     */
    private BitmapDescriptor bitmapDescriptorFromVector(Context context, int vectorResId) {
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResId);
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    // A callback method, which is invoked on configuration is changed
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // Adding the pointList arraylist to Bundle
        // Saved in baseActivity instead
        // outState.putParcelable("pois", Parcels.wrap(mListOfPOIs));

        //remember drawer state
        if (mDrawer!=null) {
            outState.putParcelable("drawerIsVisible", Parcels.wrap(mDrawer.isVisible()));
        }

        // Saving the bundle
        super.onSaveInstanceState(outState);
    }

    private void DrawerSetup(){
        //assign the mDrawer view and other contained views
        mDrawerView = findViewById(R.id.drawerView);
        mDrawerTitleTV = findViewById(R.id.title_textView);
        mDrawerDescTV = findViewById(R.id.desc_textView);
        mDrawerIconIV = findViewById(R.id.iv_drawer_icon);

        // Configure the details button and click listener
        mDrawerDetailButton = findViewById(R.id.poi_details_button);
        mDrawerDetailButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //let's go to the Details activity
                Intent i = new Intent(MapActivity.this, POIDetailActivity.class);
                Bundle bundle = new Bundle();
                bundle.putParcelable("poi", Parcels.wrap(mSelectedPOI));
                i.putExtras(bundle);
                startActivity(i); // POI is now passed to the new Activity
            }
        });

        //create the mDrawer SlideUp object
        mDrawer = new SlideUpBuilder(mDrawerView)
                .withListeners(new SlideUp.Listener.Events() {
                    @Override
                    public void onSlide(float percent) {
//                        dim.setAlpha(1 - (percent / 100));
//                        if (fab.isShown() && percent < 100) {
//                            fab.hide();
//                        }
                    }

                    @Override
                    public void onVisibilityChanged(int visibility) {
//                        if (visibility == View.GONE){
//                            fab.show();
//                        }
                    }
                })
                .withStartGravity(Gravity.BOTTOM)
                .withLoggingEnabled(true)
                .withGesturesEnabled(true)
                .withStartState(SlideUp.State.HIDDEN)
                .build();

        // close event listener

        mDraggableDrawer = findViewById(R.id.draggableArea);
        mDraggableDrawer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDrawer.hide();
            }
        });

//        mHideImageView = findViewById(R.id.arrow_down_imageView);
//        mHideImageView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                mDrawer.hide();
//            }
//        });

    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     */
    @SuppressLint("MissingPermission") @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;

        try {
            // Customise the styling of the base map using a JSON object defined
            // in a raw resource file.
            boolean success = mMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            this, R.raw.map_style));

            if (!success) {
                Log.e(TAG, "Style parsing failed.");
            }
        } catch (Resources.NotFoundException e) {
            Log.e(TAG, "Can't find style. Error: ", e);
        }

        // Allow the map to see the devices location, this should ALREADY have permission to get location from LocationBase_Activity
        mMap.setMyLocationEnabled(true);

        // Set listeners for marker events.  See the bottom of this class for their behavior.
        mMap.setOnMarkerClickListener(this);

        // Have we zoomed in on the initial load? no.
        haveNotDoneInitialZoomIn = true;

    }

    // Create a location given a Lat & Long
    public Location CreateLocation(Double latitude, Double longitude){
        Location tempLocation = new Location("");
        tempLocation.setLongitude(longitude);
        tempLocation.setLatitude(latitude);
        return tempLocation;
    }

    private LatLng locationToLatLong(Location location){
        return new LatLng(location.getLatitude(),location.getLongitude());
    }

    @Override
    public boolean onMarkerClick(final Marker marker) {

        // Assign the selected POI by marker
        mSelectedPOI = StateManager.getInstance().getPOIs().get((int)marker.getTag());

        //Set draw info to selected POI
        mDrawerTitleTV.setText(mSelectedPOI.getName());
        mDrawerDescTV.setText(mSelectedPOI.getDescription());
        mDrawerIconIV.setImageResource(getIconDrawableID(getApplicationContext(),mSelectedPOI.getCollection(),Constants.ICON));

        Log.d(TAG, "onMarkerClick: You selected marker " + mSelectedPOI.getName() );
        //Bring up the details mDrawer
        mDrawer.show();

        // We return false to indicate that we have not consumed the event and that we wish
        // for the default behavior to occur (which is for the camera to move such that the
        // marker is centered and for the marker's info window to open, if it has one).
        return false;
    }

    private void DrawNearbyMarkers(){
        // Step through all POI's and show markers for close ones (800 meters)

        for(int i = 0;i<StateManager.getInstance().getPOIs().size();i++){

            // Check to see if the POI is already shown on the map
            // If so, then skip it and go to the next
            if(!mExistingPoiMarkers.contains(i)) {
                POI poi = StateManager.getInstance().getPOIs().get(i);

                if (poi.distanceToUser() < Constants.DISTANCE_TO_SCAN_MARKERS) {

                    // Add the location's marker to the map
                    LatLng poiLatLng = new LatLng(poi.getLatitude(), poi.getLongitude());


                    Marker tempMarker = mMap.addMarker(new MarkerOptions()
                            .position(poiLatLng)
                            .icon(bitmapDescriptorFromVector(this, getIconDrawableID(this,poi.getCollection(), Constants.MARKER)))
                            .title(poi.getName()));
                    tempMarker.setTag(StateManager.getInstance().getPOIs().indexOf(poi)); //Tag is set to POI Index in full SM Arraylist

                    // Keep track of existing POI markers, so we don't need to redraw them
                    mExistingPoiMarkers.add(StateManager.getInstance().getPOIs().indexOf(poi));

                    //log that the marker is displayed
                    Log.d(TAG, "showing marker " + poi.getName());
                }
            } else {
                //We might , in the future, consider putting code here to remove markers from the map
            }
        }
    }

    /**
     *  Gets a graphic, either icon or marker
     *
     * @param context
     * @param collection
     * @param iconType
     * @return
     */
    public static int getIconDrawableID(Context context, String collection, @Loc8rGraphics String iconType) {

        String iconName = iconType + "_" + collection;
        Resources resources = context.getResources();

        // Check that the icon exists
        if(resources.getIdentifier(iconName, "drawable",
                context.getPackageName())!=0){
            return resources.getIdentifier(iconName, "drawable",
                    context.getPackageName());
        } else {
            //The icon didn't exists we'll return a default icon
            if(iconType=="marker"){
                return R.drawable.marker_default;
            }
            return R.drawable.icon_placeholder;
        }

    }



    /**
     *  Fires when we're properly connected to Google services and have all permissions
     */
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d(TAG, "onConnected() called with: " + "bundle = [" + bundle + "]");


        mMapFragment.getMapAsync(this);


        /**
        * get the last location of the device
        * */
        getContinousLocationUpdates(new LocationListener()
        {
            @Override
            public void onLocationReceived(final Location location)
            {

                // Spawn a new thread to redraw nearby markers
                MapActivity.this.runOnUiThread(new Runnable() {

                    @Override
                    public void run() {


                        // Store returned location in the state Manager and member variable
                        StateManager.getInstance().setCurrentLocation(location);

                        // Check if this is the first time location was retrieved
                        // if so, zoom into our location
                        if(haveNotDoneInitialZoomIn){
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(locationToLatLong(location), 18));
                            haveNotDoneInitialZoomIn=false;
                        }

//                      // Draw Markers
                        DrawNearbyMarkers();

                    }

                });

            }
        });
    }

    /**
     *  Returned POIs Event listener method
     *
     * @param POIs
     */
    public void onPOIsReceived(ArrayList<POI> POIs) {
        StateManager.getInstance().setPOIs(POIs);
    }

    /**
     *  Method to increase the 'clickable' area around a view.  Created to make the back arrow easier to click on
     *  see: https://stackoverflow.com/questions/1343222/is-there-an-example-of-how-to-use-a-touchdelegate-in-android-to-increase-the-siz
     *
     * @param view The view that needs more clickable space around it
     * @param extraSpace the increase size, in DP in all four directions of the view
     */
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
