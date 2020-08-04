package com.example.collegeauction.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.Window;

import com.example.collegeauction.CreationFragments.CreationFragment;
import com.example.collegeauction.DetailFragments.BuyerDetailFragment;
import com.example.collegeauction.DetailFragments.MapsFragment;
import com.example.collegeauction.DetailFragments.PurchasedDetailFragment;
import com.example.collegeauction.DetailFragments.SellerDetailFragment;
import com.example.collegeauction.Models.Listing;
import com.example.collegeauction.R;
import com.example.collegeauction.databinding.ActivityCreationBinding;
import com.example.collegeauction.databinding.ActivityListingDetailsBinding;
import com.google.android.material.transition.platform.MaterialContainerTransform;
import com.google.android.material.transition.platform.MaterialContainerTransformSharedElementCallback;

import org.parceler.Parcels;

public class ListingDetailsActivity extends AppCompatActivity {

    final FragmentManager fragmentManager = getSupportFragmentManager();
    private Fragment fragment;
    private Listing listing;
    private String viewType;
    private String sharedElementName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // Implementing ViewBinding
        ActivityListingDetailsBinding binding = ActivityListingDetailsBinding.inflate(getLayoutInflater());

        // Sets up the container transform
        View finalContainer = findViewById(android.R.id.content);

        sharedElementName = getIntent().getExtras().getString("elementName");

        finalContainer.setTransitionName(sharedElementName);

        setEnterSharedElementCallback(new MaterialContainerTransformSharedElementCallback());

        MaterialContainerTransform materialContainerTransform = new MaterialContainerTransform();
        materialContainerTransform.addTarget(finalContainer);
        materialContainerTransform.setAllContainerColors(Color.WHITE);
        materialContainerTransform.setFadeMode(MaterialContainerTransform.FADE_MODE_OUT);
        materialContainerTransform.setScrimColor(Color.WHITE);
        materialContainerTransform.setDuration(500L);
        getWindow().setSharedElementEnterTransition(materialContainerTransform);

        MaterialContainerTransform materialContainerTransformReverse = new MaterialContainerTransform();
        materialContainerTransformReverse.addTarget(finalContainer);
        materialContainerTransformReverse.setFadeMode(MaterialContainerTransform.FADE_MODE_OUT);
        materialContainerTransformReverse.setScrimColor(Color.WHITE);
        materialContainerTransformReverse.setDuration(450L);
        materialContainerTransform.setAllContainerColors(Color.WHITE);

        getWindow().setSharedElementReturnTransition(materialContainerTransformReverse);

        super.onCreate(savedInstanceState);

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

        Bundle args = new Bundle();
        args.putParcelable("listing", Parcels.wrap(listing));
        fragment.setArguments(args);

        fragmentManager.beginTransaction().replace(R.id.flContainer, fragment).commit();
    }


}