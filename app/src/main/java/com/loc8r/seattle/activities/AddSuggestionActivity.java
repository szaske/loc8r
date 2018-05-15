package com.loc8r.seattle.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
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
import com.loc8r.seattle.models.Suggestion;
import com.loc8r.seattle.utils.StateManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class AddSuggestionActivity extends LocationBase_Activity {

    private static final String TAG = AddSuggestionActivity.class.getSimpleName();
    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int REQUEST_TAKE_PHOTO = 1;

    private Location mCurrentLocation;
    private Suggestion newSuggestion;
    private boolean photoWasTaken = false;
    private String mPhotoFileName = "none";
    private String mCurrentPhotoPath = "loc8r.com";
    private UploadTask uploadTask;
    private FirebaseFirestore db;

    @BindView(R.id.submit_nameET) EditText mSuggestionNameET;
    @BindView(R.id.submit_WhereET) EditText mLocationET;
    @BindView(R.id.submit_descET) EditText mDescriptionET;
    @BindView(R.id.submit_AttachPhotoBTN) Button mCapturePhotoBTN;
    @BindView(R.id.submit_SubmitSuggestionBTN) Button mSubmitSuggestionBTN;
    @BindView(R.id.submit_camThumbnailIV) ImageView mImageThumbnail;
    @BindView(R.id.toolbar) Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_submit_suggestion);

        // Bind all views at once.
        ButterKnife.bind(this);

        //Create our Suggestion to be saved
        // newPOI = new POI();
        newSuggestion = new Suggestion();

        // Disable this editText as the position is set
        // programmatically depending on the collection selected
        // mPOICollectionPositionET.setKeyListener(null);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Access a Cloud Firestore instance from your Activity
        db = FirebaseFirestore.getInstance();

        // Not sure.  This enables us to get the data from the imageview so we can resize it to fill the view
        mImageThumbnail.setDrawingCacheEnabled(true);
        mImageThumbnail.buildDrawingCache();

        // If this is not the first time, grab state of the various views
        if (savedInstanceState != null) {
            //Reset spinner to previous location
            //mCollectionsSpinner.setSelection(savedInstanceState.getInt("mCollectionsSpinner"));

            // reset thumbnail
            if(savedInstanceState.getString("mCurrentPhotoPath")!=null){
                mCurrentPhotoPath = savedInstanceState.getString("mCurrentPhotoPath");
                File imageFile = new File(mCurrentPhotoPath);
                mImageThumbnail.setImageURI(Uri.fromFile(imageFile));
            }
        }
    }

//    public void GetAllCollections() throws IOException {
//        Log.d("STZ", "GetAllCollections method started ");
//        db.collection("collections")
//                .get()
//                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
//                    @Override
//                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
//                        if (task.isSuccessful()) {
//                            Log.d("STZ", "Getting Collections task completed successfully, now converting to POI class ");
//                            ArrayList<Collection> results = new ArrayList<>();
//                            for (DocumentSnapshot document : task.getResult()) {
//                                Collection receivedCollection = document.toObject(Collection.class);
//                                results.add(receivedCollection);
//                                // Log.d(TAG, document.getId() + " => " + document.getData());
//                            }
//
//                            // Send results back to host activity
//                            onCollectionsListReceived(results);
//                            Log.d("STZ", "onComplete: All Collections received ");
//                        } else {
//                            Log.d(TAG, "Error getting POIs. ", task.getException());
//                        }
//                    }
//                });
//        // [END get_multiple_all]
//    }


    /**
     *  Click Listeners
     */

    @OnClick(R.id.submit_SubmitSuggestionBTN)
    void onSubmitSuggestionButtonClicked() {
        Log.d(TAG, "onSubmitSuggestionButtonClicked:  Fired");

            // Some basic form validation.  Perf is 227 for 500,000 runs
            if(!"".equals(mSuggestionNameET.getText().toString()) &&
                    !"".equals(mLocationET.getText().toString()) && !"".equals(mDescriptionET.getText().toString()) ){
                // Form is correct we can continue
                Log.d(TAG, "We can create a POI now");

                //Photo is optional, so lets check for it first
                if(photoWasTaken){
                    uploadPhotoFileToFireStore();
                } else {
                    newSuggestion.setImg_url("none");
                    SubmitSuggestionToDataBase();
                }

            } else {
                // The form is not filled out
                Log.d(TAG, "The form is not filled out correctly");
            }

    }

    @OnClick(R.id.submit_AttachPhotoBTN)
    void onCapturePhotoButtonClicked() {
        Log.d(TAG, "Capture button pressed ");
        dispatchTakePictureIntent();
    }

    /**
     *  Creates and Saves a Suggestion to Firestore
     */
    private void SubmitSuggestionToDataBase() {

        newSuggestion.setName(mSuggestionNameET.getText().toString());
        newSuggestion.setLocation(mLocationET.getText().toString());
        newSuggestion.setDescription(mDescriptionET.getText().toString());

        db.collection("suggestions")
                .add(newSuggestion)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d(TAG, "DocumentSnapshot written with ID: " + documentReference.getId());

                        //no let's return to the main menu
                        Intent intent = new Intent(AddSuggestionActivity.this, MainListActivity.class);
                        startActivity(intent);
                        overridePendingTransition(R.anim.slide_in_from_bottom, R.anim.slide_out_to_top);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error adding document", e);
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

            photoWasTaken = true; //marked that a photo was taken, since it's optional
            Log.d(TAG, "Picture saved! Woohoo! URI is" + mCurrentPhotoPath);

            /** now let's resize it to make it smaller
             *
             */

            // Find the storage folder
            File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

            Bitmap b= BitmapFactory.decodeFile(mCurrentPhotoPath);
            Bitmap out = Bitmap.createScaledBitmap(b, 320, 480, false);

            File file = new File(storageDir, "small_" + mPhotoFileName);
            FileOutputStream fOut;
            try {
                fOut = new FileOutputStream(file);
                out.compress(Bitmap.CompressFormat.JPEG, 70, fOut);
                fOut.flush();
                fOut.close();
                b.recycle();
                out.recycle();
            } catch (Exception e) {}

            // Finally let's reset the pointer variable to our new smaller version
            mCurrentPhotoPath = file.getAbsolutePath();

            //And create a thumbnail for use in the UI
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
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            imageFileName = "SUGG_" + timeStamp;

        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        mPhotoFileName = image.getName();
        return image;
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
        StorageReference fbSaveLocationRef = storageRef.child("suggestions/"+file.getLastPathSegment());
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

                // save the URL to our suggestion
                newSuggestion.setImg_url(String.valueOf(downloadUrl));

                //Save the suggestion to Firebase
                SubmitSuggestionToDataBase();

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
                    mLocationET.setText(String.valueOf(mCurrentLocation.getLatitude() + "," + String.valueOf(mCurrentLocation.getLongitude())));
                    cancelContinousLocationUpdates(); // just get location once, then stop to save the users battery
                } else {
                    Log.d(TAG, "location is null ...............");
                }
            }
        });
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        // Save UI state changes to the savedInstanceState.
        // This bundle will be passed to onCreate if the process is
        // killed and restarted.

        //savedInstanceState.putBoolean("MyBoolean", true);
        //savedInstanceState.putParcelable("mCollectionsAll", Parcels.wrap(mCollectionsAll));
        if(mCurrentPhotoPath!=null){
            savedInstanceState.putString("mCurrentPhotoPath", mCurrentPhotoPath);
        }
    }

}
