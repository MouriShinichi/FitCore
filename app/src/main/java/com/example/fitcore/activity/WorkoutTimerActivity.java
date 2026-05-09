package com.example.fitcore.activity;

import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.fitcore.R;
import com.example.fitcore.database.DatabaseHelper;
import com.example.fitcore.model.WorkoutRecord;
import com.example.fitcore.utils.SessionManager;

public class WorkoutTimerActivity extends AppCompatActivity {

    private TextView tvTimer, tvTimerStatus, ivPlayIcon;
    private View fabDone, fabCancel;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private long elapsedMillis, baseMillis;
    private boolean isRunning, isPaused;
    private Runnable ticker;

    private DatabaseHelper db;
    private SessionManager session;
    private int planId = -1;

    private final String[][] FEELS = {
        {"feel_bad","1"},{"feel_poor","2"},{"feel_ok","3"},{"feel_good","4"},{"feel_great","5"},
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workout_timer);

        db = DatabaseHelper.getInstance(this);
        session = new SessionManager(this);
        planId = getIntent().getIntExtra("plan_id", -1);
        String prefilled = getIntent().getStringExtra("plan_name");
        EditText etType = findViewById(R.id.et_workout_type);
        if (prefilled != null) etType.setText(prefilled);

        tvTimer = findViewById(R.id.tv_timer);
        tvTimerStatus = findViewById(R.id.tv_timer_status);
        ivPlayIcon = findViewById(R.id.iv_play_icon);
        fabDone = findViewById(R.id.fab_done);
        fabCancel = findViewById(R.id.fab_cancel);

        ticker = new Runnable() {
            @Override public void run() {
                if (isPaused) return;
                elapsedMillis = SystemClock.elapsedRealtime() - baseMillis;
                updateTimerDisplay();
                handler.postDelayed(this, 200);
            }
        };

        View innerCircle = findViewById(R.id.view_inner_circle);
        innerCircle.setOnClickListener(v -> {
            if (!isRunning) startTimer();
            else if (isPaused) resumeTimer();
            else pauseTimer();
        });

        fabDone.setOnClickListener(v -> {
            if (isRunning) {
                pauseTimer();
                showConfirm("确认完成", "确定完成本次运动吗？",
                        "确定", () -> showSaveDialog(),
                        "继续运动", null, true);
            }
        });

