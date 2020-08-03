package com.example.collegeauction.Adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.Image;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.view.GestureDetectorCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.example.collegeauction.Miscellaneous.TimeFormatter;
import com.example.collegeauction.Models.Bid;
import com.example.collegeauction.Models.Listing;
import com.example.collegeauction.R;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.parse.ParseFile;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class BidsAdapter extends RecyclerView.Adapter<BidsAdapter.ViewHolder> {

    public static final String TAG = "BidsAdapter";

    private Context context;
    private List<Bid> bids;
    public List<String> bidIds;

    public BidsAdapter(Context context, List<Bid> bids){
        this.context = context;
        this.bids = bids;
        this.bidIds = new ArrayList<>();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_bid, parent, false);
        return new BidsAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Bid bid = bids.get(position);
        holder.bind(bid);
    }

    @Override
    public int getItemCount() {
        return bids.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        // Create variables for all of the views
        private TextView tvPrice;
        private TextView tvName;
        private TextView tvTime;

        // Creates an instance of the gesture detector
        private GestureDetectorCompat gestureDetector;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            // Creates the gesture detector
            gestureDetector = new GestureDetectorCompat(context, new BidsAdapter.ViewHolder.MyGestureListener());

            // Resolves all of the views
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvName = itemView.findViewById(R.id.tvName);
            tvTime = itemView.findViewById(R.id.tvTime);

            itemView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    gestureDetector.onTouchEvent(motionEvent);
                    return true;
                }
            });
        }

        @SuppressLint("SetTextI18n")
        public void bind(Bid bid) {
            bidIds.removeAll(Collections.singleton(bid.getObjectId()));
            bidIds.add(bid.getObjectId());
            tvTime.setText(TimeFormatter.getTimeDifference(bid.getCreatedAt().toString()));
            tvName.setText(bid.getListing().getString(Listing.KEY_NAME));
            tvPrice.setText("$" + bid.getPrice().toString());
        }

        class MyGestureListener extends GestureDetector.SimpleOnGestureListener{
            @Override
            public boolean onDown(MotionEvent e) {
                return true;
            }

            @Override
            public void onLongPress(MotionEvent e) {
                Log.i(TAG, "Long press");

                int position = getAdapterPosition();

                if (position != RecyclerView.NO_POSITION){
                    Bid bid = bids.get(position);
                    ParseFile image = bid.getListing().getParseFile("photo");
                    String name = bid.getListing().getString("name");
                    AlertDialog.Builder builder = new MaterialAlertDialogBuilder(context);

                    // Inflate xml for the bids details view
                    View detailView = LayoutInflater.from(context)
                            .inflate(R.layout.bids_detail_dialog, null);
                    ImageView ivListingImage = detailView.findViewById(R.id.ivListingImage);
                    TextView tvName = detailView.findViewById(R.id.tvName);

                    if (image != null) {
                        Glide.with(context)
                                .load(image.getUrl())
                                .transform(new CenterCrop())
                                .into(ivListingImage);
                    }
                    tvName.setText(name);

                    // Create dialog builder
                    builder
                            .setView(detailView)
                            .show();
                }
            }
        }
    }

    // Clean all elements of the recyclerview
    public void clear() {
        bids.clear();
        notifyDataSetChanged();
    }

    // Add a list of items -- change to type used
    public void addAll(List<Bid> allBids) {
        bids.addAll(allBids);
        notifyDataSetChanged();
    }
}

