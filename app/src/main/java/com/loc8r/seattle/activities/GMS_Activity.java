package com.loc8r.seattle.activities;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.loc8r.seattle.R;
import com.loc8r.seattle.interfaces.LocationListener;
import com.loc8r.seattle.models.POI;
import com.loc8r.seattle.utils.Constants;
import com.loc8r.seattle.utils.POIsRequester;

import java.util.ArrayList;

public class GMS_Activity extends BaseActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private static final int LOCATION_REQUEST_CODE = 420;
    private static final String TAG = GMS_Activity.class.getSimpleName();

    private GoogleApiClient mGoogleApiClient;
    private AlertDialog mLocationEnabledDialog;
    private com.google.android.gms.location.LocationListener mPoiLocationListener;

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d(TAG, "onConnected() called with: " + "bundle = [" + bundle + "]");
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "onConnectionSuspended() called with: " + "i = [" + i + "]");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e(TAG, "onConnectionFailed: " + connectionResult.getErrorMessage());
    }

    protected void onLocationPermissionGranted() {
        Log.d(TAG, "onLocationPermissionGranted: ");
    }

    private void initGoogleLocationServices() {
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
        Log.d(TAG, "initGoogleLocationServices: ");
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initGoogleLocationServices();
    }

    @Override
    protected void onStart() {
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
        super.onStart();
    }

    @Override
    protected void onStop() {
        if (mGoogleApiClient != null) {
            mGoogleApiClient.disconnect();
        }
        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkLocationPermission();
    }

    protected void getContinousLocationUpdates(@NonNull final LocationListener listener) {

        long INTERVAL = 1000 * 10;
        long FASTEST_INTERVAL = 1000 * 5;
        // Location mCurrentLocation = null;


        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        mPoiLocationListener = new com.google.android.gms.location.LocationListener() {
            @Override public void onLocationChanged(Location location) {
                listener.onLocationReceived(location); // Send the location to the Activity
            }
        };


        PendingResult<Status> pendingResult = LocationServices
                .FusedLocationApi
                .requestLocationUpdates(mGoogleApiClient, mLocationRequest, mPoiLocationListener);
        Log.d(TAG, "STZ Location update started ..............: ");
    }

    protected void cancelContinousLocationUpdates(){
        Log.d(TAG, "STZ Location updates canceled");
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, mPoiLocationListener);
    }

    protected void getLastLocation(@NonNull LocationListener listener)
    {
        Location location = null;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
        {
            location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        }

        if (location == null)
        {
            startLocationUpdates(listener);
        }
        else
        {
            listener.onLocationReceived(location);
        }
    }

    protected void startLocationUpdates(@NonNull final LocationListener listener)
    {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
        {

            if (!mGoogleApiClient.isConnected())
            {
                mGoogleApiClient.connect();
                return;
            }

            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, new LocationRequest().setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                    , new LocationCallback()
                    {
                        @Override
                        public void onLocationResult(LocationResult locationResult)
                        {
                            super.onLocationResult(locationResult);
                            Location lastLocation = locationResult.getLastLocation();
                            if (lastLocation != null)
                            {
                                // LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
                                listener.onLocationReceived(lastLocation);
                            }
                            Log.d(TAG, "onLocationResult: You're location is" + String.valueOf(locationResult));
                        }
                    }, getMainLooper());
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        if (requestCode == LOCATION_REQUEST_CODE)
        {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                //permission granted
                if (!isLocationEnabled())
                {
                    showLocationEnabledDialog();
                }
                else
                {
                    onLocationPermissionGranted();
                }
            }
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void showLocationPermissionDialog()
    {
        Log.d(TAG, "showLocationPermissionDialog: ");
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.DialogTheme);
        builder.setTitle(R.string.location_permission_dialog_title)
                .setMessage(R.string.location_permission_dialog_msg)
                .setCancelable(false)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        dialog.dismiss();
                        ActivityCompat.requestPermissions(GMS_Activity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION_REQUEST_CODE);
                    }
                })
                .setNegativeButton(R.string.exit, new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        dialog.dismiss();
                        finish();
                    }
                })
                .setNeutralButton(R.string.location_permission_settings, new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        dialog.dismiss();
                        openAppPermissionsSettings();
                    }
                })
                .show();
    }

    public void showLocationEnabledDialog()
    {
        if (mLocationEnabledDialog != null && mLocationEnabledDialog.isShowing())
        {
            return;
        }

        Log.d(TAG, "showLocationEnabledDialog: ");
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.DialogTheme);
        builder.setTitle(R.string.location_enabled_dialog_title)
                .setMessage(R.string.location_enabled_dialog_msg)
                .setCancelable(false)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        dialog.dismiss();
                        Intent locationIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(locationIntent);
                    }
                })
                .setNegativeButton(R.string.exit, new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        dialog.dismiss();
                        finish();
                    }
                });

        mLocationEnabledDialog = builder.create();
        mLocationEnabledDialog.show();

    }

    /**
     *  Show a dialog box to ensure the user really wants to logout
     *
     * @param context
     */
    protected void showLogoutDialog(final Context context)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.DialogTheme);
        builder.setTitle(R.string.log_out)
                .setMessage(R.string.log_out_message)
                .setPositiveButton(R.string.cancel, new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        dialog.dismiss();
                    }
                })
                .setNegativeButton(R.string.log_out, new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        dialog.dismiss();
                        // Logout here
                        logout(context);
                    }
                }).show();
    }

    /**
     *  Logout of Firebase Authentication
     *
     * @param context The current context
     */
    private void logout(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(Constants.PREFERENCES_PREVIOUS_USER_KEY, FirebaseAuth
                .getInstance()
                .getCurrentUser()
                .getEmail()).apply();

        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(context, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void checkLocationPermission()
    {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            showLocationPermissionDialog();
        }
        else
        {
            //permission granted - check if location is turned on
            if (!isLocationEnabled())
            {
                showLocationEnabledDialog();
            }
            else
            {
                onLocationPermissionGranted();
            }
        }
    }

    @SuppressWarnings("deprecation")
    private boolean isLocationEnabled()
    {
        int locationMode;
        String locationProviders;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
        {
            try
            {
                locationMode = Settings.Secure.getInt(getContentResolver(), Settings.Secure.LOCATION_MODE);

            }
            catch (Settings.SettingNotFoundException e)
            {
                e.printStackTrace();
                return false;
            }

            return locationMode != Settings.Secure.LOCATION_MODE_OFF;

        }
        else
        {
            locationProviders = Settings.Secure.getString(getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
            return !TextUtils.isEmpty(locationProviders);
        }
    }

    private void openAppPermissionsSettings()
    {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        startActivity(intent);

    }

    /**
     *  Helper methods to shorten Firebase singleton calls
     *
     * @return List of All current POIs
     */
    public ArrayList<POI> listOfAllPOIs(){
        ArrayList<POI> tempPOIList = new ArrayList<POI>();

        tempPOIList.add(new POI("1",
                "Dick's",
                CreateLocation(47.661116, -122.327877),
                "Since 1954, original of a chain dishing out burgers, fries & hand-dipped shakes (open late).",
                "https://www.zaske.com/loc8r/seattle_medal.jpg",
                "Street Art",
                "StampIS1",
                "Stamp Text 1"));
        tempPOIList.add(new POI("2","Sea Monster Lounge",CreateLocation(47.661542, -122.332299),"We feature the very best in Seattle live funk, soul and jazz music, blues/electronic, etc. Tuesday through Sunday.  and we have a covered outdoor patio! it's also a famous spot for musicians getting off gigs elsewhere and wanting to drop in late for a jam session (and some liquor) and you will know why the moment you walk in. Charlie Hunter, Macklemore, Alan Stone, Roy Hargrove, Blake Lewis, Kevin Sawka, etc... 10 NW beers on tap, house liquor infusions, snacks, games, fun!", "https://www.zaske.com/loc8r/seattle_needle.jpg", "street art", "StampIS2","Stamp Text 2"));
        tempPOIList.add(new POI("3",
                "Library",
                CreateLocation(47.661173, -122.338994),
                "The Wilmot Memorial Library opened Sept. 10, 1949, in a house donated by Alice Wilmot Dennis in memory of her sister, Florence Wilmot Metcalf. The branch moved to remodeled quarters in a former police and fire station in 1985 and was renamed the Wallingford-Wilmot Library.\n" +
                        "\n" +
                        "The branch shared its new space with the 45th Street Clinic, which eventually expanded and asked the library to relocate.",
                "https://www.zaske.com/loc8r/seattle_library.jpg",
                "Historical Photos",
                "StampIS3",
                "Stamp Text 3"));
        tempPOIList.add(new POI("4",
                "Portage Bay Cafe",
                CreateLocation(47.657846, -122.317634),
                "A tasty place to go for an all-American, local/organic/sustainable-focused breakfast or brunch, including cage-free eggs from Olympia, hams and sausages made in-house with Carlton Farms pork, all-organic and local-when-possible veggies, and so forth. Reservations are highly recommended on the weekends.",
                "https://www.zaske.com/loc8r/seattle_skyline1.jpg",
                "Movie Locations",
                "StampIS4",
                "Stamp Text 4"));
        tempPOIList.add(new POI("5",
                "University Barbershop",
                CreateLocation(47.658945, -122.313323),
                "A great neighborhood barbershop, serving 'The Ave' for over 75 years",
                "https://www.zaske.com/loc8r/seattle_market.jpg",
                "Street Art",
                "StampIS5",
                "Stamp Text 5"));
        tempPOIList.add(new POI("6",
                "Burke Museum",
                CreateLocation(47.660704, -122.310510),
                "The Burke Museum of Natural History and Culture (Burke Museum) is a natural history museum in Seattle, Washington, in the United States. Established in 1899 as the Washington State Museum, it traces its origins to a high school naturalist club formed in 1879. The museum is the oldest natural history museum west of the Mississippi River and boasts a collection of more than 16 million artifacts, including the world's largest collection of spread bird wings. Located on the campus of the University of Washington, the Burke Museum is the official state museum of Washington.",
                "https://www.zaske.com/loc8r/seattle_library.jpg",
                "street art",
                "StampIS6",
                "Stamp Text 6"));

        // Store our POIs in the public variable
         return tempPOIList;
    }

    // Create a location given a Lat & Long
    public Location CreateLocation(Double latitude, Double longitude){
        Location tempLocation = new Location("");
        tempLocation.setLongitude(longitude);
        tempLocation.setLatitude(latitude);
        return tempLocation;
    }
}

