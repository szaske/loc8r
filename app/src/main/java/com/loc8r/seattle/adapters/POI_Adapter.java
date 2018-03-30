package com.loc8r.seattle.adapters;

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

public class POI_Adapter extends RecyclerView.Adapter<POI_Adapter.POI_View_Holder> {


    // Class variables
    ArrayList<POI> mPOIslist;
    OnPOIClickListener listener;

    // Constructor
    public POI_Adapter(ArrayList<POI> list, OnPOIClickListener listener) {
        this.mPOIslist = list;
        this.listener = listener;
    }

    @Override
    public POI_View_Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        //Inflate the layout, initialize the View Holder
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.poi_recycler_item, parent, false);
        POI_View_Holder holder = new POI_View_Holder(v);
        return holder;

    }

    @Override
    public void onBindViewHolder(POI_View_Holder holder, int position) {

        //Use the provided View Holder on the onCreateViewHolder method to populate the current row on the RecyclerView
        //holder.title.setText(mPOIslist.get(position).getTitle());

        //Instead lets use the viewholder bind method to assign content
        holder.bind(mPOIslist.get(position), listener);
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
    static class POI_View_Holder extends RecyclerView.ViewHolder {

        // Variables for the ViewHolder
        private TextView name;
        private TextView positionText;
        private StampView stampView;

        POI_View_Holder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.poi_nameTV);
            positionText= itemView.findViewById(R.id.poi_positionTV);
            stampView = itemView.findViewById(R.id.poi_StampView);

//            stampView.setSaveEnabled(true); // force state saving
//            stampView.setId(getPosition());
        }

        public void bind(final POI poi, final OnPOIClickListener listener){

            // Set POI information in viewHolder
            name.setText(poi.getName());
            positionText.setText(String.valueOf(poi.getCollectionPosition()));

            if(poi.isStamped()){
                stampView.setStamped(true);
                stampView.setElevation(4);
                stampView.setTranslationZ(4);
                stampView.setClipToOutline(true);
                // stampView.invalidate();
                stampView.constructStampViewFromPOI(poi);
            } else {
                stampView.setStamped(false);
                stampView.setElevation(0);
                stampView.setTranslationZ(0);
            }
            Log.d("ViewHolder-", "bind: method fired");
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View view) {
                    listener.OnPOIClick(poi);
                }
            });
        }
    }

}
