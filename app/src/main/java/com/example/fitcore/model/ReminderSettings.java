package com.example.fitcore.model;

import android.content.ContentValues;
import android.database.Cursor;

public class ReminderSettings {
    private int id;
    private int userId;
    private int hour;
    private int minute;
    private boolean enabled;
    private String daysOfWeek = "1,2,3,4,5,6,7";

    public ReminderSettings() {}

    public ReminderSettings(int userId, int hour, int minute, boolean enabled) {
        this.userId = userId;
        this.hour = hour;
        this.minute = minute;
        this.enabled = enabled;
    }

    public static ReminderSettings fromCursor(Cursor c) {
        ReminderSettings s = new ReminderSettings();
        s.id = c.getInt(c.getColumnIndexOrThrow("id"));
        s.userId = c.getInt(c.getColumnIndexOrThrow("user_id"));
        s.hour = c.getInt(c.getColumnIndexOrThrow("hour"));
        s.minute = c.getInt(c.getColumnIndexOrThrow("minute"));
        s.enabled = c.getInt(c.getColumnIndexOrThrow("is_enabled")) == 1;
        int di = c.getColumnIndex("days_of_week");
        if (di >= 0) s.daysOfWeek = c.getString(di);
        if (s.daysOfWeek == null) s.daysOfWeek = "1,2,3,4,5,6,7";
        return s;
    }

    public ContentValues toContentValues() {
        ContentValues cv = new ContentValues();
        cv.put("user_id", userId);
        cv.put("hour", hour);
        cv.put("minute", minute);
        cv.put("is_enabled", enabled ? 1 : 0);
        cv.put("days_of_week", daysOfWeek);
        return cv;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public int getHour() { return hour; }
    public void setHour(int hour) { this.hour = hour; }
    public int getMinute() { return minute; }
    public void setMinute(int minute) { this.minute = minute; }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public String getDaysOfWeek() { return daysOfWeek; }
    public void setDaysOfWeek(String daysOfWeek) { this.daysOfWeek = daysOfWeek; }
}
