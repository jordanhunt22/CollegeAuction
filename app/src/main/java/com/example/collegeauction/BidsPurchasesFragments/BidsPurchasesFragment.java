package com.example.collegeauction.BidsPurchasesFragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager2.widget.ViewPager2;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.example.collegeauction.Activities.MainActivity;
import com.example.collegeauction.Adapters.BidsAdapter;
import com.example.collegeauction.Adapters.BidsPurchasesAdapter;
import com.example.collegeauction.Adapters.HomeAdapter;
import com.example.collegeauction.Adapters.PurchasesAdapter;
import com.example.collegeauction.Models.Bid;
import com.example.collegeauction.Models.Listing;
import com.example.collegeauction.R;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

public class BidsPurchasesFragment extends Fragment {

    public static final String TAG = "BidsPurchasesFragment";

    private TabLayout tabLayout;
    private TabLayoutMediator tabLayoutMediator;
    private ViewPager2 viewPager;
    private BidsPurchasesAdapter adapter;
    private Boolean toPurchases;

    public BidsPurchasesFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_bids_purchases, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Gets the bundle if there is one
        Bundle args = getArguments();

        // Sets up the tabs
        tabLayout = view.findViewById(R.id.tabLayout);
        viewPager = view.findViewById(R.id.viewPager);
        adapter = new BidsPurchasesAdapter(this);
        viewPager.setAdapter(adapter);
        tabLayoutMediator = new TabLayoutMediator(tabLayout, viewPager, new TabLayoutMediator.TabConfigurationStrategy() {
            @Override
            public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                if (position == 0){
                    tab.setText("Bids").setIcon(R.drawable.bids);
                }
                else{
                    tab.setText("Purchases").setIcon(R.drawable.purchases);
                }
            }
        });
        tabLayoutMediator.attach();

        // viewPager.setCurrentItem(0);

        // Makes the fab visible whenever a new fragment starts
        MainActivity.fab.show();

        // Goes to the purchases tab if toPurchases is set to true
        if (toPurchases != null && toPurchases){
            new Handler().postDelayed(
                    new Runnable() {
                        @Override public void run() {
                            tabLayout.setSmoothScrollingEnabled(true);
                            tabLayout.getTabAt(1).select();
                        }
                    }, 400);
        }
    }

    public void setToPurchases(boolean bool){
        toPurchases = bool;
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        MenuItem item=menu.findItem(R.id.action_search);
        if(item != null)
            item.setVisible(false);
    }

}