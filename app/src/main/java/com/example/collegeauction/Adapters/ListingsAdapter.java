package com.example.collegeauction.Adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.view.GestureDetectorCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.example.collegeauction.Activities.ListingDetailsActivity;
import com.example.collegeauction.Miscellaneous.DateManipulator;
import com.example.collegeauction.Models.Bid;
import com.example.collegeauction.Models.Favorite;
import com.example.collegeauction.Models.Listing;
import com.example.collegeauction.R;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;

import org.parceler.Parcels;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class ListingsAdapter extends RecyclerView.Adapter<ListingsAdapter.ViewHolder> {

    public static final String TAG = "ListingsAdapter";
    private Context context;
    private List<Listing> listings;

    public ListingsAdapter(Context context, List<Listing> listings) {
        this.context = context;
        this.listings = listings;
    }

    @NonNull
    @Override
    public ListingsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_listing, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onViewRecycled(@NonNull ViewHolder holder) {
        super.onViewRecycled(holder);
        holder.timerHandler.removeCallbacks(holder.updater);
    }

    @Override
    public void onBindViewHolder(@NonNull ListingsAdapter.ViewHolder holder, int position) {
        Listing listing = listings.get(position);
        holder.bind(listing);
    }

    @Override
    public int getItemCount() {
        return listings.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        private ImageView ivImage;
        private TextView tvName;
        private TextView tvTime;
        private TextView tvBid;
        private ImageButton btnFav;
        private Runnable updater;
        final Handler timerHandler = new Handler();
        private GestureDetectorCompat gestureDetector;

        private Listing listing;
        private DateManipulator dateManipulator;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            // Creates the gesture detector
            gestureDetector = new GestureDetectorCompat(context, new MyGestureListener());

            // Resolves the views
            ivImage = itemView.findViewById(R.id.ivImage);
            tvName = itemView.findViewById(R.id.tvName);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvBid = itemView.findViewById(R.id.tvBid);
            btnFav = itemView.findViewById(R.id.btnFav);

            itemView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    gestureDetector.onTouchEvent(motionEvent);
                    return true;
                }
            });

            btnFav.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Gets item position
                    int position = getAdapterPosition();
                    // Make sure the position is valid, i.e. actually exists in the view
                    if (position != RecyclerView.NO_POSITION) {
                        ParseUser user = ParseUser.getCurrentUser();
                        ParseRelation<ParseObject> relation = user.getRelation("favoritedListings");
                        // Get the listing at the position, this won't work if the class is static
                        listing = listings.get(position);
                        // Checks to see if the clicked listing is favorited
                        if (isFavorite(listing)) {
                            btnFav.setBackground(context.getDrawable(R.drawable.star));
                            Listing.listingsFavoritedByCurrentuser.removeAll(Collections.singleton(listing.getObjectId()));
                            ParseQuery query = ParseQuery.getQuery(Favorite.class);
                            query.whereEqualTo(Favorite.KEY_USER, user);
                            query.whereEqualTo(Favorite.KEY_LISTING, listing);
                            query.findInBackground(new FindCallback<Favorite>() {
                                @Override
                                public void done(List<Favorite> favorites, ParseException e) {
                                    for (Favorite favorite : favorites)
                                        favorite.deleteInBackground();
                                }
                            });
                            Log.i(TAG, "unfavorite");
                        } else {
                            btnFav.setBackground(context.getDrawable(R.drawable.star_active));
                            Listing.listingsFavoritedByCurrentuser.add(listing.getObjectId());
                            Favorite favorite = new Favorite();
                            favorite.setListing(listing);
                            favorite.setUser(user);
                            favorite.saveInBackground();
                            Log.i(TAG, "favorite");
                        }
                    }
                }
            });
        }

        @SuppressLint("SetTextI18n")
        public void bind(final Listing listing) {
            // Create instance of date manipulator
            if (listing.getExpireTime() != null) {
                dateManipulator = new DateManipulator(listing.getExpireTime());
                updater = new Runnable() {
                    @Override
                    public void run() {
                        if (System.currentTimeMillis() >= listing.getExpireTime().getTime()) {
                            listing.put("isSold", true);
                            listing.saveInBackground();
                            listings.removeAll(Collections.singleton(listing));
                            notifyDataSetChanged();
                        }
                        listing.fetchInBackground();
                        if (listing.getRecentBid() != null) {
                            listing.getRecentBid().fetchIfNeededInBackground(new GetCallback<Bid>() {
                                @Override
                                public void done(Bid bid, ParseException e) {
                                    tvBid.setText("$" + Objects.requireNonNull(bid.getNumber(Bid.KEY_PRICE)).toString());
                                }
                            });
                        } else {
                            tvBid.setText("$" + listing.getNumber("minPrice").toString());
                        }
                        String date = dateManipulator.getDate();
                        tvTime.setText(date);
                        timerHandler.postDelayed(updater, 1000);
                    }
                };
                timerHandler.post(updater);
            }
            // Bind the listing data to the view elements
            tvName.setText(listing.getName());
            if (listing.getRecentBid() != null) {
                tvBid.setText("$" + Objects.requireNonNull(listing.getRecentBid().getNumber(Bid.KEY_PRICE)).toString());
            } else {
                tvBid.setText("$" + listing.getNumber("minPrice").toString());
            }
            ParseFile image = listing.getImage();
            if (image != null) {
                Glide.with(context)
                        .load(image.getUrl())
                        .transform(new CenterCrop())
                        .into(ivImage);
            } else {
                Glide.with(context)
                        .load(R.drawable.ic_launcher_background)
                        .transform(new CenterCrop())
                        .into(ivImage);
            }

            if (isFavorite(listing)) {
                btnFav.setBackground(context.getDrawable(R.drawable.star_active));
            } else {
                btnFav.setBackground(context.getDrawable(R.drawable.star));
            }
        }
        class MyGestureListener extends GestureDetector.SimpleOnGestureListener {
            private static final String DEBUG_TAG = "Gestures";

            @Override
            public boolean onDown(MotionEvent e) {
                Log.i(TAG, "Down Tap");
                return true;
            }

            @Override
            public boolean onDoubleTap(MotionEvent e) {
                Log.i(TAG, "Double Tap");
                // Gets item position
                int position = getAdapterPosition();
                // Make sure the position is valid, i.e. actually exists in the view
                if (position != RecyclerView.NO_POSITION) {
                    ParseUser user = ParseUser.getCurrentUser();
                    ParseRelation<ParseObject> relation = user.getRelation("favoritedListings");
                    // Get the listing at the position, this won't work if the class is static
                    listing = listings.get(position);
                    // Checks to see if the clicked listing is favorited
                    if (isFavorite(listing)) {
                        btnFav.setBackground(context.getDrawable(R.drawable.star));
                        Listing.listingsFavoritedByCurrentuser.removeAll(Collections.singleton(listing.getObjectId()));
                        ParseQuery query = ParseQuery.getQuery(Favorite.class);
                        query.whereEqualTo(Favorite.KEY_USER, user);
                        query.whereEqualTo(Favorite.KEY_LISTING, listing);
                        query.findInBackground(new FindCallback<Favorite>() {
                            @Override
                            public void done(List<Favorite> favorites, ParseException e) {
                                for (Favorite favorite : favorites)
                                    favorite.deleteInBackground();
                            }
                        });
                        Log.i(TAG, "unfavorite");
                    } else {
                        btnFav.setBackground(context.getDrawable(R.drawable.star_active));
                        Listing.listingsFavoritedByCurrentuser.add(listing.getObjectId());
                        Favorite favorite = new Favorite();
                        favorite.setListing(listing);
                        favorite.setUser(user);
                        favorite.saveInBackground();
                        Log.i(TAG, "favorite");
                    }
                }
                return true;
            }

            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                Log.i(TAG, "Single Tap");
                ParseUser currentUser = ParseUser.getCurrentUser();
                // Gets item position
                int position = getAdapterPosition();
                // Make sure the position is valid, i.e. actually exists in the view
                if (position != RecyclerView.NO_POSITION) {
                    // Get the listing at the position, this won't work if the class is static
                    listing = listings.get(position);

                    // Create a new intent
                    Intent intent = new Intent(context, ListingDetailsActivity.class);

                    // Serialize the Post using parceler, use its short name as a key
                    intent.putExtra(Listing.class.getSimpleName(), Parcels.wrap(listing));

                    // open the buyer's detail view
                    intent.putExtra("viewType", "buyer");
                    // Start the DetailsActivity
                    context.startActivity(intent);
                }
                return true;
            }
        }

    }

    // Checks to see if the current user has favorited this post
    public boolean isFavorite(Listing listing) {
        if (Listing.listingsFavoritedByCurrentuser.contains(listing.getObjectId())) {
            return true;
        } else {
            return false;
        }
    }

    // Clean all elements of the recyclerview
    public void clear() {
        listings.clear();
        notifyDataSetChanged();
    }

    // Add a list of items -- change to type used
    public void addAll(List<Listing> allPosts) {
        listings.addAll(allPosts);
        notifyDataSetChanged();
    }



}

