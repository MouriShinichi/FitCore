package com.example.fitcore.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fitcore.R;
import com.example.fitcore.adapter.PlanAdapter;
import com.example.fitcore.database.DatabaseHelper;
import com.example.fitcore.model.FitnessPlan;
import com.example.fitcore.utils.SessionManager;

import java.util.List;

public class PlanListActivity extends AppCompatActivity {

    private DatabaseHelper db;
    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plan_list);

        db = DatabaseHelper.getInstance(this);
        session = new SessionManager(this);

        RecyclerView rv = findViewById(R.id.rv_plans);
        rv.setLayoutManager(new LinearLayoutManager(this));

        List<FitnessPlan> plans = db.getAllPlans();
        FitnessPlan currentPlan = db.getCurrentPlan(session.getUserId());
        int currentPlanId = currentPlan != null ? currentPlan.getId() : -1;

        PlanAdapter adapter = new PlanAdapter(plans, plan -> {
            Intent i = new Intent(this, WorkoutDetailActivity.class);
            i.putExtra("plan_id", plan.getId());
            startActivity(i);
        });
        adapter.setSelectedPlanId(currentPlanId);
        rv.setAdapter(adapter);
    }
}
