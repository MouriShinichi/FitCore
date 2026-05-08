package com.example.fitcore.activity;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fitcore.R;
import com.example.fitcore.adapter.WorkoutAdapter;
import com.example.fitcore.database.DatabaseHelper;
import com.example.fitcore.model.WorkoutRecord;
import com.example.fitcore.utils.SessionManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class WorkoutHistoryActivity extends AppCompatActivity {

    private DatabaseHelper db;
    private SessionManager session;
    private RecyclerView rv;
    private TextView tvEmpty;
    private List<WorkoutRecord> allRecords, filteredRecords;
    private TextView[] filters;
    private int currentFilter = 0;
    private boolean isManageMode;
    private Set<Integer> selectedIds = new HashSet<>();
    private WorkoutAdapter adapter;

    private LinearLayout layoutDelete;
    private TextView tvSelectedCount, btnManage, btnSelectAll, btnDelete, btnCancelManage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workout_history);

        db = DatabaseHelper.getInstance(this);
        session = new SessionManager(this);
        rv = findViewById(R.id.rv_history);
        tvEmpty = findViewById(R.id.tv_empty_history);
        rv.setLayoutManager(new LinearLayoutManager(this));

        allRecords = db.getRecordsByUser(session.getUserId());

        filters = new TextView[]{
            findViewById(R.id.filter_all),
            findViewById(R.id.filter_week),
            findViewById(R.id.filter_month),
            findViewById(R.id.filter_3month),
            findViewById(R.id.filter_date),
        };

        for (int i = 0; i < filters.length - 1; i++) {
            final int idx = i;
            filters[i].setOnClickListener(v -> {
                currentFilter = idx;
                updateFilterTabs();
                applyFilter();
            });
        }

        filters[4].setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            new DatePickerDialog(this, (view, y, m, d) -> {
                currentFilter = 4;
                updateFilterTabs();
                applyFilter(String.format("%04d-%02d-%02d", y, m + 1, d));
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
        });

        // 管理
        layoutDelete = findViewById(R.id.layout_delete);
        tvSelectedCount = findViewById(R.id.tv_selected_count);
        btnManage = findViewById(R.id.btn_manage);
        btnCancelManage = findViewById(R.id.btn_cancel_manage);
        btnSelectAll = findViewById(R.id.btn_select_all);
        btnDelete = findViewById(R.id.btn_delete);

        btnManage.setOnClickListener(v -> {
            isManageMode = !isManageMode;
            btnManage.setText(isManageMode ? "完成" : "管理");
            layoutDelete.setVisibility(isManageMode ? View.VISIBLE : View.GONE);
            selectedIds.clear();
            applyFilter();
        });

        btnCancelManage.setOnClickListener(v -> {
            isManageMode = false;
            btnManage.setText("管理");
            layoutDelete.setVisibility(View.GONE);
            selectedIds.clear();
            applyFilter();
        });

        btnSelectAll.setOnClickListener(v -> {
            if (adapter == null) return;
            boolean allSelected = selectedIds.size() == filteredRecords.size() && !filteredRecords.isEmpty();
            if (allSelected) {
                selectedIds.clear();
                adapter.setSelectedIds(selectedIds);
                adapter.notifyDataSetChanged();
                updateSelectedCount();
            } else {
                selectedIds.clear();
                for (WorkoutRecord r : filteredRecords) selectedIds.add(r.getId());
                adapter.setSelectedIds(selectedIds);
                adapter.notifyDataSetChanged();
                updateSelectedCount();
            }
        });

        btnDelete.setOnClickListener(v -> {
            if (selectedIds.isEmpty()) {
                Toast.makeText(this, "请先选择记录", Toast.LENGTH_SHORT).show();
                return;
            }
            View dlg = LayoutInflater.from(this).inflate(R.layout.dialog_confirm, null);
            ((TextView) dlg.findViewById(R.id.dialog_title)).setText("删除记录");
            ((TextView) dlg.findViewById(R.id.dialog_message))
                    .setText("确定要删除选中的 " + selectedIds.size() + " 条记录吗？");
            TextView bp = dlg.findViewById(R.id.dialog_positive);
            TextView bn = dlg.findViewById(R.id.dialog_negative);
            bp.setText("删除"); bn.setText("取消");

            AlertDialog dialog = new AlertDialog.Builder(this)
                    .setView(dlg).setCancelable(false).create();
            if (dialog.getWindow() != null)
                dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            bp.setOnClickListener(vv -> {
                for (int id : selectedIds) db.deleteRecord(id);
                allRecords = db.getRecordsByUser(session.getUserId());
                selectedIds.clear();
                applyFilter();
                dialog.dismiss();
                Toast.makeText(this, "已删除", Toast.LENGTH_SHORT).show();
            });
            bn.setOnClickListener(vv -> dialog.dismiss());
            dialog.show();
        });

        applyFilter();
    }

    public void updateCountFromAdapter() {
        updateSelectedCount();
    }

    private void updateSelectedCount() {
        int n = selectedIds.size();
        tvSelectedCount.setText("已选 " + n + " 项");
        if (n == 0) {
            btnDelete.setBackgroundResource(R.drawable.bg_btn_disabled);
            btnDelete.setTextColor(0xFF666666);
        } else {
            btnDelete.setBackgroundResource(R.drawable.bg_accent_btn);
            btnDelete.setTextColor(0xFF000000);
        }
    }

    private void updateFilterTabs() {
        for (int i = 0; i < filters.length; i++) {
            if (i == currentFilter) {
                filters[i].setBackgroundResource(R.drawable.bg_accent_btn);
                filters[i].setTextColor(0xFF000000);
            } else {
                filters[i].setBackgroundResource(R.drawable.bg_card);
                filters[i].setTextColor(0xFF888888);
            }
        }
    }

    private void applyFilter() { applyFilter(null); }

    private void applyFilter(String exactDate) {
        filteredRecords = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Calendar cal = Calendar.getInstance();
        String weekStart, monthStart, threeMonthStart;
        cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());
        weekStart = sdf.format(cal.getTime());
        cal = Calendar.getInstance(); cal.set(Calendar.DAY_OF_MONTH, 1);
        monthStart = sdf.format(cal.getTime());
        cal = Calendar.getInstance(); cal.add(Calendar.MONTH, -3);
        threeMonthStart = sdf.format(cal.getTime());

        for (WorkoutRecord r : allRecords) {
            String date = r.getRecordedAt();
            if (date == null || date.length() < 10) continue;
            String d = date.substring(0, 10);
            if (exactDate != null) { if (d.equals(exactDate)) filteredRecords.add(r); }
            else {
                switch (currentFilter) {
                    case 1: if (d.compareTo(weekStart) >= 0) filteredRecords.add(r); break;
                    case 2: if (d.compareTo(monthStart) >= 0) filteredRecords.add(r); break;
                    case 3: if (d.compareTo(threeMonthStart) >= 0) filteredRecords.add(r); break;
                    default: filteredRecords.add(r);
                }
            }
        }

        if (filteredRecords.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            rv.setVisibility(View.GONE);
        } else {
            tvEmpty.setVisibility(View.GONE);
            rv.setVisibility(View.VISIBLE);
            adapter = new WorkoutAdapter(filteredRecords);
            adapter.setManageMode(isManageMode);
            adapter.setSelectedIds(selectedIds);
            rv.setAdapter(adapter);
        }
        updateSelectedCount();
    }
}
