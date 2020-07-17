package com.example.collegeauction.BidsPurchasesFragments;

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

public class PurchasesFragment extends Fragment {

    public static final String TAG = "PurchasesFragment";

    private RecyclerView rvPurchases;

    private SwipeRefreshLayout swipePurchases;

    private PurchasesAdapter purchasesAdapter;

    private List<Listing> allPurchases;


    public PurchasesFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_purchases, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Look up the RecyclerViews
        rvPurchases = view.findViewById(R.id.rvPurchases);

        // Look up the swipeContainer views
        swipePurchases = view.findViewById(R.id.swipePurchases);

        swipePurchases.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Reload all of the users purchases
                queryPurchases();
            }
        });

        allPurchases = new ArrayList<>();
        purchasesAdapter = new PurchasesAdapter(getContext(), allPurchases);

        rvPurchases.setAdapter(purchasesAdapter);

        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 1);
        rvPurchases.setLayoutManager(gridLayoutManager);

        // Makes fab disappear when scrolling
        rvPurchases.addOnScrollListener(new RecyclerView.OnScrollListener() {
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
}