package com.example.fitcore.activity;

import android.app.TimePickerDialog;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import com.example.fitcore.R;
import com.example.fitcore.database.DatabaseHelper;
import com.example.fitcore.model.ReminderSettings;
import com.example.fitcore.utils.NotificationHelper;
import com.example.fitcore.utils.SessionManager;

public class ReminderSettingsActivity extends AppCompatActivity {

    private DatabaseHelper db;
    private SessionManager session;
    private ReminderSettings settings;
    private TextView tvTimeDisplay;
    private SwitchCompat swReminder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reminder_settings);

        db = DatabaseHelper.getInstance(this);
        session = new SessionManager(this);

        tvTimeDisplay = findViewById(R.id.tv_time_display);
        swReminder = findViewById(R.id.sw_reminder);

        settings = db.getReminderSettings(session.getUserId());
        if (settings == null) {
            settings = new ReminderSettings(session.getUserId(), 7, 0, true);
            db.insertReminderSettings(settings);
        }

        updateTimeDisplay();
        swReminder.setChecked(settings.isEnabled());

        findViewById(R.id.btn_time_picker).setOnClickListener(v -> {
            TimePickerDialog dialog = new TimePickerDialog(this,
                    (view, hour, minute) -> {
                        settings.setHour(hour);
                        settings.setMinute(minute);
                        updateTimeDisplay();
                    },
                    settings.getHour(), settings.getMinute(), true);
            dialog.show();
        });

        findViewById(R.id.btn_test_notify).setOnClickListener(v -> {
            NotificationHelper.showInstantNotification(this);
            Toast.makeText(this, "测试通知已发送", Toast.LENGTH_SHORT).show();
        });

        findViewById(R.id.btn_save_reminder).setOnClickListener(v -> {
            settings.setEnabled(swReminder.isChecked());
            db.updateReminderSettings(settings);
            NotificationHelper.scheduleReminder(this, settings.getHour(),
                    settings.getMinute(), settings.isEnabled());
            Toast.makeText(this, "提醒设置已保存", Toast.LENGTH_SHORT).show();
            finish();
        });
    }

    private void updateTimeDisplay() {
        tvTimeDisplay.setText(String.format("%02d:%02d", settings.getHour(), settings.getMinute()));
    }
}
