package com.example.collegeauction.Adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.collegeauction.FavFragments.FavoritesFragment;
import com.example.collegeauction.FavFragments.SuggestedFragment;

public class FavoritesAdapter extends FragmentStateAdapter {

    public FavoritesAdapter(@NonNull Fragment fragment) {
        super(fragment);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position == 0){
            return new FavoritesFragment();
        }
        else {
            return new SuggestedFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}


