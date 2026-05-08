package com.example.fitcore.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fitcore.R;
import com.example.fitcore.activity.WorkoutDetailActivity;
import com.example.fitcore.adapter.PlanAdapter;
import com.example.fitcore.database.DatabaseHelper;
import com.example.fitcore.model.FitnessPlan;

import java.util.List;

public class ExploreFragment extends Fragment {

    private DatabaseHelper db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_explore, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = DatabaseHelper.getInstance(requireContext());
        RecyclerView rv = view.findViewById(R.id.rv_explore);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));

        List<FitnessPlan> plans = db.getAllPlans();
        PlanAdapter adapter = new PlanAdapter(plans, plan -> {
            Intent i = new Intent(getActivity(), WorkoutDetailActivity.class);
            i.putExtra("plan_id", plan.getId());
            startActivity(i);
        });
        rv.setAdapter(adapter);
    }
}
