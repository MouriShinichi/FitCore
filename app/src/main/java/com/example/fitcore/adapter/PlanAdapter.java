package com.example.fitcore.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fitcore.R;
import com.example.fitcore.model.FitnessPlan;

import java.util.List;

public class PlanAdapter extends RecyclerView.Adapter<PlanAdapter.ViewHolder> {

    public interface OnPlanClickListener {
        void onPlanClick(FitnessPlan plan);
    }

    private final List<FitnessPlan> plans;
    private final OnPlanClickListener listener;
    private int selectedPlanId = -1;

    public PlanAdapter(List<FitnessPlan> plans, OnPlanClickListener listener) {
        this.plans = plans;
        this.listener = listener;
    }

    public void setSelectedPlanId(int id) {
        this.selectedPlanId = id;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_plan_card, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FitnessPlan plan = plans.get(position);
        holder.tvName.setText(plan.getName());
        holder.tvDesc.setText(plan.getDescription());
        holder.tvDuration.setText(plan.getDuration());
        holder.tvLevel.setText(plan.getLevel());
        holder.tvCategory.setText(plan.getCategory());

        // 高亮当前选中计划
        if (plan.getId() == selectedPlanId) {
            holder.itemView.setBackground(
                    ContextCompat.getDrawable(holder.itemView.getContext(), R.drawable.bg_card_accent));
        } else {
            holder.itemView.setBackground(
                    ContextCompat.getDrawable(holder.itemView.getContext(), R.drawable.bg_card));
        }

        holder.itemView.setOnClickListener(v -> listener.onPlanClick(plan));
    }

    @Override
    public int getItemCount() {
        return plans.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvDesc, tvDuration, tvLevel, tvCategory;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_card_name);
            tvDesc = itemView.findViewById(R.id.tv_card_desc);
            tvDuration = itemView.findViewById(R.id.tv_card_duration);
            tvLevel = itemView.findViewById(R.id.tv_card_level);
            tvCategory = itemView.findViewById(R.id.tv_card_category);
        }
    }
}
