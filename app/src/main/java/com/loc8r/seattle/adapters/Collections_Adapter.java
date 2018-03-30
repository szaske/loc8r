package com.loc8r.seattle.adapters;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.loc8r.seattle.R;
import com.loc8r.seattle.interfaces.OnCollectionClickListener;
import com.loc8r.seattle.models.Collection;
import com.loc8r.seattle.utils.StampView;

import java.util.ArrayList;

/**
 * Created by steve on 1/9/2018.
 */

public class Collections_Adapter extends RecyclerView.Adapter<Collections_Adapter.Collections_View_Holder> {


    // Class variables
    ArrayList<Collection> mCollectionslist;
    OnCollectionClickListener listener;
    //Context context;

    // Constructor
    public Collections_Adapter(ArrayList<Collection> list, OnCollectionClickListener listener) {
        this.mCollectionslist = list;
        this.listener = listener;
        //this.context = context;
    }

    @Override
    public Collections_View_Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        //Inflate the layout, initialize the View Holder
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.collections_recycler_item, parent, false);
        Collections_View_Holder holder = new Collections_View_Holder(v);
        return holder;

    }

    @Override
    public void onBindViewHolder(Collections_View_Holder holder, int position) {

        //Use the provided View Holder on the onCreateViewHolder method to populate the current row on the RecyclerView
        //holder.title.setText(mPOIslist.get(position).getTitle());
        //holder.imageView.setImageResource(mPOIslist.get(position).getImageURL());

        //Instead lets use the viewholder bind method to assign content
        holder.bind(mCollectionslist.get(position), listener);

        //animate(holder);
    }

    @Override
    public int getItemCount() {
        //returns the number of elements the RecyclerView will display
        return mCollectionslist.size();
    }

    //
    // The VIEWHOLDER
    //
    //
    static class Collections_View_Holder extends RecyclerView.ViewHolder {

        TextView name;
        ImageView collectionIcon;
        StampView stampView;
        Context context;

        Collections_View_Holder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.nameTV);
            collectionIcon = itemView.findViewById(R.id.iv_collection_icon);
            context = itemView.getContext();
        }

        public void bind(final Collection item, final OnCollectionClickListener listener){
            name.setText(item.getName());

            // Get and display the correct icon for each collection

            int iconResource = context.getResources().getIdentifier(item.getIconName(), "drawable", context.getPackageName());

            if ( iconResource != 0 ) {  // the resource exists...
                Drawable image = context.getResources().getDrawable(iconResource);
                collectionIcon.setImageDrawable(image);
            }
            else {  // checkExistence == 0  // the resource does NOT exist!!
                collectionIcon.setImageResource(R.drawable.stamp_placeholder);
            }

            // Set background image here

            Log.d("ViewHolder-", "bind: method fired");
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View view) {
                    listener.OnCollectionClick(item);
                }
            });
        }
    }

}
