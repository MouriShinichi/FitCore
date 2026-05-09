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
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AnalyticsFragment extends Fragment {

    private DatabaseHelper db;
    private SessionManager session;
    private int weekOffset = 0;
    private TextView tvWeekRange, tvWeekLabel, tvWeekWorkouts, tvWeekMinutes, tvCheckinTitle;
    private LinearLayout chartDayLabels, dayPieLegend;
    private PieChart dayPieCount, dayPieMinute;
    private TextView tvDayPieTitle;
    private TextView[] dayLabels = new TextView[7];
    private int selectedDayIndex = -1;
    private java.util.Map<String, Integer> typeToCategory;
    private java.util.Map<String, String> typeToEmoji;

    // 运动分类: [类别名, 运动名列表...]
    private static final String[][] EXERCISE_CATEGORY = {
        {"有氧运动", "跑步","骑行","游泳","跳绳","慢跑","快走","跳舞","登山","椭圆机","划船","轮滑","有氧操","马拉松","徒步","攀岩","越野跑","动感单车","自由泳","冲刺跑","街舞"},
        {"力量训练", "俯卧撑","深蹲","仰卧起坐","硬拉","引体向上","平板支撑","哑铃","弓步蹲","卧推","深蹲架","飞鸟","腿举","弯举","推举","划船机","挺举","侧平举","臀桥","卷腹","窄距俯卧撑"},
        {"柔韧拉伸", "瑜伽","拉伸","普拉提","泡沫轴","太极","开肩","压腿","猫式","阴瑜伽","流瑜伽","劈叉","髋部拉伸","肩颈放松","婴儿式","八段锦","冥想","动态拉伸","筋膜放松","高温瑜伽","脊柱扭转"},
        {"球类运动", "篮球","足球","乒乓球","网球","排球","羽毛球","台球","曲棍球","棒球","橄榄球","保龄球","高尔夫","冰球","板球","垒球","手球","沙滩排球","壁球","长曲棍球","桌式足球"},
        {"其他运动", "自定义","拳击","滑板","高尔夫","滑雪","武术","射箭","击剑","飞镖","摔跤","跆拳道","滑雪板","骑马","皮划艇","冲浪","潜水","溜冰","跳伞","钓鱼","龙舟"},
    };
    private static final String[] CAT_EMOJI = {"🏃", "💪", "🧘", "⚽", "➕"};
    private static final int[] CAT_COLORS = {0xFF7CB342, 0xFFEF5350, 0xFF42A5F5, 0xFFFFA726, 0xFF9E9E9E};
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
        chartDayLabels = view.findViewById(R.id.chart_day_labels);
        dayPieCount = view.findViewById(R.id.day_pie_count);
        dayPieMinute = view.findViewById(R.id.day_pie_minutes);
        tvDayPieTitle = view.findViewById(R.id.tv_day_pie_title);
        dayPieLegend = view.findViewById(R.id.day_pie_legend);
        for (int i = 0; i < 7; i++) {
            int id = getResources().getIdentifier("day_label_" + (i + 1), "id", requireContext().getPackageName());
            dayLabels[i] = view.findViewById(id);
        }
        buildCategoryMap();
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
                refreshWeek(); setupCheckin(view); setupWeekStats(view); setupChart(view); setupDayPies(view);
            }, cal.get(java.util.Calendar.YEAR), cal.get(java.util.Calendar.MONTH),
                    cal.get(java.util.Calendar.DAY_OF_MONTH)).show();
        });

        view.findViewById(R.id.btn_prev_week).setOnClickListener(v -> {
            weekOffset--; refreshWeek(); setupCheckin(view); setupWeekStats(view); setupChart(view); setupDayPies(view);
        });
        view.findViewById(R.id.btn_next_week).setOnClickListener(v -> {
            if (weekOffset < 0) { weekOffset++; refreshWeek(); setupCheckin(view); setupWeekStats(view); setupChart(view); setupDayPies(view); }
        });

        // 4 卡片点击 → 饼图弹窗
        view.findViewById(R.id.card_total_workouts).setOnClickListener(v ->
                showPieSheet("总锻炼次数分布", false, false));
        view.findViewById(R.id.card_total_hours).setOnClickListener(v ->
                showPieSheet("总时长分布", true, false));
        view.findViewById(R.id.card_week_workouts).setOnClickListener(v ->
                showPieSheet("周锻炼次数分布", false, true));
        view.findViewById(R.id.card_week_minutes).setOnClickListener(v ->
                showPieSheet("周时长分布", true, true));

        refreshWeek();
        setupCheckin(view);
        setupWeekStats(view);
        setupChart(view);
        setupDayPies(view);
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
                setupDayPies(getView());
            });
            ((FrameLayout) getView()).addView(btnToday);
        }
        if (btnToday != null) btnToday.setVisibility(weekOffset == 0 ? View.GONE : View.VISIBLE);

        if (weekOffset == 0) tvCheckinTitle.setText("本周打卡");
        else if (weekOffset == -1) tvCheckinTitle.setText("上周打卡");
        else tvCheckinTitle.setText(Math.abs(weekOffset) + "周前打卡");
    }

    private void setupWeekStats(View view) {
        java.util.Set<String> dateSet = new java.util.HashSet<>();
        for (String d : weekDates) dateSet.add(d);
        int count = 0, totalMins = 0;
        for (com.example.fitcore.model.WorkoutRecord r : db.getRecordsByUser(session.getUserId())) {
            if (r.getRecordedAt() != null && r.getRecordedAt().length() >= 10
                    && dateSet.contains(r.getRecordedAt().substring(0, 10))) {
                count++;
                totalMins += r.getDurationMinutes();
            }
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
        xAxis.setDrawLabels(false);
        xAxis.setAxisLineColor(Color.parseColor("#333333"));

        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setTextColor(Color.parseColor("#888888"));
        leftAxis.setAxisLineColor(Color.parseColor("#333333"));
        leftAxis.setDrawGridLines(true);
        leftAxis.setGridColor(Color.parseColor("#222222"));

        chart.getAxisRight().setEnabled(false);

        java.util.Calendar cal = java.util.Calendar.getInstance();
        int todayIdx = (cal.get(java.util.Calendar.DAY_OF_WEEK) + 5) % 7;
        boolean isCurrentWeek = (weekOffset == 0);

        // 设置选中日期（非本周重置）
        if (!isCurrentWeek) selectedDayIndex = -1;
        if (selectedDayIndex < 0) selectedDayIndex = isCurrentWeek ? todayIdx : 0;
        final int selIdx = selectedDayIndex;

        List<int[]> weekly = db.getWeeklyMinutesForDates(session.getUserId(), weekDates);
        List<BarEntry> entries = new ArrayList<>();
        List<Integer> barColors = new ArrayList<>();

        for (int i = 0; i < 7 && i < weekly.size(); i++) {
            entries.add(new BarEntry(i, weekly.get(i)[1]));
        }
        while (entries.size() < 7) entries.add(new BarEntry(entries.size(), 0));

        for (int i = 0; i < 7; i++) {
            if (isCurrentWeek && i == selIdx) {
                barColors.add(Color.parseColor("#7CB342"));
            } else {
                barColors.add(Color.parseColor("#337CB342"));
            }
        }

        BarDataSet dataSet = new BarDataSet(entries, "Minutes");
        dataSet.setColors(barColors);
        dataSet.setDrawValues(true);
        dataSet.setValueTextColor(Color.parseColor("#FFFFFF"));
        dataSet.setValueTextSize(10f);

        BarData data = new BarData(dataSet);
        data.setBarWidth(0.5f);

        chart.setData(data);
        xAxis.setGranularity(1f);
        chart.animateY(500);

        // 自定义星期标签 + 点击切换日期
        String[] labelTexts = {"一","二","三","四","五","六","日"};
        for (int i = 0; i < 7; i++) {
            dayLabels[i].setText(labelTexts[i]);
            dayLabels[i].setTextColor(isCurrentWeek && i == selIdx
                    ? Color.parseColor("#7CB342") : Color.parseColor("#888888"));
            final int di = i;
            dayLabels[i].setOnClickListener(v -> {
                if (!isCurrentWeek) return;
                selectedDayIndex = di;
                for (int j = 0; j < 7; j++) {
                    dayLabels[j].setTextColor(j == di
                            ? Color.parseColor("#7CB342") : Color.parseColor("#888888"));
                }
                chart.highlightValue(di, 0);
                setupDayPies(getView());
            });
        }

        // 高亮选中日期的柱子
        chart.highlightValue(selIdx, 0);

        // 点击柱子 → 弹出当天记录
        chart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(com.github.mikephil.charting.data.Entry e, Highlight h) {
                int idx = (int) e.getX();
                if (idx >= 0 && idx < 7) showDayRecordsSheet(idx, chart);
            }
            @Override public void onNothingSelected() {}
        });

        chart.invalidate();
    }

    private void showDayRecordsSheet(int dayIndex, BarChart chart) {
        String date = weekDates[dayIndex];
        String[] labelTexts = {"一","二","三","四","五","六","日"};
        String[] feelEmoji = {"", "😫", "😕", "😐", "😊", "🔥"};
        String[] feelLabel = {"", "差", "较差", "一般", "良好", "爽"};

        BottomSheetDialog sheet = new BottomSheetDialog(requireContext());
        View content = LayoutInflater.from(requireContext())
                .inflate(R.layout.bottom_sheet_day_records, null);

        String title = date.substring(5) + " 周" + labelTexts[dayIndex];
        ((TextView) content.findViewById(R.id.bs_day_title)).setText(title);

        LinearLayout container = content.findViewById(R.id.bs_records_container);

        List<com.example.fitcore.model.WorkoutRecord> dayRecords = new ArrayList<>();
        for (com.example.fitcore.model.WorkoutRecord r : db.getRecordsByUser(session.getUserId())) {
            if (r.getRecordedAt() != null && r.getRecordedAt().length() >= 10
                    && r.getRecordedAt().substring(0, 10).equals(date)) {
                dayRecords.add(r);
            }
        }

        if (dayRecords.isEmpty()) {
            TextView empty = new TextView(requireContext());
            empty.setText("当天暂无运动记录");
            empty.setTextColor(Color.parseColor("#888888"));
            empty.setTextSize(14);
            empty.setPadding(0, dp(16), 0, 0);
            container.addView(empty);
        } else {
            for (com.example.fitcore.model.WorkoutRecord r : dayRecords) {
                LinearLayout item = new LinearLayout(requireContext());
                item.setOrientation(LinearLayout.VERTICAL);
                item.setPadding(dp(14), dp(10), dp(14), dp(10));
                item.setBackgroundResource(R.drawable.bg_card);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
                lp.topMargin = dp(6);
                item.setLayoutParams(lp);

                TextView tvType = new TextView(requireContext());
                tvType.setText(r.getType());
                tvType.setTextColor(Color.parseColor("#FFFFFF"));
                tvType.setTextSize(15);
                tvType.setTypeface(null, android.graphics.Typeface.BOLD);

                TextView tvInfo = new TextView(requireContext());
                int f = r.getFeeling();
                String emoji = (f >= 1 && f <= 5) ? feelEmoji[f] : "";
                String label = (f >= 1 && f <= 5) ? feelLabel[f] : "";
                tvInfo.setText(r.getDurationMinutes() + "分钟 · 体感 " + emoji + " " + label);
                tvInfo.setTextColor(Color.parseColor("#AAAAAA"));
                tvInfo.setTextSize(12);
                tvInfo.setPadding(0, dp(4), 0, 0);

                item.addView(tvType);
                item.addView(tvInfo);
                container.addView(item);
            }
        }

        sheet.setContentView(content);
        sheet.setOnDismissListener(d -> chart.highlightValue(null));
        sheet.show();
    }

    private void buildCategoryMap() {
        typeToCategory = new java.util.HashMap<>();
        typeToEmoji = new java.util.HashMap<>();
        for (int c = 0; c < EXERCISE_CATEGORY.length; c++) {
            for (int i = 1; i < EXERCISE_CATEGORY[c].length; i++) {
                typeToCategory.put(EXERCISE_CATEGORY[c][i], c);
                typeToEmoji.put(EXERCISE_CATEGORY[c][i], CAT_EMOJI[c]);
            }
        }
    }

    private int getCategory(String type) {
        if (type == null) return -1;
        for (int c = 0; c < EXERCISE_CATEGORY.length; c++) {
            for (int i = 1; i < EXERCISE_CATEGORY[c].length; i++) {
                if (type.contains(EXERCISE_CATEGORY[c][i])) return c;
            }
        }
        return 4; // 其他
    }

    private void showPieSheet(String title, boolean useMinutes, boolean filterWeek) {
        int[] counts = new int[5];
        int[] minutes = new int[5];

        final java.util.Set<String> dateSet;
        if (filterWeek) {
            java.util.Set<String> ds = new java.util.HashSet<>();
            for (String d : weekDates) ds.add(d);
            dateSet = ds;
        } else {
            dateSet = null;
        }

        for (com.example.fitcore.model.WorkoutRecord r : db.getRecordsByUser(session.getUserId())) {
            if (r.getRecordedAt() == null || r.getRecordedAt().length() < 10) continue;
            if (dateSet != null && !dateSet.contains(r.getRecordedAt().substring(0, 10))) continue;
            int cat = getCategory(r.getType());
            if (cat < 0 || cat >= 5) continue;
            counts[cat]++;
            minutes[cat] += r.getDurationMinutes();
        }

        // 生成 PieChart
        BottomSheetDialog sheet = new BottomSheetDialog(requireContext());
        View content = LayoutInflater.from(requireContext())
                .inflate(R.layout.bottom_sheet_pie_chart, null);
        ((TextView) content.findViewById(R.id.bs_pie_title)).setText(title);
        PieChart pie = content.findViewById(R.id.pie_chart);

        List<PieEntry> entries = new ArrayList<>();
        List<Integer> colors = new ArrayList<>();
        for (int c = 0; c < 5; c++) {
            float val = useMinutes ? minutes[c] : counts[c];
            if (val > 0) {
                entries.add(new PieEntry(val, EXERCISE_CATEGORY[c][0]));
                colors.add(CAT_COLORS[c]);
            }
        }
        if (entries.isEmpty()) {
            entries.add(new PieEntry(1, "暂无数据"));
            colors.add(0xFF555555);
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(colors);
        dataSet.setSliceSpace(3f);
        dataSet.setValueTextColor(0xFFFFFFFF);
        dataSet.setValueTextSize(12f);
        dataSet.setValueFormatter(new com.github.mikephil.charting.formatter.ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return useMinutes ? ((int)value + "min") : ((int)value + "次");
            }
        });

        PieData data = new PieData(dataSet);
        pie.setData(data);
        pie.setUsePercentValues(false);
        pie.getDescription().setEnabled(false);
        pie.setDrawHoleEnabled(true);
        pie.setHoleColor(0xFF111111);
        pie.setHoleRadius(45f);
        pie.setTransparentCircleRadius(50f);
        pie.setRotationEnabled(false);
        pie.setDrawEntryLabels(false);
        pie.getLegend().setEnabled(false);
        pie.animateY(500);

        // 自定义图例行
        LinearLayout legendRow = content.findViewById(R.id.bs_pie_legend);
        buildPieLegend(legendRow, new String[]{"有氧运动", "力量训练", "柔韧拉伸", "球类运动", "其他运动"});

        // 点击饼块 → 显示该类记录
        int[] finalCounts = counts;
        int[] finalMinutes = minutes;
        pie.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(com.github.mikephil.charting.data.Entry e, Highlight h) {
                if (h.getX() < 0 || h.getX() >= 5) return;
                int catIdx = (int) h.getX();
                if (finalCounts[catIdx] == 0) return;
                String catName = EXERCISE_CATEGORY[catIdx][0];
                String subTitle = useMinutes
                        ? (finalMinutes[catIdx] >= 60 ? (finalMinutes[catIdx]/60 + "h" + finalMinutes[catIdx]%60 + "min") : finalMinutes[catIdx]+"min")
                        : finalCounts[catIdx] + "次";
                showCategoryRecordsSheet(catName + " · " + subTitle, catIdx, dateSet);
            }
            @Override public void onNothingSelected() {}
        });

        pie.invalidate();
        sheet.setContentView(content);
        sheet.show();
    }

    private void showCategoryRecordsSheet(String title, int catIdx, java.util.Set<String> dateSet) {
        BottomSheetDialog sheet = new BottomSheetDialog(requireContext());
        View content = LayoutInflater.from(requireContext())
                .inflate(R.layout.bottom_sheet_day_records, null);
        ((TextView) content.findViewById(R.id.bs_day_title)).setText(title);

        LinearLayout container = content.findViewById(R.id.bs_records_container);
        String[] feelEmoji = {"", "😫", "😕", "😐", "😊", "🔥"};
        String[] feelLabel = {"", "差", "较差", "一般", "良好", "爽"};

        for (com.example.fitcore.model.WorkoutRecord r : db.getRecordsByUser(session.getUserId())) {
            if (r.getRecordedAt() == null || r.getRecordedAt().length() < 10) continue;
            if (dateSet != null && !dateSet.contains(r.getRecordedAt().substring(0, 10))) continue;
            if (getCategory(r.getType()) != catIdx) continue;

            LinearLayout item = new LinearLayout(requireContext());
            item.setOrientation(LinearLayout.VERTICAL);
            item.setPadding(dp(14), dp(10), dp(14), dp(10));
            item.setBackgroundResource(R.drawable.bg_card);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.topMargin = dp(6);
            item.setLayoutParams(lp);

            TextView tvType = new TextView(requireContext());
            tvType.setText(r.getType());
            tvType.setTextColor(Color.parseColor("#FFFFFF"));
            tvType.setTextSize(15);
            tvType.setTypeface(null, android.graphics.Typeface.BOLD);

            int f = r.getFeeling();
            String emoji = (f >= 1 && f <= 5) ? feelEmoji[f] : "";
            String label = (f >= 1 && f <= 5) ? feelLabel[f] : "";
            TextView tvInfo = new TextView(requireContext());
            tvInfo.setText(r.getDurationMinutes() + "分钟 · 体感 " + emoji + " " + label);
            tvInfo.setTextColor(Color.parseColor("#AAAAAA"));
            tvInfo.setTextSize(12);
            tvInfo.setPadding(0, dp(4), 0, 0);

            item.addView(tvType);
            item.addView(tvInfo);
            container.addView(item);
        }

        sheet.setContentView(content);
        sheet.show();
    }

    private void setupDayPies(View view) {
        int di = selectedDayIndex;
        if (di < 0 || di >= 7) {
            dayPieCount.setVisibility(View.GONE);
            dayPieMinute.setVisibility(View.GONE);
            tvDayPieTitle.setVisibility(View.GONE);
            dayPieLegend.setVisibility(View.GONE);
            return;
        }

        String date = weekDates[di];
        int[] counts = new int[5];
        int[] minutes = new int[5];
        String[] catNames = new String[5];
        for (int c = 0; c < 5; c++) catNames[c] = EXERCISE_CATEGORY[c][0];

        for (com.example.fitcore.model.WorkoutRecord r : db.getRecordsByUser(session.getUserId())) {
            if (r.getRecordedAt() != null && r.getRecordedAt().length() >= 10
                    && r.getRecordedAt().substring(0, 10).equals(date)) {
                int cat = getCategory(r.getType());
                if (cat >= 0 && cat < 5) {
                    counts[cat]++;
                    minutes[cat] += r.getDurationMinutes();
                }
            }
        }

        boolean hasData = false;
        for (int c : counts) if (c > 0) { hasData = true; break; }

        if (!hasData) {
            dayPieCount.setVisibility(View.GONE);
            dayPieMinute.setVisibility(View.GONE);
            tvDayPieTitle.setVisibility(View.GONE);
            dayPieLegend.setVisibility(View.GONE);
            return;
        }

        dayPieCount.setVisibility(View.VISIBLE);
        dayPieMinute.setVisibility(View.VISIBLE);
        tvDayPieTitle.setVisibility(View.VISIBLE);
        dayPieLegend.setVisibility(View.VISIBLE);

        String[] labelTexts = {"一","二","三","四","五","六","日"};
        tvDayPieTitle.setText(date.substring(5) + " 周" + labelTexts[di] + " 分布");

        final java.util.Set<String> dateSet = new java.util.HashSet<>();
        dateSet.add(date);

        // 左: 次数饼图
        setupSinglePie(dayPieCount, counts, catNames, "次", dateSet);
        // 右: 时长饼图
        setupSinglePie(dayPieMinute, minutes, catNames, "min", dateSet);

        // 图例行
        buildPieLegend(dayPieLegend, catNames);
    }

    private void setupSinglePie(PieChart pie, int[] values, String[] catNames, String unit,
                                 java.util.Set<String> dateSet) {
        List<PieEntry> entries = new ArrayList<>();
        List<Integer> colors = new ArrayList<>();
        for (int c = 0; c < 5; c++) {
            if (values[c] > 0) {
                entries.add(new PieEntry(values[c], catNames[c]));
                colors.add(CAT_COLORS[c]);
            }
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(colors);
        dataSet.setSliceSpace(2f);
        dataSet.setValueTextColor(0xFFFFFFFF);
        dataSet.setValueTextSize(10f);
        dataSet.setValueFormatter(new com.github.mikephil.charting.formatter.ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                int v = (int) value;
                if ("min".equals(unit) && v >= 60) return (v / 60) + "h" + (v % 60) + "min";
                return v + unit;
            }
        });

        PieData data = new PieData(dataSet);
        pie.setData(data);
        pie.setUsePercentValues(false);
        pie.getDescription().setEnabled(false);
        pie.setDrawHoleEnabled(true);
        pie.setHoleColor(0xFF111111);
        pie.setHoleRadius(30f);
        pie.setTransparentCircleRadius(33f);
        pie.setRotationEnabled(false);
        pie.setDrawEntryLabels(false);
        pie.getLegend().setEnabled(false);

        pie.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(com.github.mikephil.charting.data.Entry e, Highlight h) {
                if (h.getX() < 0 || h.getX() >= 5) return;
                int catIdx = (int) h.getX();
                if (values[catIdx] == 0) return;
                showCategoryRecordsSheet(catNames[catIdx] + " · " + (int) e.getY() + unit, catIdx, dateSet);
            }
            @Override public void onNothingSelected() {}
        });

        pie.animateY(300);
        pie.invalidate();
    }

    private void buildPieLegend(LinearLayout container, String[] catNames) {
        container.removeAllViews();
        for (int c = 0; c < 5; c++) {
            LinearLayout item = new LinearLayout(requireContext());
            item.setOrientation(LinearLayout.HORIZONTAL);
            item.setGravity(Gravity.CENTER);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.setMargins(dp(6), 0, dp(6), 0);
            item.setLayoutParams(lp);

            View dot = new View(requireContext());
            dot.setBackgroundColor(CAT_COLORS[c]);
            LinearLayout.LayoutParams dlp = new LinearLayout.LayoutParams(dp(8), dp(8));
            dlp.gravity = Gravity.CENTER;
            dot.setLayoutParams(dlp);

            TextView label = new TextView(requireContext());
            label.setText(catNames[c]);
            label.setTextColor(Color.parseColor("#AAAAAA"));
            label.setTextSize(10);
            label.setPadding(dp(3), 0, 0, 0);

            item.addView(dot);
            item.addView(label);
            container.addView(item);
        }
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
            setupDayPies(getView());
        }
    }
}
