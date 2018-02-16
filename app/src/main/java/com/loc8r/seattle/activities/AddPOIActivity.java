package com.loc8r.seattle.activities;

import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.loc8r.seattle.R;
import com.loc8r.seattle.interfaces.LocationListener;
import com.loc8r.seattle.models.Collection;
import com.loc8r.seattle.models.POI;
import com.loc8r.seattle.utils.CollectionsRequester;
import com.loc8r.seattle.utils.StateManager;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class AddPOIActivity extends GMS_Activity implements
        CollectionsRequester.FireBaseCollectionsResponse,
        AdapterView.OnItemSelectedListener {

    private static final String TAG = AddPOIActivity.class.getSimpleName();
    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int REQUEST_TAKE_PHOTO = 1;

    private Location mCurrentLocation;
    private CollectionsRequester mCollectionRequester;
    private ArrayList<Collection> mCollectionsAll;
    //private POI newPOI;
    private String mCurrentPhotoPath;
    private UploadTask uploadTask;
    private FirebaseFirestore db;

    @BindView(R.id.addpoi_capturePhotoBTN) Button mCapturePhotoBTN;
    @BindView(R.id.addpoi_uploadPhotoBTN) Button mUploadPhotoBTN;
    @BindView(R.id.addpoi_CreatePoiBTN) Button mCreatePoiBTN;
    @BindView(R.id.addpoi_nameET) EditText mPOIName;
    @BindView(R.id.addpoi_positionET) EditText mPOICollectionPosition;
    @BindView(R.id.addpoi_imgUrlET) EditText mPOIImgUrlTV;
    @BindView(R.id.addpoi_camThumbnailIV) ImageView mImageThumbnail;
    @BindView(R.id.addpoi_latTV) TextView mLatitudeTV;
    @BindView(R.id.addpoi_lonTV) TextView mLongitudeTV;
    @BindView(R.id.addpoi_collectionsSPIN) Spinner mCollectionsSpinner;
    @BindView(R.id.toolbar) Toolbar toolbar;

    /**
     *  Click Listeners
     */

    @OnClick(R.id.addpoi_CreatePoiBTN)
    void onCreatePoiButtonClicked() {
        Log.d(TAG, "onCreatePoiButtonClicked:  Fired");

            // Some basic form validation.  Perf is 227 for 500,000 runs
            if(!"".equals(mPOIName.getText().toString()) &&
                    !"".equals(mPOIImgUrlTV.getText().toString())){
                // Form is correct we can continue
                CreateNewPoi();
                Log.d(TAG, "We can create a POI now");
            } else {
                // The form is not filled out
                Log.d(TAG, "The form is not filled out correctly");
            }

    }

//    public POI(String id,
//               String name,
//               int release,
//               Double latitude,
//               Double longitude,
//               String description,
//               String img_url,
//               String collection,
//               int collectionPosition,
//               String stampText



    private void CreateNewPoi() {
        // CollectionReference poisCollectionRef = db.collection("pois");

        String name = mPOIName.getText().toString();
        int release = 999999; // Releases 999999 are beta POIs that are not yet live on the service
        Double lat = mCurrentLocation.getLatitude();
        Double lon = mCurrentLocation.getLongitude();
        String desc = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.";
        String imgurl = mPOIImgUrlTV.getText().toString();
        String col = mCollectionsSpinner.getSelectedItem().toString();
        int colPos = Integer.parseInt(mPOICollectionPosition.getText().toString());
        String stampText = col.substring(0, 2) + "_" + name.substring(0, 2);

        // Needs to come after colPos and collection
        String id = col.substring(0, 2) + String.format("%03d", colPos);

        POI newPOI = new POI(id,
                name,
                release,
                lat,
                lon,
                desc,
                imgurl,
                col,
                colPos,
                stampText);

        db.collection("cities").document(id)
                .set(newPOI)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "DocumentSnapshot successfully written!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error writing document", e);
                    }
                });
    }

    @OnClick(R.id.addpoi_capturePhotoBTN)
    void onCapturePhotoButtonClicked() {
        Log.d(TAG, "Capture button pressed ");
        dispatchTakePictureIntent();
    }

    @OnClick(R.id.addpoi_uploadPhotoBTN)
    void onUploadPhotoButtonClicked() {
        Log.d(TAG, "Upload photo button pressed ");
        uploadPhotoFileToFireStore();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_poi);

        // Bind all views at once.
        ButterKnife.bind(this);

        // Disable this editText as the position is set
        // programmatically depending on the collection selected
        mPOICollectionPosition.setKeyListener(null);

        setSupportActionBar(toolbar);

        // Access a Cloud Firestore instance from your Activity
        db = FirebaseFirestore.getInstance();

        //
        CollectionReference poisCollectionRef = db.collection("pois");

