package com.example.collegeauction.DetailFragments;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.example.collegeauction.Miscellaneous.DateManipulator;
import com.example.collegeauction.Miscellaneous.TimeFormatter;
import com.example.collegeauction.Models.Bid;
import com.example.collegeauction.Models.Listing;
import com.example.collegeauction.R;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.parse.DeleteCallback;
import com.parse.ParseException;
import com.parse.ParseFile;

import org.parceler.Parcels;

import java.util.Objects;

public class PurchasedDetailFragment extends Fragment {

    public static final String TAG = "PurchasedDetailFragment";

    private Listing listing;
    private TextView tvName;
    private TextView tvDescription;
    private TextView tvLocation;
    private TextView tvNumber;
    private TextView tvCurrentBid;
    private TextView tvTime;
    private ImageView ivListingImage;

    private String sellersNumber;

    private AlertDialog.Builder builder;

    private Bid lastBid;
    private Long minBid;
    private DateManipulator dateManipulator;

    // For the runnable that updates the current bid
    Runnable updater;
    final Handler timerHandler = new Handler();

    public PurchasedDetailFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_purchased_detail, container, false);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Resolve all of the view elements
        tvName = view.findViewById(R.id.tvName);
        tvDescription = view.findViewById(R.id.tvDescription);
        tvLocation = view.findViewById(R.id.tvLocation);
        tvCurrentBid = view.findViewById(R.id.tvCurrentBid);
        tvTime = view.findViewById(R.id.tvTime);
        tvNumber = view.findViewById(R.id.tvNumber);
        ivListingImage = view.findViewById(R.id.ivListingImage);

        builder = new MaterialAlertDialogBuilder(getContext());

        // Gets the bundle with listing that was passed in
        Bundle args = getArguments();

        if (listing.getString("locationName") != null){
            String location = listing.getString("locationName");
            tvLocation.setText("Location: " + location);
        }
        else{
            tvLocation.setText("Location: Not Available");
        }

        // Unwraps the listings
        listing = Parcels.unwrap(args.getParcelable("listing"));

        tvName.setText(listing.getName());
        tvDescription.setText("Description: " + listing.getDescription());
        ParseFile image = listing.getImage();
        Glide.with(getContext())
                .load(image.getUrl())
                .transform(new CenterCrop())
                .into(ivListingImage);


        dateManipulator = new DateManipulator(listing.getExpireTime());

        lastBid = (Bid) listing.getRecentBid();
        minBid = lastBid
                .getLong(Bid.KEY_PRICE);
        tvCurrentBid
                .setText("$" + Objects.requireNonNull(minBid
                        .toString()));

        tvTime.setText("Expired " + TimeFormatter
                .getTimeDifference(listing.getDate("expiresAt").toString()) + " ago");

        sellersNumber = listing.getUser().getString("phoneNumber");

        tvNumber.setText("Seller's Number: " + sellersNumber);

        // Allows the user to see the location
        tvLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listing.getLocation() != null){
                    Fragment fragment = new MapsFragment();
                    Bundle bundle = new Bundle();
                    bundle.putParcelable("listing", Parcels.wrap(listing));
                    fragment.setArguments(bundle);

                    getFragmentManager()
                            .beginTransaction()
                            .addToBackStack(null)
                            .replace(R.id.flContainer, fragment)
                            .commit();
                }
                else{
                    Toast.makeText(getContext(), "This listing has no location", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}