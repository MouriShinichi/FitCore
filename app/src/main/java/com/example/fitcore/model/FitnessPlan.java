package com.example.fitcore.model;

import android.content.ContentValues;
import android.database.Cursor;

public class FitnessPlan {
    private int id;
    private String name;
    private String description;
    private String duration;
    private String frequency;
    private String level;
    private String category;

    public FitnessPlan() {}

    public FitnessPlan(String name, String description, String duration,
                       String frequency, String level, String category) {
        this.name = name;
        this.description = description;
        this.duration = duration;
        this.frequency = frequency;
        this.level = level;
        this.category = category;
    }

    public static FitnessPlan fromCursor(Cursor c) {
        FitnessPlan p = new FitnessPlan();
        p.id = c.getInt(c.getColumnIndexOrThrow("id"));
        p.name = c.getString(c.getColumnIndexOrThrow("name"));
        p.description = c.getString(c.getColumnIndexOrThrow("description"));
        p.duration = c.getString(c.getColumnIndexOrThrow("duration"));
        p.frequency = c.getString(c.getColumnIndexOrThrow("frequency"));
        p.level = c.getString(c.getColumnIndexOrThrow("level"));
        p.category = c.getString(c.getColumnIndexOrThrow("category"));
        return p;
    }

    public ContentValues toContentValues() {
        ContentValues cv = new ContentValues();
        cv.put("name", name);
        cv.put("description", description);
        cv.put("duration", duration);
        cv.put("frequency", frequency);
        cv.put("level", level);
        cv.put("category", category);
        return cv;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getDuration() { return duration; }
    public void setDuration(String duration) { this.duration = duration; }
    public String getFrequency() { return frequency; }
    public void setFrequency(String frequency) { this.frequency = frequency; }
    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
}
