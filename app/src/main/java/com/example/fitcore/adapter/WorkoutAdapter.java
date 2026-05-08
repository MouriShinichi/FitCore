package com.example.fitcore.adapter;

import android.app.AlertDialog;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fitcore.R;
import com.example.fitcore.model.WorkoutRecord;

import java.util.List;

public class WorkoutAdapter extends RecyclerView.Adapter<WorkoutAdapter.ViewHolder> {

    private final List<WorkoutRecord> records;
    private boolean manageMode;
    private java.util.Set<Integer> selectedIds = new java.util.HashSet<>();

    public WorkoutAdapter(List<WorkoutRecord> records) {
        this.records = records;
    }

    public void setManageMode(boolean m) { this.manageMode = m; }
    public void setSelectedIds(java.util.Set<Integer> ids) { this.selectedIds = ids; }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_workout_record, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        WorkoutRecord r = records.get(position);
        holder.tvType.setText(r.getType());
        holder.tvDuration.setText(r.getDurationMinutes() + " 分钟");

        String date = r.getRecordedAt();
        if (date != null && date.length() >= 10) date = date.substring(0, 10);
        holder.tvDate.setText(date);

        String[] feels = {"", "😫差", "😕较差", "😐一般", "😊良好", "🔥爽"};
        int f = r.getFeeling();
        holder.tvFeeling.setText(f >= 1 && f <= 5 ? feels[f] : "");

        // 有备注时显示提示
        boolean hasNotes = !TextUtils.isEmpty(r.getNotes());
        holder.tvHint.setVisibility(hasNotes ? View.VISIBLE : View.GONE);

        // 管理模式显示选择框
        holder.checkBox.setVisibility(manageMode ? View.VISIBLE : View.GONE);
        holder.checkBox.setText(selectedIds.contains(r.getId()) ? "●" : "○");
        holder.checkBox.setTextColor(selectedIds.contains(r.getId()) ? 0xFFCCFF00 : 0xFF888888);

        // 点击
        holder.itemView.setOnClickListener(v -> {
            if (manageMode) {
                if (selectedIds.contains(r.getId())) selectedIds.remove(r.getId());
                else selectedIds.add(r.getId());
                notifyItemChanged(position);
                // 通知 activity 更新计数
                if (v.getContext() instanceof com.example.fitcore.activity.WorkoutHistoryActivity) {
                    ((com.example.fitcore.activity.WorkoutHistoryActivity) v.getContext()).updateCountFromAdapter();
                }
                return;
            }
            View dlg = LayoutInflater.from(v.getContext())
                    .inflate(R.layout.dialog_workout_detail, null);

            ((TextView) dlg.findViewById(R.id.detail_type)).setText(r.getType());
            ((TextView) dlg.findViewById(R.id.detail_duration))
                    .setText(r.getDurationMinutes() + " 分钟");
            ((TextView) dlg.findViewById(R.id.detail_date)).setText(
                    r.getRecordedAt() != null && r.getRecordedAt().length() >= 16
                            ? r.getRecordedAt().substring(0, 16) : "");
            ((TextView) dlg.findViewById(R.id.detail_feeling)).setText(
                    f >= 1 && f <= 5 ? feels[f] : "无");
            ((TextView) dlg.findViewById(R.id.detail_notes)).setText(
                    hasNotes ? r.getNotes() : "无备注");

            AlertDialog dialog = new AlertDialog.Builder(v.getContext())
                    .setView(dlg)
                    .setCancelable(true)
                    .create();
            if (dialog.getWindow() != null)
                dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

            dlg.findViewById(R.id.btn_close).setOnClickListener(cv -> dialog.dismiss());
            dialog.show();
        });
    }

    @Override
    public int getItemCount() {
        return records.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvType, tvDate, tvDuration, tvFeeling, tvHint, checkBox;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvType = itemView.findViewById(R.id.tv_record_type);
            tvDate = itemView.findViewById(R.id.tv_record_date);
            tvDuration = itemView.findViewById(R.id.tv_record_duration);
            tvFeeling = itemView.findViewById(R.id.tv_record_feeling);
            tvHint = itemView.findViewById(R.id.tv_note_hint);
            checkBox = itemView.findViewById(R.id.tv_checkbox);
        }
    }
}
