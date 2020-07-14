package com.example.collegeauction.MainFragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.collegeauction.Adapters.ListingsAdapter;
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

public class FavoritesFragment extends HomeFragment {

   private ListingsAdapter adapter;
   private List<Listing> favoriteListings;

   public FavoritesFragment(){
       // Required empty constructor
   }

   @Override
   public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
      super.onViewCreated(view, savedInstanceState);
      favoriteListings = new ArrayList<>();
      adapter = new ListingsAdapter(getContext(), favoriteListings);
   }

   @Override
   protected void queryListings() {
      ParseUser user = ParseUser.getCurrentUser();
      user.fetchInBackground(new GetCallback<ParseObject>() {
         @Override
         public void done(final ParseObject object, ParseException e) {
            if (e != null){
               Log.e(TAG, "Issue with getting current user", e);
               return;
            }
            ParseRelation<Listing> favoritedPosts = object.getRelation("likes");
            ParseQuery<Listing> q = favoritedPosts.getQuery();
            q.findInBackground(new FindCallback<Listing>() {
               @Override
               public void done(List<Listing> listings, ParseException e) {
                  if (e != null){
                     Log.e(TAG, "Issue with getting current user's favorite listings", e);
                     return;
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
}