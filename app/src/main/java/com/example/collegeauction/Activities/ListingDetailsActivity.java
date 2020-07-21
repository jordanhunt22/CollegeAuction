package com.example.collegeauction.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.os.Bundle;
import android.view.View;

import com.example.collegeauction.CreationFragments.CreationFragment;
import com.example.collegeauction.DetailFragments.BuyerDetailFragment;
import com.example.collegeauction.DetailFragments.MapsFragment;
import com.example.collegeauction.DetailFragments.PurchasedDetailFragment;
import com.example.collegeauction.DetailFragments.SellerDetailFragment;
import com.example.collegeauction.Models.Listing;
import com.example.collegeauction.R;
import com.example.collegeauction.databinding.ActivityCreationBinding;
import com.example.collegeauction.databinding.ActivityListingDetailsBinding;

import org.parceler.Parcels;

public class ListingDetailsActivity extends AppCompatActivity {

    final FragmentManager fragmentManager = getSupportFragmentManager();
    private Fragment fragment;
    private Listing listing;
    private String viewType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listing_details);

        // Implementing ViewBinding
        ActivityListingDetailsBinding binding = ActivityListingDetailsBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        // Find the toolbar view inside the activity layout
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        // Sets the Toolbar to act as the ActionBar for this Activity window.
        // Make sure the toolbar exists in the activity and is not null
        setSupportActionBar(toolbar);

        // Unwrap the listing
        listing = Parcels.unwrap(getIntent().getParcelableExtra(Listing.class.getSimpleName()));

        // Get whether the user is a buyer or not
        // use in listingsAdapter class --> intent.putExtra("yourBoolName", true);
        viewType = getIntent().getExtras().getString("viewType");

        // Sets the toolbar text to nothing
        getSupportActionBar().setTitle("");
        setContentView(R.layout.activity_creation);

        if (viewType.equals("seller")){
            fragment = new SellerDetailFragment();
        }
        else if (viewType.equals("buyer")){
            fragment = new BuyerDetailFragment();
        }
        else{
            fragment = new PurchasedDetailFragment();
        }

//        fragment = new MapsFragment();

        Bundle args = new Bundle();
        args.putParcelable("listing", Parcels.wrap(listing));
        fragment.setArguments(args);

        fragmentManager.beginTransaction().replace(R.id.flContainer, fragment).commit();
    }


}