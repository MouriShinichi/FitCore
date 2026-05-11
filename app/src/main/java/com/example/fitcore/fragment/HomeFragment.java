package com.example.fitcore.fragment;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.net.Uri;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.fitcore.R;
import com.example.fitcore.activity.PlanListActivity;
import com.example.fitcore.database.DatabaseHelper;
import com.example.fitcore.model.FitnessPlan;
import com.example.fitcore.model.WorkoutRecord;
import com.example.fitcore.utils.SessionManager;

public class HomeFragment extends Fragment {

    private DatabaseHelper db;
    private SessionManager session;
    private ImageView ivHomeAvatar;
    private final Handler handler = new Handler(Looper.getMainLooper());

    private static final String[] QUOTES = {
        "千里之行，始于足下", "生命在于运动", "自律给我自由", "汗水不会骗人",
        "今天的坚持，明天的蜕变", "没有借口，只有行动", "每一次锻炼都是投资",
        "越努力越幸运", "放弃不难，但坚持很酷", "你就是自己的英雄",
        "每一步都算数", "改变，从现在开始", "突破极限，超越自我",
        "运动使人快乐", "让运动成为习惯", "热爱可抵岁月漫长",
        "没有白流的汗", "你的对手只有自己", "最好的时机就是现在",
    };

    private static final String[][][] PLAN_PHASES = {
        {{"🔥 热身", "5min", "快走、动态拉伸、跳绳"},
         {"🏋 主训", "30min", "卧推、哑铃、深蹲、硬拉、引体向上、俯卧撑"},
         {"🏃 有氧", "10min", "跑步、骑行、划船、椭圆机"},
         {"🧘 拉伸", "5min", "拉伸、泡沫轴、瑜伽"}},
        {{"🔥 热身", "5min", "快走、动态拉伸、跳绳"},
         {"⚡ 高强度", "15min", "冲刺跑、有氧操、跳绳、登山、拳击"},
         {"🔄 间歇恢复", "5min", "慢跑、快走"},
         {"🧘 冷却", "5min", "拉伸、泡沫轴、筋膜放松"}},
        {{"🧎 呼吸冥想", "5min", "冥想、太极、八段锦"},
         {"🧘 主拉伸", "20min", "瑜伽、普拉提、开肩、压腿、猫式、婴儿式"},
         {"🦯 筋膜放松", "5min", "泡沫轴、筋膜放松、脊柱扭转"}},
        {{"🔥 热身", "5min", "快走、动态拉伸"},
         {"🏃 主跑", "30min", "跑步、慢跑、越野跑、马拉松"},
         {"🔄 交叉训练", "15min", "骑行、游泳、划船、登山"},
         {"🧘 拉伸", "5min", "拉伸、泡沫轴、压腿"}},
        // 篮球特训
        {{"🔥 热身", "10min", "慢跑、动态拉伸、高抬腿"},
         {"🏀 球技训练", "20min", "运球、投篮、传球、上篮"},
         {"⚡ 弹跳训练", "10min", "深蹲跳、弓步蹲、跳绳"},
         {"🧘 冷却", "5min", "拉伸、泡沫轴"}},
        // 足球专项
        {{"🔥 热身", "10min", "慢跑、动态拉伸、折返跑"},
         {"⚽ 技术训练", "20min", "盘带、传球、射门、头球"},
         {"🏃 体能跑", "15min", "冲刺跑、耐力跑、间歇跑"},
         {"🧘 拉伸", "5min", "拉伸、泡沫轴、压腿"}},
        // 游泳训练
        {{"🔥 陆上热身", "5min", "动态拉伸、开肩、压腿"},
         {"🏊 水中训练", "25min", "自由泳、蛙泳、打腿、划臂"},
         {"🔄 技术练习", "10min", "转身练习、呼吸节奏、浮板"},
         {"🧘 放松", "5min", "拉伸、泡沫轴"}},
        // 综合体能训练
        {{"🔥 热身", "5min", "快走、动态拉伸、跳绳"},
         {"💪 力量", "15min", "俯卧撑、深蹲、哑铃、引体向上"},
         {"🏃 有氧", "10min", "跑步、骑行、划船"},
         {"🧘 柔韧", "10min", "瑜伽、拉伸、泡沫轴"}},
    };
    private final Runnable greetingUpdater = new Runnable() {
        @Override public void run() {
            refreshGreeting();
            handler.postDelayed(this, 1000);
        }
    };

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        db = DatabaseHelper.getInstance(requireContext());
        session = new SessionManager(requireContext());

