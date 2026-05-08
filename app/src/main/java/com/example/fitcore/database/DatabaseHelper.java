package com.example.fitcore.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.fitcore.model.*;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "fitcore.db";
    private static final int DB_VERSION = 2;

    private static volatile DatabaseHelper instance;

    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    private DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE users (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT NOT NULL, " +
                "username TEXT NOT NULL UNIQUE, " +
                "password_hash TEXT NOT NULL, " +
                "gender TEXT NOT NULL DEFAULT '', " +
                "age INTEGER NOT NULL DEFAULT 0, " +
                "height REAL NOT NULL DEFAULT 0, " +
                "weight REAL NOT NULL DEFAULT 0, " +
                "created_at TEXT NOT NULL)");

        db.execSQL("CREATE TABLE fitness_plans (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT NOT NULL, " +
                "description TEXT, " +
                "duration TEXT NOT NULL, " +
                "frequency TEXT NOT NULL, " +
                "level TEXT NOT NULL, " +
                "category TEXT NOT NULL)");

        db.execSQL("CREATE TABLE user_plans (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "user_id INTEGER NOT NULL, " +
                "plan_id INTEGER NOT NULL, " +
                "selected_at TEXT NOT NULL, " +
                "is_current INTEGER NOT NULL DEFAULT 1, " +
                "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE, " +
                "FOREIGN KEY (plan_id) REFERENCES fitness_plans(id) ON DELETE CASCADE)");

        db.execSQL("CREATE TABLE workout_records (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "user_id INTEGER NOT NULL, " +
                "plan_id INTEGER, " +
                "type TEXT NOT NULL, " +
                "duration_minutes INTEGER NOT NULL, " +
                "feeling INTEGER NOT NULL CHECK(feeling >= 1 AND feeling <= 5), " +
                "notes TEXT, " +
                "recorded_at TEXT NOT NULL, " +
                "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE)");

        db.execSQL("CREATE TABLE reminder_settings (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "user_id INTEGER NOT NULL UNIQUE, " +
                "hour INTEGER NOT NULL DEFAULT 7, " +
                "minute INTEGER NOT NULL DEFAULT 0, " +
                "is_enabled INTEGER NOT NULL DEFAULT 0, " +
                "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE)");

        // v2: 添加 days_of_week 列
        db.execSQL("ALTER TABLE reminder_settings ADD COLUMN days_of_week TEXT NOT NULL DEFAULT '1,2,3,4,5,6,7'");

        // 种子数据：4 个健身计划
        seedPlan(db, "推拉腿分化训练", "经典分部训练，均衡增肌",
                "6周", "5次/周", "中级", "增肌");
        seedPlan(db, "HIIT燃脂训练", "高强度间歇燃脂计划",
                "4周", "4次/周", "高级", "减脂");
        seedPlan(db, "柔韧流动训练", "提升灵活性与关节健康",
                "8周", "3次/周", "初级", "柔韧");
        seedPlan(db, "耐力跑步训练", "逐步提升耐力与心肺功能",
                "6周", "4次/周", "中级", "有氧");
    }

    private void seedPlan(SQLiteDatabase db, String name, String desc,
                          String duration, String freq, String level, String cat) {
        ContentValues cv = new ContentValues();
        cv.put("name", name);
        cv.put("description", desc);
        cv.put("duration", duration);
        cv.put("frequency", freq);
        cv.put("level", level);
        cv.put("category", cat);
        db.insert("fitness_plans", null, cv);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE reminder_settings ADD COLUMN days_of_week TEXT NOT NULL DEFAULT '1,2,3,4,5,6,7'");
        }
    }

    // ========== User CRUD ==========

    public long insertUser(User user) {
        return getWritableDatabase().insert("users", null, user.toContentValues());
    }

    public User getUserByUsername(String username) {
        Cursor c = getReadableDatabase().query("users", null,
                "username=?", new String[]{username}, null, null, null);
        if (c.moveToFirst()) {
            User u = User.fromCursor(c);
            c.close();
            return u;
        }
        c.close();
        return null;
    }

    public User getUserById(int userId) {
        Cursor c = getReadableDatabase().query("users", null,
                "id=?", new String[]{String.valueOf(userId)}, null, null, null);
        if (c.moveToFirst()) {
            User u = User.fromCursor(c);
            c.close();
            return u;
        }
        c.close();
        return null;
    }

    public int updateUser(User user) {
        return getWritableDatabase().update("users", user.toContentValues(),
                "id=?", new String[]{String.valueOf(user.getId())});
    }

    // ========== FitnessPlan CRUD ==========

    public List<FitnessPlan> getAllPlans() {
        List<FitnessPlan> list = new ArrayList<>();
        Cursor c = getReadableDatabase().query("fitness_plans",
                null, null, null, null, null, "id ASC");
        while (c.moveToNext()) {
            list.add(FitnessPlan.fromCursor(c));
        }
        c.close();
        return list;
    }

    public FitnessPlan getPlanById(int planId) {
        Cursor c = getReadableDatabase().query("fitness_plans", null,
                "id=?", new String[]{String.valueOf(planId)}, null, null, null);
        if (c.moveToFirst()) {
            FitnessPlan p = FitnessPlan.fromCursor(c);
            c.close();
            return p;
        }
        c.close();
        return null;
    }

    // ========== UserPlan CRUD ==========

    public void selectPlan(int userId, int planId) {
        SQLiteDatabase db = getWritableDatabase();
        // 先清空当前计划
        ContentValues cv1 = new ContentValues();
        cv1.put("is_current", 0);
        db.update("user_plans", cv1, "user_id=?", new String[]{String.valueOf(userId)});
        // 插入新计划
        ContentValues cv2 = new ContentValues();
        cv2.put("user_id", userId);
        cv2.put("plan_id", planId);
        java.text.SimpleDateFormat sdfu = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.getDefault());
        cv2.put("selected_at", sdfu.format(new java.util.Date()));
        cv2.put("is_current", 1);
        db.insert("user_plans", null, cv2);
    }

    public FitnessPlan getCurrentPlan(int userId) {
        Cursor c = getReadableDatabase().rawQuery(
                "SELECT fp.* FROM fitness_plans fp " +
                "INNER JOIN user_plans up ON fp.id = up.plan_id " +
                "WHERE up.user_id=? AND up.is_current=1 " +
                "LIMIT 1",
                new String[]{String.valueOf(userId)});
        if (c.moveToFirst()) {
            FitnessPlan p = FitnessPlan.fromCursor(c);
            c.close();
            return p;
        }
        c.close();
        return null;
    }

    // ========== WorkoutRecord CRUD ==========

    public long insertWorkoutRecord(WorkoutRecord record) {
        return getWritableDatabase().insert("workout_records", null, record.toContentValues());
    }

    public List<WorkoutRecord> getRecordsByUser(int userId) {
        List<WorkoutRecord> list = new ArrayList<>();
        Cursor c = getReadableDatabase().query("workout_records", null,
                "user_id=?", new String[]{String.valueOf(userId)},
                null, null, "recorded_at DESC", "50");
        while (c.moveToNext()) {
            list.add(WorkoutRecord.fromCursor(c));
        }
        c.close();
        return list;
    }

    public int getTotalWorkouts(int userId) {
        Cursor c = getReadableDatabase().rawQuery(
                "SELECT COUNT(*) FROM workout_records WHERE user_id=?",
                new String[]{String.valueOf(userId)});
        int count = 0;
        if (c.moveToFirst()) count = c.getInt(0);
        c.close();
        return count;
    }

    public int getTotalMinutes(int userId) {
        Cursor c = getReadableDatabase().rawQuery(
                "SELECT SUM(duration_minutes) FROM workout_records WHERE user_id=?",
                new String[]{String.valueOf(userId)});
        int total = 0;
        if (c.moveToFirst()) total = c.getInt(0);
        c.close();
        return total;
    }

    /**
     * 返回最近 7 天的运动分钟数，用于柱状图
     */
    public List<int[]> getWeeklyMinutes(int userId) {
        List<int[]> result = new ArrayList<>();
        Cursor c = getReadableDatabase().rawQuery(
                "SELECT date(recorded_at) as day, SUM(duration_minutes) as total " +
                "FROM workout_records " +
                "WHERE user_id=? AND recorded_at >= date('now', '-6 days') " +
                "GROUP BY date(recorded_at) ORDER BY day ASC",
                new String[]{String.valueOf(userId)});
        // dayIndex: 1=Mon..7=Sun
        java.util.Map<String, Integer> map = new java.util.LinkedHashMap<>();
        java.util.Calendar cal = java.util.Calendar.getInstance();
        java.text.SimpleDateFormat sdfd = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
        for (int i = 6; i >= 0; i--) {
            cal.add(java.util.Calendar.DAY_OF_MONTH, - (i == 6 ? 6 : 1));
            String dateStr = sdfd.format(cal.getTime());
            map.put(dateStr, 0);
            cal = java.util.Calendar.getInstance();
        }
        while (c.moveToNext()) {
            String day = c.getString(0);
            int minutes = c.getInt(1);
            map.put(day, minutes);
        }
        c.close();
        int idx = 0;
        for (java.util.Map.Entry<String, Integer> e : map.entrySet()) {
            result.add(new int[]{idx++, e.getValue()});
        }
        return result;
    }

    public List<int[]> getWeeklyMinutesForDates(int userId, String[] dates) {
        List<int[]> result = new ArrayList<>();
        for (int i = 0; i < dates.length; i++) result.add(new int[]{i, 0});
        Cursor c = getReadableDatabase().rawQuery(
                "SELECT date(recorded_at) as day, SUM(duration_minutes) as total " +
                "FROM workout_records WHERE user_id=? " +
                "GROUP BY date(recorded_at) ORDER BY day ASC",
                new String[]{String.valueOf(userId)});
        java.util.Map<String, Integer> map = new java.util.LinkedHashMap<>();
        while (c.moveToNext()) map.put(c.getString(0), c.getInt(1));
        c.close();
        for (int i = 0; i < dates.length; i++) {
            Integer mins = map.get(dates[i]);
            if (mins != null) result.set(i, new int[]{i, mins});
        }
        return result;
    }

    public void deleteRecord(int id) {
        getWritableDatabase().delete("workout_records", "id=?", new String[]{String.valueOf(id)});
    }

    // ========== ReminderSettings CRUD ==========

    public ReminderSettings getReminderSettings(int userId) {
        Cursor c = getReadableDatabase().query("reminder_settings", null,
                "user_id=?", new String[]{String.valueOf(userId)}, null, null, null);
        if (c.moveToFirst()) {
            ReminderSettings s = ReminderSettings.fromCursor(c);
            c.close();
            return s;
        }
        c.close();
        return null;
    }

    public long insertReminderSettings(ReminderSettings settings) {
        return getWritableDatabase().insert("reminder_settings", null, settings.toContentValues());
    }

    public int updateReminderSettings(ReminderSettings settings) {
        return getWritableDatabase().update("reminder_settings", settings.toContentValues(),
                "user_id=?", new String[]{String.valueOf(settings.getUserId())});
    }
}
