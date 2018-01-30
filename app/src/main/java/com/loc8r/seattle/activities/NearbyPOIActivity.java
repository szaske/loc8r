package com.loc8r.seattle.activities;

import android.app.Dialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.loc8r.seattle.R;
import com.loc8r.seattle.interfaces.QueryListener;
import com.loc8r.seattle.models.POI;
import com.loc8r.seattle.mongodb.MongoDBManager;
import com.loc8r.seattle.utils.ProgressDialog;
import com.loc8r.seattle.utils.StateManager;
import com.loc8r.seattle.interfaces.LocationListener;

import com.paginate.Paginate;
import com.squareup.picasso.Picasso;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class NearbyPOIActivity extends GMS_Activity {

    // Variables
    private static final String TAG = NearbyPOIActivity.class.getSimpleName();
    private boolean mIsProgressDialogShowing = false;
    private String mUsersFirstName;
    private SearchView mSearchView;
    private MenuItem mSearchMenuItem; // ???

    // Recycler Variables
    private RecyclerView mRecyclerView;
    private Paginate mPaginate;
    private String mKeyword;

    // private Location mLastLocation;
    private poiAdapter mAdapter;
    private boolean mIsLoading;
    private POI mFarthestPOI;
    private boolean mIsFinished;
    private static final int PAGE_SIZE = 10;
    private Dialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nearbypois);
        // ButterKnife.bind(this);
        Toolbar myToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);

        //Allow up link to parent
        //getActionBar().setDisplayHomeAsUpEnabled(true);

        initRecyclerView();


        // Test name access in Main
        // TODO Put in check for Anonymous users
        Toast.makeText(getApplicationContext(), getString(R.string.welcome_back_message) + MongoDBManager.getInstance(getApplicationContext()).mUserName, Toast.LENGTH_SHORT).show();

        //Get list of categories
        getListOfCategories();

        //get all allPOIs
        // getAllPOIs();


        Log.d(TAG, "onCreate: We're done");
    }

//    @Override
//    public boolean onPrepareOptionsMenu(Menu menu)
//    {
//        MenuItem mapItem = menu.findItem(R.id.menu_map);
//
//        return super.onPrepareOptionsMenu(menu);
//    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.options_menu, menu);

        SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);

        mSearchMenuItem = menu.findItem(R.id.menu_search);

        /**  onCloseListener doesn't work.
             Workaround as suggested in http://stackoverflow.com/questions/9327826/searchviews-oncloselistener-doesnt-work
         **/
        MenuItemCompat.setOnActionExpandListener(mSearchMenuItem, new MenuItemCompat.OnActionExpandListener()
        {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item)
            {
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item)
            {

                /*
                * clear previous results
                * */
                mAdapter.clear();
                mFarthestPOI = null;
                mKeyword = null;

                getPoisByDistance(true, true);
                return true;
            }
        });
        mSearchView = (SearchView) MenuItemCompat.getActionView(mSearchMenuItem);

        mSearchView.setQueryHint(getString(R.string.search));
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener()
        {
            @Override
            public boolean onQueryTextSubmit(String query)
            {
                //close the keyboard after the search
                closeKeyboard();

                //need a dummy view to request focus so that the search EditText doesn't pop the keyboard back up
                mRecyclerView.requestFocus();

                //clear previous POIs
                mAdapter.clear();
                mFarthestPOI = null;

                mKeyword = query;

                //get list of POIs with search query
                getPoisByDistance(true, true);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText)
            {
                return false;
            }
        });
        mSearchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.menu_filter:
                //showFilterDialog();
                break;
            case R.id.menu_map:
                //openMap();
                break;
            case R.id.menu_log_out:
                showLogoutDialog();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

