package com.example.collegeauction.DetailFragments;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.collegeauction.Miscellaneous.DateManipulator;
import com.example.collegeauction.Models.Bid;
import com.example.collegeauction.Models.Listing;
import com.example.collegeauction.ParseApplication;
import com.example.collegeauction.R;
import com.facebook.share.model.ShareHashtag;
import com.facebook.share.model.SharePhoto;
import com.facebook.share.model.SharePhotoContent;
import com.facebook.share.widget.ShareButton;
import com.facebook.share.widget.ShareDialog;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.transition.MaterialSharedAxis;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.parceler.Parcels;

import java.util.List;
import java.util.Objects;

public class BuyerDetailFragment extends Fragment {

    public static final String TAG = "BuyerDetailFragment";

    // Add handling for when an item is sold

    private Listing listing;
    private TextView tvName;
    private TextView tvDescription;
    private TextView tvLocation;
    private TextView tvCurrentBid;
    private TextView tvTime;
    private ImageView ivListingImage;
    private Button btnBid;
    private TextInputEditText etBid;
    private ShareButton sbFacebook;
    private Bitmap fbPhoto;
    private SharePhoto photo;
    private SharePhotoContent content;

    private Bid bid;
    private Bid lastBid;
    private Long minBid;
    private Long numberBid;
    private DateManipulator dateManipulator;

    // For the runnable that updates the current bid
    Runnable updater;
    final Handler timerHandler = new Handler();


    public BuyerDetailFragment(){
        // Required empty constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Sets up the transitions
        androidx.transition.Transition backward = new MaterialSharedAxis(MaterialSharedAxis.Z, false);
        androidx.transition.Transition forward  = new MaterialSharedAxis(MaterialSharedAxis.Z, true);
        setReenterTransition(backward);
        setExitTransition(forward);
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
        tvLocation = view.findViewById(R.id.tvLocation);
        tvCurrentBid = view.findViewById(R.id.tvCurrentBid);
        tvTime = view.findViewById(R.id.tvTime);
        etBid = view.findViewById(R.id.etBid);
        btnBid = view.findViewById(R.id.btnBid);
        ivListingImage = view.findViewById(R.id.ivListingImage);
        sbFacebook = view.findViewById(R.id.shareButton);

        // Gets the bundle with listing that was passed in
        Bundle args = getArguments();

        // Unwraps the listings
        listing = Parcels.unwrap(args.getParcelable("listing"));

        tvName.setText(listing.getName());
        tvDescription.setText("Description: " + listing.getDescription());
        ParseFile image = listing.getImage();
        Glide.with(getContext())
                .load(image.getUrl())
                .transform(new CenterCrop())
                .into(ivListingImage);

        if (listing.getString("locationName") != null){
            String location = listing.getString("locationName");
            tvLocation.setText("Location: " + location);
        }
        else{
            tvLocation.setText("Location: Not Available");
        }
        // Set an onClickListener for when the user submits a bid
        btnBid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (etBid.getText().toString().isEmpty()){
                    Toast.makeText(getContext(), "Your bid cannot be empty!", Toast.LENGTH_SHORT).show();
                    return;
                }
                try {
                    numberBid = Long.parseLong(etBid.getText().toString());
                } catch (NumberFormatException e) {
                    Log.e(TAG, "Your bid is not a valid number", e);
                    Toast.makeText(getContext(), "Your bid is invalid. Try again", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (numberBid <= minBid ){
                    Toast.makeText(getContext(), "Your bid is less than the current bid!", Toast.LENGTH_SHORT).show();
                    return;
                }
                else{
                    bid = new Bid();
                    bid.setUser(ParseUser.getCurrentUser());
                    bid.setPrice(numberBid);
                    bid.setListing(listing);
                    bid.put("isCurrent", true);
                    if (lastBid != null){
                        lastBid.put("isCurrent", false);
                        lastBid.saveInBackground();
                    }
                    bid.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            listing.setRecentBid(bid);
                            listing.saveInBackground();
                            tvCurrentBid.setText("$" + numberBid.toString());
                            etBid.setText("");
                            // Logs new new bids
                            Bundle bundle = new Bundle();
                            bundle.putString("item_id", listing.getObjectId());;
                            bundle.putInt("price", numberBid.intValue());
                            ParseApplication.mFireBaseAnalytics
                                    .logEvent("new_bid", bundle);
                        }
                    });
                }
            }
        });

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

        dateManipulator = new DateManipulator(listing.getExpireTime());
        updater = new Runnable() {
            @SuppressLint("SetTextI18n")
            @Override
            public void run() {
                if (System.currentTimeMillis() >= listing.getExpireTime().getTime()){
                    getActivity().finish();
                }
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
                    minBid = listing
                            .getLong("minPrice");
                    tvCurrentBid
                            .setText("$" + minBid
                                    .toString());
                }

                String date = dateManipulator.getDate();
                tvTime.setText(date);
                Log.i(TAG, "Handler is running");
                timerHandler.postDelayed(updater,1000);
            }
        };
        timerHandler.post(updater);

        // Prepares the content for the facebook share button
        Glide.with(getContext())
                .asBitmap()
                .load(listing.getImage().getUrl())
                .into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        fbPhoto = resource;
                        photo = new SharePhoto.Builder()
                                .setBitmap(fbPhoto)
                                .build();
                        content = new SharePhotoContent.Builder()
                                .addPhoto(photo)
                                .setShareHashtag(new ShareHashtag.Builder()
                                        .setHashtag("#CollegeAuction")
                                        .build())
                                .build();
                        sbFacebook.setShareContent(content);
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {
                    }
                });
        sbFacebook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Logs when a user opens the Facebook SDK to share an item
                Bundle bundle = new Bundle();
                bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "listing");
                bundle.putString(FirebaseAnalytics.Param.ITEM_ID, listing.getObjectId());
                ParseApplication.mFireBaseAnalytics
                        .logEvent(FirebaseAnalytics.Event.SHARE, bundle);
            }
        });
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