package com.example.fitcore.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
    private final Handler handler = new Handler(Looper.getMainLooper());
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

        refreshGreeting();
        ((TextView) view.findViewById(R.id.tv_greeting_name)).setText(session.getUserName());

        refreshStats();
        refreshPlan();

        view.findViewById(R.id.plan_card).setOnClickListener(v ->
                startActivity(new Intent(getActivity(), PlanListActivity.class)));
        view.findViewById(R.id.btn_view_plans).setOnClickListener(v ->
                startActivity(new Intent(getActivity(), PlanListActivity.class)));
    }

    private void refreshStats() {
        if (getView() == null) return;
        int userId = session.getUserId();
        ((TextView) getView().findViewById(R.id.tv_workouts))
                .setText(String.valueOf(db.getTotalWorkouts(userId)));
        int totalMins = db.getTotalMinutes(userId);
        ((TextView) getView().findViewById(R.id.tv_minutes))
                .setText((totalMins / 60) + "h " + (totalMins % 60) + "min");
        ((TextView) getView().findViewById(R.id.tv_streak)).setText(String.valueOf(calcStreak()));

        // 今日目标
        int goal = 60;
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
    }

    private void refreshPlan() {
        if (getView() == null) return;
        FitnessPlan cp = db.getCurrentPlan(session.getUserId());
        if (cp != null) {
            ((TextView) getView().findViewById(R.id.tv_plan_name)).setText(cp.getName());
            ((TextView) getView().findViewById(R.id.tv_plan_duration)).setText(cp.getDuration());
            ((TextView) getView().findViewById(R.id.tv_plan_freq)).setText(cp.getFrequency());
        }
    }

    private int calcStreak() {
        java.util.Set<String> days = new java.util.HashSet<>();
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
        for (WorkoutRecord r : db.getRecordsByUser(session.getUserId())) {
            if (r.getRecordedAt() != null && r.getRecordedAt().length() >= 10) {
                days.add(r.getRecordedAt().substring(0, 10));
            }
        }
        int streak = 0;
        java.util.Calendar cal = java.util.Calendar.getInstance();
        while (true) {
            String d = sdf.format(cal.getTime());
            if (days.contains(d)) { streak++; cal.add(java.util.Calendar.DAY_OF_MONTH, -1); }
            else break;
        }
        return streak;
    }

    private void refreshGreeting() {
        if (getView() == null) return;
        TextView tv = getView().findViewById(R.id.tv_greeting);
        if (tv != null) tv.setText(getGreeting());
    }

    private String getGreeting() {
        int h = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY);
        if (h < 5) return "凌晨好"; if (h < 9) return "早上好";
        if (h < 12) return "上午好"; if (h < 14) return "中午好";
        if (h < 18) return "下午好"; return "晚上好";
    }

    @Override public void onResume() {
        super.onResume();
        handler.post(greetingUpdater);
        if (getView() != null) { refreshGreeting(); refreshStats(); refreshPlan(); }
    }
    @Override public void onPause() { super.onPause(); handler.removeCallbacks(greetingUpdater); }
}
