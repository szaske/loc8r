package com.loc8r.seattle.activities.base;

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
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.loc8r.seattle.R;
import com.loc8r.seattle.activities.LoginActivity;
import com.loc8r.seattle.interfaces.LocationListener;
import com.loc8r.seattle.utils.CollectionsRequester;
import com.loc8r.seattle.utils.Constants;

public class LocationBase_Activity extends FirebaseBaseActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener
{
    private static final int LOCATION_REQUEST_CODE = 420;
    private static final String TAG = LocationBase_Activity.class.getSimpleName();

    private boolean locationNeeded = true;
    AlertDialog.Builder locationPermissionsDialog;

    private CollectionsRequester mCollectionRequester;
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
        initGoogleLocationServices();
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onStart() {
        if (mGoogleApiClient != null && locationNeeded) {
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

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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

    protected void setLocationNeeded(boolean bol){
        this.locationNeeded = bol;
    }

    protected void cancelContinousLocationUpdates(){
        Log.d(TAG, "STZ Location updates canceled");
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, mPoiLocationListener);
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
        // ensures we only get one dialog at a time
        if(locationPermissionsDialog==null){
            Log.d(TAG, "showLocationPermissionDialog: ");
            locationPermissionsDialog = new AlertDialog.Builder(this, R.style.DialogTheme);
            locationPermissionsDialog.setTitle(R.string.location_permission_dialog_title)
                    .setMessage(R.string.location_permission_dialog_msg)
                    .setCancelable(false)
                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {

                            dialog.dismiss();
                            ActivityCompat.requestPermissions(LocationBase_Activity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
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

        // Check for location permissions
//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
//        {
//            showLocationPermissionDialog();
//        }


        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
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


}

