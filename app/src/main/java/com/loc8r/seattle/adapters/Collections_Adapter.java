package com.loc8r.seattle.adapters;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.loc8r.seattle.R;
import com.loc8r.seattle.interfaces.OnCollectionClickListener;
import com.loc8r.seattle.models.ListItem;
import com.loc8r.seattle.models.POI;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.SortedSet;

/**
 * Created by steve on 1/9/2018.
 */

public class Collections_Adapter extends RecyclerView.Adapter<Collections_Adapter.Collections_View_Holder> {


    // Class variables
    ArrayList<String> list;
    OnCollectionClickListener listener;
    //Context context;

    // Constructor
    public Collections_Adapter(ArrayList<String> list, OnCollectionClickListener listener) {
        this.list = list;
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
        //holder.title.setText(list.get(position).getTitle());
        //holder.imageView.setImageResource(list.get(position).getImageURL());

        //Instead lets use the viewholder bind method to assign content
        holder.bind(list.get(position), listener);

        //animate(holder);
    }

    @Override
    public int getItemCount() {
        //returns the number of elements the RecyclerView will display
        return list.size();
    }

    //
    // The VIEWHOLDER
    //
    //
    static class Collections_View_Holder extends RecyclerView.ViewHolder {

        TextView name;
        //ImageView imageView;

        Collections_View_Holder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.nameTV);
            // imageView = (ImageView) itemView.findViewById(R.id.poiGroupBackgroundImageView);
        }

        public void bind(final String item, final OnCollectionClickListener listener){
            name.setText(item);
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
