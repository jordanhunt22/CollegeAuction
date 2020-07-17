package com.example.collegeauction.Adapters;

import android.content.Context;
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
import com.example.collegeauction.Miscellaneous.TimeFormatter;
import com.example.collegeauction.Models.Bid;
import com.example.collegeauction.Models.Listing;
import com.example.collegeauction.R;
import com.google.android.material.textfield.TextInputEditText;
import com.parse.ParseFile;

import java.util.List;
import java.util.Objects;

public class PurchasesAdapter extends RecyclerView.Adapter<PurchasesAdapter.ViewHolder> {

    private Context context;
    private List<Listing> purchases;

    public PurchasesAdapter(Context context, List<Listing> purchases){
        this.context = context;
        this.purchases = purchases;
    }

    @NonNull
    @Override
    public PurchasesAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_purchase, parent, false);
        return new PurchasesAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PurchasesAdapter.ViewHolder holder, int position) {
        Listing purchase = purchases.get(position);
        holder.bind(purchase);
    }

    @Override
    public int getItemCount() { return purchases.size(); }

    public class ViewHolder extends RecyclerView.ViewHolder {

        // Create variables for all of the views
        private TextView tvName;
        private TextView tvTime;
        private TextView tvBid;
        private ImageView ivImage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            // Resolve all of the view elements
            tvName = itemView.findViewById(R.id.tvName);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvBid = itemView.findViewById(R.id.tvBid);
            ivImage = itemView.findViewById(R.id.ivImage);
        }

        public void bind(Listing purchase) {

            // Bind the listing data to the view elements
            tvName.setText(purchase.getName());

            ParseFile image = purchase.getImage();
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

            String timeStamp = TimeFormatter
                    .getTimeDifference(Objects
                            .requireNonNull(purchase.getDate("expiresAt"))
                            .toString());
            tvTime.setText(timeStamp);
        }
    }

    // Clean all elements of the recyclerview
    public void clear() {
        purchases.clear();
        notifyDataSetChanged();
    }

    // Add a list of items -- change to type used
    public void addAll(List<Listing> allPurchases) {
        purchases.addAll(allPurchases);
        notifyDataSetChanged();
    }
}

