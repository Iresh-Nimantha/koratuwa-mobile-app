package com.example.koratuwa;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class OfficerAdapter extends RecyclerView.Adapter<OfficerAdapter.OfficerHolder> {
    private final List<Officer> officers;
    private final Context context;

    public OfficerAdapter(Context ctx, List<Officer> officers) {
        this.context = ctx;
        this.officers = officers;
    }

    @Override
    public OfficerHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_officer, parent, false);
        return new OfficerHolder(v);
    }

    @Override
    public void onBindViewHolder(OfficerHolder holder, int position) {
        Officer o = officers.get(position);
        holder.tvName.setText(o.getName());
        holder.tvRole.setText(o.getState());
        holder.tvPhone.setText(o.getPhoneNumber());
        holder.tvPhone.setOnClickListener(v -> {
            String phone = holder.tvPhone.getText().toString();
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:" + phone));
            context.startActivity(intent);
        });

        Glide.with(holder.ivProfile.getContext())
                .load(o.getProfileImageUrl())
                .placeholder(R.drawable.ic_placeholder)
                .circleCrop()
                .into(holder.ivProfile);

    }


    @Override
    public int getItemCount() {
        return officers.size();
    }

    static class OfficerHolder extends RecyclerView.ViewHolder {
        ImageView ivProfile;
        TextView tvName, tvRole, tvPhone;

        OfficerHolder(View v) {
            super(v);
            ivProfile = v.findViewById(R.id.iv_profile);
            tvName = v.findViewById(R.id.tv_name);
            tvRole = v.findViewById(R.id.tv_state);
            tvPhone = v.findViewById(R.id.tv_phone);
        }
    }
}
