package com.loc8r.seattle.activities;

import android.app.Dialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AlertDialog;
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
import android.widget.TextView;
import android.widget.Toast;

import com.loc8r.seattle.R;
import com.loc8r.seattle.interfaces.QueryListener;
import com.loc8r.seattle.models.POI;
import com.loc8r.seattle.mongodb.MongoDBManager;
import com.loc8r.seattle.utils.ProgressDialog;
import com.loc8r.seattle.interfaces.LocationListener;

import com.paginate.Paginate;


import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.ButterKnife;

import static android.nfc.tech.MifareUltralight.PAGE_SIZE;

public class MainActivity extends LoggedInActivity {

    // Variables
    private static final String TAG = MainActivity.class.getSimpleName();
    private boolean mIsProgressDialogShowing = false;
    private String mUsersFirstName;
    private SearchView mSearchView;
    private MenuItem mSearchMenuItem; // ???

    // Recycler Variables
    private RecyclerView mRecyclerView;
    private Paginate mPaginate;
    private String mKeyword;
    private Location mLastLocation;
    private poiAdapter mAdapter;
    private boolean mIsLoading;
    private POI mFarthestPOI;
    private boolean mIsFinished;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // ButterKnife.bind(this);
        Toolbar myToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);
        initRecyclerView();


        // Test name access in Main
        // TODO Put in check for Anonymous users
        Toast.makeText(getApplicationContext(), getString(R.string.welcome_back_message) + MongoDBManager.getInstance(getApplicationContext()).mUserName, Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu)
    {
        MenuItem mapItem = menu.findItem(R.id.menu_map);

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.options_menu, menu);

        SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);

        mSearchMenuItem = menu.findItem(R.id.menu_search);

        /* onCloseListener doesn't work.
          Workaround as suggested in http://stackoverflow.com/questions/9327826/searchviews-oncloselistener-doesnt-work
         */
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

                //get the restaurants
                getPois(true, true);
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

                //clear previous restaurants
                mAdapter.clear();
                mFarthestPOI = null;

                mKeyword = query;

                //get list of restaurants with search query
                getPois(true, true);
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

    private void showLogoutDialog()
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
                        logout();
                    }
                }).show();
    }

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
                        startActivity(SignInActivity.newIntent(MainActivity.this));
                        finish();
                    }

                    @Override
                    public void onError(Exception e)
                    {
                        Log.e(TAG, "onError: unable to logout", e);
                        dialog.dismiss();
                        mIsProgressDialogShowing = false;
                        Toast.makeText(MainActivity.this, "Unable to logout", Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void getPois(/*@Nullable String keyword, */boolean withProgressDialog, final boolean clearList)
    {
        if (mLastLocation == null)
        {
            Toast.makeText(this, R.string.location_error, Toast.LENGTH_SHORT).show();
            return;
        }


        mIsLoading = true;
        mAdapter.setLoading(true);

        final Dialog dialog = withProgressDialog ? ProgressDialog.getDialog(this, false) : null;
        if (dialog != null)
        {
            dialog.show();
            mIsProgressDialogShowing = true;
            mAdapter.notifyDataSetChanged();
        }

        /*
        Get a list of pois, sorted by the geo location (closest pois go first), filters and query regex (if not null)
        */
        MongoDBManager.getInstance(getApplicationContext()).geoNear(mKeyword, mLastLocation.getLatitude()
                , mLastLocation.getLongitude(), mFarthestPOI, PAGE_SIZE, new QueryListener<List<POI>>()
                {
                    @Override
                    public void onSuccess(List<POI> pois)
                    {
                        mAdapter.setLoading(false);
                        mIsLoading = false;

                        //if the list of results is smaller than the page size, the pagination finished
                        mIsFinished = pois.size() < PAGE_SIZE;

                        if (dialog != null)
                        {
                            dialog.dismiss();
                            mIsProgressDialogShowing = false;
                        }

                        if (!pois.isEmpty())
                        {
                            /*
                            * update the farthest restaurant so we can use it for the next page
                            * */
                            mFarthestPOI = pois.get(pois.size() - 1);
                        }

                        //clear previous results
                        if (clearList)
                        {
                            mAdapter.clear();
                        }

                        mAdapter.addData(pois);
                        mAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onError(Exception e)
                    {
                        mAdapter.setLoading(false);
                        mIsLoading = false;
                        mIsFinished = true;
                        mAdapter.notifyDataSetChanged();
                        Toast.makeText(MainActivity.this, R.string.unable_to_get_results, Toast.LENGTH_SHORT).show();
                        if (dialog != null)
                        {
                            dialog.dismiss();
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
                mLastLocation = location;
                if (mLastLocation != null && mAdapter != null)
                {

                    /*once we have a location, add the pagination mechanism and continue*/
                    if (mPaginate == null)
                    {
                        mPaginate = Paginate.with(mRecyclerView, mPaginateCallback)
                                .setLoadingTriggerThreshold(2)
                                .build();
                    }
                    mRecyclerView.getAdapter().notifyDataSetChanged();
                }
            }
        });

    }


    private void initRecyclerView()
    {
        mRecyclerView = findViewById(R.id.recycler_view);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(this);

        mRecyclerView.setHasFixedSize(false);
        mRecyclerView.setLayoutManager(layoutManager);

        DividerItemDecoration divider = new DividerItemDecoration(this, layoutManager.getOrientation());
        mRecyclerView.addItemDecoration(divider);
        mAdapter = new poiAdapter();
        mAdapter.setLoading(true);
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
            this.mLoading = loading;
        }

        ArrayList<POI> getDataSet()
        {
            return pois;
        }

        void addData(List<POI> data)
        {
            pois.addAll(data);
            // invalidateOptionsMenu();
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
        {
            if (viewType == EMPTY_VIEW)
            {
                /* No results */
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
            /*
            Bind the POI to the UI
            * */

            if (holder instanceof ViewHolder)
            {
                ViewHolder vh = (ViewHolder) holder;
                POI data = pois.get(position);
                vh.mName.setText(data.getName());
                //setAddress(vh, data.getAddress());
                // vh.mPhone.setText(data.getPhone());
                if (mLastLocation != null)
                {
                    vh.mDistance.setText(formatDistance(data.getDistance()));
                }
                else
                {
                    vh.mDistance.setText(R.string.unknown);
                }
            }
        }

        /*In this app if the distance to the POI is bigger than 10 miles, we round the number.
        * If the distance is smaller than 10 miles, we wanna show the distance with 1 digit after the decimal point
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

//    private void setAddress(ViewHolder vh, String address)
//    {
//        try
//        {
//            String[] split = address.split(",");
//            vh.mAddress.setText(split[0].trim() + ", " + split[1].trim());
//            vh.mZipCode.setText(split[2].trim());
//
//        }
//        catch (Exception e)
//        {
//            Log.e(TAG, "unable to parse address", e);
//        }
//    }

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
            if (pois == null || pois.size() == 0)
            {
                if (mLoading)
                {
                    return 0;
                }
                else
                {
                    return 1;
                }
            }

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
                POI data = pois.get(itemPosition);

                //get the POI and show it in the map activity
//                Intent intent = POIDetailActivity.newIntent(MainActivity.this, data);
//                startActivityForResult(intent, REQUEST_CODE);
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
            TextView mName;
            TextView mDistance;

            ViewHolder(View itemView)
            {
                super(itemView);
                mName = itemView.findViewById(R.id.poiNameTextView);
                mDistance = itemView.findViewById(R.id.poiDistanceTextView);
            }
        }

    }


    /*
    * Callbacks for RecyclerView pagination
    * */
    private Paginate.Callbacks mPaginateCallback = new Paginate.Callbacks()
    {
        @Override
        public void onLoadMore()
        {
            /*
            * Get the next page of restaurants
            * */
            getPois(false, false);
        }

        @Override
        public boolean isLoading()
        {
            /*
            loading indication for the pagination adapter
            */
            return mIsLoading && !isSearching() && !mIsProgressDialogShowing;
        }

        @Override
        public boolean hasLoadedAllItems()
        {
            /*
            * indication of whether we finished the pagination or not
            * */
            return mLastLocation != null && (mIsFinished || isSearching());
        }
    };

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
}