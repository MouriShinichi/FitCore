package com.example.fitcore.utils;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import java.util.Calendar;

public class NotificationHelper {

    private static final String CHANNEL_ID = "fitcore_reminder";
    private static final String CHANNEL_NAME = "运动提醒";
    private static final int NOTIFY_ID = 1001;
    private static final int ALARM_REQUEST_CODE = 2001;

    /* 保存提醒的时间，供 Receiver 重新调度用 */
    private static final String PREFS_ALARM = "fitcore_alarm";

    public static void createChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("提醒您按时锻炼");
            channel.setLockscreenVisibility(NotificationCompat.VISIBILITY_PUBLIC);
            channel.enableVibration(true);
            NotificationManager nm = context.getSystemService(NotificationManager.class);
            if (nm != null) nm.createNotificationChannel(channel);
        }
    }

    // 向后兼容
    public static void scheduleReminder(Context context, int hour, int minute, boolean enabled) {
        scheduleReminder(context, hour, minute, enabled, "");
    }

    public static void scheduleReminder(Context context, int hour, int minute, boolean enabled, String daysMode) {
        context.getSharedPreferences(PREFS_ALARM, Context.MODE_PRIVATE).edit()
                .putInt("hour", hour)
                .putInt("minute", minute)
                .putBoolean("enabled", enabled)
                .putString("days_mode", daysMode)
                .apply();

        AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, ReminderReceiver.class);
        PendingIntent pending = getPendingIntent(context);

        alarm.cancel(pending);
        if (!enabled) return;

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, hour);
        cal.set(Calendar.MINUTE, minute);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        if (cal.before(Calendar.getInstance())) {
            cal.add(Calendar.DAY_OF_MONTH, 1);
        }

        if (alarm != null) {
            alarm.setExact(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pending);
        }
    }

    public static void cancelReminder(Context context) {
        AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarm != null) alarm.cancel(getPendingIntent(context));
    }

    private static PendingIntent getPendingIntent(Context context) {
        int flags = PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT;
        return PendingIntent.getBroadcast(context, ALARM_REQUEST_CODE,
                new Intent(context, ReminderReceiver.class), flags);
    }

    public static void showInstantNotification(Context context) {
        Intent clickIntent = context.getPackageManager()
                .getLaunchIntentForPackage(context.getPackageName());
        clickIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent clickPending = PendingIntent.getActivity(context, 0, clickIntent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("🏋 运动时间到！")
                .setContentText("该锻炼了，坚持就是胜利！")
                .setContentIntent(clickPending)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setAutoCancel(true)
                .setVibrate(new long[]{0, 300, 200, 300});

        NotificationManager nm = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm != null) nm.notify(NOTIFY_ID, builder.build());
    }

    public static class ReminderReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            showInstantNotification(context);

            var prefs = context.getSharedPreferences(PREFS_ALARM, Context.MODE_PRIVATE);
            String mode = prefs.getString("days_mode", "");

            if ("once".equals(mode)) {
                // 响一次：关闭提醒，更新数据库
                prefs.edit().putBoolean("enabled", false).apply();
                com.example.fitcore.database.DatabaseHelper db =
                        com.example.fitcore.database.DatabaseHelper.getInstance(context);
                int uid = new com.example.fitcore.utils.SessionManager(context).getUserId();
                if (uid > 0) {
                    var s = db.getReminderSettings(uid);
                    if (s != null) { s.setEnabled(false); db.updateReminderSettings(s); }
                }
            } else {
                // 自定义/每周：重新调度
                if (prefs.getBoolean("enabled", false)) {
                    scheduleReminder(context,
                            prefs.getInt("hour", 7),
                            prefs.getInt("minute", 0),
                            true, mode);
                }
            }
        }
    }
}
