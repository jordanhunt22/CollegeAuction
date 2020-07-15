package com.example.collegeauction.DetailFragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.example.collegeauction.Models.Listing;
import com.example.collegeauction.R;
import com.parse.ParseFile;

import org.parceler.Parcels;

public class BuyerDetailFragment extends Fragment {

    private Listing listing;
    private TextView tvName;
    private TextView tvDescription;
    private ImageView ivListingImage;

    public BuyerDetailFragment(){
        // Required empty constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the view
        return inflater.inflate(R.layout.fragment_buyer_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Resolve all of the view elements
        tvName = view.findViewById(R.id.tvName);
        tvDescription = view.findViewById(R.id.tvDescription);
        ivListingImage = view.findViewById(R.id.ivListingImage);

        // Gets the bundle with listing that was passed in
        Bundle args = getArguments();

        // Unwraps the listings
        listing = Parcels.unwrap(args.getParcelable("listing"));

        tvName.setText(listing.getName());
        tvDescription.setText(listing.getDescription());
        ParseFile image = listing.getImage();
        Glide.with(getContext())
                .load(image.getUrl())
                .transform(new CenterCrop())
                .into(ivListingImage);
    }
}