package com.example.fitcore.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.fitcore.R;
import com.example.fitcore.database.DatabaseHelper;
import com.example.fitcore.model.FitnessPlan;
import com.example.fitcore.utils.SessionManager;

import java.util.List;

public class PlanListActivity extends AppCompatActivity {

    private DatabaseHelper db;
    private SessionManager session;
    private LinearLayout container;
    private int currentPlanId = -1;

    private static final String[][][] PLAN_PHASES = {
        // 推拉腿分化训练
        {
            {"🔥 热身", "5min", "快走, 动态拉伸, 跳绳"},
            {"🏋 主训", "30min", "卧推, 哑铃, 深蹲, 硬拉, 引体向上, 俯卧撑"},
            {"🏃 有氧", "10min", "跑步, 骑行, 划船, 椭圆机"},
            {"🧘 拉伸", "5min", "拉伸, 泡沫轴, 瑜伽"},
        },
        // HIIT燃脂训练
        {
            {"🔥 热身", "5min", "快走, 动态拉伸, 跳绳"},
            {"⚡ 高强度", "15min", "冲刺跑, 有氧操, 跳绳, 登山, 拳击"},
            {"🔄 间歇恢复", "5min", "慢跑, 快走"},
            {"🧘 冷却", "5min", "拉伸, 泡沫轴, 筋膜放松"},
        },
        // 柔韧流动训练
        {
            {"🧎 呼吸冥想", "5min", "冥想, 太极, 八段锦"},
            {"🧘 主拉伸", "20min", "瑜伽, 普拉提, 开肩, 压腿, 猫式, 婴儿式"},
            {"🦯 筋膜放松", "5min", "泡沫轴, 筋膜放松, 脊柱扭转"},
        },
        // 耐力跑步训练
        {
            {"🔥 热身", "5min", "快走, 动态拉伸"},
            {"🏃 主跑", "30min", "跑步, 慢跑, 越野跑, 马拉松"},
            {"🔄 交叉训练", "15min", "骑行, 游泳, 划船, 登山"},
            {"🧘 拉伸", "5min", "拉伸, 泡沫轴, 压腿"},
        },
        // 篮球特训
        {
            {"🔥 热身", "10min", "慢跑, 动态拉伸, 高抬腿"},
            {"🏀 球技训练", "20min", "运球, 投篮, 传球, 上篮"},
            {"⚡ 弹跳训练", "10min", "深蹲跳, 弓步蹲, 跳绳"},
            {"🧘 冷却", "5min", "拉伸, 泡沫轴"},
        },
        // 足球专项
        {
            {"🔥 热身", "10min", "慢跑, 动态拉伸, 折返跑"},
            {"⚽ 技术训练", "20min", "盘带, 传球, 射门, 头球"},
            {"🏃 体能跑", "15min", "冲刺跑, 耐力跑, 间歇跑"},
            {"🧘 拉伸", "5min", "拉伸, 泡沫轴, 压腿"},
        },
        // 游泳训练
        {
            {"🔥 陆上热身", "5min", "动态拉伸, 开肩, 压腿"},
            {"🏊 水中训练", "25min", "自由泳, 蛙泳, 打腿, 划臂"},
            {"🔄 技术练习", "10min", "转身练习, 呼吸节奏, 浮板"},
            {"🧘 放松", "5min", "拉伸, 泡沫轴"},
        },
        // 综合体能训练
        {
            {"🔥 热身", "5min", "快走, 动态拉伸, 跳绳"},
            {"💪 力量", "15min", "俯卧撑, 深蹲, 哑铃, 引体向上"},
            {"🏃 有氧", "10min", "跑步, 骑行, 划船"},
            {"🧘 柔韧", "10min", "瑜伽, 拉伸, 泡沫轴"},
        },
    };

    private static final String[] PLAN_INTRO = {
        "经典分部训练，推/拉/腿三天循环，中间穿插有氧日，适合增肌塑形人群",
        "高强度间歇，短时高效燃脂，适合时间紧的减脂人群，需一定体能基础",
        "低强度长时间保持，提升柔韧性和关节健康，适合久坐族和运动后恢复",
        "逐步提升跑步距离和心肺耐力，适合马拉松/越野跑爱好者",
        "球场专项体能训练，提升弹跳、爆发力和篮球技巧",
        "足球专项体能和综合技术训练，全面提升球场表现",
        "水中训练计划，提升泳姿技巧和心肺耐力",
        "力量、有氧、柔韧全方位覆盖的综合体能计划",
    };

    private static final String[] PLAN_ICONS = {"🏋", "⚡", "🧘", "🏃", "🏀", "⚽", "🏊", "💪"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plan_list);

        db = DatabaseHelper.getInstance(this);
        session = new SessionManager(this);
        container = findViewById(R.id.plan_container);

        FitnessPlan cp = db.getCurrentPlan(session.getUserId());
        currentPlanId = cp != null ? cp.getId() : -1;

