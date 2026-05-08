package com.example.fitcore.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.fitcore.R;
import com.example.fitcore.database.DatabaseHelper;
import com.example.fitcore.utils.SessionManager;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import java.util.ArrayList;
import java.util.List;

public class AnalyticsFragment extends Fragment {

    private DatabaseHelper db;
    private SessionManager session;
    private int weekOffset = 0;
    private TextView tvWeekRange, tvWeekLabel, tvWeekWorkouts, tvWeekMinutes, tvCheckinTitle;
    private String[] weekDates = new String[7];

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_analytics, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = DatabaseHelper.getInstance(requireContext());
        session = new SessionManager(requireContext());

        int userId = session.getUserId();
        int total = db.getTotalWorkouts(userId);
        int totalMins = db.getTotalMinutes(userId);

        ((TextView) view.findViewById(R.id.tv_total_workouts)).setText(String.valueOf(total));
        ((TextView) view.findViewById(R.id.tv_total_hours)).setText((totalMins/60) + "h " + (totalMins%60) + "min");

        tvWeekRange = view.findViewById(R.id.tv_week_range);
        tvWeekLabel = view.findViewById(R.id.tv_week_label);
        tvWeekWorkouts = view.findViewById(R.id.tv_week_workouts);
        tvWeekMinutes = view.findViewById(R.id.tv_week_minutes);
        tvCheckinTitle = view.findViewById(R.id.tv_checkin_title);
        View layoutWeekRange = view.findViewById(R.id.layout_week_range);

        // 点击日期区间 → 弹日期选择器
        layoutWeekRange.setOnClickListener(v -> {
            java.util.Calendar cal = java.util.Calendar.getInstance();
            cal.add(java.util.Calendar.DAY_OF_MONTH, weekOffset * 7);
            new android.app.DatePickerDialog(requireContext(), (view1, y, m, d) -> {
                // 计算选中日期所在的周和当前周的偏移
                java.util.Calendar selected = java.util.Calendar.getInstance();
                selected.set(y, m, d);
                java.util.Calendar now = java.util.Calendar.getInstance();
                int selectedWeek = selected.get(java.util.Calendar.WEEK_OF_YEAR);
                int nowWeek = now.get(java.util.Calendar.WEEK_OF_YEAR);
                int yearDiff = selected.get(java.util.Calendar.YEAR) - now.get(java.util.Calendar.YEAR);
                weekOffset = (yearDiff * 52) + (selectedWeek - nowWeek);
                refreshWeek(); setupCheckin(view); setupWeekStats(view); setupChart(view);
            }, cal.get(java.util.Calendar.YEAR), cal.get(java.util.Calendar.MONTH),
                    cal.get(java.util.Calendar.DAY_OF_MONTH)).show();
        });

        view.findViewById(R.id.btn_prev_week).setOnClickListener(v -> {
            weekOffset--; refreshWeek(); setupCheckin(view); setupWeekStats(view); setupChart(view);
        });
        view.findViewById(R.id.btn_next_week).setOnClickListener(v -> {
            if (weekOffset < 0) { weekOffset++; refreshWeek(); setupCheckin(view); setupWeekStats(view); setupChart(view); }
        });

