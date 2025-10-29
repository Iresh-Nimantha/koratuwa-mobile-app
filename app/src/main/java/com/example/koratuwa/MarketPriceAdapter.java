package com.example.koratuwa;

import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

// Rest of your adapter class...


public class MarketPriceAdapter extends RecyclerView.Adapter<MarketPriceAdapter.MarketPriceViewHolder> {

    private final List<MarketPrice> marketPrices;
    private final Context context;

    public MarketPriceAdapter(Context context, List<MarketPrice> marketPrices) {
        this.context = context;
        this.marketPrices = marketPrices;
    }

    @Override
    public MarketPriceViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_market_price, parent, false);
        return new MarketPriceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MarketPriceViewHolder holder, int position) {
        MarketPrice item = marketPrices.get(position);

        holder.tvCropName.setText(item.getCropName());
        holder.tvLatestPrice.setText(String.format("Rs. %.2f / pcs", item.getLatestPrice()));

        if (item.getOldPrice() > 0 && item.getOldPrice() != item.getLatestPrice()) {
            holder.tvOldPrice.setVisibility(View.VISIBLE);
            holder.tvOldPrice.setText(String.format("Rs. %.2f / pcs", item.getOldPrice()));
            holder.tvOldPrice.setPaintFlags(holder.tvOldPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            holder.tvOldPrice.setVisibility(View.GONE);
        }

        Glide.with(context)
                .load(item.getImageUrl())
                .placeholder(R.drawable.ic_placeholder)
                .into(holder.ivProduct);
    }

    @Override
    public int getItemCount() {
        return marketPrices.size();
    }

    static class MarketPriceViewHolder extends RecyclerView.ViewHolder {
        ImageView ivProduct;
        TextView tvCropName, tvLatestPrice, tvOldPrice;

        MarketPriceViewHolder(View itemView) {
            super(itemView);
            ivProduct = itemView.findViewById(R.id.iv_product);
            tvCropName = itemView.findViewById(R.id.tv_crop_name);
            tvLatestPrice = itemView.findViewById(R.id.tv_latest_price);
            tvOldPrice = itemView.findViewById(R.id.tv_old_price);
        }
    }
}
