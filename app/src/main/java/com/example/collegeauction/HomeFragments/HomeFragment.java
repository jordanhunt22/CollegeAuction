package com.example.collegeauction.HomeFragments;

import android.graphics.drawable.Drawable;
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
import android.widget.Toast;

import com.example.collegeauction.Adapters.HomeAdapter;
import com.example.collegeauction.R;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class HomeFragment extends Fragment {

    private TabLayout tabLayout;
    private TabLayoutMediator tabLayoutMediator;
    private ViewPager2 viewPager;
    private HomeAdapter adapter;

    private Menu menu;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Sets up the tabs
        tabLayout = view.findViewById(R.id.tabLayout);
        viewPager = view.findViewById(R.id.viewPager);
        adapter = new HomeAdapter(this);
        viewPager.setAdapter(adapter);
        tabLayoutMediator = new TabLayoutMediator(tabLayout, viewPager, new TabLayoutMediator.TabConfigurationStrategy() {
            @Override
            public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                if (position == 0){
                    tab.setText("Expires soon").setIcon(R.drawable.clock);
                }
                else{
                    tab.setText("Nearby").setIcon(R.drawable.nearby);
                }
            }
        });
        viewPager.setCurrentItem(0);
        tabLayoutMediator.attach();
    }

    public void queryString(String query){
        if (adapter.fragment instanceof SoonHomeFragment){
            SoonHomeFragment fragment = (SoonHomeFragment) adapter.fragment;
            fragment.queryListingsFromSearch(query);
        }
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        this.menu = menu;
        super.onPrepareOptionsMenu(menu);
    }

    public void collapseMenuItem(){
        if (menu != null)
        {
            MenuItem item = menu.findItem(R.id.action_search);
            if(item!=null)
                item.collapseActionView();
        }
    }
}