        ivHomeAvatar = view.findViewById(R.id.iv_home_avatar);
        loadHomeAvatar();
        refreshGreeting();

        refreshStats();
        refreshPlan();

        view.findViewById(R.id.plan_card).setOnClickListener(v ->
                startActivity(new Intent(getActivity(), PlanListActivity.class)));
        view.findViewById(R.id.btn_view_plans).setOnClickListener(v ->
                startActivity(new Intent(getActivity(), PlanListActivity.class)));

        view.findViewById(R.id.card_today_goal).setOnClickListener(v -> showGoalDialog());
        view.findViewById(R.id.card_recent_workout).setOnClickListener(v ->
                startActivity(new Intent(getActivity(),
                        com.example.fitcore.activity.WorkoutHistoryActivity.class)));
        view.findViewById(R.id.tv_deselect_plan).setOnClickListener(v -> {
            showDeselectConfirm();
        });

        // 随机励志语
        ((TextView) view.findViewById(R.id.tv_quote)).setText(
                "「" + QUOTES[new java.util.Random().nextInt(QUOTES.length)] + "」");
    }

    private void refreshStats() {
        if (getView() == null) return;
        int userId = session.getUserId();
        ((TextView) getView().findViewById(R.id.tv_workouts))
                .setText(String.valueOf(db.getTotalWorkouts(userId)));
        int totalMins = db.getTotalMinutes(userId);
        ((TextView) getView().findViewById(R.id.tv_minutes))
                .setText((totalMins / 60) + "h " + (totalMins % 60) + "min");
        ((TextView) getView().findViewById(R.id.tv_streak)).setText(String.valueOf(calcTotalDays()));

        // 今日目标
        SharedPreferences prefs = requireContext()
                .getSharedPreferences("fitcore_prefs", 0);
        int goal = prefs.getInt("daily_goal_minutes", 60);
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
        String today = sdf.format(new java.util.Date());
        int todayMins = 0;
        for (WorkoutRecord r : db.getRecordsByUser(userId)) {
            if (r.getRecordedAt() != null && r.getRecordedAt().startsWith(today))
                todayMins += r.getDurationMinutes();
        }
        ((TextView) getView().findViewById(R.id.tv_today_goal))
                .setText(todayMins + " / " + goal + " 分钟");
        ((TextView) getView().findViewById(R.id.tv_goal_pct))
                .setText(Math.min(100, todayMins * 100 / goal) + "%");

        // 今日快照
        int todayCount = 0, todayCal = 0;
        for (WorkoutRecord r : db.getRecordsByUser(userId)) {
            if (r.getRecordedAt() != null && r.getRecordedAt().startsWith(today)) {
                todayCount++;
                todayCal += calcHomeCalories(r.getType(), r.getDurationMinutes());
            }
        }
        if (todayCount > 0) {
            ((TextView) getView().findViewById(R.id.tv_today_snapshot))
                    .setText("今天已完成 " + todayCount + " 次运动 · 共 " + todayMins + " 分钟 · 消耗 " + todayCal + " 千卡");
        } else {
            ((TextView) getView().findViewById(R.id.tv_today_snapshot)).setText("");
        }

        // 本周概要
        java.util.Calendar cal = java.util.Calendar.getInstance();
        int dow = cal.get(java.util.Calendar.DAY_OF_WEEK);
        int daysBack = (dow + 5) % 7;
        cal.add(java.util.Calendar.DAY_OF_MONTH, -daysBack);
        int weekCount = 0, weekMins = 0, weekCal = 0;
        for (int d = 0; d < 7; d++) {
            String ds = sdf.format(cal.getTime());
            for (WorkoutRecord r : db.getRecordsByUser(userId)) {
                if (r.getRecordedAt() != null && r.getRecordedAt().startsWith(ds)) {
                    weekCount++;
                    weekMins += r.getDurationMinutes();
                    weekCal += calcHomeCalories(r.getType(), r.getDurationMinutes());
                }
            }
            cal.add(java.util.Calendar.DAY_OF_MONTH, 1);
        }
        ((TextView) getView().findViewById(R.id.tv_week_summary))
                .setText("本周 " + weekCount + "次 · " + weekMins + "分钟 · 消耗 " + weekCal + "千卡");

        // 最近一次运动
        java.util.List<WorkoutRecord> allRecords = db.getRecordsByUser(userId);
        View cardRecent = getView().findViewById(R.id.card_recent_workout);
        if (allRecords.size() > 0) {
            WorkoutRecord latest = allRecords.get(0);
            int f = latest.getFeeling();
            String[] emoji = {"", "😫", "😕", "😐", "😊", "🔥"};
            String fe = (f >= 1 && f <= 5) ? emoji[f] : "";
            String timeStr = "";
            try {
                String at = latest.getRecordedAt();
                java.text.SimpleDateFormat sdf2 = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.getDefault());
                java.util.Date dt = sdf2.parse(at);
                java.text.SimpleDateFormat sdf3 = new java.text.SimpleDateFormat("MM-dd HH:mm", java.util.Locale.getDefault());
                timeStr = sdf3.format(dt);
            } catch (Exception ignored) {}
            ((TextView) getView().findViewById(R.id.tv_recent_workout))
                    .setText(latest.getType() + " · " + latest.getDurationMinutes() + "分钟 · " + timeStr);
            ((TextView) getView().findViewById(R.id.tv_recent_feeling)).setText(fe);
            cardRecent.setVisibility(View.VISIBLE);
        } else {
            cardRecent.setVisibility(View.GONE);
        }
    }

    private int calcHomeCalories(String type, int minutes) {
        double met = 5.0;
        String t = type.toLowerCase();
        if (t.contains("跑") || t.contains("马拉松")) met = 10.0;
        else if (t.contains("跳绳")) met = 11.0;
        else if (t.contains("游泳")) met = 8.0;
        else if (t.contains("拳击") || t.contains("跆拳道")) met = 10.0;
        else if (t.contains("篮球") || t.contains("足球")) met = 8.0;
        else if (t.contains("骑") || t.contains("单车")) met = 7.0;
        else if (t.contains("深蹲") || t.contains("硬拉") || t.contains("卧推")) met = 6.0;
        else if (t.contains("瑜伽") || t.contains("普拉提")) met = 3.5;
        else if (t.contains("走")) met = 4.0;
        com.example.fitcore.model.User user = db.getUserById(session.getUserId());
        double w = (user != null && user.getWeight() > 0) ? user.getWeight() : 70;
        return (int) (met * w * (minutes / 60.0));
    }

    private void refreshPlan() {
        if (getView() == null) return;
        LinearLayout phasesArea = getView().findViewById(R.id.plan_phases_area);
        phasesArea.removeAllViews();

        FitnessPlan cp = db.getCurrentPlan(session.getUserId());
        TextView tvDeselect = getView().findViewById(R.id.tv_deselect_plan);
        if (cp != null) {
            tvDeselect.setVisibility(View.VISIBLE);
            ((TextView) getView().findViewById(R.id.tv_plan_name)).setText(cp.getName());
            ((TextView) getView().findViewById(R.id.tv_plan_duration)).setText(cp.getDuration());
            ((TextView) getView().findViewById(R.id.tv_plan_freq)).setText(cp.getFrequency());
            int idx = cp.getName().contains("推拉") ? 0 :
                      cp.getName().contains("HIIT") || cp.getName().contains("燃脂") ? 1 :
                      cp.getName().contains("柔韧") ? 2 :
                      cp.getName().contains("耐力") ? 3 :
                      cp.getName().contains("篮球") ? 4 :
                      cp.getName().contains("足球") ? 5 :
                      cp.getName().contains("游泳") ? 6 : 7;

            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
            String today = sdf.format(new java.util.Date());
            SharedPreferences prefs = requireContext().getSharedPreferences("fitcore_prefs", 0);

            for (int pi = 0; pi < PLAN_PHASES[idx].length; pi++) {
                String[] phase = PLAN_PHASES[idx][pi];
                String phaseKey = "phase_done_" + today + "_" + idx + "_" + pi;
                boolean done = prefs.getBoolean(phaseKey, false);

                LinearLayout row = new LinearLayout(requireContext());
                row.setOrientation(LinearLayout.HORIZONTAL);
                row.setGravity(android.view.Gravity.CENTER_VERTICAL);
                row.setPadding(dp(12), dp(10), dp(12), dp(10));
                row.setBackgroundResource(done ? R.drawable.bg_card_accent : R.drawable.bg_card);
                LinearLayout.LayoutParams rlp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
                rlp.topMargin = dp(4);
                row.setLayoutParams(rlp);

                LinearLayout textCol = new LinearLayout(requireContext());
                textCol.setOrientation(LinearLayout.VERTICAL);
                LinearLayout.LayoutParams tclp = new LinearLayout.LayoutParams(
                        0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
                textCol.setLayoutParams(tclp);

                TextView tvPhase = new TextView(requireContext());
                tvPhase.setText(phase[0] + "  " + phase[1]);
                tvPhase.setTextColor(done ? 0xFFCCFF00 : 0xFFCCFF00);
                tvPhase.setTextSize(13);
                tvPhase.setTypeface(null, android.graphics.Typeface.BOLD);
                textCol.addView(tvPhase);

                TextView tvEx = new TextView(requireContext());
                tvEx.setText(phase[2]);
                tvEx.setTextColor(0xFF999999);
                tvEx.setTextSize(11);
                tvEx.setPadding(0, dp(2), 0, 0);
                textCol.addView(tvEx);

                row.addView(textCol);

                // 完成勾选按钮
                TextView cb = new TextView(requireContext());
                cb.setText(done ? "✓" : "○");
                cb.setTextColor(done ? 0xFF000000 : 0xFF888888);
                cb.setTextSize(18);
                cb.setGravity(android.view.Gravity.CENTER);
                cb.setBackgroundResource(done ? R.drawable.bg_accent_circle : R.drawable.bg_card);
                cb.setPadding(dp(8), dp(4), dp(8), dp(4));
                int[] finalIdx = {done ? 1 : 0};
                cb.setOnClickListener(cv -> {
                    boolean newDone = !prefs.getBoolean(phaseKey, false);
                    prefs.edit().putBoolean(phaseKey, newDone).apply();
                    refreshPlan();
                    // 检查是否所有环节完成
                    boolean allDone = true;
                    for (int pj = 0; pj < PLAN_PHASES[idx].length; pj++) {
                        String pk = "phase_done_" + today + "_" + idx + "_" + pj;
                        if (!prefs.getBoolean(pk, false)) { allDone = false; break; }
                    }
                    if (allDone) showPlanCompleteDialog();
                });
                row.addView(cb);

                phasesArea.addView(row);
            }
        } else {
            tvDeselect.setVisibility(View.GONE);
            ((TextView) getView().findViewById(R.id.tv_plan_name)).setText("未选择计划");
            ((TextView) getView().findViewById(R.id.tv_plan_duration)).setText("-");
            ((TextView) getView().findViewById(R.id.tv_plan_freq)).setText("-");
        }
    }

    private int calcTotalDays() {
        java.util.Set<String> days = new java.util.HashSet<>();
        for (WorkoutRecord r : db.getRecordsByUser(session.getUserId())) {
            if (r.getRecordedAt() != null && r.getRecordedAt().length() >= 10) {
                days.add(r.getRecordedAt().substring(0, 10));
            }
        }
        return days.size();
    }

    private void showGoalDialog() {
        View dlg = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_confirm, null);
        ((android.widget.ImageView) dlg.findViewById(R.id.dialog_icon))
                .setImageResource(android.R.drawable.ic_menu_edit);
        ((TextView) dlg.findViewById(R.id.dialog_title)).setText("设置每日目标");
        ((TextView) dlg.findViewById(R.id.dialog_message)).setText("单位：分钟");

        EditText et = new EditText(requireContext());
        et.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        SharedPreferences prefs = requireContext()
                .getSharedPreferences("fitcore_prefs", 0);
        et.setText(String.valueOf(prefs.getInt("daily_goal_minutes", 60)));
        et.setTextColor(0xFFFFFFFF);
        et.setTextSize(18);
        et.setBackgroundResource(R.drawable.bg_card);
        et.setPadding(dp(14), dp(10), dp(14), dp(10));
        LinearLayout.LayoutParams elp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        elp.topMargin = dp(12);
        elp.bottomMargin = dp(12);
        et.setLayoutParams(elp);
        ((ViewGroup) dlg).addView(et, 1); // 插入到标题行和说明文字之间

        TextView btnPos = dlg.findViewById(R.id.dialog_positive);
        TextView btnNeg = dlg.findViewById(R.id.dialog_negative);
        btnPos.setText("保存");
        btnNeg.setText("取消");

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(dlg).setCancelable(true).create();
        if (dialog.getWindow() != null)
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        btnPos.setOnClickListener(v -> {
            try {
                int g = Integer.parseInt(et.getText().toString().trim());
                if (g < 1) g = 1;
                prefs.edit().putInt("daily_goal_minutes", g).apply();
                refreshStats();
            } catch (NumberFormatException ignored) {}
            dialog.dismiss();
        });
        btnNeg.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void showDeselectConfirm() {
        View dlg = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_confirm, null);
        ((android.widget.ImageView) dlg.findViewById(R.id.dialog_icon))
                .setImageResource(android.R.drawable.ic_dialog_alert);
        ((TextView) dlg.findViewById(R.id.dialog_title)).setText("取消选择计划");
        ((TextView) dlg.findViewById(R.id.dialog_message)).setText("确定要取消当前计划吗？环节进度将被清除");
        TextView btnPos = dlg.findViewById(R.id.dialog_positive);
        TextView btnNeg = dlg.findViewById(R.id.dialog_negative);
        btnPos.setText("确定取消");
        btnNeg.setText("保留计划");

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(dlg).setCancelable(true).create();
        if (dialog.getWindow() != null)
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        btnPos.setOnClickListener(v -> {
            db.clearCurrentPlan(session.getUserId());
            refreshPlan();
            dialog.dismiss();
        });
        btnNeg.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void showPlanCompleteDialog() {
        View dlg = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_confirm, null);
        ((android.widget.ImageView) dlg.findViewById(R.id.dialog_icon))
                .setImageResource(android.R.drawable.ic_media_play);
        ((TextView) dlg.findViewById(R.id.dialog_title)).setText("计划完成！");
        ((TextView) dlg.findViewById(R.id.dialog_message)).setText("今日所有环节已完成，是否移除该计划？");
        TextView btnPos = dlg.findViewById(R.id.dialog_positive);
        TextView btnNeg = dlg.findViewById(R.id.dialog_negative);
        btnPos.setText("移除计划");
        btnNeg.setText("保留");

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(dlg).setCancelable(true).create();
        if (dialog.getWindow() != null)
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        btnPos.setOnClickListener(v -> {
            db.clearCurrentPlan(session.getUserId());
            refreshPlan();
            dialog.dismiss();
        });
        btnNeg.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private int dp(int d) { return (int) (d * getResources().getDisplayMetrics().density); }

    private void refreshGreeting() {
        if (getView() == null) return;
        String greeting = getGreeting() + "，" + session.getUserName();
        TextView tv = getView().findViewById(R.id.tv_greeting);
        if (tv != null) tv.setText(greeting);

        java.util.Calendar cal = java.util.Calendar.getInstance();
        int m = cal.get(java.util.Calendar.MONTH) + 1;
        int d = cal.get(java.util.Calendar.DAY_OF_MONTH);
        int dow = cal.get(java.util.Calendar.DAY_OF_WEEK);
        String[] weeks = {"周日","周一","周二","周三","周四","周五","周六"};
        TextView tvDate = getView().findViewById(R.id.tv_date_week);
        if (tvDate != null) tvDate.setText(m + "月" + d + "日 " + weeks[dow - 1]);
    }

    private String getGreeting() {
        int h = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY);
        if (h < 5) return "凌晨好"; if (h < 9) return "早上好";
        if (h < 12) return "上午好"; if (h < 14) return "中午好";
        if (h < 18) return "下午好"; return "晚上好";
    }

    private void loadHomeAvatar() {
        ivHomeAvatar.setClipToOutline(true);
        ivHomeAvatar.setOutlineProvider(new android.view.ViewOutlineProvider() {
            @Override
            public void getOutline(View view, android.graphics.Outline outline) {
                outline.setOval(0, 0, view.getWidth(), view.getHeight());
            }
        });
        java.io.File file = new java.io.File(requireContext().getFilesDir(),
                "avatar_" + session.getUserId() + ".jpg");
        if (file.exists()) {
            ivHomeAvatar.setImageURI(Uri.fromFile(file));
            ivHomeAvatar.setBackground(null);
            ivHomeAvatar.setImageTintList(null);
        } else {
            ivHomeAvatar.setImageResource(R.drawable.ic_gender);
            ivHomeAvatar.setBackgroundResource(R.drawable.bg_avatar_ring);
            ivHomeAvatar.setImageTintList(android.content.res.ColorStateList.valueOf(0xFFFFFFFF));
        }
    }

    @Override public void onResume() {
        super.onResume();
        handler.post(greetingUpdater);
        if (getView() != null) { loadHomeAvatar(); refreshGreeting(); refreshStats(); refreshPlan(); }
    }
    @Override public void onPause() { super.onPause(); handler.removeCallbacks(greetingUpdater); }
}
