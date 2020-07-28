package com.example.collegeauction.HomeFragments;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.collegeauction.Activities.MainActivity;
import com.example.collegeauction.Adapters.NearbyAdapter;
import com.example.collegeauction.Miscellaneous.EndlessRecyclerViewScrollListener;
import com.example.collegeauction.Models.Favorite;
import com.example.collegeauction.Models.Listing;
import com.example.collegeauction.R;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.SettingsClient;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.RuntimePermissions;

import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;

@RuntimePermissions
public class NearbyHomeFragment extends Fragment {

    public static final String TAG = "NearbyHomeFragment";

    private LocationRequest mLocationRequest;
    Location mCurrentLocation;
    private long UPDATE_INTERVAL = 60000;  /* 60 secs */
    private long FASTEST_INTERVAL = 5000; /* 5 secs */

    private Boolean onStart;

    private RecyclerView rvPosts;
    private TextView tvEmpty;
    protected SwipeRefreshLayout swipeContainer;
    private NearbyAdapter adapter;
    private List<Listing> allListings;
    private EndlessRecyclerViewScrollListener scrollListener;

    public NearbyHomeFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout in this fragment
        setHasOptionsMenu(true);
        return inflater.inflate(R.layout.fragment_home_all, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        NearbyHomeFragmentPermissionsDispatcher.startLocationUpdatesWithPermissionCheck(this);

        onStart = true;

        tvEmpty = view.findViewById(R.id.tvEmpty);
        tvEmpty.setVisibility(View.GONE);

        // Lookup the swipe container view
        swipeContainer = view.findViewById(R.id.swipeContainer);
        // Setup refresh listener which triggers new data loading
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Your code to refresh the list here.
                // Make sure you call swipeContainer.setRefreshing(false)
                // once the network request has completed successfully.
                queryListings();
            }
        });

        swipeContainer.setRefreshing(true);

        rvPosts = view.findViewById(R.id.rvPosts);

        allListings = new ArrayList<>();


        // Makes the fab visible whenever a new fragment starts
        MainActivity.fab.show();

        // Retrieves a user's favorited listings
        ParseUser user = ParseUser.getCurrentUser();
        ParseQuery query = ParseQuery.getQuery(Favorite.class);
        // Includes the listing and user for every listing
        query.include(Favorite.KEY_LISTING);
        query.include(Favorite.KEY_USER);
        query.whereEqualTo(Favorite.KEY_USER, user);
        query.findInBackground(new FindCallback<Favorite>() {
            @Override
            public void done(List<Favorite> favorites, ParseException e) {
                for (Favorite favorite : favorites) {
                    Listing.listingsFavoritedByCurrentuser.removeAll(Collections.singleton(favorite.getListing().getObjectId()));
                    Listing.listingsFavoritedByCurrentuser.add(favorite.getListing().getObjectId());
                }
            }
        });
    }

    private void loadMoreData() {
        final ParseUser currentUser = ParseUser.getCurrentUser();
        Date queryDate = new Date();
        ParseQuery query = ParseQuery.getQuery(Listing.class);
        query.include(Listing.KEY_BID);
        // limit query to latest 10 items
        query.setLimit(10);
        // Does not query items that are already in the adapter
        query.setLimit(adapter.getItemCount());
        // Only shows items that have not expired yet
        query.whereGreaterThanOrEqualTo(Listing.KEY_EXPIRATION, queryDate);
        // Does not show the current user's posts
        query.whereNotEqualTo(Listing.KEY_USER, currentUser);
        // Queries the items that are closest to the user
        ParseGeoPoint returnPoint = new ParseGeoPoint();
        returnPoint.setLatitude(mCurrentLocation.getLatitude());
        returnPoint.setLongitude(mCurrentLocation.getLongitude());
        query.whereNear("location", returnPoint);
        // start an asynchronous call for posts
        query.findInBackground(new FindCallback<Listing>() {
            @Override
            public void done(List<Listing> listings, ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Issue with getting listings", e);
                    return;
                }
                List<Listing> returnListings = new ArrayList<>();
                for (Listing listing : listings) {
                    if (!adapter.listingIds.contains(listing.getObjectId())) {
                        returnListings.add(listing);
                    }
                }

                // Clears the adapter
                adapter.addAll(returnListings);

                // Save received posts to list and notify adapter of new data
                swipeContainer.setRefreshing(false);
            }
        });
    }

    public void queryListings() {
        final ParseUser currentUser = ParseUser.getCurrentUser();
        Date queryDate = new Date();
        ParseQuery query = ParseQuery.getQuery(Listing.class);
        query.include(Listing.KEY_BID);
        // limit query to latest 10 items
        query.setLimit(10);
        // Does not show the current user's posts
        query.whereNotEqualTo(Listing.KEY_USER, currentUser);
        // Only shows items that have not expired yet
        query.whereGreaterThanOrEqualTo(Listing.KEY_EXPIRATION, queryDate);
        // Queries the items that are closest to the user
        ParseGeoPoint returnPoint = new ParseGeoPoint();
        returnPoint.setLatitude(mCurrentLocation.getLatitude());
        returnPoint.setLongitude(mCurrentLocation.getLongitude());
        query.whereNear("location", returnPoint);
        // start an asynchronous call for posts
        query.findInBackground(new FindCallback<Listing>() {
            @Override
            public void done(List<Listing> listings, ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Issue with getting listings", e);
                    return;
                }

                // Clears the adapter
                adapter.clear();
                adapter.addAll(listings);

                if (listings.isEmpty()) {
                    tvEmpty.setVisibility(View.VISIBLE);
                } else {
                    tvEmpty.setVisibility(View.GONE);
                }

                // Save received posts to list and notify adapter of new data
                swipeContainer.setRefreshing(false);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mCurrentLocation != null) {
            queryListings();
        }
    }

    @NeedsPermission({Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION})
    protected void startLocationUpdates() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        LocationSettingsRequest locationSettingsRequest = builder.build();

        SettingsClient settingsClient = LocationServices.getSettingsClient(getContext());
        settingsClient.checkLocationSettings(locationSettingsRequest);
        //noinspection MissingPermission
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        getFusedLocationProviderClient(getContext()).requestLocationUpdates(mLocationRequest, new LocationCallback() {
                    @Override
                    public void onLocationResult(LocationResult locationResult) {
                        onLocationChanged(locationResult.getLastLocation());
                    }
                },
                Looper.myLooper());
    }

    public void onLocationChanged(Location location) {
        // GPS may be turned off
        if (location == null) {
            return;
        }
        // Report to the UI that the location was updated
        mCurrentLocation = location;

        if (onStart) {
            adapter = new NearbyAdapter(getContext(), allListings, mCurrentLocation);
            // Set the adapter on the recycler view
            rvPosts.setAdapter(adapter);

            // set the layout manager on the recycler view
            GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 2);
            rvPosts.setLayoutManager(gridLayoutManager);

            // Makes the fab disappear when scrolling
            rvPosts.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    if (dy > 0 && MainActivity.fab.getVisibility() == View.VISIBLE) {
                        MainActivity.fab.hide();
                    } else if (dy < 0 && MainActivity.fab.getVisibility() != View.VISIBLE) {
                        MainActivity.fab.show();
                    }
                }
            });

            // Implement ScrollListener for infinite scroll
            scrollListener = new EndlessRecyclerViewScrollListener(gridLayoutManager) {
                @Override
                public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                    // Triggered only when new data needs to be appended to the list
                    // Add whatever code is needed to append new items to the bottom of the list
                    loadMoreData();
                }
            };

            // Adds the scroll listener to the RecyclerView
            rvPosts.addOnScrollListener(scrollListener);
            onStart = false;
            queryListings();
        }
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        MenuItem item = menu.findItem(R.id.action_search);
        if (item != null)
            item.setVisible(false);
    }
}

