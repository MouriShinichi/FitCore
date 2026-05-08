package com.example.fitcore.model;

import android.content.ContentValues;
import android.database.Cursor;

public class User {
    private int id;
    private String name;
    private String username;
    private String passwordHash;
    private String gender;
    private int age;
    private double height;
    private double weight;
    private String createdAt;

    public User() {}

    public User(String name, String username, String passwordHash) {
        this.name = name;
        this.username = username;
        this.passwordHash = passwordHash;
        this.gender = "";
        this.age = 0;
        this.height = 0;
        this.weight = 0;
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.getDefault());
        this.createdAt = sdf.format(new java.util.Date());
    }

    public static User fromCursor(Cursor c) {
        User u = new User();
        u.id = c.getInt(c.getColumnIndexOrThrow("id"));
        u.name = c.getString(c.getColumnIndexOrThrow("name"));
        u.username = c.getString(c.getColumnIndexOrThrow("username"));
        u.passwordHash = c.getString(c.getColumnIndexOrThrow("password_hash"));
        u.gender = c.getString(c.getColumnIndexOrThrow("gender"));
        u.age = c.getInt(c.getColumnIndexOrThrow("age"));
        u.height = c.getDouble(c.getColumnIndexOrThrow("height"));
        u.weight = c.getDouble(c.getColumnIndexOrThrow("weight"));
        u.createdAt = c.getString(c.getColumnIndexOrThrow("created_at"));
        return u;
    }

    public ContentValues toContentValues() {
        ContentValues cv = new ContentValues();
        cv.put("name", name);
        cv.put("username", username);
        cv.put("password_hash", passwordHash);
        cv.put("gender", gender);
        cv.put("age", age);
        cv.put("height", height);
        cv.put("weight", weight);
        cv.put("created_at", createdAt);
        return cv;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }
    public double getHeight() { return height; }
    public void setHeight(double height) { this.height = height; }
    public double getWeight() { return weight; }
    public void setWeight(double weight) { this.weight = weight; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