//    private void showLogoutDialog()
//    {
//        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.DialogTheme);
//        builder.setTitle(R.string.log_out)
//                .setMessage(R.string.log_out_message)
//                .setPositiveButton(R.string.cancel, new DialogInterface.OnClickListener()
//                {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which)
//                    {
//                        dialog.dismiss();
//                    }
//                })
//                .setNegativeButton(R.string.log_out, new DialogInterface.OnClickListener()
//                {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which)
//                    {
//                        dialog.dismiss();
//                        logout();
//                    }
//                }).show();
//    }

    private void logout()
    {
        final Dialog dialog = ProgressDialog.getDialog(this, false);
        dialog.show();
        mIsProgressDialogShowing = true;
        // mAdapter.notifyDataSetChanged();

        //logout from application
        MongoDBManager.getInstance(getApplicationContext())
                .logout(new QueryListener<Void>()
                {
                    @Override
                    public void onSuccess(Void result)
                    {
                        dialog.dismiss();
                        mIsProgressDialogShowing = false;
//                        startActivity(SignInActivity.newIntent(NearbyPOIActivity.this));
//                        finish();
                    }

                    @Override
                    public void onError(Exception e)
                    {
                        Log.e(TAG, "onError: unable to logout", e);
                        dialog.dismiss();
                        mIsProgressDialogShowing = false;
                        Toast.makeText(NearbyPOIActivity.this, "Unable to logout", Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void getPoisByDistance(boolean withProgressDialog, final boolean clearList)
    {
        // Make sure we have a location
        if (StateManager.getInstance().getCurrentLocation() == null)
        {
            Toast.makeText(this, R.string.location_error, Toast.LENGTH_SHORT).show();
            return;
        }

        mProgressDialog = ProgressDialog.getDialog(this, true);
        mProgressDialog.show();

        /*
        Get a list of pois, sorted by the geo location (closest pois go first), filters and query regex (if not null)
        */
        MongoDBManager.getInstance(getApplicationContext()).geoNear(StateManager.getInstance().getCurrentLocation().getLatitude()
            , StateManager.getInstance().getCurrentLocation().getLongitude(), new QueryListener<List<POI>>() {
            @Override
            public void onSuccess(List<POI> pois)
            {

                //Sort the POIs
                Collections.sort(pois, new Comparator<POI>() {
                            @Override public int compare(POI p1, POI p2) {
                                return p1.getDistance() - p2.getDistance();
                            }
                        });
                mAdapter.setData(pois);
                mAdapter.notifyDataSetChanged();
                mProgressDialog.dismiss();
            }

            @Override
            public void onError(Exception e)
            {
                mAdapter.setLoading(false);
                mIsLoading = false;
                mIsFinished = true;
                mAdapter.notifyDataSetChanged();
                Toast.makeText(NearbyPOIActivity.this, R.string.unable_to_get_results, Toast.LENGTH_SHORT).show();
                if (mProgressDialog != null)
                {
                    mProgressDialog.dismiss();
                }
            }
        });
    }

    @Override
    public void onConnected(@Nullable Bundle bundle)
    {
        super.onConnected(bundle);

        //update the location of the device
        updateLocation();
    }

    private void updateLocation()
    {
        /*
        * get the last location of the device
        * */
        getLastLocation(new LocationListener()
        {
            @Override
            public void onLocationReceived(Location location)
            {
                StateManager.getInstance().setCurrentLocation(location);
                if (StateManager.getInstance().getCurrentLocation() != null && mAdapter != null)
                {
                    // This is where the magic happens
                    if (mAdapter.getItemCount()==0){
                        getPoisByDistance(false, true);
                    }
                }
            }
        });
    }

    private void getListOfCategories()
    {
        final Dialog progressDialog = ProgressDialog.getDialog(this, true);
        progressDialog.show();

        // Create a listener
        QueryListener<List<String>> catQueryListener = new QueryListener<List<String>>()
        {
            @Override
            public void onSuccess(List<String> result)
            {
                progressDialog.dismiss();
                Log.e(TAG, "onSuccess: We got ourselves some categories peeps.........." + result.toString());
            }

            @Override
            public void onError(Exception e)
            {
                progressDialog.dismiss();
                Log.e(TAG, "onError: ", e);
            }
        };

        // Call DB singleton to get categories
        MongoDBManager.getInstance(getApplicationContext()).getCategories(catQueryListener);
    }

    private void getAllPOIs()
    {
        final Dialog progressDialog = ProgressDialog.getDialog(this, true);
        progressDialog.show();

        // Create a listener
        QueryListener<List<POI>> poisQueryListener = new QueryListener<List<POI>>()
        {
            @Override
            public void onSuccess(List<POI> result)
            {
                // Currently NOTHING is done with the results, instead we're accessing the POIs from the singleton
                progressDialog.dismiss();
                Log.e(TAG, "onSuccess: We got ourselves some POI peeps..." + DB().getAllPOIs().toString());
            }

            @Override
            public void onError(Exception e)
            {
                progressDialog.dismiss();
                Log.e(TAG, "onError: ", e);
            }
        };

        // Call DB singleton to get categories
        MongoDBManager.getInstance(getApplicationContext()).getPOIs(poisQueryListener);
    }

    /**
     *  Initialize the Recycler View & Adapter
     */
    private void initRecyclerView()
    {
        mRecyclerView = findViewById(R.id.recycler_view);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(this);

        mRecyclerView.setHasFixedSize(false);
        mRecyclerView.setLayoutManager(layoutManager);

        DividerItemDecoration divider = new DividerItemDecoration(this, layoutManager.getOrientation());
        mRecyclerView.addItemDecoration(divider);
        mAdapter = new poiAdapter();
        //mAdapter.setLoading(true);
        mRecyclerView.setAdapter(mAdapter);
    }

    /**
     * RecyclerView adapter to display a list of Seattle POI
     */
    private class poiAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private static final int EMPTY_VIEW = 2;
        boolean mLoading;
        private ArrayList<POI> pois = new ArrayList<>();
        private POIClickListener mClickListener = new POIClickListener();

        poiAdapter()
        {
        }

        void clear()
        {
            pois.clear();
        }

        public void setData(List<POI> dataSet)
        {
            pois.clear();
            pois.addAll(dataSet);
            notifyDataSetChanged();
            //invalidateOptionsMenu();
        }

        void setLoading(boolean loading)
        {
            // Nothing to see here
        }

        ArrayList<POI> getDataSet()
        {
            return pois;
        }

        void addData(List<POI> data)
        {
            pois.addAll(data);
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
        {
            if (viewType == EMPTY_VIEW)
            {
                /* No results. show an empty page */
                // TODO: Add text to blank page explaining that now results were found.
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.empty_view, parent, false);
                return new EmptyViewHolder(view);
            }
            else
            {
                View root = LayoutInflater.from(parent.getContext()).inflate(R.layout.poi_recycler_item, parent, false);
                root.setOnClickListener(mClickListener);
                return new ViewHolder(root);
            }

        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position)
        {
            /**
            Bind the POI to the UI
            **/

            // TODO Determine why we're checking for instance of ViewHolder
            if (holder instanceof ViewHolder)
            {
                // ViewHolder vh = (ViewHolder) holder;
                POI data = pois.get(position);
                ((ViewHolder) holder).bindPoi(data);
            }
        }

        /** In this app if the distance to the POI is bigger than 10 miles, we round the number.
         *  If the distance is smaller than 10 miles, we wanna show the distance with 1 digit after the decimal point
         * (I.e 12.8 miles will be 12 miles,  7.823 will be 7.8 miles).*/
        private String formatDistance(double distanceMeters)
        {
            double miles = metersToMiles(distanceMeters);
            String formatDistance;
            if (miles > 10)
            {
                formatDistance = String.valueOf(Math.round(miles));
            }
            else
            {
                formatDistance = String.format(Locale.ENGLISH, "%.1f", miles);
            }

            return getString(R.string.distance_miles, formatDistance);
        }

        /*
        * Convert meters (unit used by MongoDB) to miles
        * */
        private double metersToMiles(double meters)
        {
            return meters * 0.000621371192;
        }

        @Override
        public int getItemViewType(int position)
        {
            if (pois.size() == 0)
            {
                /*
                * no results view
                * */
                return EMPTY_VIEW;
            }

            return super.getItemViewType(position);
        }

        @Override
        public int getItemCount()
        {
            return pois.size();
        }

        void updatePOI(@NonNull POI poi)
        {
            int index = pois == null ? -1 : pois.indexOf(poi);
            if (index > 0)
            {
                POI oldRest = pois.get(index);
                //TODO Add POI ratings
                // oldRest.setAverageRating(poi.getAverageRating());
                // oldRest.setNumberOfRates(poi.getNumberOfRates());
                notifyDataSetChanged();
            }
        }

        class POIClickListener implements View.OnClickListener
        {

            @Override
            public void onClick(View v)
            {
                int itemPosition = mRecyclerView.getChildLayoutPosition(v);
                POI dataToSend = pois.get(itemPosition);

                Intent i = new Intent(NearbyPOIActivity.this, POIDetailActivity.class);
                Bundle bundle = new Bundle();
                bundle.putParcelable("poi", Parcels.wrap(dataToSend));
                i.putExtras(bundle);
                startActivity(i); // dataToSend is now passed to the new Activity
            }
        }

        class EmptyViewHolder extends RecyclerView.ViewHolder
        {
            EmptyViewHolder(View itemView)
            {
                super(itemView);
            }
        }

        class ViewHolder extends RecyclerView.ViewHolder
        {
            // a list of references to the lifecycle of the object to allow the
            // ViewHolder to hang on to your ImageView and TextView, so it
            // doesn’t have to repeatedly query the same information.
            // This is why we're using a recyclerview
            POI mPoi;
            TextView mName;
            TextView mDistance;
            ImageView mPoiImg;

            ViewHolder(View itemView)
            {
                super(itemView);
                mName = itemView.findViewById(R.id.poiNameTextView);
                mDistance = itemView.findViewById(R.id.poiDistanceTextView);
                mPoiImg = itemView.findViewById(R.id.poiImageView);
            }

            public void bindPoi(POI poi){
                //So now the viewholder has a copy of each POI
                mPoi = poi;
                Picasso
                        .with(mPoiImg.getContext())
                        .load(poi.getImg_url())
                        .into(mPoiImg);
                mName.setText(poi.getName());
                if (StateManager.getInstance().getCurrentLocation() != null)
                {
                    mDistance.setText(formatDistance(poi.getDistance()));
                } else
                {
                    mDistance.setText(R.string.unknown);
                }

            }
        }

    }

    private boolean isSearching()
    {
        /*
        * Simple way to know if we are currently in the 'search' mode
        * */
        return mSearchView != null && !mSearchView.isIconified();
    }

    // Close after searching
    private void closeKeyboard()
    {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mSearchView.getWindowToken(), 0);
    }

    // Name shortener
    private MongoDBManager DB(){
        return MongoDBManager.getInstance(getApplicationContext());
    }
}