package com.example.collegeauction.HomeFragments;

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
import android.widget.Toast;

import com.example.collegeauction.Activities.MainActivity;
import com.example.collegeauction.Adapters.ListingsAdapter;
import com.example.collegeauction.Miscellaneous.EndlessRecyclerViewScrollListener;
import com.example.collegeauction.Models.Bid;
import com.example.collegeauction.Models.Favorite;
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
import java.util.Collections;
import java.util.List;

public class SoonHomeFragment extends Fragment {

    public static final String TAG = "SoonHomeFragment";

    private RecyclerView rvPosts;
    private TextView tvEmpty;
    protected SwipeRefreshLayout swipeContainer;
    private ListingsAdapter adapter;
    private List<Listing> allListings;
    private EndlessRecyclerViewScrollListener scrollListener;

    public SoonHomeFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout in this fragment
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
                        for(Favorite favorite : favorites) {
                            Listing.listingsFavoritedByCurrentuser.removeAll(Collections.singleton(favorite.getListing().getObjectId()));
                            Listing.listingsFavoritedByCurrentuser.add(favorite.getListing().getObjectId());
                        }
                        queryListings();
                    }
        });
    }

    private void loadMoreData() {
        final ParseUser currentUser = ParseUser.getCurrentUser();
        ParseQuery query = ParseQuery.getQuery(Listing.class);
        query.include(Listing.KEY_BID);
        // limit query to latest 20 items
        query.setLimit(20);
        // Only displays items that have not been sold yet
        query.whereEqualTo("isSold", false);
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
                if (e != null){
                    Log.e(TAG, "Issue with getting listings", e);
                    return;
                }
                List <Listing> returnListings = new ArrayList<>();
                for (Listing listing : listings){
                    if (!adapter.listingIds.contains(listing.getObjectId())){
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
        // Collapses the SearchView if it is open
        HomeFragment parentFrag = ((HomeFragment)SoonHomeFragment.this.getParentFragment());
        parentFrag.collapseMenuItem();
        final ParseUser currentUser = ParseUser.getCurrentUser();
        ParseQuery query = ParseQuery.getQuery(Listing.class);
        query.include(Listing.KEY_BID);
        // limit query to latest 20 items
        query.setLimit(20);
        // Only displays items that have not been sold yet
        query.whereEqualTo("isSold", false);
        // order posts by creation date (newest first)
        query.addAscendingOrder(Listing.KEY_EXPIRATION);
        // Does not show the current user's posts
        query.whereNotEqualTo(Listing.KEY_USER, currentUser);
        // start an asynchronous call for posts
        query.findInBackground(new FindCallback<Listing>() {
            @Override
            public void done(List<Listing> listings, ParseException e) {
                if (e != null){
                    Log.e(TAG, "Issue with getting listings", e);
                    return;
                }
                List <Listing> returnListings = new ArrayList<>();
                returnListings.addAll(listings);
                ParseRelation<ParseObject> relation = currentUser.getRelation("purchases");
                for (int i = 0; i < listings.size(); i++){
                    Listing listing = listings.get(i);
                    if(listing.getRecentBid() != null) {
                        if (listing.getRecentBid().getParseUser(Bid.KEY_USER).equals(currentUser)) {
                            relation.add(listing);
                            currentUser.saveInBackground();
                        }
                    }
                    if (System.currentTimeMillis() > listing.getExpireTime().getTime()){
                        listing.put("isSold", true);
                        listing.saveInBackground();
                        returnListings.removeAll(Collections.singleton(listing));
                    }
                }

                // Clears the adapter
                adapter.clear();
                adapter.addAll(returnListings);

                if (allListings.isEmpty()){
                    tvEmpty.setVisibility(View.VISIBLE);
                }
                else{
                    tvEmpty.setVisibility(View.GONE);
                }

                // Save received posts to list and notify adapter of new data
                swipeContainer.setRefreshing(false);
            }
        });
    }

    public void queryListingsFromSearch(String queryString){
        final ParseUser currentUser = ParseUser.getCurrentUser();
        ParseQuery query = ParseQuery.getQuery(Listing.class);
        query.include(Listing.KEY_BID);
        // limit query to latest 20 items
        query.setLimit(20);
        // Only displays items that have not been sold yet
        query.whereEqualTo("isSold", false);
        // order posts by creation date (newest first)
        query.addAscendingOrder(Listing.KEY_EXPIRATION);
        // Query only returns items which have the search string in its name or description
        query.whereContains(Listing.KEY_NAME, queryString);
        query.whereContains(Listing.KEY_DESCRIPTION, queryString);
        // Does not show the current user's posts
        query.whereNotEqualTo(Listing.KEY_USER, currentUser);
        // start an asynchronous call for posts
        query.findInBackground(new FindCallback<Listing>() {
            @Override
            public void done(List<Listing> listings, ParseException e) {
                if (e != null){
                    Log.e(TAG, "Issue with getting listings", e);
                    return;
                }
                List <Listing> returnListings = new ArrayList<>();
                returnListings.addAll(listings);
                ParseRelation<ParseObject> relation = currentUser.getRelation("purchases");
                for (int i = 0; i < listings.size(); i++){
                    Listing listing = listings.get(i);
                    if(listing.getRecentBid() != null) {
                        if (listing.getRecentBid().getParseUser(Bid.KEY_USER).equals(currentUser)) {
                            relation.add(listing);
                            currentUser.saveInBackground();
                        }
                    }
                    if (System.currentTimeMillis() > listing.getExpireTime().getTime()){
                        listing.put("isSold", true);
                        listing.saveInBackground();
                        returnListings.removeAll(Collections.singleton(listing));
                    }
                }

                // Clears the adapter
                adapter.clear();
                adapter.addAll(returnListings);

                adapter.notifyDataSetChanged();

                if (allListings.isEmpty()){
                    tvEmpty.setVisibility(View.VISIBLE);
                }
                else{
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
}