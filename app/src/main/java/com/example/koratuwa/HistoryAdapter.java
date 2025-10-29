package com.example.koratuwa;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder> {

    private List<com.example.koratuwa.SellPost> sellPosts;

    public HistoryAdapter(List<com.example.koratuwa.SellPost> sellPosts) {
        this.sellPosts = sellPosts;
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_history_post, parent, false);
        return new HistoryViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull HistoryAdapter.HistoryViewHolder holder, int position) {
        SellPost post = sellPosts.get(position);

        holder.tvTitle.setText(post.getCropType());
        holder.tvQuantity.setText(post.getQuantity() + " kg");
        holder.tvRequestDate.setText("Request posted on " + formatDate(post.getTimestamp()));

        String status = post.getStatus().toLowerCase();

        switch (status) {
            case "approved":
//                holder.btnStatus.setText("Approved");
                holder.btnStatus.setBackgroundResource(R.drawable.bg_status_approved);  // green background drawable
//                holder.btnStatus.setTextColor(Color.WHITE);
                break;
            case "rejected":
//                holder.btnStatus.setText("Cancelled");
                holder.btnStatus.setBackgroundResource(R.drawable.bg_status_rejected);  // red background drawable
//                holder.btnStatus.setTextColor(Color.WHITE);
                break;
            case "pending":
            default:
//                holder.btnStatus.setText("Pending");
                holder.btnStatus.setBackgroundResource(R.drawable.bg_status_pending);  // yellow background drawable
//                holder.btnStatus.setTextColor(Color.WHITE);
                break;
        }
    }



    private String capitalize(String text) {
        if (text == null || text.isEmpty()) return "";
        return text.substring(0, 1).toUpperCase(Locale.getDefault()) + text.substring(1);
    }

    private String formatDate(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("d/M/yyyy", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    @Override
    public int getItemCount() {
        return sellPosts.size();
    }

    static class HistoryViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvQuantity, tvRequestDate;
        Button btnStatus;

        public HistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            tvRequestDate = itemView.findViewById(R.id.tvRequestDate);
            btnStatus = itemView.findViewById(R.id.btnStatus);
        }
    }
}
