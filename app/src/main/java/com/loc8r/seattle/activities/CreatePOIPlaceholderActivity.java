package com.loc8r.seattle.activities;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.loc8r.seattle.R;
import com.loc8r.seattle.models.POI;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class CreatePOIPlaceholderActivity extends AppCompatActivity {

    private static final String TAG = CreatePOIPlaceholderActivity.class.getSimpleName();
    private POI newPOI;
    private FirebaseFirestore db;

    @BindView(R.id.bn_createPOI) Button bn_CreatePoi;
    @BindView(R.id.et_AddPOI_Index) EditText et_Index;
    @BindView(R.id.et_AddPOI_Name) EditText et_Name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_poi_placeholder);

        // Bind all views at once.
        ButterKnife.bind(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Create our to be saved POI
        newPOI = new POI();

        // Access a Cloud Firestore instance from your Activity
        db = FirebaseFirestore.getInstance();
    }

    @OnClick(R.id.bn_createPOI)
    void onCreatePoiButtonClicked() {
        Log.d(TAG, "onSubmitSuggestionButtonClicked:  Fired");

        // Some basic form validation.  Perf is 227 for 500,000 runs
        if(!"".equals(et_Index.getText().toString()) &&
                !"".equals(et_Name.getText().toString())){
            // Form is correct we can continue
            CreateNewPoi();
            Log.d(TAG, "We can create a POI now");
        } else {
            // The form is not filled out
            Log.d(TAG, "The form is not filled out correctly");
        }
    }

    /**
     *  Creates a POI placeholder in the Firestore DB
     */
    private void CreateNewPoi() {
        newPOI.setName(et_Name.getText().toString());
        newPOI.setRelease(999999); // Releases 999999 are beta POIs that are not yet live on the service
        newPOI.setLatitude(47.66);
        newPOI.setLongitude(-122.333);
        newPOI.setDescription("Lorem ipsum");
        newPOI.setImg_url("www.loc8r.com");
        newPOI.setImgFocalpointX(0D);
        newPOI.setImgFocalpointY(0D);
        newPOI.setCollection("collection");
        newPOI.setCollectionPosition(1);
        newPOI.setStampText("StampText");

        // Needs to come after colPos and collection
        newPOI.setId(et_Index.getText().toString());

        db.collection("pois").document(et_Index.getText().toString())
                .set(newPOI)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "POI:" + newPOI.getId() + " successfully written!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error writing document", e);
                    }
                });
    }

}
