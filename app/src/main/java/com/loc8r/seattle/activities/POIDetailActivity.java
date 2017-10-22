package com.loc8r.seattle.activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.loc8r.seattle.R;
import com.loc8r.seattle.models.POI;
import com.squareup.picasso.Picasso;

import org.parceler.Parcels;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.loc8r.seattle.R.id.poiImageView;

public class POIDetailActivity extends LoggedInActivity {

    // Variables
    private static final String TAG = MainActivity.class.getSimpleName();
    @BindView(R.id.poiImageView) ImageView mImageIV;
    @BindView(R.id.poiNameTextView) TextView mNameTV;
    @BindView(R.id.poiDescriptionTextView) TextView mDescriptionTV;
    @BindView(R.id.poiLocationTextView) TextView mLocationTV;
    @BindView(R.id.poiTagsTextView) TextView mTagsTV;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_poi_detail);
        ButterKnife.bind(this);
        POI detailedPoi = Parcels.unwrap(getIntent().getParcelableExtra("poi"));
        Log.d(TAG, "Detail view " + detailedPoi.getName() + "Lat =" + detailedPoi.getLatitude());
        Picasso
                .with(this)
                .load(detailedPoi.getImg_url())
                .into(mImageIV);
        mNameTV.setText(detailedPoi.getName());
        mDescriptionTV.setText(detailedPoi.getDescription());
        mLocationTV.setText(detailedPoi.getLongitude().toString()+","+detailedPoi.getLatitude().toString());

    }
}
