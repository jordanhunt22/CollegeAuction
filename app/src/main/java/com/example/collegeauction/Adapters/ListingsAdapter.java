package com.example.collegeauction.Adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.FitCenter;
import com.example.collegeauction.Miscellaneous.DateManipulator;
import com.example.collegeauction.Models.Bid;
import com.example.collegeauction.Models.Listing;
import com.example.collegeauction.R;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseRelation;
import com.parse.ParseUser;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class ListingsAdapter extends RecyclerView.Adapter<ListingsAdapter.ViewHolder> {

    public static final String TAG = "ListingsAdapter";
    private Context context;
    private List<Listing> listings;

    public ListingsAdapter(Context context, List<Listing> listings){
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
    public void onBindViewHolder(@NonNull ListingsAdapter.ViewHolder holder, int position) {
        Listing listing = listings.get(position);
        holder.bind(listing);
    }

    @Override
    public int getItemCount() { return listings.size(); }

    class ViewHolder extends RecyclerView.ViewHolder{

        private ImageView ivImage;
        private TextView tvName;
        private TextView tvTime;
        private TextView tvBid;
        private ImageButton btnFav;

        private Listing listing;
        private DateManipulator dateManipulator;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            // Resolves the views
            ivImage = itemView.findViewById(R.id.ivImage);
            tvName = itemView.findViewById(R.id.tvName);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvBid = itemView.findViewById(R.id.tvBid);
            btnFav = itemView.findViewById(R.id.btnFav);

            ivImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Gets item position
                    int position = getAdapterPosition();
                    // Make sure the position is valid, i.e. actually exists in the view
                    if (position != RecyclerView.NO_POSITION){
                        // Open the buyer's detail view
                    }
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
                            Glide.with(context)
                                    .load(R.drawable.heart)
                                    .transform(new CenterCrop())
                                    .into(btnFav);
                            Listing.listingsFavoritedByCurrentuser.removeAll(Collections.singleton(listing.getObjectId()));
                            relation.remove(listing);
                            Log.i(TAG, "unlike");
                        }
                        else{
                            Glide.with(context)
                                    .load(R.drawable.heart_active)
                                    .transform(new CenterCrop())
                                    .into(btnFav);
                            relation.add(listing);
                            Listing.listingsFavoritedByCurrentuser.add(listing.getObjectId());
                            Log.i(TAG, "like");
                        }
                        user.saveInBackground();
                    }
                }
            });
        }

        @SuppressLint("SetTextI18n")
        public void bind(Listing listing) {
            // Create instance of date manipulator
            dateManipulator = new DateManipulator(listing.getCreatedAt());
            // Bind the listing data to the view elements
            tvName.setText(listing.getName());
            tvBid.setText("$" + Objects.requireNonNull(listing.getRecentBid().getNumber(Bid.KEY_PRICE)).toString());
            ParseFile image = listing.getImage();
            if (image != null) {
                Glide.with(context)
                        .load(image.getUrl())
                        .transform(new CenterCrop())
                        .into(ivImage);
            } else {
                //ivImage.setImageResource(android.R.color.transparent);
                Glide.with(context)
                        .load(R.drawable.ic_launcher_background)
                        .transform(new CenterCrop())
                        .into(ivImage);
            }

            if (isFavorite(listing)){
                Glide.with(context)
                        .load(R.drawable.heart_active)
                        .transform(new CenterCrop())
                        .into(btnFav);
            }
            else{
                Glide.with(context)
                        .load(R.drawable.heart)
                        .transform(new CenterCrop())
                        .into(btnFav);
            }
        }
    }

    public boolean isFavorite(Listing listing){
        if (Listing.listingsFavoritedByCurrentuser.contains(listing.getObjectId())){
            return true;
        }
        else { return false; }
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

