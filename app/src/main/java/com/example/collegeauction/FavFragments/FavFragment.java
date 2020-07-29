package com.example.collegeauction.FavFragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.example.collegeauction.Adapters.FavoritesAdapter;
import com.example.collegeauction.Adapters.HomeAdapter;
import com.example.collegeauction.R;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class FavFragment extends Fragment {

    private TabLayout tabLayout;
    private TabLayoutMediator tabLayoutMediator;
    private ViewPager2 viewPager;
    private FavoritesAdapter adapter;

    public FavFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_fav, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Sets up the tabs
        tabLayout = view.findViewById(R.id.tabLayout);
        viewPager = view.findViewById(R.id.viewPager);
        adapter = new FavoritesAdapter(this);
        viewPager.setAdapter(adapter);
        tabLayoutMediator = new TabLayoutMediator(tabLayout, viewPager, new TabLayoutMediator.TabConfigurationStrategy() {
            @Override
            public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                if (position == 0){
                    tab.setText("Favorites").setIcon(R.drawable.star);
                }
                else{
                    tab.setText("Discover").setIcon(R.drawable.discover);
                }
            }
        });
        viewPager.setCurrentItem(0);
        tabLayoutMediator.attach();
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        MenuItem item=menu.findItem(R.id.action_search);
        if(item != null)
            item.setVisible(false);
    }
}