//        mCapturePhotoBTN.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//            }
//        });

        // Not sure.  This enables us to get the data from the imageview so we can resize it to fill the view
        mImageThumbnail.setDrawingCacheEnabled(true);
        mImageThumbnail.buildDrawingCache();

//        //Create upload button
//        mUploadPhotoBTN.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//            }
//        });

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mCollectionRequester = new CollectionsRequester(this);

    }

    /**
     *  Event fires after OnCreate
     *
     */
    @Override
    protected void onStart() {

        if(StateManager.getInstance().getCollections().size()==0){
            try {
                mCollectionRequester.GetAllCollections();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        super.onStart();
    }

    // Per https://stackoverflow.com/questions/12725989/how-to-capture-image-thumbnail-and-save-file-in-a-custom-folder-in-android
    // since we're saving the file we don't unfortunately get the thumbnail data back from the intent, so we have to
    // grab the image from the file itself

    /**
     *  Event listener that fires after an activity has completed.
     *
     * @param requestCode This is the Activity id
     * @param resultCode -1 means the activity completed successfully
     * @param data the return data for the activity
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check to see what Activity this was and the results
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {

            Log.d(TAG, "Picture saved! Woohoo! URI is" + mCurrentPhotoPath);

            //Create a thumbnail
            File imageFile = new File(mCurrentPhotoPath);
            mImageThumbnail.setImageURI(Uri.fromFile(imageFile));

            // uploadCapturedPhoto((Bitmap) extras.get("data"));
        }
    }

    /**
     *  Creates a blank file to save the image as
     *
     * @return Returns a blank file and URI
     * @throws IOException
     */
    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "SPTEMP_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void initializeSpinner(){

        mCollectionsSpinner = findViewById(R.id.addpoi_collectionsSPIN);
        // Initialize the selected event listener
        mCollectionsSpinner.setOnItemSelectedListener(this);
        
        // Add each collection to the spinner arraym
        ArrayList<String> spinnerList = new ArrayList<>();
        for (Collection collectionName: mCollectionsAll) {
            spinnerList.add(collectionName.getName());
        }

        ArrayAdapter<String> collections_adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, spinnerList);

        // Specify the layout to use when the list of choices appears
        collections_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        mCollectionsSpinner.setAdapter(collections_adapter);

    }

    /**
     *  Item Selected Event Listener
     *
     * @param parent
     * @param view
     * @param pos
     * @param id
     */
    public void onItemSelected(AdapterView<?> parent, View view,
                               int pos, long id) {
        // An item was selected. 
        // You can retrieve the selected item using
        // parent.getItemAtPosition(pos)
        Log.d(TAG, "onItemSelected: The Item selected was " + parent.getItemAtPosition(pos));

        db.collection("pois")
                .whereEqualTo("collection", parent.getItemAtPosition(pos))
                .orderBy("collectionPosition", Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            Log.d("STZ", "Got the last POI in the collection, now converting to POI class ");
                            // ArrayList<POI> results = new ArrayList<>();
                            POI lastPOI = new POI();

                            for (DocumentSnapshot document : task.getResult()) {
                                lastPOI = document.toObject(POI.class);
                                lastPOI.setId(document.getId());
//                                results.add(sentPOI);
                                // Log.d(TAG, document.getId() + " => " + document.getData());
                            }

                            // Send results back to host activity
                            // mResponseListener.onPOIsReceived(results);
                            Log.d("STZ", "the next entry in collection [" + lastPOI.getCollection() + "] should be #" + (lastPOI.getCollectionPosition()+1));
                            mPOICollectionPosition.setText(String.valueOf(lastPOI.getCollectionPosition()+1));

                        } else {
                            Log.d(TAG, "Error getting POIs. ", task.getException());
                        }
                    }
                });
        // [END get_multiple_all]


        //mPOICollectionPosition.setText("23");
    }

    /**
     *  Event listener for spinner, if nothing is selected
     * @param parent
     */
    public void onNothingSelected(AdapterView<?> parent) {
        // Another interface callback
        Log.d(TAG, "onNothingSelected: Nothing was selected");
    }

    /**
     *  This kicks off the take picture intent, and sets up the settings for the picture
     */
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
                Log.d(TAG, "Caught IO error exception");
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.loc8r.seattle.fileprovider",
                        photoFile);

                // images are stored in
                // mnt/sdcard/Android/data/com.loc8r.seattle/files/pictures

                takePictureIntent.putExtra("crop", "true");
                takePictureIntent.putExtra("outputX",600);
                takePictureIntent.putExtra("outputY", 600);
                takePictureIntent.putExtra("aspectX", 0);
                takePictureIntent.putExtra("aspectY", 0);
                takePictureIntent.putExtra("scale", true);

                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);

            }
        }
    }

    /**
     *  Method to upload the image to Firestore storage
     */
    private void uploadPhotoFileToFireStore(){

        FirebaseStorage storage = FirebaseStorage.getInstance();

        // Create a storage reference from our app
        StorageReference storageRef = storage.getReference();

        Uri file = Uri.fromFile(new File(mCurrentPhotoPath));
        StorageReference fbSaveLocationRef = storageRef.child("images/"+file.getLastPathSegment());
        uploadTask = fbSaveLocationRef.putFile(file);

        // Register observers to listen for when the download is done or if it fails
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                Uri downloadUrl = taskSnapshot.getDownloadUrl();
                Log.d(TAG, "File uploaded successfully.  URL is: " + downloadUrl);
                mPOIImgUrlTV.setText(downloadUrl.toString());

            }
        });

    }

    // TODO Determine if I need to cancel location update onPause or onStop.
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d(TAG, "onConnected() called with: " + "bundle = [" + bundle + "]");
                /*
        * get the last location of the device
        * */
        getContinousLocationUpdates(new LocationListener()
        {
            @Override
            public void onLocationReceived(Location location)
            {
                mCurrentLocation = location;
                Log.d(TAG, "UI update initiated .............");
                if (null != mCurrentLocation) {
                    mLatitudeTV.setText(String.valueOf(mCurrentLocation.getLatitude()));
                    mLongitudeTV.setText(String.valueOf(mCurrentLocation.getLongitude()));
                    //String dist = String.valueOf(detailedPoi.distanceToUser());
//                    String total = "At Time: " + DateFormat.getTimeInstance().format(new Date()) + "\n" +
//                            "Latitude: " + lat + "\n" +
//                            "Longitude: " + lng + "\n" +
//                            "Accuracy: " + String.valueOf(mCurrentLocation.getAccuracy()) + "\n" +
//                            "Provider: " + String.valueOf(mCurrentLocation.getProvider()) + "\n" +
//                            "Distance to POI: " + dist;
//                    mLocTV.setText(total);
                } else {
                    Log.d(TAG, "location is null ...............");
                }
            }
        });
    }


    @Override public void onCollectionsListReceived(ArrayList<Collection> collections) {
        mCollectionsAll = collections;
        initializeSpinner();

    }

}
