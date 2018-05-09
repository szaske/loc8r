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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.loc8r.seattle.R;
import com.loc8r.seattle.activities.base.LocationBase_Activity;
import com.loc8r.seattle.interfaces.LocationListener;
import com.loc8r.seattle.models.Collection;
import com.loc8r.seattle.models.POI;
import com.loc8r.seattle.utils.CollectionsRequester;
import com.loc8r.seattle.utils.StateManager;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class AddPOIActivity extends LocationBase_Activity implements
        AdapterView.OnItemSelectedListener {

    private static final String TAG = AddPOIActivity.class.getSimpleName();
    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int REQUEST_TAKE_PHOTO = 1;

    private Location mCurrentLocation;
    // private CollectionsRequester mCollectionRequester;
    private ArrayList<Collection> mCollectionsAll;
    private POI newPOI;
    private String mCurrentPhotoPath;
    private UploadTask uploadTask;
    private FirebaseFirestore db;

    @BindView(R.id.addpoi_capturePhotoBTN) Button mCapturePhotoBTN;
    @BindView(R.id.addpoi_uploadPhotoBTN) Button mUploadPhotoBTN;
    @BindView(R.id.addpoi_CreatePoiBTN) Button mCreatePoiBTN;
    @BindView(R.id.addpoi_idTV) TextView mPOIidTV;
    @BindView(R.id.addpoi_nameET) EditText mPOINameET;
    @BindView(R.id.addpoi_positionET) EditText mPOICollectionPositionET;
    @BindView(R.id.addpoi_imgUrlET) EditText mPOIImgUrlET;
    @BindView(R.id.addpoi_camThumbnailIV) ImageView mImageThumbnail;
    @BindView(R.id.addpoi_latTV) TextView mLatitudeTV;
    @BindView(R.id.addpoi_lonTV) TextView mLongitudeTV;
    @BindView(R.id.addpoi_collectionsSPIN) Spinner mCollectionsSpinner;
    @BindView(R.id.toolbar) Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_poi);

        // Bind all views at once.
        ButterKnife.bind(this);

        //Create our to be saved POI
        newPOI = new POI();

        // Disable this editText as the position is set
        // programmatically depending on the collection selected
        mPOICollectionPositionET.setKeyListener(null);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Access a Cloud Firestore instance from your Activity
        db = FirebaseFirestore.getInstance();

        // Not sure.  This enables us to get the data from the imageview so we can resize it to fill the view
        mImageThumbnail.setDrawingCacheEnabled(true);
        mImageThumbnail.buildDrawingCache();

        // mCollectionRequester = new CollectionsRequester(this);

        // Get collection list if we don't have it already
            try {
                GetAllCollections();
            } catch (IOException e) {
                e.printStackTrace();
            }

        // If this is not the first time, grab state of the various views
        if (savedInstanceState != null) {
            //Reset spinner to previous location
            mCollectionsSpinner.setSelection(savedInstanceState.getInt("mCollectionsSpinner"));

            // reset thumbnail
            if(savedInstanceState.getString("mCurrentPhotoPath")!=null){
                mCurrentPhotoPath = savedInstanceState.getString("mCurrentPhotoPath");
                File imageFile = new File(mCurrentPhotoPath);
                mImageThumbnail.setImageURI(Uri.fromFile(imageFile));
            }
        }
    }

    public void GetAllCollections() throws IOException {
        Log.d("STZ", "GetAllCollections method started ");
        db.collection("collections")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            Log.d("STZ", "Getting Collections task completed successfully, now converting to POI class ");
                            ArrayList<Collection> results = new ArrayList<>();
                            for (DocumentSnapshot document : task.getResult()) {
                                Collection receivedCollection = document.toObject(Collection.class);
                                results.add(receivedCollection);
                                // Log.d(TAG, document.getId() + " => " + document.getData());
                            }

                            // Send results back to host activity
                            onCollectionsListReceived(results);
                            Log.d("STZ", "onComplete: All Collections received ");
                        } else {
                            Log.d(TAG, "Error getting POIs. ", task.getException());
                        }
                    }
                });
        // [END get_multiple_all]
    }


    /**
     *  Click Listeners
     */

    @OnClick(R.id.addpoi_CreatePoiBTN)
    void onCreatePoiButtonClicked() {
        Log.d(TAG, "onCreatePoiButtonClicked:  Fired");

            // Some basic form validation.  Perf is 227 for 500,000 runs
            if(!"".equals(mPOINameET.getText().toString()) &&
                    !"".equals(mPOIImgUrlET.getText().toString())){
                // Form is correct we can continue
                CreateNewPoi();
                Log.d(TAG, "We can create a POI now");
            } else {
                // The form is not filled out
                Log.d(TAG, "The form is not filled out correctly");
            }

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

    /**
     *  Creates and Saves a POI from UI to Firestore
     */
    private void CreateNewPoi() {
        newPOI.setName(mPOINameET.getText().toString());
        newPOI.setRelease(999999); // Releases 999999 are beta POIs that are not yet live on the service
        newPOI.setLatitude(mCurrentLocation.getLatitude());
        newPOI.setLongitude(mCurrentLocation.getLongitude());
        newPOI.setDescription("Lorem ipsum dolor sit amet, consectetur adipiscing elit");
        newPOI.setImg_url(mPOIImgUrlET.getText().toString());
        newPOI.setImgFocalpointX(0.0D);
        newPOI.setImgFocalpointY(0.0D);
        newPOI.setCollection(mCollectionsSpinner.getSelectedItem().toString());
        newPOI.setCollectionPosition(Integer.parseInt(mPOICollectionPositionET.getText().toString()));
        newPOI.setStampText(mCollectionsSpinner.getSelectedItem().toString().substring(0, 3) + "_" + mPOINameET.getText().toString().substring(0, 3));

        // Needs to come after colPos and collection
        newPOI.setId(mCollectionsSpinner.getSelectedItem().toString().substring(0, 3) + String.format("%03d", Integer.parseInt(mPOICollectionPositionET.getText().toString())));

        db.collection("pois").document(newPOI.getId())
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
        }
    }

    /**
     *  Creates a blank file to save the image as
     *
     * @return Returns a blank file and URI
     * @throws IOException
     */
    private File createImageFile() throws IOException {

        // Find the storage folder
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        //Delete any previous pictures
        // File dir = new File(Environment.getExternalStorageDirectory()+"Dir_name_here");
        if (storageDir.isDirectory())
        {
            String[] children = storageDir.list();
            for (int i = 0; i < children.length; i++)
            {
                new File(storageDir, children[i]).delete();
            }
        }

        // Create an image file name
        String imageFileName = "";

        // Give it a name
        if(!mPOIidTV.getText().toString().equals("")){
            imageFileName = mPOIidTV.getText().toString();
        } else {
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            imageFileName = "SPTEMP_" + timeStamp;
        }
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

        // See https://stackoverflow.com/questions/4234985/how-to-for-each-the-hashmap
        for(Map.Entry<String, Collection> entry : StateManager.getInstance().getCollections().entrySet()) {
            Collection collection = entry.getValue();
            spinnerList.add(collection.getName());
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
    public void onItemSelected(final AdapterView<?> parent, View view,
                               final int pos, long id) {
        // An item was selected. 
        // You can retrieve the selected item using
        // parent.getItemAtPosition(pos)
        Log.d(TAG, "onItemSelected: The Item selected was " + parent.getItemAtPosition(pos));

        final String collect = parent.getItemAtPosition(pos).toString();

        db.collection("pois")
                .whereEqualTo("collection", collect)
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
                            }

                            // Send results back to host activity
                            // mResponseListener.onPOIsReceived(results);
                            Log.d("STZ", "the next entry in collection [" + lastPOI.getCollection() + "] should be #" + (lastPOI.getCollectionPosition()+1));
                            mPOICollectionPositionET.setText(String.valueOf(lastPOI.getCollectionPosition()+1));

                            // Set ID shown as well
                            mPOIidTV.setText(collect.substring(0,3).toUpperCase()+String.format("%03d", lastPOI.getCollectionPosition()+1));

                        } else {
                            Log.d(TAG, "Error getting POIs. ", task.getException());
                        }
                    }
                });
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
                mPOIImgUrlET.setText(downloadUrl.toString());

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


    public void onCollectionsListReceived(ArrayList<Collection> collections) {
        mCollectionsAll = collections;
        initializeSpinner();

    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        // Save UI state changes to the savedInstanceState.
        // This bundle will be passed to onCreate if the process is
        // killed and restarted.

        //savedInstanceState.putBoolean("MyBoolean", true);
        //savedInstanceState.putParcelable("mCollectionsAll", Parcels.wrap(mCollectionsAll));
        savedInstanceState.putInt("mCollectionsSpinner", mCollectionsSpinner.getSelectedItemPosition());
        if(mCurrentPhotoPath!=null){
            savedInstanceState.putString("mCurrentPhotoPath", mCurrentPhotoPath);
        }

    }

}
