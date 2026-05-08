package com.example.fitcore.activity;

import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.fitcore.R;

public class CalorieGuideActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calorie_guide);

        FrameLayout fl = findViewById(R.id.calorie_root);

        ScrollView sv = new ScrollView(this);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        int pad = dp(16);
        root.setPadding(pad, pad, pad, pad);

        addTitle(root, "MET 消耗计算", 22);
        addFormulaCard(root);
        addSection(root, "有氧运动", new String[][]{
            {"跑步 / 马拉松", "10.0"}, {"跳绳", "11.0"}, {"游泳 / 自由泳", "8.0"},
            {"登山 / 攀岩 / 越野跑", "8.0"}, {"骑行 / 动感单车 / 划船", "7.0"},
            {"跳舞 / 街舞", "6.0"}, {"椭圆机 / 有氧操", "6.0"}, {"快走 / 徒步", "4.0"},
        });
        addSection(root, "力量训练", new String[][]{
            {"深蹲 / 硬拉 / 卧推 / 腿举", "6.0"}, {"引体向上 / 哑铃 / 弯举", "5.5"},
            {"俯卧撑 / 仰卧起坐 / 卷腹", "5.0"}, {"平板支撑 / 弓步蹲", "5.0"},
        });
        addSection(root, "球类运动", new String[][]{
            {"篮球 / 足球 / 橄榄球", "8.0"}, {"网球 / 羽毛球", "7.0"},
            {"排球 / 手球", "5.0"}, {"乒乓球 / 台球", "4.0"}, {"高尔夫 / 保龄球", "4.5"},
        });
        addSection(root, "其他运动", new String[][]{
            {"拳击 / 跆拳道 / 摔跤", "10.0"}, {"击剑 / 壁球", "8.0"},
            {"滑雪 / 滑板 / 冲浪", "6.0"}, {"骑马 / 轮滑 / 溜冰", "5.5"},
            {"瑜伽 / 普拉提", "3.5"}, {"太极 / 八段锦 / 冥想", "3.0"}, {"钓鱼", "2.5"},
        });

        View spacer = new View(this);
        spacer.setLayoutParams(new LinearLayout.LayoutParams(dp(24), dp(24)));
        root.addView(spacer);

        sv.addView(root);
        fl.addView(sv, 0);
    }

    private void addTitle(LinearLayout root, String text, int sizeSp) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextColor(0xFFFFFFFF);
        tv.setTextSize(sizeSp);
        tv.setTypeface(null, android.graphics.Typeface.BOLD);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.topMargin = dp(12);
        tv.setLayoutParams(lp);
        root.addView(tv);
    }

    private void addFormulaCard(LinearLayout root) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        int p = dp(16);
        card.setPadding(p, p, p, p);
        card.setBackgroundResource(R.drawable.bg_card_accent);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.topMargin = dp(16);
        card.setLayoutParams(lp);

        TextView t1 = new TextView(this);
        t1.setText("计算公式");
        t1.setTextColor(0xFFCCFF00);
        t1.setTextSize(14);
        t1.setTypeface(null, android.graphics.Typeface.BOLD);
        card.addView(t1);

        TextView t2 = new TextView(this);
        t2.setText("千卡 = MET值 × 体重(kg) × 时长(小时)");
        t2.setTextColor(0xFFFFFFFF);
        t2.setTextSize(16);
        t2.setTypeface(null, android.graphics.Typeface.BOLD);
        LinearLayout.LayoutParams lp2 = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp2.topMargin = dp(8);
        t2.setLayoutParams(lp2);
        card.addView(t2);

        root.addView(card);
    }

    private void addSection(LinearLayout root, String title, String[][] data) {
        TextView tv = new TextView(this);
        tv.setText(title);
        tv.setTextColor(0xFFCCFF00);
        tv.setTextSize(16);
        tv.setTypeface(null, android.graphics.Typeface.BOLD);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.topMargin = dp(20);
        tv.setLayoutParams(lp);
        root.addView(tv);

        LinearLayout table = new LinearLayout(this);
        table.setOrientation(LinearLayout.VERTICAL);
        table.setBackgroundResource(R.drawable.bg_card);
        int p = dp(4);
        table.setPadding(p, p, p, p);
        LinearLayout.LayoutParams tlp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        tlp.topMargin = dp(8);
        table.setLayoutParams(tlp);

        // header
        table.addView(makeRow("运动类型", "MET", true));

        View div = new View(this);
        div.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 1));
        div.setBackgroundColor(0xFF333333);
        table.addView(div);

        for (String[] d : data) {
            table.addView(makeRow(d[0], d[1], false));
        }

        root.addView(table);
    }

    private LinearLayout makeRow(String name, String met, boolean isHeader) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        int h = dp(isHeader ? 36 : 34);
        row.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, h));
        row.setPadding(dp(12), 0, dp(12), 0);
        if (isHeader) row.setBackgroundColor(0xFF1A1A1A);

        TextView tvName = new TextView(this);
        tvName.setText(name);
        tvName.setTextColor(isHeader ? 0xFFCCFF00 : 0xFFFFFFFF);
        tvName.setTextSize(isHeader ? 12 : 13);
        if (isHeader) tvName.setTypeface(null, android.graphics.Typeface.BOLD);
        LinearLayout.LayoutParams nlp = new LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.WRAP_CONTENT, 1);
        tvName.setLayoutParams(nlp);
        row.addView(tvName);

        TextView tvMet = new TextView(this);
        tvMet.setText(met);
        tvMet.setTextColor(isHeader ? 0xFFCCFF00 : 0xFFCCFF00);
        tvMet.setTextSize(isHeader ? 12 : 13);
        tvMet.setGravity(Gravity.END);
        if (isHeader) tvMet.setTypeface(null, android.graphics.Typeface.BOLD);
        tvMet.setLayoutParams(new LinearLayout.LayoutParams(dp(60),
                ViewGroup.LayoutParams.WRAP_CONTENT));
        row.addView(tvMet);

        return row;
    }

    private int dp(int d) { return (int) (d * getResources().getDisplayMetrics().density); }
}
