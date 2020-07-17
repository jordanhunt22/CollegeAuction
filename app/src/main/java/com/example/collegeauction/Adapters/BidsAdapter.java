package com.example.collegeauction.Adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.collegeauction.Miscellaneous.TimeFormatter;
import com.example.collegeauction.Models.Bid;
import com.example.collegeauction.Models.Listing;
import com.example.collegeauction.R;

import java.util.List;

public class BidsAdapter extends RecyclerView.Adapter<BidsAdapter.ViewHolder> {

    private Context context;
    private List<Bid> bids;

    public BidsAdapter(Context context, List<Bid> bids){
        this.context = context;
        this.bids = bids;
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

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            // Resolves all of the views
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvName = itemView.findViewById(R.id.tvName);
            tvTime = itemView.findViewById(R.id.tvTime);
        }

        @SuppressLint("SetTextI18n")
        public void bind(Bid bid) {
            tvTime.setText(TimeFormatter.getTimeDifference(bid.getCreatedAt().toString()));
            tvName.setText(bid.getListing().getString(Listing.KEY_NAME));
            tvPrice.setText("$" + bid.getPrice().toString());
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

