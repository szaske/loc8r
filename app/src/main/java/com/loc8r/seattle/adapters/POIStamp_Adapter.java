package com.loc8r.seattle.adapters;

import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.loc8r.seattle.R;
import com.loc8r.seattle.interfaces.OnPOIClickListener;
import com.loc8r.seattle.models.POI;
import com.loc8r.seattle.utils.StampView;

import java.util.ArrayList;

/**
 * Created by steve on 1/9/2018.
 */

public class POIStamp_Adapter extends RecyclerView.Adapter<POIStamp_Adapter.POI_View_Holder> {


    // Class variables
    private static final int TYPE_RIGHT = 0;
    private static final int TYPE_LEFT = 1;
    private static final int TYPE_SPACER = 2;


    ArrayList<POI> mPOIslist;
    OnPOIClickListener listener;
    int viewholderHeight;

    // Constructor
    public POIStamp_Adapter(ArrayList<POI> list, OnPOIClickListener listener) {
        this.mPOIslist = list;
        this.listener = listener;

    }

    @Override
    public int getItemViewType(int position) {
        if(isOdd(position)) {
            return TYPE_LEFT;
        }
        return TYPE_RIGHT;
    }

    private boolean isOdd(int position) {
        return position%2==0;
    }

    @Override
    public POI_View_Holder onCreateViewHolder(ViewGroup parent, int viewType) {

        //Inflate the layout, initialize the View Holder

        if (viewType == TYPE_RIGHT) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.poi_recycler_item_right, parent, false);
            POI_View_Holder vh = new POI_View_Holder(view);

            // Check once for height of viewholder view
            if(viewholderHeight==0){
                view.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
                viewholderHeight = view.getMeasuredHeight() ;
            }
            return new POI_View_Holder(view);
        } else if (viewType == TYPE_LEFT) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.poi_recycler_item_left, parent, false);
            return new POI_View_Holder(view);
        }

        throw new RuntimeException("there is no type that matches the type " + viewType + " + make sure your using types correctly");

        // View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.poi_recycler_item_right, parent, false);
        // POI_View_Holder holder = new POI_View_Holder(v);
        // return holder;

    }

    @Override
    public void onBindViewHolder(POI_View_Holder holder, int position) {

        // POI_View_Holder viewHolder = (POI_View_Holder)holder;
        holder.bind(position, mPOIslist.get(position), listener);

    }

    @Override
    public int getItemCount() {
        //returns the number of elements the RecyclerView will display
        return mPOIslist.size();
    }

    //
    // The VIEWHOLDER
    //
    //
    class POI_View_Holder extends RecyclerView.ViewHolder {

        // Variables for the ViewHolder
        private TextView name;
        private ConstraintLayout contentLayout;
        private StampView stampView;
        private ConstraintLayout placeholderLayout;

        POI_View_Holder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.poi_nameTV);
            contentLayout = itemView.findViewById(R.id.content_layout);
            stampView = itemView.findViewById(R.id.poi_StampView);
            placeholderLayout = itemView.findViewById(R.id.poiPlaceholderLayout);

//            stampView.setSaveEnabled(true); // force state saving
//            stampView.setId(getPosition());
        }


        public void bind(int position, final POI poi, final OnPOIClickListener listener){

            // Check if the poi is a spacer.  This handles the rare case where the list ends
            // in an odd number.  To solve this problem we add a new item to the list when it's past
            // to the collection activity
            if(poi.getName().equals("blank")){
                contentLayout.setVisibility(View.INVISIBLE);
            } else {

                // Set visibility to true.  I do this so the recyclerview does not
                // accidently recycler this setting on resume
                contentLayout.setVisibility(View.VISIBLE);

                // Set POI information in viewHolder
                name.setText(poi.getName());
                // positionText.setText(String.valueOf(position+1));

                // Put collection location into the view, so we can use it in building list decorations
                placeholderLayout.setTag(position+1);

                if(poi.isStamped()){
                    stampView.setStamped(true);
                    stampView.setElevation(4);
                    stampView.setTranslationZ(4);
                    stampView.constructStampViewFromPOI(poi);
                    showPlaceholder(false);
                } else {
                    stampView.setStamped(false);
                    stampView.setElevation(0);
                    stampView.setTranslationZ(0);
                    showPlaceholder(true);
                }
                Log.d("ViewHolder-", "bind: method fired");
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override public void onClick(View view) {
                        listener.OnPOIClick(poi);
                    }
                });
            }
        }

        private void showPlaceholder(boolean b) {
            if(b){
                name.setVisibility(View.VISIBLE);
            } else {
                name.setVisibility(View.INVISIBLE);
            }
        }
    }

}