        fabCancel.setOnClickListener(v -> {
            if (isRunning) pauseTimer();
            showConfirm("取消记录", "确定要取消本次记录吗？",
                    "取消记录", () -> finish(),
                    "继续运动", null, false);
        });
    }

    private void startTimer() {
        baseMillis = SystemClock.elapsedRealtime();
        elapsedMillis = 0;
        handler.removeCallbacks(ticker);
        handler.post(ticker);
        isRunning = true; isPaused = false;
        tvTimerStatus.setText("运动中");
        ivPlayIcon.setText("⏸");
        fabDone.setVisibility(View.VISIBLE);
    }

    private void resumeTimer() {
        baseMillis = SystemClock.elapsedRealtime() - elapsedMillis;
        handler.removeCallbacks(ticker);
        handler.post(ticker);
        isPaused = false;
        tvTimerStatus.setText("运动中");
        ivPlayIcon.setText("⏸");
    }

    private void pauseTimer() {
        elapsedMillis = SystemClock.elapsedRealtime() - baseMillis;
        handler.removeCallbacks(ticker);
        isPaused = true;
        tvTimerStatus.setText("已暂停");
        ivPlayIcon.setText("▶");
    }

    private void updateTimerDisplay() {
        int secs = (int) (elapsedMillis / 1000);
        tvTimer.setText(String.format("%02d:%02d:%02d", secs/3600, (secs%3600)/60, secs%60));
    }

    private void showSaveDialog() {
        View dlg = LayoutInflater.from(this).inflate(R.layout.dialog_save_workout, null);
        final int[] feelingResult = {3};
        for (String[] f : FEELS) {
            int rid = getResources().getIdentifier(f[0], "id", getPackageName());
            View v = dlg.findViewById(rid);
            final int val = Integer.parseInt(f[1]);
            v.setOnClickListener(fv -> {
                for (String[] ff : FEELS) {
                    int rj = getResources().getIdentifier(ff[0], "id", getPackageName());
                    View vj = dlg.findViewById(rj);
                    vj.setBackgroundResource(R.drawable.bg_card);
                    ((TextView) vj).setTextColor(0xFF888888);
                }
                fv.setBackgroundResource(R.drawable.bg_card_accent);
                ((TextView) fv).setTextColor(0xFFCCFF00);
                feelingResult[0] = val;
            });
        }
        dlg.findViewById(R.id.feel_ok).performClick();
        EditText etNotes = dlg.findViewById(R.id.et_notes);

        // 卡路里计算
        String calType = ((EditText) findViewById(R.id.et_workout_type))
                .getText().toString().trim();
        if (calType.isEmpty()) calType = "运动";
        com.example.fitcore.model.User user = db.getUserById(session.getUserId());
        double w = (user != null && user.getWeight() > 0) ? user.getWeight() : 70;
        int min = (int) (elapsedMillis / 60000);
        if (min < 1) min = 1;
        double met = estimateMET(calType);
        int cal = (int) (met * w * (min / 60.0));
        ((TextView) dlg.findViewById(R.id.tv_cal_info))
                .setText("预计消耗 " + cal + " 千卡");

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dlg).setCancelable(false).create();
        if (dialog.getWindow() != null)
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        dlg.findViewById(R.id.btn_save).setOnClickListener(v -> {
            int minutes = (int) (elapsedMillis / 60000);
            if (minutes < 1) minutes = 1;
            String type = ((EditText) findViewById(R.id.et_workout_type))
                    .getText().toString().trim();
            if (type.isEmpty()) type = "运动";
            Integer pid = planId > 0 ? planId : null;
            db.insertWorkoutRecord(new WorkoutRecord(session.getUserId(), pid,
                    type, minutes, feelingResult[0], etNotes.getText().toString().trim()));
            Toast.makeText(this, "保存成功！", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
            finish();
        });
        dlg.findViewById(R.id.btn_discard).setOnClickListener(v -> {
            dialog.dismiss();
            showConfirm("丢弃记录", "确定要丢弃本次运动记录吗？",
                    "丢弃", () -> finish(),
                    "取消", () -> {}, false);
        });
        dialog.show();
    }

    private void showConfirm(String title, String msg, String pos, Runnable posAct,
                               String neg, Runnable negAct, boolean posHighlighted) {
        View dlg = LayoutInflater.from(this).inflate(R.layout.dialog_confirm, null);
        ((TextView) dlg.findViewById(R.id.dialog_title)).setText(title);
        ((TextView) dlg.findViewById(R.id.dialog_message)).setText(msg);
        TextView btnPos = dlg.findViewById(R.id.dialog_positive);
        TextView btnNeg = dlg.findViewById(R.id.dialog_negative);
        if (posHighlighted) { btnPos.setText(pos); btnNeg.setText(neg); }
        else { btnPos.setText(neg); btnNeg.setText(pos); }
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dlg).setCancelable(false).create();
        if (dialog.getWindow() != null)
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.show();
        btnPos.setOnClickListener(v -> {
            dialog.dismiss();
            Runnable a = posHighlighted ? posAct : negAct;
            if (a != null) a.run();
        });
        btnNeg.setOnClickListener(v -> {
            dialog.dismiss();
            Runnable a = posHighlighted ? negAct : posAct;
            if (a != null) a.run();
        });
    }

    private double estimateMET(String type) {
        String t = type.toLowerCase();
        if (t.contains("跑") || t.contains("马拉松")) return 10.0;
        if (t.contains("跳绳")) return 11.0;
        if (t.contains("游泳") || t.contains("自由泳")) return 8.0;
        if (t.contains("拳击") || t.contains("跆拳道") || t.contains("摔跤")) return 10.0;
        if (t.contains("篮球") || t.contains("足球")) return 8.0;
        if (t.contains("骑") || t.contains("单车")) return 7.0;
        if (t.contains("网球") || t.contains("羽毛球")) return 7.0;
        if (t.contains("舞")) return 6.0;
        if (t.contains("深蹲") || t.contains("硬拉") || t.contains("卧推")) return 6.0;
        if (t.contains("瑜伽") || t.contains("普拉提")) return 3.5;
        if (t.contains("太极") || t.contains("冥想")) return 3.0;
        if (t.contains("走")) return 4.0;
        return 5.0;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(ticker);
    }
}
