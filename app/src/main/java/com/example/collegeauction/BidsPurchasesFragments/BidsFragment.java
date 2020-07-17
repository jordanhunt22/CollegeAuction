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

public class BidsFragment extends Fragment {

    public static final String TAG = "BidsFragment";

    private RecyclerView rvBids;

    private SwipeRefreshLayout swipeBids;

    private BidsAdapter bidsAdapter;

    private List<Bid> allBids;

    public BidsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_bids, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Look up the RecyclerViews
        rvBids = view.findViewById(R.id.rvBids);

        // Look up the swipeContainer views
        swipeBids = view.findViewById(R.id.swipeBids);

        // Set up the onRefreshListeners
        swipeBids.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Reload all of the bids
                queryBids();
            }
        });


        allBids = new ArrayList<>();
        bidsAdapter = new BidsAdapter(getContext(), allBids);

        rvBids.setAdapter(bidsAdapter);

        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 1);
        rvBids.setLayoutManager(gridLayoutManager);

        // Makes fab disappear when scrolling
        rvBids.addOnScrollListener(new RecyclerView.OnScrollListener() {
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

        queryBids();
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