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
import android.widget.TextView;
import android.widget.Toast;

import com.example.collegeauction.Activities.MainActivity;
import com.example.collegeauction.Adapters.ListingsAdapter;
import com.example.collegeauction.Miscellaneous.EndlessRecyclerViewScrollListener;
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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class FavoritesFragment extends Fragment {

   public static final String TAG = "HomeFragment";

   private RecyclerView rvPosts;
   private TextView tvEmpty;
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
      return inflater.inflate(R.layout.fragment_home_all, container, false);
   }

   @Override
   public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
      super.onViewCreated(view, savedInstanceState);

      tvEmpty = view.findViewById(R.id.tvEmpty);
      tvEmpty.setText("You have no favorites");
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

      // Makes the fab visible whenever a new fragment starts
      MainActivity.fab.show();

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

      queryListings();

   }

   private void loadMoreData() {
      // Retrieves a user's favorited listings
      ParseUser user = ParseUser.getCurrentUser();
      ParseQuery query = ParseQuery.getQuery(Favorite.class);
      // Skips the number of items that are in the adapter currently
      query.setSkip(adapter.getItemCount());
      // Sets query limit to 20
      query.setLimit(20);
      // Includes the listing and user for every listing
      query.include(Favorite.KEY_LISTING);
      query.include(Favorite.KEY_USER);
      query.include("listing.mostRecentBid");
      query.whereEqualTo(Favorite.KEY_USER, user);
      query.addAscendingOrder("listing.expiresAt");
      query.findInBackground(new FindCallback<Favorite>() {
         @Override
         public void done(List<Favorite> favorites, ParseException e) {
            if (e != null){
               Toast.makeText(getContext(), "Error getting favorite posts", Toast.LENGTH_SHORT).show();
            }
            List<Listing> listings = new ArrayList<>();
            for(Favorite favorite : favorites) {
               Listing listing = (Listing) favorite.getListing();
               if (listing.getExpireTime().getTime() >= System.currentTimeMillis()) {
                  Listing.listingsFavoritedByCurrentuser.removeAll(Collections.singleton(listing.getObjectId()));
                  Listing.listingsFavoritedByCurrentuser.add(listing.getObjectId());
                  listings.add(listing);
               }
            }
            List<Listing> returnListings = new ArrayList<>();
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

   protected void queryListings() {
      // Retrieves a user's favorited listings
      ParseUser user = ParseUser.getCurrentUser();
      ParseQuery query = ParseQuery.getQuery(Favorite.class);
      // Sets query limit to 20
      query.setLimit(20);
      // Includes the listing and user for every listing
      query.include(Favorite.KEY_LISTING);
      query.include(Favorite.KEY_USER);
      query.include("listing.mostRecentBid");
      query.whereEqualTo(Favorite.KEY_USER, user);
      query.addAscendingOrder("listing.expiresAt");
      query.findInBackground(new FindCallback<Favorite>() {
         @Override
         public void done(List<Favorite> favorites, ParseException e) {
            if (e != null){
               Toast.makeText(getContext(), "Error getting favorite posts", Toast.LENGTH_SHORT).show();
            }
            List<Listing> listings = new ArrayList<>();
            for(Favorite favorite : favorites) {
               Listing listing = (Listing) favorite.getListing();
               if (listing.getExpireTime().getTime() >= System.currentTimeMillis()) {
                  Listing.listingsFavoritedByCurrentuser.add(listing.getObjectId());
                  listings.add(listing);
               }
            }

            // Clears the adapter
            adapter.clear();
            adapter.addAll(listings);

            // Shows text if the RecyclerView is empty
            if (favoriteListings.isEmpty()){
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