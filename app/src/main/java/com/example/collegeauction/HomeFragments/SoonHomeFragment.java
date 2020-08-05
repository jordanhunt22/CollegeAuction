package com.example.collegeauction.HomeFragments;

import android.app.Activity;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.collegeauction.Activities.MainActivity;
import com.example.collegeauction.Adapters.ListingsAdapter;
import com.example.collegeauction.Miscellaneous.EndlessRecyclerViewScrollListener;
import com.example.collegeauction.Models.Bid;
import com.example.collegeauction.Models.Favorite;
import com.example.collegeauction.Models.Listing;
import com.example.collegeauction.ParseApplication;
import com.example.collegeauction.R;
import com.google.android.material.slider.LabelFormatter;
import com.google.android.material.slider.RangeSlider;
import com.google.android.material.slider.Slider;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;

import java.text.Format;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Currency;
import java.util.Date;
import java.util.List;

public class SoonHomeFragment extends Fragment {

    public static final String TAG = "SoonHomeFragment";

    private RecyclerView rvPosts;
    private TextView tvEmpty;
    protected SwipeRefreshLayout swipeContainer;
    private RangeSlider sPrices;
    private ListingsAdapter adapter;
    private List<Listing> allListings;
    private EndlessRecyclerViewScrollListener scrollListener;
    private Boolean queryWithinRange;
    private List<Integer> sliderVals;

