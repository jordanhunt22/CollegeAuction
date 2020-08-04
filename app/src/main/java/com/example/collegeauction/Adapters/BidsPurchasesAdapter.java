package com.example.collegeauction.Adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.collegeauction.BidsPurchasesFragments.BidsFragment;
import com.example.collegeauction.BidsPurchasesFragments.PurchasesFragment;

public class BidsPurchasesAdapter extends FragmentStateAdapter {

    public BidsPurchasesAdapter(@NonNull Fragment fragment) {
        super(fragment);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position == 0){
            return new BidsFragment();
        }
        else{
            return new PurchasesFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}

