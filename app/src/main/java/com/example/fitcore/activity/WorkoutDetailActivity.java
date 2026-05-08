package com.example.fitcore.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.fitcore.R;
import com.example.fitcore.database.DatabaseHelper;
import com.example.fitcore.model.FitnessPlan;
import com.example.fitcore.utils.SessionManager;

public class WorkoutDetailActivity extends AppCompatActivity {

    private DatabaseHelper db;
    private SessionManager session;
    private int planId;
    private FitnessPlan plan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workout_detail);

        db = DatabaseHelper.getInstance(this);
        session = new SessionManager(this);
        planId = getIntent().getIntExtra("plan_id", -1);

        if (planId == -1) {
            finish();
            return;
        }

        plan = db.getPlanById(planId);
        if (plan == null) {
            finish();
            return;
        }

        ((TextView) findViewById(R.id.tv_detail_name)).setText(plan.getName());
        ((TextView) findViewById(R.id.tv_detail_desc)).setText(plan.getDescription());
        ((TextView) findViewById(R.id.tv_detail_duration)).setText(plan.getDuration());
        ((TextView) findViewById(R.id.tv_detail_freq)).setText(plan.getFrequency());
        ((TextView) findViewById(R.id.tv_detail_level)).setText(plan.getLevel() + " · " + plan.getCategory());

        findViewById(R.id.btn_select_plan).setOnClickListener(v -> {
            db.selectPlan(session.getUserId(), planId);
            Toast.makeText(this, "已选择：" + plan.getName(), Toast.LENGTH_SHORT).show();
            finish();
        });

        findViewById(R.id.btn_start_workout).setOnClickListener(v -> {
            Intent i = new Intent(this, WorkoutTimerActivity.class);
            i.putExtra("plan_id", planId);
            i.putExtra("plan_name", plan.getName());
            startActivity(i);
        });
    }
}
