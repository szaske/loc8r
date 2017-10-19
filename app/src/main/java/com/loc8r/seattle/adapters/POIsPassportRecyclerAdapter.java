package com.loc8r.seattle.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.loc8r.seattle.R;
import com.loc8r.seattle.activities.POIDetailActivity;
import com.loc8r.seattle.models.POI;
import com.loc8r.seattle.utils.Constants;

import org.parceler.Parcel;
import org.parceler.Parcels;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

import com.squareup.picasso.Picasso;

/**
 * Created by Guest on 10/5/17.
 */

public class POIsPassportRecyclerAdapter extends RecyclerView.Adapter<POIsPassportRecyclerAdapter.poiHolder> {

    // My variables
    private ArrayList<POI> mPOIs;

    //
    // extend RecyclerView.ViewHolder, allowing it to be used as a ViewHolder for the adapter
    //
    //
    //
    public static class poiHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        // a list of references to the lifecycle of the object to allow the
        // ViewHolder to hang on to your ImageView and TextView, so it
        // doesn’t have to repeatedly query the same information.
        // This is why we're using a recyclerview
        @BindView(R.id.poiImageView) ImageView mPOIImageView; // poiImageView
        @BindView(R.id.poiTitleTextView) TextView mPOITitleTextView; //poiTitleTextView
        private POI mPOI;


        // The Constructor
        public poiHolder(View v) {
            super(v);
            ButterKnife.bind(this,v);
            v.setOnClickListener(this);
        }

        /**
         * @param v the Clicked view
         */
        @Override
        public void onClick(View v) {
            Log.d("RecyclerView", "CLICK on" + v.toString());

            //itemView is a special object held by the viewHolder
            Context context = itemView.getContext();
            //Parcelable wrapped = Parcels.wrap(context.mAwwList(1));

            Intent showPOIIntent = new Intent(context, POIDetailActivity.class);
            showPOIIntent.putExtra(Constants.INTENT_POI_KEY, Parcels.wrap(mPOI));
            context.startActivity(showPOIIntent);
        }

        /**
         * This method binds the photo object to the viewHolder
         *
         * @param POI The POI object to be bound
         */
        public void bindPOI(POI poi) {

            // Here is where we can add text to a textview to show this.mPosition on each item

            // This makes it so the Holder has a copy of the object
            mPOI = poi;
            mPOITitleTextView.setText(mPOI.getName());
            Picasso
                    .with(mPOIImageView.getContext())
                    .load(mPOI.getImg_url())
                    .into(mPOIImageView);
        }
    }  //end of viewHolder class

    public POIsPassportRecyclerAdapter(ArrayList<POI> pois) {
        mPOIs = pois;
    }

    @Override
    public POIsPassportRecyclerAdapter.poiHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View inflatedView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.poi_recycler_item, parent, false);
        Log.d("onCreateViewHolder: ", "Fired" );
        return new poiHolder(inflatedView);
    }

    @Override
    public void onBindViewHolder(POIsPassportRecyclerAdapter.poiHolder holder, int position) {
        POI itemPOI = mPOIs.get(position);
        holder.bindPOI(itemPOI);
    }

    @Override
    public int getItemCount() {
        return mPOIs.size();
    }

} //end of class
