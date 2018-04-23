package com.loc8r.seattle.adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.TextView;

import com.loc8r.seattle.R;
import com.loc8r.seattle.interfaces.OnCollectionClickListener;
import com.loc8r.seattle.models.Collection;
import com.loc8r.seattle.utils.CollectionLayout;
import com.loc8r.seattle.utils.FocusedCropTransform;
import com.loc8r.seattle.utils.StampView;
import com.squareup.picasso.Picasso;

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
        ImageView bgImage;
        StampView stampView;
        Context context;
        CollectionLayout rootView;
        int bgResourceId;

        Collections_View_Holder(View itemView) {
            super(itemView);

            // Configure the title textView including a drop shadow
            name = itemView.findViewById(R.id.nameTV);
            Typeface mainTypeface = Typeface.createFromAsset(name.getContext().getAssets(), "fonts/Roboto-Bold.ttf");
            name.setTypeface(mainTypeface);
            name.setShadowLayer(15, 0, 0, Color.BLACK );

            collectionIcon = itemView.findViewById(R.id.iv_collection_icon);
            rootView = itemView.findViewById(R.id.collection_item_row_rootView);
            context = itemView.getContext();

        }

        public void bind(final Collection item, final OnCollectionClickListener listener){

            // Bind the title
            name.setText(item.getName());

            // Get and display the correct icon for each collection
            int iconResourceId = context.getResources().getIdentifier("icon_" + item.getCollectionResourceID(), "drawable", context.getPackageName());

            if ( iconResourceId != 0 ) {  // the resource exists...
                Drawable image = context.getResources().getDrawable(iconResourceId);
                collectionIcon.setImageDrawable(image);
            }
            else {  // checkExistence == 0  // the resource does NOT exist!!
                collectionIcon.setImageResource(R.drawable.stamp_placeholder);
            }

            // Get the Id for the background image resource
            bgResourceId = context.getResources().getIdentifier("backg_" + item.getCollectionResourceID(), "drawable", context.getPackageName());

            if(bgResourceId==0){
                bgResourceId = (int) R.drawable.main_menu_bg;
            }

            // see https://stackoverflow.com/questions/18081001/android-get-width-of-layout-programatically-having-fill-parent-in-its-xml
            ViewTreeObserver vto = rootView.getViewTreeObserver();
            vto.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                public boolean onPreDraw() {
                    rootView.getViewTreeObserver().removeOnPreDrawListener(this);
                    int rootWidth = rootView.getMeasuredWidth();
                    int rootHeight = rootView.getMeasuredHeight();
                    Picasso.get()
                            .load(bgResourceId)
                            .transform(new FocusedCropTransform(rootWidth,rootHeight,0,0))
                            .into(rootView);

                    Log.d("tester-", "STZ _ onPreDraw: Width is " + rootView.getMeasuredWidth() + " - Height:"+ rootView.getMeasuredHeight());
                    return true;
                }
            });

            Log.d("Log Info-", "STZ _ RootView Width is " + rootView.getMeasuredWidth() + " - Height:"+ rootView.getMeasuredHeight());


            Log.d("ViewHolder-", "bind: method fired");
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View view) {
                    listener.OnCollectionClick(item);
                }
            });
        }
    }

}
