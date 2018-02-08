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

import java.util.ArrayList;

/**
 * Created by steve on 1/9/2018.
 */

public class POI_Adapter extends RecyclerView.Adapter<POI_Adapter.POI_View_Holder> {


    // Class variables
    ArrayList<POI> mPOIslist;
    OnPOIClickListener listener;
    //Context context;

    // Constructor
    public POI_Adapter(ArrayList<POI> list, OnPOIClickListener listener) {
        this.mPOIslist = list;
        this.listener = listener;
        //this.context = context;
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
        //holder.imageView.setImageResource(mPOIslist.get(position).getImageURL());

        //Instead lets use the viewholder bind method to assign content
        holder.bind(mPOIslist.get(position), listener);

        //animate(holder);
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

        TextView name;
        TextView stampText;
        TextView positionText;
        //ImageView imageView;

        POI_View_Holder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.poi_nameTV);
            stampText= itemView.findViewById(R.id.poi_stampTextTV);
            positionText= itemView.findViewById(R.id.poi_positionTV);
            // imageView = (ImageView) itemView.findViewById(R.id.poiGroupBackgroundImageView);
        }

        public void bind(final POI poi, final OnPOIClickListener listener){

            // Set POI information in viewHolder
            name.setText(poi.getName());
            stampText.setText(poi.getStampText());
            positionText.setText(String.valueOf(poi.getCollectionPosition()));

            if(poi.isStamped()){
                name.setBackgroundColor(0xFF00FF00);
            } else {
                name.setBackgroundColor(0xFFFFFFFF);
            }
            // Set background image here
            Log.d("ViewHolder-", "bind: method fired");
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View view) {
                    listener.OnPOIClick(poi);
                }
            });
        }
    }

}
