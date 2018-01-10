package com.loc8r.seattle.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.loc8r.seattle.R;

/**
 * Created by steve on 1/9/2018.
 */

public class Main_List_View_Holder extends RecyclerView.ViewHolder {

    TextView title;
    ImageView imageView;

    Main_List_View_Holder(View itemView) {
        super(itemView);
        title = (TextView) itemView.findViewById(R.id.poiGroupTextView);
        imageView = (ImageView) itemView.findViewById(R.id.poiGroupBackgroundImageView);
    }
}
