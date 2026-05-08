package com.example.fitcore.model;

import android.content.ContentValues;
import android.database.Cursor;

public class WorkoutRecord {
    private int id;
    private int userId;
    private Integer planId;
    private String type;
    private int durationMinutes;
    private int feeling;
    private String notes;
    private String recordedAt;

    public WorkoutRecord() {}

    public WorkoutRecord(int userId, Integer planId, String type,
                         int durationMinutes, int feeling, String notes) {
        this.userId = userId;
        this.planId = planId;
        this.type = type;
        this.durationMinutes = durationMinutes;
        this.feeling = feeling;
        this.notes = notes;
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.getDefault());
        this.recordedAt = sdf.format(new java.util.Date());
    }

    public static WorkoutRecord fromCursor(Cursor c) {
        WorkoutRecord r = new WorkoutRecord();
        r.id = c.getInt(c.getColumnIndexOrThrow("id"));
        r.userId = c.getInt(c.getColumnIndexOrThrow("user_id"));
        int pi = c.getColumnIndexOrThrow("plan_id");
        r.planId = c.isNull(pi) ? null : c.getInt(pi);
        r.type = c.getString(c.getColumnIndexOrThrow("type"));
        r.durationMinutes = c.getInt(c.getColumnIndexOrThrow("duration_minutes"));
        r.feeling = c.getInt(c.getColumnIndexOrThrow("feeling"));
        r.notes = c.getString(c.getColumnIndexOrThrow("notes"));
        r.recordedAt = c.getString(c.getColumnIndexOrThrow("recorded_at"));
        return r;
    }

    public ContentValues toContentValues() {
        ContentValues cv = new ContentValues();
        cv.put("user_id", userId);
        if (planId != null) cv.put("plan_id", planId);
        cv.put("type", type);
        cv.put("duration_minutes", durationMinutes);
        cv.put("feeling", feeling);
        cv.put("notes", notes);
        cv.put("recorded_at", recordedAt);
        return cv;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public Integer getPlanId() { return planId; }
    public void setPlanId(Integer planId) { this.planId = planId; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public int getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(int durationMinutes) { this.durationMinutes = durationMinutes; }
    public int getFeeling() { return feeling; }
    public void setFeeling(int feeling) { this.feeling = feeling; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public String getRecordedAt() { return recordedAt; }
    public void setRecordedAt(String recordedAt) { this.recordedAt = recordedAt; }
}
