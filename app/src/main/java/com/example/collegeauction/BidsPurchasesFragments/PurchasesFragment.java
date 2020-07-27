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
import android.widget.TextView;

import com.example.collegeauction.Activities.MainActivity;
import com.example.collegeauction.Adapters.BidsAdapter;
import com.example.collegeauction.Adapters.PurchasesAdapter;
import com.example.collegeauction.Miscellaneous.EndlessRecyclerViewScrollListener;
import com.example.collegeauction.Models.Bid;
import com.example.collegeauction.Models.Listing;
import com.example.collegeauction.Models.Purchase;
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

public class PurchasesFragment extends Fragment {

    public static final String TAG = "PurchasesFragment";

    private RecyclerView rvPurchases;
    private TextView tvEmpty;
    private SwipeRefreshLayout swipePurchases;
    private PurchasesAdapter purchasesAdapter;
    private List<Purchase> allPurchases;
    private EndlessRecyclerViewScrollListener scrollListener;


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

        tvEmpty = view.findViewById(R.id.tvEmpty);
        tvEmpty.setVisibility(View.GONE);

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

        swipePurchases.setRefreshing(true);

        allPurchases = new ArrayList<>();
        purchasesAdapter = new PurchasesAdapter(getContext(), allPurchases);

        rvPurchases.setAdapter(purchasesAdapter);

        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 2);
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
        rvPurchases.addOnScrollListener(scrollListener);

        queryPurchases();
    }

    private void loadMoreData() {
        final ParseUser currentUser = ParseUser.getCurrentUser();
        ParseQuery query = ParseQuery.getQuery(Purchase.class);
        query.include("listing");
        query.include("listing.user");
        query.include("finalBid");
        query.setSkip(purchasesAdapter.getItemCount());
        query.include("listing.mostRecentBid");
        query.setLimit(10);
        query.whereEqualTo("buyer", currentUser);
        query.addDescendingOrder("createdAt");
        query.findInBackground(new FindCallback<Purchase>() {
            @Override
            public void done(List<Purchase> purchases, ParseException e) {

                if (e != null) {
                    Log.e(TAG, "Error with getting purchases: " + e);
                    e.printStackTrace();
                    return;
                }

                List<Purchase> returnPurchases = new ArrayList<>();
                for (Purchase purchase : purchases) {
                    if (!purchasesAdapter.purchaseIds.contains(purchase.getObjectId())){
                        returnPurchases.add(purchase);
                    }
                }

                purchasesAdapter.addAll(returnPurchases);

                // Save received posts to list and notify adapter of new data
                swipePurchases.setRefreshing(false);

                // Save received posts to list and notify adapter of new data
                swipePurchases.setRefreshing(false);

            }
        });
    }

    private void queryPurchases() {
        final ParseUser currentUser = ParseUser.getCurrentUser();
        ParseQuery query = ParseQuery.getQuery(Purchase.class);
        query.include("listing");
        query.include("listing.user");
        query.include("finalBid");
        query.include("listing.mostRecentBid");
        query.setLimit(10);
        query.whereEqualTo("buyer", currentUser);
        query.addDescendingOrder("createdAt");
        query.findInBackground(new FindCallback<Purchase>() {
            @Override
            public void done(List<Purchase> purchases, ParseException e) {

                if (e != null){
                    Log.e(TAG, "Error with getting purchases: " + e);
                    e.printStackTrace();
                    return;
                }

                purchasesAdapter.clear();
                purchasesAdapter.addAll(purchases);

                // Save received posts to list and notify adapter of new data
                swipePurchases.setRefreshing(false);

                if (purchases.isEmpty()){
                    tvEmpty.setVisibility(View.VISIBLE);
                }
                else{
                    tvEmpty.setVisibility(View.GONE);
                }

                // Save received posts to list and notify adapter of new data
                swipePurchases.setRefreshing(false);

            }
        });
    }
}