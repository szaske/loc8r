package com.loc8r.seattle.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.loc8r.seattle.R;
import com.loc8r.seattle.interfaces.OnMenuClickListener;
import com.loc8r.seattle.models.ListItem;

import java.util.Collections;
import java.util.List;

/**
 * Created by steve on 1/9/2018.
 */

public class Main_List_Adapter extends RecyclerView.Adapter<Main_List_Adapter.Main_List_View_Holder> {

    // An interface is needed so we can return an item of that listener type.  It returns a ListItem.
    public interface OnMenuClickListener {
        void OnMenuClick(ListItem item);
    }

    // Class variables
    List<ListItem> list = Collections.emptyList();
    OnMenuClickListener listener;
    //Context context;

    // Constructor
    public Main_List_Adapter(List<ListItem> list, OnMenuClickListener listener) {
        this.list = list;
        this.listener = listener;
        //this.context = context;
    }

    @Override
    public Main_List_View_Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        //Inflate the layout, initialize the View Holder
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.main_list_recycler_item, parent, false);
        Main_List_View_Holder holder = new Main_List_View_Holder(v);
        return holder;

    }

    @Override
    public void onBindViewHolder(Main_List_View_Holder holder, int position) {

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

//    @Override
//    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
//        super.onAttachedToRecyclerView(recyclerView);
//    }
//
//    // Insert a new item to the RecyclerView on a predefined position
//    public void insert(int position, ListItem data) {
//        list.add(position, data);
//        notifyItemInserted(position);
//    }
//
//    // Remove a RecyclerView item containing a specified Data object
//    public void remove(ListItem data) {
//        int position = list.indexOf(data);
//        list.remove(position);
//        notifyItemRemoved(position);
//    }

    //
    // The VIEWHOLDER
    //
    //
    static class Main_List_View_Holder extends RecyclerView.ViewHolder {

        TextView title;
        ImageView imageView;

        Main_List_View_Holder(View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.poiGroupTextView);
            imageView = (ImageView) itemView.findViewById(R.id.poiGroupBackgroundImageView);
        }

        public void bind(final ListItem item, final OnMenuClickListener listener){
            title.setText(item.getTitle());
            // Set background image here
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View view) {
                    listener.OnMenuClick(item);
                }
            });
        }
    }

}
