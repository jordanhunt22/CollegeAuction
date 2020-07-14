package com.example.collegeauction.MainFragments;

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

import com.example.collegeauction.Adapters.ListingsAdapter;
import com.example.collegeauction.Miscellaneous.EndlessRecyclerViewScrollListener;
import com.example.collegeauction.Models.Listing;
import com.example.collegeauction.R;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    public static final String TAG = "HomeFragment";

    private RecyclerView rvPosts;
    private SwipeRefreshLayout swipeContainer;
    private ListingsAdapter adapter;
    private List<Listing> allListings;
    private EndlessRecyclerViewScrollListener scrollListener;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout in this fragment
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Lookup the swipe container view
        swipeContainer = view.findViewById(R.id.swipeContainer);
        // Setup refresh listener which triggers new data loading
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Your code to refresh the list here.
                // Make sure you call swipeContainer.setRefreshing(false)
                // once the network request has completed successfully.

            }
        });
        // Configure the refreshing colors
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        rvPosts = view.findViewById(R.id.rvPosts);

        allListings = new ArrayList<>();
        adapter = new ListingsAdapter(getContext(), allListings);

        // Set the adapter on the recycler view
        rvPosts.setAdapter(adapter);

        // set the layout manager on the recycler view
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 2);
        rvPosts.setLayoutManager(gridLayoutManager);

        // Implement ScrollListener for infinite scroll
//        scrollListener = new EndlessRecyclerViewScrollListener(linearLayoutManager) {
//            @Override
//            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
//                // Triggered only when new data needs to be appended to the list
//                // Add whatever code is needed to append new items to the bottom of the list
//            }
//        };

        // Adds the scroll listener to the RecyclerView
//        rvPosts.addOnScrollListener(scrollListener);
//
//
        ParseUser user = ParseUser.getCurrentUser();
        user.fetchInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(final ParseObject object, ParseException e) {
                ParseRelation<Listing> likedPosts = object.getRelation("favorites");
                ParseQuery<Listing> q = likedPosts.getQuery();
                q.findInBackground(new FindCallback<Listing>() {
                    @Override
                    public void done(List<Listing> objects, ParseException e) {
                        for(Listing listing : objects) {
                            Listing.listingsFavoritedByCurrentuser.add(listing.getObjectId());
                        }
                        queryListings();
                    }
                });
            }
        });
    }

    private void queryListings() {
        ParseQuery query = ParseQuery.getQuery(Listing.class);
        // query.include(Listing.KEY_USER);
        // limit query to latest 20 items
        query.setLimit(20);
        // order posts by creation date (newest first)
        // TODO: Change later to only items that have not been sold yet
        query.addAscendingOrder(Listing.KEY_CREATED);
        // start an asynchronous call for posts
        query.findInBackground(new FindCallback<Listing>() {
            @Override
            public void done(List<Listing> listings, ParseException e) {
                if (e != null){
                    Log.e(TAG, "Issue with getting listings", e);
                    return;
                }
                // Clears the adapter
                adapter.clear();
                adapter.addAll(listings);

                // Save received posts to list and notify adapter of new data
                swipeContainer.setRefreshing(false);
                for (Listing listing : listings){
                    // Log.i(TAG, "Listing: " + listing.getDescription() + ", username: " + listing.getUser().getUsername());
                }
            }
        });
    }
}