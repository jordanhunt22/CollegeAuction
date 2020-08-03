package com.example.collegeauction.Adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.example.collegeauction.Activities.ListingDetailsActivity;
import com.example.collegeauction.Miscellaneous.DateManipulator;
import com.example.collegeauction.Miscellaneous.TimeFormatter;
import com.example.collegeauction.Models.Bid;
import com.example.collegeauction.Models.Listing;
import com.example.collegeauction.Models.Purchase;
import com.example.collegeauction.R;
import com.google.android.material.textfield.TextInputEditText;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class PurchasesAdapter extends RecyclerView.Adapter<PurchasesAdapter.ViewHolder> {

    private Context context;
    private List<Purchase> purchases;
    public List<String> purchaseIds;

    public PurchasesAdapter(Context context, List<Purchase> purchases){
        this.context = context;
        this.purchases = purchases;
        this.purchaseIds = new ArrayList<>();
    }

    @NonNull
    @Override
    public PurchasesAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_purchase, parent, false);
        return new PurchasesAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PurchasesAdapter.ViewHolder holder, int position) {
        holder.bind(position);
    }

    @Override
    public int getItemCount() { return purchases.size(); }

    public class ViewHolder extends RecyclerView.ViewHolder {

        // Create variables for all of the views
        private TextView tvName;
        private TextView tvTime;
        private TextView tvBid;
        private ImageView ivImage;

        private Purchase purchase;
        private Listing listing;

        private Runnable updater;
        final Handler timerHandler = new Handler();
        private DateManipulator dateManipulator;

        public ViewHolder(@NonNull final View itemView) {
            super(itemView);

            // Resolve all of the view elements
            tvName = itemView.findViewById(R.id.tvName);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvBid = itemView.findViewById(R.id.tvBid);
            ivImage = itemView.findViewById(R.id.ivImage);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ParseUser currentUser = ParseUser.getCurrentUser();
                    // Gets item position
                    int position = getAdapterPosition();
                    // Make sure the position is valid, i.e. actually exists in the view
                    if (position != RecyclerView.NO_POSITION){
                        // Get the listing at the position, this won't work if the class is static
                        purchase = purchases.get(position);

                        // Create a new intent
                        Intent intent = new Intent(context, ListingDetailsActivity.class);

                        // Serialize the Post using parceler, use its short name as a key
                        intent.putExtra(Listing.class.getSimpleName(), Parcels.wrap(purchase.getListing()));

                        // adds the name of the shared element container
                        intent.putExtra("elementName", "shared_item_purchase");

                        if (currentUser.getObjectId().equals(purchase.getSeller().getObjectId())){
                            // Open the seller's detail view
                            intent.putExtra("viewType", "seller");
                        }
                        else{
                            // Open the buyer's detail view
                            intent.putExtra("viewType", "purchased");
                        }
                        // Sets up the container transform
                        ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(
                                (Activity) context,
                                itemView,
                                "shared_item_purchase");
                        // Start the DetailsActivity
                        context.startActivity(intent, options.toBundle());
                    }
                }
            });
        }

        @SuppressLint("SetTextI18n")
        public void bind(int position) {

            // Gets the listing at the position of the ViewHolder
            purchase = purchases.get(position);

            listing = (Listing) purchase.getListing();

            // Adds to object Id to list
            purchaseIds.removeAll(Collections.singleton(purchase.getObjectId()));
            purchaseIds.add(purchase.getObjectId());

            // Bind the listing data to the view elements
            tvName.setText(listing.getName());

            if (purchase.getBid() != null){
                tvBid.setText("$" + purchase.getBid().getNumber(Bid.KEY_PRICE).toString());
            }
            else if(System.currentTimeMillis() <= listing.getExpireTime().getTime()){
                tvBid.setText("$" + listing.getMinPrice().toString());
            }
            else{
                tvBid.setText("NO SALE");
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

            // Create instance of date manipulator
            if (listing.getExpireTime() != null && !listing.getBoolean("isSold")) {
                dateManipulator = new DateManipulator(listing.getExpireTime());
                updater = new Runnable() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void run() {
                         if(System.currentTimeMillis() >= listing.getExpireTime().getTime()){
                             tvTime.setText("Expired " + TimeFormatter
                                     .getTimeDifference(listing.getExpireTime().toString()) + " ago");
                             return;
                         }
                         else {
                             String date = dateManipulator.getDate();
                             tvTime.setText(date);
                         }

                        // purchase.fetchInBackground();
                        if (purchase.getBid() != null) {
                            purchase.getBid().fetchIfNeededInBackground(new GetCallback<Bid>() {
                                @Override
                                public void done(Bid bid, ParseException e) {
                                    tvBid.setText("$" + Objects.requireNonNull(bid.getNumber(Bid.KEY_PRICE)).toString());
                                }
                            });
                        } else {
                            tvBid.setText("$" + listing.getNumber("minPrice").toString());
                        }
                        timerHandler.postDelayed(updater,1000);
                    }
                };
                timerHandler.post(updater);
            }
            // If the listing is sold,
            else{
                tvTime.setText("Expired " + TimeFormatter
                        .getTimeDifference(listing.getExpireTime().toString()) + " ago");
            }
        }
    }

    // Clean all elements of the recyclerview
    public void clear() {
        purchases.clear();
        notifyDataSetChanged();
    }

    // Add a list of items -- change to type used
    public void addAll(List<Purchase> allPurchases) {
        purchases.addAll(allPurchases);
        notifyDataSetChanged();
    }
}

