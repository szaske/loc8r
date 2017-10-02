package com.loc8r.android.loc8r.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;

import com.loc8r.android.loc8r.R;
import com.loc8r.android.loc8r.tests.PinOnMap;
import com.loc8r.android.loc8r.tests.mapFeatureCollection;
import com.loc8r.android.loc8r.tests.mapPlusRecycler;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    // My variables
    //
    @BindView(R.id.pinOnMapButton) Button mPinOnMapButton;
    @BindView(R.id.mapCollectionButton) Button mMapCollectionButton;

    // OnClick Listeners
    //
    @OnClick(R.id.pinOnMapButton)
    public void testPinOnMap(){
        Intent pinTest = new Intent(this, PinOnMap.class);
        startActivity(pinTest);
    }

    @OnClick(R.id.mapCollectionButton)
    public void testMapCollection(){
        Intent mapCollection = new Intent(this, mapFeatureCollection.class);
        startActivity(mapCollection);
    }

    @OnClick(R.id.withRecyclerButton)
    public void testWithRecycler(){
        Intent withRecycler = new Intent(this, mapPlusRecycler.class);
        startActivity(withRecycler);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
    }

}