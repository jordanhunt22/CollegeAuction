package com.example.collegeauction.BidsPurchasesFragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.example.collegeauction.Activities.MainActivity;
import com.example.collegeauction.Adapters.BidsAdapter;
import com.example.collegeauction.Adapters.PurchasesAdapter;
import com.example.collegeauction.Models.Bid;
import com.example.collegeauction.Models.Listing;
import com.example.collegeauction.R;
import com.google.android.material.tabs.TabLayout;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

public class BidsPurchasesFragment extends Fragment {

    public static final String TAG = "BidsPurchasesFragment";

    private FragmentManager fragmentManager;

    private TabLayout tabLayout;

    Fragment fragment;

    public BidsPurchasesFragment() {
        // Required empty public constructor
    }

    public BidsPurchasesFragment(FragmentManager fragmentManager){
        this.fragmentManager = fragmentManager;
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

        tabLayout = view.findViewById(R.id.tabLayout);

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.equals(tabLayout.getTabAt(0))){
                    fragment = new BidsFragment();
                }
                else {
                    fragment = new PurchasesFragment();
                }
                fragmentManager.beginTransaction().replace(R.id.flBidsPurchases, fragment).commit();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        // Makes the fab visible whenever a new fragment starts
        MainActivity.fab.show();

        fragment = new BidsFragment();
        fragmentManager.beginTransaction().replace(R.id.flBidsPurchases, fragment).commit();

    }



}