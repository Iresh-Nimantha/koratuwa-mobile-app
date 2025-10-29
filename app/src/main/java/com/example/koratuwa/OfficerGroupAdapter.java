package com.example.koratuwa;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class OfficerGroupAdapter extends RecyclerView.Adapter<OfficerGroupAdapter.GroupHolder> {
    private final List<OfficerGroup> groups;
    private final Context context;

    public OfficerGroupAdapter(Context ctx, List<OfficerGroup> groups) {
        this.context = ctx;
        this.groups = groups;
    }

    @Override
    public GroupHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_group_officers, parent, false);
        return new GroupHolder(v);
    }

    @Override
    public void onBindViewHolder(GroupHolder holder, int position) {
        OfficerGroup group = groups.get(position);
        holder.tvGroupName.setText(group.getDistrict());
        holder.nestedRecycler.setAdapter(new OfficerAdapter(context, group.getOfficers()));
    }

    @Override
    public int getItemCount() { return groups.size(); }

    static class GroupHolder extends RecyclerView.ViewHolder {
        TextView tvGroupName;
        RecyclerView nestedRecycler;
        GroupHolder(View itemView) {
            super(itemView);
            tvGroupName = itemView.findViewById(R.id.tv_group_name);
            nestedRecycler = itemView.findViewById(R.id.rv_group_officers);
            nestedRecycler.setLayoutManager(new LinearLayoutManager(itemView.getContext()));
        }
    }
}
