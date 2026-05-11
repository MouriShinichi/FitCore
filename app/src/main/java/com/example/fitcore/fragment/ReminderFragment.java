package com.example.fitcore.fragment;

import android.Manifest;
import android.app.AlarmManager;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.fitcore.R;
import com.example.fitcore.database.DatabaseHelper;
import com.example.fitcore.model.ReminderSettings;
import com.example.fitcore.utils.NotificationHelper;
import com.example.fitcore.utils.SessionManager;

public class ReminderFragment extends Fragment {

    private DatabaseHelper db;
    private SessionManager session;
    private ReminderSettings settings;
    private TextView tvTimeDisplay;
    private SwitchCompat swReminder;

    private TextView modeOnce, modeCustom;
    private View layoutDays;
    private TextView[] dayViews;
    private boolean[] selectedDays = new boolean[7];
    private boolean isOnceMode = true;
    private boolean onceEnabled = false, customEnabled = false;

    private ActivityResultLauncher<String> notificationLauncher;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_reminder, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        notificationLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                granted -> {
                    if (granted) {
                        enableReminder(true);
                    } else {
                        setSwitchSafely(false);
                        Toast.makeText(requireContext(), "需要通知权限才能发送提醒", Toast.LENGTH_SHORT).show();
                    }
                });

        db = DatabaseHelper.getInstance(requireContext());
        session = new SessionManager(requireContext());
        tvTimeDisplay = view.findViewById(R.id.tv_time_display);
        swReminder = view.findViewById(R.id.sw_reminder);

        settings = db.getReminderSettings(session.getUserId());
        if (settings == null) {
            settings = new ReminderSettings(session.getUserId(), 7, 0, false);
            db.insertReminderSettings(settings);
        }
        updateTimeDisplay();
        swReminder.setChecked(settings.isEnabled());

        // 加载独立状态
        onceEnabled = settings.isEnabled();
        customEnabled = settings.isEnabled();
        swReminder.setChecked(settings.isEnabled());

        // 随机语句
        loadRandomQuotes(view);

        // 开关切换 → 检查权限后保存
        swReminder.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!isChecked) {
                // 关闭提醒不需要权限
                if (isOnceMode) onceEnabled = false;
                else customEnabled = false;
                settings.setEnabled(false);
                saveSettings();
                return;
            }
            // 开启提醒：先检查权限
            checkPermissionsThenEnable(isChecked);
        });

        // 模式切换
        modeOnce = view.findViewById(R.id.mode_once);
        modeCustom = view.findViewById(R.id.mode_custom);
        layoutDays = view.findViewById(R.id.layout_days);

        modeOnce.setOnClickListener(v -> { switchMode(true); saveSettings(); });
        modeCustom.setOnClickListener(v -> { switchMode(false); saveSettings(); });

        // 星期按钮
        String[] dayIds = {"day_mon","day_tue","day_wed","day_thu","day_fri","day_sat","day_sun"};
        dayViews = new TextView[7];
        for (int i = 0; i < 7; i++) {
            final int idx = i;
            int rid = getResources().getIdentifier(dayIds[i], "id", requireContext().getPackageName());
            dayViews[i] = view.findViewById(rid);
            dayViews[i].setOnClickListener(v -> {
                selectedDays[idx] = !selectedDays[idx];
                updateDayStyle(idx);
                saveSettings();
            });
        }

        // 默认不选
        for (int i = 0; i < 7; i++) selectedDays[i] = false;
        switchMode(true);

        // 时间选择
        view.findViewById(R.id.btn_time_picker).setOnClickListener(v -> {
            new TimePickerDialog(requireContext(),
                    (vp, hour, minute) -> {
                        settings.setHour(hour);
                        settings.setMinute(minute);
                        updateTimeDisplay();
                    }, settings.getHour(), settings.getMinute(), true).show();
        });

        // 测试通知
        view.findViewById(R.id.btn_test_notify).setOnClickListener(v -> {
            NotificationHelper.createChannel(requireContext());
            NotificationHelper.showInstantNotification(requireContext());
            Toast.makeText(requireContext(), "通知已发送，请查看状态栏", Toast.LENGTH_SHORT).show();
        });

    }

    private void checkPermissionsThenEnable(boolean toggled) {
        // 检查通知权限 (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
                return;
            }
        }
        // 检查精确闹钟权限 (Android 12+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager alarm = (AlarmManager) requireContext().getSystemService(android.content.Context.ALARM_SERVICE);
            if (alarm != null && !alarm.canScheduleExactAlarms()) {
                setSwitchSafely(false);
                Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                intent.setData(android.net.Uri.parse("package:" + requireContext().getPackageName()));
                startActivity(intent);
                Toast.makeText(requireContext(), "请允许精确闹钟权限后再开启提醒", Toast.LENGTH_LONG).show();
                return;
            }
        }
        enableReminder(true);
    }

    private void enableReminder(boolean enabled) {
        if (isOnceMode) onceEnabled = enabled;
        else customEnabled = enabled;
        settings.setEnabled(enabled);
        saveSettings();
    }

    private void saveSettings() {
        try {
            StringBuilder sb = new StringBuilder();
            if (isOnceMode) {
                sb.append("once");
            } else {
                for (int i = 0; i < 7; i++)
                    if (selectedDays[i]) sb.append(i + 1).append(",");
                if (sb.length() > 0) sb.deleteCharAt(sb.length() - 1);
            }
            settings.setDaysOfWeek(sb.toString());
            db.updateReminderSettings(settings);
            NotificationHelper.scheduleReminder(requireContext(), settings.getHour(),
                    settings.getMinute(), settings.isEnabled(), settings.getDaysOfWeek());
        } catch (Exception ignored) {}
    }

    private void switchMode(boolean once) {
        isOnceMode = once;
        modeOnce.setBackground(once ? ContextCompat.getDrawable(requireContext(), R.drawable.bg_toggle_active) : null);
        modeOnce.setTextColor(once ? 0xFF000000 : 0xFF888888);
        modeCustom.setBackground(once ? null : ContextCompat.getDrawable(requireContext(), R.drawable.bg_toggle_active));
        modeCustom.setTextColor(once ? 0xFF888888 : 0xFF000000);
        layoutDays.setVisibility(once ? View.GONE : View.VISIBLE);
        if (!once) for (int i = 0; i < 7; i++) updateDayStyle(i);

        // 切换独立开关状态
        setSwitchSafely(once ? onceEnabled : customEnabled);
    }

    private void setSwitchSafely(boolean checked) {
        swReminder.setOnCheckedChangeListener(null);
        swReminder.setChecked(checked);
        settings.setEnabled(checked);
        swReminder.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!isChecked) {
                if (isOnceMode) onceEnabled = false;
                else customEnabled = false;
                settings.setEnabled(false);
                saveSettings();
                return;
            }
            checkPermissionsThenEnable(isChecked);
        });
    }

    private void updateDayStyle(int idx) {
        if (selectedDays[idx]) {
            dayViews[idx].setBackgroundResource(R.drawable.bg_accent_btn);
            dayViews[idx].setTextColor(0xFF000000);
        } else {
            dayViews[idx].setBackgroundResource(R.drawable.bg_card);
            dayViews[idx].setTextColor(0xFF888888);
        }
    }

    private void loadRandomQuotes(View view) {
        String[] quotes1 = {
            "千里之行，始于足下", "生命在于运动", "自律给我自由", "汗水不会骗人",
            "今天的坚持，明天的蜕变", "没有借口，只有行动", "每一次锻炼都是投资",
            "越努力越幸运", "放弃不难，但坚持很酷", "你就是自己的英雄",
            "每一步都算数", "改变，从现在开始", "身体是革命的本钱",
            "运动使人快乐", "突破极限，超越自我", "让运动成为习惯",
            "热爱可抵岁月漫长", "没有白流的汗", "你的对手只有自己",
            "今天流的汗，明天都会回报你", "最好的时机就是现在", "不逼自己一把怎么知道",
        };
        String[] quotes2 = {
            "坚持锻炼，成就更好的自己", "每天进步一点点", "健康是最大的财富",
            "你的坚持终将美好", "行动起来吧", "付出总会有收获",
            "加油，你可以的！", "今天的努力成就明天的你", "生命不息，运动不止",
            "你就是你坚持的结果", "每一滴汗都值得", "向着目标前进",
            "做更好的自己", "相信过程", "时间会给你答案",
            "不辜负每一份努力", "没有什么不可能", "好身材是练出来的",
            "坚持下去就是胜利", "运动是最好的护肤品", "身体和灵魂都要在路上",
        };
        java.util.Random rnd = new java.util.Random();
        ((TextView) view.findViewById(R.id.tv_quote1)).setText(quotes1[rnd.nextInt(quotes1.length)]);
        ((TextView) view.findViewById(R.id.tv_quote2)).setText(quotes2[rnd.nextInt(quotes2.length)]);
    }

    private void updateTimeDisplay() {
        tvTimeDisplay.setText(String.format("%02d:%02d", settings.getHour(), settings.getMinute()));
    }

    @Override
    public void onResume() {
        super.onResume();
        // 重新从数据库加载，确保响一次后开关自动归位
        ReminderSettings fresh = db.getReminderSettings(session.getUserId());
        if (fresh != null) {
            settings = fresh;
            swReminder.setChecked(settings.isEnabled());
        }
    }
}
