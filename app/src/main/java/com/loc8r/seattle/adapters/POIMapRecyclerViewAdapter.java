package com.loc8r.seattle.adapters;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.loc8r.seattle.R;
import com.loc8r.seattle.models.IndividualLocation;
import com.loc8r.seattle.models.POI;

import java.util.List;

/**
 * Created by steve on 1/19/2018.
 */

public class POIMapRecyclerViewAdapter extends
        RecyclerView.Adapter<POIMapRecyclerViewAdapter.ViewHolder> {

    private List<IndividualLocation> listOfPOI;
    private Context context;
    private int selectedTheme;
    private static ClickListener clickListener;
    private Drawable emojiForCircle = null;
    private Drawable backgroundCircle = null;
    private int upperCardSectionColor = 0;

    private int locationNameColor = 0;
    private int locationAddressColor = 0;
    private int locationPhoneNumColor = 0;
    private int locationPhoneHeaderColor = 0;
    private int locationHoursColor = 0;
    private int locationHoursHeaderColor = 0;
    private int locationDistanceNumColor = 0;
    private int milesAbbreviationColor = 0;

    public POIMapRecyclerViewAdapter(List<IndividualLocation> styles,
                                       Context context, ClickListener cardClickListener, int selectedTheme) {
        this.context = context;
        this.listOfPOI = styles;
        this.selectedTheme = selectedTheme;
        this.clickListener = cardClickListener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        int singleRvCardToUse = R.layout.single_poi_map_rv_card;
        View itemView = LayoutInflater.from(parent.getContext()).inflate(singleRvCardToUse, parent, false);
        return new ViewHolder(itemView);
    }

    public interface ClickListener {
        void onItemClick(int position);
    }

    @Override
    public int getItemCount() {
        return listOfPOI.size();
    }

    @Override
    public void onBindViewHolder(ViewHolder card, int position) {

        IndividualLocation locationCard = listOfPOI.get(position);

        card.nameTextView.setText(locationCard.getName());
        card.addressTextView.setText(locationCard.getAddress());
        card.phoneNumTextView.setText(locationCard.getPhoneNum());
        card.hoursTextView.setText(locationCard.getHours());
        card.distanceNumberTextView.setText(locationCard.getDistance());

        // Set Theme items, adjusted for SP color scheme
        emojiForCircle = ResourcesCompat.getDrawable(context.getResources(), R.drawable.money_bag_icon, null);
        card.emojiImageView.setPadding(8, 0, 0, 0);
        backgroundCircle = ResourcesCompat.getDrawable(context.getResources(), R.drawable.green_circle, null);
        setColors(R.color.colorPrimaryDark_green, R.color.white, R.color.white, R.color.cardHourAndPhoneTextColor_green,
                R.color.black, R.color.cardHourAndPhoneTextColor_green,
                R.color.black, R.color.white, R.color.white);
        setAlphas(card, 100f, .48f, 100f, .48f,
                100f,
                100f);

        card.emojiImageView.setImageDrawable(emojiForCircle);
        card.constraintUpperColorSection.setBackgroundColor(upperCardSectionColor);
        card.backgroundCircleImageView.setImageDrawable(backgroundCircle);
        card.nameTextView.setTextColor(locationNameColor);
        card.phoneNumTextView.setTextColor(locationPhoneNumColor);
        card.hoursTextView.setTextColor(locationHoursColor);
        card.hoursHeaderTextView.setTextColor(locationHoursHeaderColor);
        card.distanceNumberTextView.setTextColor(locationDistanceNumColor);
        card.milesAbbreviationTextView.setTextColor(milesAbbreviationColor);
        card.addressTextView.setTextColor(locationAddressColor);
        card.phoneHeaderTextView.setTextColor(locationPhoneHeaderColor);
    }

    private void setColors(int colorForUpperCard, int colorForName, int colorForAddress,
                           int colorForHours, int colorForHoursHeader, int colorForPhoneNum,
                           int colorForPhoneHeader, int colorForDistanceNum, int colorForMilesAbbreviation) {
        upperCardSectionColor = ResourcesCompat.getColor(context.getResources(), colorForUpperCard, null);
        locationNameColor = ResourcesCompat.getColor(context.getResources(), colorForName, null);
        locationAddressColor = ResourcesCompat.getColor(context.getResources(), colorForAddress, null);
        locationHoursColor = ResourcesCompat.getColor(context.getResources(), colorForHours, null);
        locationHoursHeaderColor = ResourcesCompat.getColor(context.getResources(), colorForHoursHeader, null);
        locationPhoneNumColor = ResourcesCompat.getColor(context.getResources(), colorForPhoneNum, null);
        locationPhoneHeaderColor = ResourcesCompat.getColor(context.getResources(), colorForPhoneHeader, null);
        locationDistanceNumColor = ResourcesCompat.getColor(context.getResources(), colorForDistanceNum, null);
        milesAbbreviationColor = ResourcesCompat.getColor(context.getResources(), colorForMilesAbbreviation, null);
    }

    private void setAlphas(ViewHolder card, float addressAlpha, float hoursHeaderAlpha, float hoursNumAlpha,
                           float phoneHeaderAlpha, float phoneNumAlpha, float milesAbbreviationAlpha) {
        card.addressTextView.setAlpha(addressAlpha);
        card.hoursHeaderTextView.setAlpha(hoursHeaderAlpha);
        card.hoursTextView.setAlpha(hoursNumAlpha);
        card.phoneHeaderTextView.setAlpha(phoneHeaderAlpha);
        card.phoneNumTextView.setAlpha(phoneNumAlpha);
        card.milesAbbreviationTextView.setAlpha(milesAbbreviationAlpha);
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
