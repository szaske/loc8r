package com.loc8r.seattle.activities;

import android.app.Dialog;
import android.location.Location;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.loc8r.seattle.R;
import com.loc8r.seattle.interfaces.LocationListener;
import com.loc8r.seattle.interfaces.QueryListener;
import com.loc8r.seattle.models.POI;
import com.loc8r.seattle.models.Stamp;
import com.loc8r.seattle.mongodb.MongoDBManager;
import com.loc8r.seattle.utils.ProgressDialog;
import com.paginate.Paginate;
import com.squareup.picasso.Picasso;

import org.parceler.Parcels;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
    @BindView(R.id.currentLocationtextView) TextView mLocTV;

    // TODO Should I move currentLocation to the State Manager?
    Location mCurrentLocation;
    POI detailedPoi;

    @OnClick(R.id.getStampBtn)
    public void onGetStampClick() {
        Log.i(TAG, ": Get Stamp Button was clicked");
        // TODO submit data to server...
        addStamp();
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
            MongoDBManager.getInstance(getApplicationContext()).addStamp(detailedPoi, queryListener);
        }
    }
}
