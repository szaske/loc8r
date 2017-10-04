package com.loc8r.seattle.adapters;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.loc8r.android.loc8r.R;
import com.loc8r.seattle.models.POI;

import java.util.List;

/**
 * RecyclerView adapter to display a list of location cards on top of the map
 */
public class poiRecyclerViewAdapter extends
        RecyclerView.Adapter<poiRecyclerViewAdapter.ViewHolder> {

    private List<POI> listOfPOIs;
    private Context context;
    // private int selectedTheme;
    private static ClickListener clickListener;
    private Drawable emojiForCircle = null;
    private Drawable backgroundCircle = null;
    private int upperCardSectionColor = 0;
    private int poiNameColor = 0;

    public poiRecyclerViewAdapter(List<POI> items,
                                       Context context, ClickListener cardClickListener) {
        this.context = context;
        this.listOfPOIs = items;
        //this.selectedTheme = selectedTheme;
        this.clickListener = cardClickListener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        int singleRvCardToUse = R.layout.single_poi_map_card;
        View itemView = LayoutInflater.from(parent.getContext()).inflate(singleRvCardToUse, parent, false);
        return new ViewHolder(itemView);
    }

    public interface ClickListener {
        void onItemClick(int position);
    }

    @Override
    public int getItemCount() {
        return listOfPOIs.size();
    }

    @Override
    public void onBindViewHolder(ViewHolder card, int position) {

        POI locationCard = listOfPOIs.get(position);

        card.nameTextView.setText(locationCard.getName());
        // card.emojiImageView.setImageDrawable(emojiForCircle);
        // card.constraintUpperColorSection.setBackgroundColor(upperCardSectionColor);
        // card.backgroundCircleImageView.setImageDrawable(backgroundCircle);
        // card.nameTextView.setTextColor(poiNameColor);
    }

    static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView nameTextView;
        TextView addressTextView;
        TextView phoneNumTextView;
        TextView hoursTextView;
        TextView distanceNumberTextView;
        TextView hoursHeaderTextView;
        TextView milesAbbreviationTextView;
        TextView phoneHeaderTextView;
        ConstraintLayout constraintUpperColorSection;
        CardView cardView;
        ImageView backgroundCircleImageView;
        ImageView emojiImageView;

        ViewHolder(View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.location_name_tv);
            addressTextView = itemView.findViewById(R.id.location_description_tv);
            phoneNumTextView = itemView.findViewById(R.id.location_phone_num_tv);
            phoneHeaderTextView = itemView.findViewById(R.id.phone_header_tv);
            hoursTextView = itemView.findViewById(R.id.location_hours_tv);
            backgroundCircleImageView = itemView.findViewById(R.id.background_circle);
            emojiImageView = itemView.findViewById(R.id.emoji);
            constraintUpperColorSection = itemView.findViewById(R.id.constraint_upper_color);
            distanceNumberTextView = itemView.findViewById(R.id.distance_num_tv);
            hoursHeaderTextView = itemView.findViewById(R.id.hours_header_tv);
            milesAbbreviationTextView = itemView.findViewById(R.id.miles_mi_tv);
            cardView = itemView.findViewById(R.id.map_view_location_card);
            cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    clickListener.onItemClick(getLayoutPosition());
                }
            });
        }

        @Override
        public void onClick(View view) {
        }
    }
}
