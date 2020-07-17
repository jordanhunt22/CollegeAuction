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

import com.example.collegeauction.Adapters.BidsAdapter;
import com.example.collegeauction.Adapters.PurchasesAdapter;
import com.example.collegeauction.Models.Bid;
import com.example.collegeauction.Models.Listing;
import com.example.collegeauction.R;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

public class BidsPurchasesFragment extends Fragment {

    public static final String TAG = "BidsPurchasesFragment";

    private RecyclerView rvBids;
    private RecyclerView rvPurchases;

    private SwipeRefreshLayout swipeBids;
    private SwipeRefreshLayout swipePurchases;

    private BidsAdapter bidsAdapter;
    private PurchasesAdapter purchasesAdapter;

    private List<Listing> allPurchases;
    private List<Bid> allBids;

    public BidsPurchasesFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_bids_purchases, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Look up the RecyclerViews
        rvBids = view.findViewById(R.id.rvBids);
        rvPurchases = view.findViewById(R.id.rvPurchases);

        // Look up the swipeContainer views
        swipeBids = view.findViewById(R.id.swipeBids);
        swipePurchases = view.findViewById(R.id.swipePurchases);

        // Set up the onRefreshListeners
        swipeBids.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Reload all of the bids
                queryBids();
            }
        });

        swipePurchases.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Reload all of the users purchases
                queryPurchases();
            }
        });

        allBids = new ArrayList<>();
        bidsAdapter = new BidsAdapter(getContext(), allBids);

        rvBids.setAdapter(bidsAdapter);

        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 1);
        rvBids.setLayoutManager(gridLayoutManager);

        allPurchases = new ArrayList<>();
        purchasesAdapter = new PurchasesAdapter(getContext(), allPurchases);

        rvPurchases.setAdapter(purchasesAdapter);

        GridLayoutManager gridLayoutManager2 = new GridLayoutManager(getContext(), 1);
        rvPurchases.setLayoutManager(gridLayoutManager2);

        queryBids();
        queryPurchases();
    }

    private void queryPurchases() {
        ParseUser currentUser = ParseUser.getCurrentUser();
        ParseQuery query = ParseQuery.getQuery(Bid.class);
        // Only gets the currentUser's bids
        query.whereEqualTo("user", currentUser);
        // Includes the attached listing
        query.include(Bid.KEY_LISTING);
        // Only displays items that have not been sold
        query.whereEqualTo("isCurrent", true);
        // order posts by creation date (newest first)
        query.addDescendingOrder("listing.expiresAt");
        // start an asynchronous call for posts
        query.findInBackground(new FindCallback<Bid>() {
            @Override
            public void done(List<Bid> bids, ParseException e) {
                if (e != null){
                    Log.e(TAG, "Issue with getting listings", e);
                    return;
                }
                List<Listing> listings = new ArrayList<>();
                for (Bid bid : bids){
                    Listing listing = (Listing) bid.getListing();
                    if (listing.getBoolean("isSold")){
                        listings.add(listing);
                    }
                }
                purchasesAdapter.clear();
                purchasesAdapter.addAll(listings);
                // Save received posts to list and notify adapter of new data
                swipePurchases.setRefreshing(false);
                }
            });
    }


    private void queryBids() {
        ParseUser currentUser = ParseUser.getCurrentUser();
        ParseQuery query = ParseQuery.getQuery(Bid.class);
        query.include(Bid.KEY_LISTING);
        // limit query to latest 20 items
        query.setLimit(20);
        // Only displays items that have not been sold yet
        query.whereEqualTo("user", currentUser);
        // order posts by creation date (newest first)t
        query.addDescendingOrder(Listing.KEY_CREATED);
        // start an asynchronous call for posts
        query.findInBackground(new FindCallback<Bid>() {
            @Override
            public void done(List<Bid> bids, ParseException e) {
                if (e != null){
                    Log.e(TAG, "Issue with getting listings", e);
                    return;
                }
                // Clears the adapter
                bidsAdapter.clear();
                bidsAdapter.addAll(bids);

                // Save received posts to list and notify adapter of new data
                swipeBids.setRefreshing(false);
            }
        });
    }
}