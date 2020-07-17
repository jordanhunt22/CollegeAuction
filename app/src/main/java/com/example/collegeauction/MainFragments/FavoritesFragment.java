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

import com.example.collegeauction.Activities.MainActivity;
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
import java.util.Collections;
import java.util.List;

public class FavoritesFragment extends Fragment {

   public static final String TAG = "HomeFragment";

   private RecyclerView rvPosts;
   protected SwipeRefreshLayout swipeContainer;
   private ListingsAdapter adapter;
   private List<Listing> favoriteListings;
   private EndlessRecyclerViewScrollListener scrollListener;

   public FavoritesFragment() {
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
            queryListings();
         }
      });
      // Configure the refreshing colors
      swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
              android.R.color.holo_green_light,
              android.R.color.holo_orange_light,
              android.R.color.holo_red_light);

      rvPosts = view.findViewById(R.id.rvPosts);

      favoriteListings = new ArrayList<>();
      adapter = new ListingsAdapter(getContext(), favoriteListings);

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
            ParseRelation<Listing> likedPosts = object.getRelation("favoritedListings");
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

   protected void queryListings() {
      final ParseUser user = ParseUser.getCurrentUser();
      user.fetchInBackground(new GetCallback<ParseObject>() {
         @Override
         public void done(final ParseObject object, ParseException e) {
            if (e != null){
               Log.e(TAG, "Issue with getting current user", e);
               return;
            }
            ParseRelation<Listing> favoritedPosts = object.getRelation("favoritedListings");
            ParseQuery<Listing> q = favoritedPosts.getQuery();
            // Add in ascending order?
            q.addAscendingOrder("createdAt");
            // Does not show the current user's posts
            q.whereNotEqualTo(Listing.KEY_USER, user);
            // Only displays items that have not been sold yet
            q.whereEqualTo("isSold", false);
            // Includes the most recent bid
            q.include(Listing.KEY_BID);
            q.findInBackground(new FindCallback<Listing>() {
               @Override
               public void done(List<Listing> listings, ParseException e) {
                  if (e != null){
                     Log.e(TAG, "Issue with getting current user's favorite listings", e);
                     return;
                  }
                  for (int i = 0; i < listings.size(); i++){
                     Listing listing = listings.get(i);
                     if (System.currentTimeMillis() > listing.getExpireTime().getTime()){
                        listing.put("isSold", true);
                        listing.saveInBackground();
                        listings.removeAll(Collections.singleton(listing));
                     }
                  }
                  // Clears the adapter
                  adapter.clear();
                  adapter.addAll(listings);

                  // Save received posts to list and notify adapter of new data
                  swipeContainer.setRefreshing(false);
               }
            });
         }
      });
   }

   @Override
   public void onResume() {
      super.onResume();
      queryListings();
   }
}