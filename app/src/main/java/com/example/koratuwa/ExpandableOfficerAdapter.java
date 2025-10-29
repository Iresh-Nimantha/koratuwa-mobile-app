package com.example.koratuwa;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class ExpandableOfficerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final List<OfficerGroup> groups;
    private final Context context;

    private static final int TYPE_DISTRICT = 0;
    private static final int TYPE_OFFICER = 1;

    public ExpandableOfficerAdapter(Context ctx, List<OfficerGroup> groups) {
        this.context = ctx;
        this.groups = groups;
    }

    @Override
    public int getItemViewType(int position) {
        int count = 0;
        for (OfficerGroup group : groups) {
            if (count == position) return TYPE_DISTRICT;
            count++;
            if (group.isExpanded()) {
                int size = group.getOfficers().size();
                if (position < count + size) return TYPE_OFFICER;
                count += size;
            }
        }
        return TYPE_DISTRICT;
    }

    @Override
    public int getItemCount() {
        int count = 0;
        for (OfficerGroup group : groups) {
            count++; // for district header
            if (group.isExpanded()) {
                count += group.getOfficers().size();
            }
        }
        return count;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        if (viewType == TYPE_DISTRICT) {
            View v = inflater.inflate(R.layout.item_district, parent, false);
            return new DistrictViewHolder(v);
        } else {
            View v = inflater.inflate(R.layout.item_officer, parent, false);
            return new OfficerViewHolder(v);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        int count = 0;
        for (OfficerGroup group : groups) {
            if (count == position) {
                DistrictViewHolder districtHolder = (DistrictViewHolder) holder;
                districtHolder.bind(group);

                final int pos = count;
                final OfficerGroup grp = group;

                districtHolder.ivArrow.setOnClickListener(v -> {
                    grp.setExpanded(!grp.isExpanded());
                    notifyItemChanged(pos);
                    if (grp.isExpanded()) {
                        notifyItemRangeInserted(pos + 1, grp.getOfficers().size());
                    } else {
                        notifyItemRangeRemoved(pos + 1, grp.getOfficers().size());
                    }
                });

                districtHolder.itemView.setOnClickListener(null);
                return;
            }
            count++;
            if (group.isExpanded()) {
                int size = group.getOfficers().size();
                if (position < count + size) {
                    OfficerViewHolder officerHolder = (OfficerViewHolder) holder;
                    officerHolder.bind(group.getOfficers().get(position - count));
                    return;
                }
                count += size;
            }
        }
    }

    class DistrictViewHolder extends RecyclerView.ViewHolder {
        TextView tvDistrict;
        ImageView ivArrow;

        public DistrictViewHolder(View itemView) {
            super(itemView);
            tvDistrict = itemView.findViewById(R.id.tv_district_title);
            ivArrow = itemView.findViewById(R.id.iv_arrow);
        }

        void bind(OfficerGroup group) {
            tvDistrict.setText(group.getDistrict());
            ivArrow.setRotation(group.isExpanded() ? 180f : 0f);
        }
    }

    class OfficerViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvRole, tvPhone;
        ImageView ivProfile;

        public OfficerViewHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_name);
            tvRole = itemView.findViewById(R.id.tv_state);
            tvPhone = itemView.findViewById(R.id.tv_phone);
            ivProfile = itemView.findViewById(R.id.iv_profile);
        }

        void bind(Officer officer) {
            tvName.setText(officer.getName());
            tvRole.setText(officer.getState());
            tvPhone.setText(officer.getPhoneNumber());
            Glide.with(ivProfile.getContext()).load(officer.getProfileImageUrl())
                    .placeholder(R.drawable.ic_placeholder)
                    .circleCrop()
                    .into(ivProfile);

            tvPhone.setOnClickListener(v -> {
                Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + officer.getPhoneNumber()));
                ivProfile.getContext().startActivity(intent);
            });
        }
    }

    // Item decoration class to add spacing between items
    public static class VerticalSpaceItemDecoration extends RecyclerView.ItemDecoration {
        private final int verticalSpaceHeight;

        public VerticalSpaceItemDecoration(int verticalSpaceHeight) {
            this.verticalSpaceHeight = verticalSpaceHeight;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent,
                                   RecyclerView.State state) {
            int position = parent.getChildAdapterPosition(view);
            if (position != RecyclerView.NO_POSITION) {
                outRect.bottom = verticalSpaceHeight;
            }
        }
    }
}
