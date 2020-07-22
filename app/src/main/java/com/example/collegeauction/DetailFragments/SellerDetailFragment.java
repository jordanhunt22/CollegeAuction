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
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.parse.DeleteCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.parceler.Parcels;

import java.util.Objects;

public class SellerDetailFragment extends Fragment {

    public static final String TAG = "SellerDetailFragment";

    private Listing listing;
    private TextView tvName;
    private TextView tvDescription;
    private TextView tvLocation;
    private TextView tvNumber;
    private TextView tvCurrentBid;
    private TextView tvTime;
    private ImageView ivListingImage;
    private Button btnDelete;

    private AlertDialog.Builder builder;

    private Bid lastBid;
    private Long minBid;
    private DateManipulator dateManipulator;

    // For the runnable that updates the current bid
    Runnable updater;
    final Handler timerHandler = new Handler();

    public SellerDetailFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_seller_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Resolve all of the view elements
        tvName = view.findViewById(R.id.tvName);
        tvDescription = view.findViewById(R.id.tvDescription);
        tvNumber = view.findViewById(R.id.tvNumber);
        tvLocation = view.findViewById(R.id.tvLocation);
        tvCurrentBid = view.findViewById(R.id.tvCurrentBid);
        tvTime = view.findViewById(R.id.tvTime);
        ivListingImage = view.findViewById(R.id.ivListingImage);
        btnDelete = view.findViewById(R.id.btnDelete);

         builder = new MaterialAlertDialogBuilder(getContext());

        // Gets the bundle with listing that was passed in
        Bundle args = getArguments();

        // Unwraps the listing
        listing = Parcels.unwrap(args.getParcelable("listing"));

        tvNumber.setVisibility(View.GONE);

        if (listing.getString("locationName") != null){
            String location = listing.getString("locationName");
            tvLocation.setText("Location: " + location);
        }
        else{
            tvLocation.setText("Location: Not Available");
        }

        tvName.setText(listing.getName());
        tvDescription.setText("Description: " + listing.getDescription());
        ParseFile image = listing.getImage();
        Glide.with(getContext())
                .load(image.getUrl())
                .transform(new CenterCrop())
                .into(ivListingImage);

        // Allows the user to delete an item
        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                builder
                        // Add customization options here
                        .setTitle("Are you sure you want to delete this listing?")
                        .setNegativeButton("YES", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                listing.deleteInBackground(new DeleteCallback() {
                                    @Override
                                    public void done(ParseException e) {
                                        Toast.makeText(getContext(), "Your listing was successfully deleted", Toast.LENGTH_SHORT)
                                                .show();
                                        getActivity().finish();
                                    }
                                });
                            }
                        })
                        .setPositiveButton("NO", null)
                        .show();
            }
        });

        dateManipulator = new DateManipulator(listing.getExpireTime());
        updater = new Runnable() {
            @SuppressLint("SetTextI18n")
            @Override
            public void run() {
                getCurrentBids();
                lastBid = (Bid) listing.getRecentBid();
                if (lastBid != null) {
                    minBid = lastBid
                            .getLong(Bid.KEY_PRICE);
                    tvCurrentBid
                            .setText("$" + Objects.requireNonNull(minBid
                                    .toString()));
                }
                else{
                    if (System.currentTimeMillis() >= listing.getExpireTime().getTime()){
                        tvTime.setText("NO SALE");
                        listing.put("isSold", true);
                        listing.saveInBackground();
                    }
                    else {
                        minBid = (Long) listing
                                .getLong("minPrice");
                        tvCurrentBid
                                .setText("$" + minBid
                                        .toString());
                    }
                }

                if(System.currentTimeMillis() >= listing.getExpireTime().getTime()){
                    tvTime.setText("Expired " + TimeFormatter
                            .getTimeDifference(listing.getDate("expiresAt").toString()) + " ago");
                    listing.put("isSold", true);
                    listing.saveInBackground();
                }
                else {
                    String date = dateManipulator.getDate();
                    tvTime.setText(date);
                }
                Log.i(TAG, "Handler is running");
                timerHandler.postDelayed(updater,1000);
            }
        };
        timerHandler.post(updater);

        if (listing.getBoolean("isSold")){
            if (listing.getRecentBid() != null){
                tvNumber.setVisibility(View.VISIBLE);
                ParseUser buyer = null;
                try {
                    buyer = listing.getRecentBid().getParseUser(Bid.KEY_USER)
                            .fetchIfNeeded();
                } catch (ParseException e) {
                    e.printStackTrace();
                    tvNumber.setText("Error getting number");
                    return;
                }
                String buyersNumber = buyer.getString("phoneNumber");
                tvNumber.setText(buyersNumber);
            }
            else{
                tvCurrentBid.setText("NO SALE");
            }
        }
    }

    public void getCurrentBids(){
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Listing");
        query.include(Listing.KEY_BID);
        // Retrieve the object by id
        query.getInBackground(listing.getObjectId(), new GetCallback<ParseObject>() {
            public void done(ParseObject cloudListing, ParseException e) {
                if (e == null) {
                    listing = (Listing) cloudListing;
                }
            }
        });

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        timerHandler.removeCallbacks(updater);
    }
}