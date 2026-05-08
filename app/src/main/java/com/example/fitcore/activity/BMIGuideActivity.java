package com.example.fitcore.activity;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.fitcore.R;
import com.example.fitcore.database.DatabaseHelper;
import com.example.fitcore.model.User;
import com.example.fitcore.utils.SessionManager;

public class BMIGuideActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bmi_guide);

        DatabaseHelper db = DatabaseHelper.getInstance(this);
        SessionManager session = new SessionManager(this);
        User user = db.getUserById(session.getUserId());

        if (user == null) return;

        double h = user.getHeight();
        double w = user.getWeight();
        TextView tvBmi = findViewById(R.id.tv_user_bmi);
        TextView tvAdvice = findViewById(R.id.tv_user_advice);

        if (h > 0 && w > 0) {
            double bmi = w / ((h / 100) * (h / 100));
            tvBmi.setText(String.format("%.1f", bmi));

            String advice;
            if (bmi < 18.5) {
                advice = "你当前体重偏瘦，BMI=" + String.format("%.1f", bmi)
                        + "。\n\n建议：增加蛋白质和碳水化合物摄入，适当进行力量训练增肌。建议目标体重："
                        + String.format("%.0f", 18.5 * (h/100) * (h/100)) + " kg ~ "
                        + String.format("%.0f", 22.9 * (h/100) * (h/100)) + " kg";
            } else if (bmi < 23) {
                advice = "你的体重处于正常健康范围，BMI=" + String.format("%.1f", bmi)
                        + "。\n\n建议：保持当前体重和运动习惯，均衡饮食，继续保持！";
            } else if (bmi < 27.5) {
                advice = "你当前体重偏重，BMI=" + String.format("%.1f", bmi)
                        + "。\n\n建议：增加有氧运动（跑步、游泳等），控制热量摄入，减少高糖高脂食物。建议减重至："
                        + String.format("%.0f", 22.9 * (h/100) * (h/100)) + " kg 左右";
            } else {
                advice = "你当前属于肥胖范围，BMI=" + String.format("%.1f", bmi)
                        + "。\n\n建议：咨询医生或营养师制定专业减重计划，从低强度运动开始，逐步增加运动量。";
            }
            tvAdvice.setText(advice);
        }
    }
}
