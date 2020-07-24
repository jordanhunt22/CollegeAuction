package com.example.collegeauction.Adapters;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.adapter.FragmentViewHolder;

import com.example.collegeauction.HomeFragments.NearbyHomeFragment;
import com.example.collegeauction.HomeFragments.SoonHomeFragment;

import java.util.List;

public class HomeAdapter extends FragmentStateAdapter {

    public Fragment fragment;

    public HomeAdapter(@NonNull Fragment fragment) {
        super(fragment);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position == 0){
            fragment = new SoonHomeFragment();
            return fragment;
        }
        else{
            fragment = new NearbyHomeFragment();
            return fragment;
        }
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}