    public SoonHomeFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //  Sets hasOptionMenu to true
        setHasOptionsMenu(true);
        // Inflate the layout in this fragment
        return inflater.inflate(R.layout.fragment_home_all, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initially, the slider values are not considered
        queryWithinRange = false;

        tvEmpty = view.findViewById(R.id.tvEmpty);
        tvEmpty.setVisibility(View.GONE);

        sPrices = view.findViewById(R.id.sPrices);
        sPrices.setVisibility(View.VISIBLE);

        // Sets up the formatter for prices
        sPrices.setLabelFormatter(new LabelFormatter() {
            @NonNull
            @Override
            public String getFormattedValue(float value) {
                NumberFormat labelFormatter = NumberFormat.getCurrencyInstance();
                labelFormatter.setMaximumFractionDigits(0);
                labelFormatter.setCurrency(Currency.getInstance("USD"));
                return labelFormatter.format(value);
            }
        });

        // Is triggered whenever the price is changed
        sPrices.addOnChangeListener(new RangeSlider.OnChangeListener() {
            @Override
            public void onValueChange(@NonNull RangeSlider slider, float value, boolean fromUser) {
                if (fromUser) {
                    // Sets the adapter to filtering by price
                    queryWithinRange = true;
                    sliderVals.clear();
                    for (float val : slider.getValues()){
                        sliderVals.add((int) val);
                    }
                    queryListingsInRange(sliderVals);
                }
            }
        });

        // Lookup the swipe container view
        swipeContainer = view.findViewById(R.id.swipeContainer);
        // Setup refresh listener which triggers new data loading
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Your code to refresh the list here.
                // Make sure you call swipeContainer.setRefreshing(false)
                // once the network request has completed successfully.
                if (queryWithinRange){
                    queryListingsInRange(sliderVals);
                }
                else{
                    queryListings();
                }
            }
        });

        swipeContainer.setRefreshing(true);

        rvPosts = view.findViewById(R.id.rvPosts);

        allListings = new ArrayList<>();
        adapter = new ListingsAdapter(getContext(), allListings);

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
                if (favorites != null){
                    for (Favorite favorite : favorites) {
                        Listing.listingsFavoritedByCurrentuser.removeAll(Collections.singleton(favorite.getListing().getObjectId()));
                        Listing.listingsFavoritedByCurrentuser.add(favorite.getListing().getObjectId());
                    }
                }
                queryListings();
            }
        });

        sliderVals = new ArrayList<>();
    }

    private void queryListingsInRange(final List<Integer> queryVals) {
        // Collapses the SearchView if it is open
        HomeFragment parentFrag = ((HomeFragment) SoonHomeFragment.this.getParentFragment());
        assert parentFrag != null;
        parentFrag.collapseMenuItem();
        final ParseUser currentUser = ParseUser.getCurrentUser();
        Date queryDate = new Date();
        ParseQuery query = ParseQuery.getQuery(Listing.class);
        query.include(Listing.KEY_BID);
        // limit query to latest 10 items
        query.setLimit(40);
        // Only shows items that have not expired yet
        query.whereGreaterThanOrEqualTo(Listing.KEY_EXPIRATION, queryDate);
        // order posts by creation date (newest first)
        query.addAscendingOrder(Listing.KEY_EXPIRATION);
        // Does not show the current user's posts
        query.whereNotEqualTo(Listing.KEY_USER, currentUser);
        // start an asynchronous call for posts
        query.findInBackground(new FindCallback<Listing>() {
            @Override
            public void done(List<Listing> listings, ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Issue with getting listings", e);
                    return;
                }

                List<Listing> returnListings = new ArrayList<>();
                for (Listing listing : listings){
                    if (listing.getRecentBid() == null){
                        if (listing.getMinPrice() >= queryVals.get(0) && listing.getMinPrice() <= queryVals.get(1)){
                            returnListings.add(listing);
                        }
                    }
                    else{
                        int price = (int) listing.getRecentBid().getNumber("price");
                        if ((price >= queryVals.get(0) && price <= queryVals.get(1))){
                            returnListings.add(listing);
                        }
                    }
                }

                // Clears the adapter
                adapter.clear();
                adapter.addAll(returnListings);

                if (returnListings.isEmpty()) {
                    tvEmpty.setVisibility(View.VISIBLE);
                } else {
                    tvEmpty.setVisibility(View.GONE);
                }

                // Save received posts to list and notify adapter of new data
                swipeContainer.setRefreshing(false);
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
        // Only shows items that have not expired yet
        query.whereGreaterThanOrEqualTo(Listing.KEY_EXPIRATION, queryDate);
        // order posts by creation date (newest first)
        query.addAscendingOrder(Listing.KEY_EXPIRATION);
        // Does not show the current user's posts
        query.whereNotEqualTo(Listing.KEY_USER, currentUser);
        // Skips the items that are already in the adapter
        query.setSkip(adapter.getItemCount());
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
                
                // Only loads items within a range if the slider has been moved
                if (queryWithinRange){
                    List<Listing> finalListings = new ArrayList<>();
                    for (Listing listing : listings){
                        if (listing.getRecentBid() == null){
                            if (listing.getMinPrice() >= sliderVals.get(0) && listing.getMinPrice() <= sliderVals.get(1)){
                                finalListings.add(listing);
                            }
                        }
                        else{
                            int price = (int) listing.getRecentBid().getNumber("price");
                            if ((price >= sliderVals.get(0) && price <= sliderVals.get(1))){
                                finalListings.add(listing);
                            }
                        }
                    }
                    returnListings = finalListings;
                }

                // Clears the adapter
                adapter.addAll(returnListings);

                // Save received posts to list and notify adapter of new data
                swipeContainer.setRefreshing(false);
            }
        });
    }

    public void queryListings() {
        // Checks to see if there are new purchases
        MainActivity main = (MainActivity) getActivity();
        assert main != null;
        main.queryBuys();
        main.querySales();
        // Collapses the SearchView if it is open
        HomeFragment parentFrag = ((HomeFragment) SoonHomeFragment.this.getParentFragment());
        assert parentFrag != null;
        parentFrag.collapseMenuItem();
        final ParseUser currentUser = ParseUser.getCurrentUser();
        Date queryDate = new Date();
        ParseQuery query = ParseQuery.getQuery(Listing.class);
        query.include(Listing.KEY_BID);
        // limit query to latest 10 items
        query.setLimit(10);
        // Only shows items that have not expired yet
        query.whereGreaterThanOrEqualTo(Listing.KEY_EXPIRATION, queryDate);
        // order posts by creation date (newest first)
        query.addAscendingOrder(Listing.KEY_EXPIRATION);
        // Does not show the current user's posts
        query.whereNotEqualTo(Listing.KEY_USER, currentUser);
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

    public void queryListingsFromSearch(String queryString) {
        rvPosts.scrollToPosition(0);
        final ParseUser currentUser = ParseUser.getCurrentUser();
        Date queryDate = new Date();
        ParseQuery query = ParseQuery.getQuery(Listing.class);
        query.include(Listing.KEY_BID);
        // limit query to latest 20 items
        query.setLimit(10);
        // Only shows items that have not expired yet
        query.whereGreaterThanOrEqualTo(Listing.KEY_EXPIRATION, queryDate);
        // order posts by creation date (newest first)
        query.addAscendingOrder(Listing.KEY_EXPIRATION);
        // Query only returns items which have the search string in its name or description
        query.whereContains(Listing.KEY_NAME, queryString);
        // Does not show the current user's posts
        query.whereNotEqualTo(Listing.KEY_USER, currentUser);
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

                adapter.notifyDataSetChanged();

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
        queryListings();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        MenuItem item = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                queryListingsFromSearch(query);
                searchView.clearFocus();
                // Logs the searches of each user
                Bundle bundle = new Bundle();
                bundle.putString(FirebaseAnalytics.Param.SEARCH_TERM, "Android Application");
                ParseApplication.mFireBaseAnalytics
                        .logEvent(FirebaseAnalytics.Event.SEARCH, bundle);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
    }
}