        List<FitnessPlan> plans = db.getAllPlans();
        for (int i = 0; i < plans.size(); i++) {
            FitnessPlan plan = plans.get(i);
            addPlanCard(plan, i);
        }
    }

    private void addPlanCard(FitnessPlan plan, int index) {
        boolean isSelected = plan.getId() == currentPlanId;

        // 外层卡片
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackgroundResource(isSelected ? R.drawable.bg_card_accent : R.drawable.bg_card);
        int pad = dp(16);
        card.setPadding(pad, pad, pad, pad);
        LinearLayout.LayoutParams clp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        clp.topMargin = dp(16);
        clp.bottomMargin = dp(4);
        card.setLayoutParams(clp);

        // 头部
        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.VERTICAL);

        // 图标 + 名称 + 选中标记
        LinearLayout titleRow = new LinearLayout(this);
        titleRow.setGravity(Gravity.CENTER_VERTICAL);

        TextView tvIcon = new TextView(this);
        tvIcon.setText(PLAN_ICONS[index]);
        tvIcon.setTextSize(28);

        LinearLayout nameCol = new LinearLayout(this);
        nameCol.setOrientation(LinearLayout.VERTICAL);
        nameCol.setPadding(dp(12), 0, 0, 0);
        LinearLayout.LayoutParams nclp = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
        nameCol.setLayoutParams(nclp);

        TextView tvName = new TextView(this);
        tvName.setText(plan.getName());
        tvName.setTextColor(0xFFFFFFFF);
        tvName.setTextSize(18);
        tvName.setTypeface(null, android.graphics.Typeface.BOLD);

        TextView tvMeta = new TextView(this);
        tvMeta.setText(plan.getDuration() + " · " + plan.getFrequency() + " · " + plan.getLevel());
        tvMeta.setTextColor(0xFF999999);
        tvMeta.setTextSize(12);
        tvMeta.setPadding(0, dp(4), 0, 0);

        nameCol.addView(tvName);
        nameCol.addView(tvMeta);

        titleRow.addView(tvIcon);
        titleRow.addView(nameCol);

        if (isSelected) {
            TextView tvSel = new TextView(this);
            tvSel.setText("✅");
            tvSel.setTextSize(20);
            titleRow.addView(tvSel);
        }

        header.addView(titleRow);

        // 简介
        TextView tvIntro = new TextView(this);
        tvIntro.setText(PLAN_INTRO[index]);
        tvIntro.setTextColor(0xFFAAAAAA);
        tvIntro.setTextSize(13);
        tvIntro.setPadding(0, dp(10), 0, 0);
        header.addView(tvIntro);

        card.addView(header);

        // 分环节区域
        LinearLayout phaseArea = new LinearLayout(this);
        phaseArea.setOrientation(LinearLayout.VERTICAL);
        phaseArea.setPadding(0, dp(12), 0, 0);

        for (String[] phase : PLAN_PHASES[index]) {
            LinearLayout phaseRow = new LinearLayout(this);
            phaseRow.setOrientation(LinearLayout.HORIZONTAL);
            phaseRow.setGravity(Gravity.CENTER_VERTICAL);
            phaseRow.setPadding(dp(8), dp(8), dp(8), dp(8));
            phaseRow.setBackgroundResource(R.drawable.bg_chip);
            LinearLayout.LayoutParams plp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            plp.topMargin = dp(6);
            phaseRow.setLayoutParams(plp);

            TextView tvPhase = new TextView(this);
            tvPhase.setText(phase[0] + "  " + phase[1]);
            tvPhase.setTextColor(0xFFCCFF00);
            tvPhase.setTextSize(13);
            tvPhase.setTypeface(null, android.graphics.Typeface.BOLD);
            phaseRow.addView(tvPhase);

            LinearLayout funcRow = new LinearLayout(this);
            funcRow.setOrientation(LinearLayout.HORIZONTAL);
            LinearLayout.LayoutParams flp = new LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
            flp.setMargins(dp(10), 0, 0, 0);
            funcRow.setLayoutParams(flp);

            TextView tvEx = new TextView(this);
            tvEx.setText(phase[2]);
            tvEx.setTextColor(0xFF999999);
            tvEx.setTextSize(11);
            tvEx.setMaxLines(2);
            funcRow.addView(tvEx);

            phaseRow.addView(funcRow);
            phaseArea.addView(phaseRow);
        }

        card.addView(phaseArea);

        // 选择按钮
        TextView btnSelect = new TextView(this);
        btnSelect.setText(isSelected ? "● 当前计划" : "选择这个计划");
        btnSelect.setTextColor(isSelected ? 0xFFCCFF00 : 0xFF000000);
        btnSelect.setTextSize(14);
        btnSelect.setTypeface(null, android.graphics.Typeface.BOLD);
        btnSelect.setGravity(Gravity.CENTER);
        btnSelect.setPadding(0, dp(14), 0, dp(14));
        btnSelect.setBackgroundResource(isSelected ? R.drawable.bg_card : R.drawable.bg_accent_btn);
        LinearLayout.LayoutParams blp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        blp.topMargin = dp(14);
        btnSelect.setLayoutParams(blp);

        btnSelect.setOnClickListener(v -> {
            if (isSelected) return;
            db.selectPlan(session.getUserId(), plan.getId());
            Toast.makeText(this, "已选择 " + plan.getName(), Toast.LENGTH_SHORT).show();
            finish();
        });

        card.addView(btnSelect);
        container.addView(card);
    }

    private int dp(int d) { return (int) (d * getResources().getDisplayMetrics().density); }
}
