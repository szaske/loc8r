package com.loc8r.seattle.activities;

import android.location.Location;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.loc8r.seattle.R;
import com.loc8r.seattle.interfaces.LocationListener;
import com.loc8r.seattle.models.POI;
import com.paginate.Paginate;
import com.squareup.picasso.Picasso;

import org.parceler.Parcels;

import java.text.DateFormat;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.loc8r.seattle.R.id.poiImageView;

public class POIDetailActivity extends LoggedInActivity {

    // Variables
    private static final String TAG = LoggedInActivity.class.getSimpleName();
    @BindView(R.id.poiImageView) ImageView mImageIV;
    @BindView(R.id.poiNameTextView) TextView mNameTV;
    @BindView(R.id.poiDescriptionTextView) TextView mDescriptionTV;
    @BindView(R.id.poiLocationTextView) TextView mLocationTV;
    @BindView(R.id.poiTagsTextView) TextView mTagsTV;
    @BindView(R.id.currentLocationtextView) TextView mLocTV;

    Location mCurrentLocation;
    POI detailedPoi;

    @OnClick(R.id.getStampBtn)
    public void onGetStampClick() {
        Log.i(TAG, ": Get Stamp Button was clicked");
        // TODO submit data to server...
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_poi_detail);
        ButterKnife.bind(this);
        detailedPoi = Parcels.unwrap(getIntent().getParcelableExtra("poi"));
        Log.d(TAG, "Detail view " + detailedPoi.getName() + "Lat =" + detailedPoi.getLatitude());
        Picasso
                .with(this)
                .load(detailedPoi.getImg_url())
                .into(mImageIV);
        mNameTV.setText(detailedPoi.getName());
        mDescriptionTV.setText(detailedPoi.getDescription());
        mLocationTV.setText(detailedPoi.getLongitude().toString()+","+detailedPoi.getLatitude().toString());
    }

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
                    String dist = String.valueOf(distance(mCurrentLocation.getLatitude(),mCurrentLocation.getLongitude(),detailedPoi.getLatitude(),detailedPoi.getLongitude()));
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

    /**
     * Calculate distance between two points in latitude and longitude
     * elevation has been ignored.
     * Uses Haversine method as its base.
     *
     * @param lat1 Starting location's latitude
     * @param lat2 The destination's latitude
     * @param lon1 Starting location's longitude
     * @param lon2 The destination's longitude
     * @return distance in meters
     */
    public double distance(double lat1,
                                  double lon1,
                                  double lat2,
                                  double lon2) {

        final int R = 6371; // Radius of the earth

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c * 1000; // convert to meters

        distance = Math.pow(distance, 2);

        return Math.sqrt(distance);
    }
}
