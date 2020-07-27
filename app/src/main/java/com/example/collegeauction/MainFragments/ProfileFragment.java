package com.example.collegeauction.MainFragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.collegeauction.Activities.MainActivity;
import com.example.collegeauction.Adapters.CurrentUserListingsAdapter;
import com.example.collegeauction.Adapters.ListingsAdapter;
import com.example.collegeauction.Activities.LoginActivity;
import com.example.collegeauction.Adapters.PurchasesAdapter;
import com.example.collegeauction.Miscellaneous.EndlessRecyclerViewScrollListener;
import com.example.collegeauction.Models.Listing;
import com.example.collegeauction.Models.Purchase;
import com.example.collegeauction.R;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ProfileFragment extends Fragment {

    public static final String TAG = "ProfileFragment";

    private Button btnLogOut;
    private TextView tvUsername;

    private RecyclerView rvPosts;
    private TextView tvEmpty;
    protected SwipeRefreshLayout swipeContainer;
    private CurrentUserListingsAdapter adapter;
    private List<Listing> allListings;
    private EndlessRecyclerViewScrollListener scrollListener;

    public ProfileFragment(){
        // Required empty constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        // Inflate the layout
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvEmpty = view.findViewById(R.id.tvEmpty);
        tvEmpty.setText("You have no items listed");
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
                queryUsersListings();
            }
        });

        swipeContainer.setRefreshing(true);

        rvPosts = view.findViewById(R.id.rvPosts);

        allListings = new ArrayList<>();
        adapter = new CurrentUserListingsAdapter(getContext(), allListings);

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

        // Sets the TextView at the top to the current user's username
        tvUsername = view.findViewById(R.id.tvUsername);
        ParseUser user = ParseUser.getCurrentUser();
        tvUsername.setText(user.getUsername());

        // Sends the user to the login screen when they logout
        btnLogOut = view.findViewById(R.id.btnLogOut);
        btnLogOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ParseUser.logOut();
                goLogIn();
            }
        });

        queryUsersListings();
    }

    private void loadMoreData() {
        final ParseUser currentUser = ParseUser.getCurrentUser();
        ParseQuery query = ParseQuery.getQuery(Listing.class);
        query.include(Listing.KEY_BID);
        query.include(Listing.KEY_USER);
        // limit query to latest 10 items
        query.setLimit(10);
        // Skips all the items that are already in the adapter
        query.setSkip(adapter.getItemCount());
        // only shows listings where the current user is the seller
        query.whereEqualTo("user", currentUser);
        // order posts by creation date (oldest first)
        query.addDescendingOrder(Listing.KEY_EXPIRATION);
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
                    if (!adapter.purchaseIds.contains(listing.getObjectId())){
                        returnListings.add(listing);
                    }
                }

                // Adds new listings to the adapter
                adapter.addAll(returnListings);

                // Save received posts to list and notify adapter of new data
                swipeContainer.setRefreshing(false);
            }
        });
    }


    private void queryUsersListings() {
        // Checks to see if there are new purchases
        MainActivity main = (MainActivity) getActivity();
        assert main != null;
        main.queryBuys();
        main.querySales();
        final ParseUser currentUser = ParseUser.getCurrentUser();
        ParseQuery query = ParseQuery.getQuery(Listing.class);
        query.include(Listing.KEY_BID);
        query.include(Listing.KEY_USER);
        // limit query to latest 10 items
        query.setLimit(10);
        // only shows listings where the current user is the seller
        query.whereEqualTo("user", currentUser);
        // order posts by creation date (oldest first)
        query.addDescendingOrder(Listing.KEY_EXPIRATION);
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

                if (listings.isEmpty()){
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

    private void goLogIn() {
        Intent i = new Intent(getContext(), LoginActivity.class);
        startActivity(i);
        getActivity().finish();
    }

    @Override
    public void onResume() {
        super.onResume();
        // queryUsersListings();
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        MenuItem item = menu.findItem(R.id.action_search);
        if(item!=null)
            item.setVisible(false);
    }
}