        refreshWeek();
        setupCheckin(view);
        setupWeekStats(view);
        setupChart(view);
    }

    private TextView btnToday;

    private void refreshWeek() {
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.add(java.util.Calendar.DAY_OF_MONTH, weekOffset * 7);
        int dow = cal.get(java.util.Calendar.DAY_OF_WEEK);
        int daysBack = (dow + 5) % 7;
        cal.add(java.util.Calendar.DAY_OF_MONTH, -daysBack);

        java.text.SimpleDateFormat fullSdf = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
        for (int i = 0; i < 7; i++) {
            weekDates[i] = fullSdf.format(cal.getTime());
            cal.add(java.util.Calendar.DAY_OF_MONTH, 1);
        }

        String year = weekDates[0].substring(0, 4);
        tvWeekRange.setText(year + " " + weekDates[0].substring(5) + " - " + weekDates[6].substring(5));

        if (weekOffset == 0) tvWeekLabel.setText("本周");
        else if (weekOffset == -1) tvWeekLabel.setText("上周");
        else tvWeekLabel.setText(Math.abs(weekOffset) + "周前");

        // "今" 按钮
        if (btnToday == null && getView() != null) {
            btnToday = new TextView(requireContext());
            btnToday.setText("今");
            btnToday.setTextColor(0xFF000000);
            btnToday.setTextSize(14);
            btnToday.setTypeface(null, android.graphics.Typeface.BOLD);
            btnToday.setGravity(Gravity.CENTER);
            android.graphics.drawable.GradientDrawable gd = new android.graphics.drawable.GradientDrawable();
            gd.setShape(android.graphics.drawable.GradientDrawable.OVAL);
            gd.setColor(0xFFCCFF00);
            btnToday.setBackground(gd);
            FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(dp(44), dp(44));
            lp.gravity = Gravity.END | Gravity.TOP;
            lp.rightMargin = dp(8);
            lp.topMargin = dp(210);
            btnToday.setLayoutParams(lp);
            btnToday.setOnClickListener(v -> {
                weekOffset = 0;
                refreshWeek();
                setupCheckin(getView());
                setupWeekStats(getView());
                setupChart(getView());
            });
            ((FrameLayout) getView()).addView(btnToday);
        }
        if (btnToday != null) btnToday.setVisibility(weekOffset == 0 ? View.GONE : View.VISIBLE);

        if (weekOffset == 0) tvCheckinTitle.setText("本周打卡");
        else if (weekOffset == -1) tvCheckinTitle.setText("上周打卡");
        else tvCheckinTitle.setText(Math.abs(weekOffset) + "周前打卡");
    }

    private void setupWeekStats(View view) {
        List<int[]> weekly = db.getWeeklyMinutesForDates(session.getUserId(), weekDates);
        int count = 0, totalMins = 0;
        for (int[] entry : weekly) {
            if (entry[1] > 0) count++;
            totalMins += entry[1];
        }
        tvWeekWorkouts.setText(String.valueOf(count));
        if (totalMins >= 60) {
            tvWeekMinutes.setText((totalMins / 60) + "h " + (totalMins % 60) + "min");
        } else {
            tvWeekMinutes.setText(totalMins + "min");
        }
    }

    private void setupCheckin(View view) {
        LinearLayout container = view.findViewById(R.id.checkin_days);
        container.removeAllViews();

        String[] labels = {"一","二","三","四","五","六","日"};

        // 获取本周有记录的日期
        java.util.Set<String> workoutDays = new java.util.HashSet<>();
        for (com.example.fitcore.model.WorkoutRecord r : db.getRecordsByUser(session.getUserId())) {
            if (r.getRecordedAt() != null && r.getRecordedAt().length() >= 10)
                workoutDays.add(r.getRecordedAt().substring(0, 10));
        }

        // 今天索引
        java.util.Calendar cal = java.util.Calendar.getInstance();
        int today = (cal.get(java.util.Calendar.DAY_OF_WEEK) + 5) % 7;
        boolean isCurrentWeek = (weekOffset == 0);

        for (int i = 0; i < 7; i++) {
            boolean worked = workoutDays.contains(weekDates[i]);
            boolean isToday = isCurrentWeek && (i == today);

            int size = dp(40);
            FrameLayout dayItem = new FrameLayout(requireContext());
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    0, size, 1);
            dayItem.setLayoutParams(lp);

            // 圆形背景
            View dot = new View(requireContext());
            android.graphics.drawable.GradientDrawable gd = new android.graphics.drawable.GradientDrawable();
            gd.setShape(android.graphics.drawable.GradientDrawable.OVAL);
            if (worked) gd.setColor(0xFFCCFF00);
            else gd.setColor(0xFF222222);
            if (isToday) gd.setStroke(dp(2), worked ? 0xFF000000 : 0xFFCCFF00);
            dot.setBackground(gd);
            FrameLayout.LayoutParams dotLp = new FrameLayout.LayoutParams(size, size);
            dotLp.gravity = Gravity.CENTER;
            dayItem.addView(dot, dotLp);

            // 文字标签居中叠加
            TextView label = new TextView(requireContext());
            label.setText(labels[i]);
            label.setTextSize(14);
            label.setTextColor(worked ? 0xFF000000 : (isToday ? 0xFFCCFF00 : 0xFF888888));
            FrameLayout.LayoutParams lblLp = new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
            lblLp.gravity = Gravity.CENTER;
            dayItem.addView(label, lblLp);

            container.addView(dayItem);
        }
    }

    private int dp(int d) { return (int) (d * getResources().getDisplayMetrics().density); }

    private void setupChart(View view) {
        BarChart chart = view.findViewById(R.id.bar_chart);
        chart.getDescription().setEnabled(false);
        chart.setDrawGridBackground(false);
        chart.setBackgroundColor(Color.parseColor("#111111"));
        chart.setDrawBarShadow(false);
        chart.setDrawValueAboveBar(true);
        chart.setPinchZoom(false);
        chart.setScaleEnabled(false);
        chart.getLegend().setEnabled(false);

        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setTextColor(Color.parseColor("#888888"));
        xAxis.setAxisLineColor(Color.parseColor("#333333"));

        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setTextColor(Color.parseColor("#888888"));
        leftAxis.setAxisLineColor(Color.parseColor("#333333"));
        leftAxis.setDrawGridLines(true);
        leftAxis.setGridColor(Color.parseColor("#222222"));

        chart.getAxisRight().setEnabled(false);

        List<int[]> weekly = db.getWeeklyMinutesForDates(session.getUserId(), weekDates);
        String[] labels = new String[]{"一", "二", "三", "四", "五", "六", "日"};
        List<BarEntry> entries = new ArrayList<>();

        for (int i = 0; i < 7 && i < weekly.size(); i++) {
            entries.add(new BarEntry(i, weekly.get(i)[1]));
        }
        // 填充到 7 天
        while (entries.size() < 7) entries.add(new BarEntry(entries.size(), 0));

        BarDataSet dataSet = new BarDataSet(entries, "Minutes");
        dataSet.setColor(Color.parseColor("#CCFF00"));
        dataSet.setDrawValues(true);
        dataSet.setValueTextColor(Color.parseColor("#FFFFFF"));
        dataSet.setValueTextSize(10f);

        BarData data = new BarData(dataSet);
        data.setBarWidth(0.5f);

        chart.setData(data);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setGranularity(1f);
        chart.animateY(500);
        chart.invalidate();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getView() != null) {
            int userId = session.getUserId();
            int total = db.getTotalWorkouts(userId);
            int totalMins = db.getTotalMinutes(userId);
            ((TextView) getView().findViewById(R.id.tv_total_workouts)).setText(String.valueOf(total));
            ((TextView) getView().findViewById(R.id.tv_total_hours)).setText((totalMins / 60) + "h " + (totalMins % 60) + "min");
            refreshWeek();
            setupCheckin(getView());
            setupWeekStats(getView());
            setupChart(getView());
        }
    }
}
