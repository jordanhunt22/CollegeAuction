package com.example.collegeauction.FavFragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.collegeauction.Activities.MainActivity;
import com.example.collegeauction.Adapters.ListingsAdapter;
import com.example.collegeauction.HomeFragments.HomeFragment;
import com.example.collegeauction.HomeFragments.SoonHomeFragment;
import com.example.collegeauction.Miscellaneous.EndlessRecyclerViewScrollListener;
import com.example.collegeauction.Models.Favorite;
import com.example.collegeauction.Models.Listing;
import com.example.collegeauction.R;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class SuggestedFragment extends Fragment {

    public static final String TAG = "SuggestedFragment";

    private RecyclerView rvPosts;
    private TextView tvEmpty;
    protected SwipeRefreshLayout swipeContainer;
    private ListingsAdapter adapter;
    private List<Listing> allListings;
    private EndlessRecyclerViewScrollListener scrollListener;

    public SuggestedFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home_all, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

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
                for (Favorite favorite : favorites) {
                    Listing.listingsFavoritedByCurrentuser.removeAll(Collections.singleton(favorite.getListing().getObjectId()));
                    Listing.listingsFavoritedByCurrentuser.add(favorite.getListing().getObjectId());
                }
                queryListings();
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

                // Clears the adapter
                adapter.addAll(returnListings);

                // Save received posts to list and notify adapter of new data
                swipeContainer.setRefreshing(false);
            }
        });
    }

    public void queryListings() {
        // Checks to see if there are new purchases
//        MainActivity main = (MainActivity) getActivity();
//        assert main != null;
//        main.queryBuys();
//        main.querySales();
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
}