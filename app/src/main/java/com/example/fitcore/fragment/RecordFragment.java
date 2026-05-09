package com.example.fitcore.fragment;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fitcore.R;
import com.example.fitcore.activity.WorkoutHistoryActivity;
import com.example.fitcore.database.DatabaseHelper;
import com.example.fitcore.model.WorkoutRecord;
import com.example.fitcore.utils.SessionManager;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.ArrayList;
import java.util.List;

public class RecordFragment extends Fragment {

    private DatabaseHelper db;
    private SessionManager session;

    // 今日统计
    private TextView tvTodayMinutes, tvTodayCalories;
    private TextView tvProgressPct;
    private int todayMinutes, todayGoal = 60;

    // 运动网格
    private RecyclerView rvExercises;
    private ExerciseAdapter adapter;
    private List<ExerciseItem> currentExercises = new ArrayList<>();
    private int currentCategory = 0;
    private boolean isExpanded = false;
    private TextView btnToggleMore;
    private static final int COLLAPSED_COUNT = 8;

    // 最近记录
    private ViewGroup recentList;

    private String[] CAT_NAMES = {"有氧运动", "力量训练", "柔韧拉伸", "球类运动", "其他"};
    private String[] CAT_ICONS = {"🏃", "💪", "🧘", "⚽", "➕"};

    // ===== 运动数据 =====
    private final ExerciseItem[][] EXERCISE_DATA = {
        { // 有氧运动
            e("🏃","跑步"), e("🚴","骑行"), e("🏊","游泳"), e("🪢","跳绳"),
            e("🏃‍","慢跑"), e("🚶","快走"), e("💃","跳舞"), e("🧗","登山"),
            e("🏋","椭圆机"), e("🚣","划船"), e("🛼","轮滑"), e("🤸","有氧操"),
            e("🏃","马拉松"), e("🚶","徒步"), e("🧗","攀岩"), e("🏔","越野跑"),
            e("🚴","动感单车"), e("🏊","自由泳"), e("🏃","冲刺跑"), e("🕺","街舞"),
        },
        { // 力量训练
            e("💪","俯卧撑"), e("🦵","深蹲"), e("🔄","仰卧起坐"), e("🏋","硬拉"),
            e("🦾","引体向上"), e("🪑","平板支撑"), e("💪","哑铃"), e("🦿","弓步蹲"),
            e("🏋","卧推"), e("🏋","深蹲架"), e("💪","飞鸟"), e("🦵","腿举"),
            e("🏋","弯举"), e("💪","推举"), e("🦾","划船机"), e("🏋","挺举"),
            e("💪","侧平举"), e("🦵","臀桥"), e("🪑","卷腹"), e("💪","窄距俯卧撑"),
        },
        { // 柔韧拉伸
            e("🧘","瑜伽"), e("🤸","拉伸"), e("🧘‍","普拉提"), e("🦯","泡沫轴"),
            e("🕺","太极"), e("🙆","开肩"), e("🦵","压腿"), e("🧎","猫式"),
            e("🧘","阴瑜伽"), e("🧘","流瑜伽"), e("🤸","劈叉"), e("🦵","髋部拉伸"),
            e("🙆","肩颈放松"), e("🧎","婴儿式"), e("🕺","八段锦"), e("🧘","冥想"),
            e("🤸","动态拉伸"), e("🦯","筋膜放松"), e("🧘","高温瑜伽"), e("🙆","脊柱扭转"),
        },
        { // 球类运动
            e("🏀","篮球"), e("⚽","足球"), e("🏓","乒乓球"), e("🎾","网球"),
            e("🏐","排球"), e("🏸","羽毛球"), e("🎱","台球"), e("🏒","曲棍球"),
            e("⚾","棒球"), e("🏉","橄榄球"), e("🎳","保龄球"), e("⛳","高尔夫"),
            e("🏑","冰球"), e("🏏","板球"), e("🥎","垒球"), e("🤾","手球"),
            e("🏐","沙滩排球"), e("🎯","壁球"), e("🥍","长曲棍球"), e("🏓","桌式足球"),
        },
        { // 其他运动
            e("➕","自定义"), e("🥊","拳击"), e("🛹","滑板"), e("🏌","高尔夫"),
            e("🎿","滑雪"), e("🥋","武术"), e("🎯","射箭"), e("🤺","击剑"),
            e("🏹","飞镖"), e("🤼","摔跤"), e("🥊","跆拳道"), e("🛹","滑雪板"),
            e("🏇","骑马"), e("🚣","皮划艇"), e("🏄","冲浪"), e("🤿","潜水"),
            e("⛸","溜冰"), e("🪂","跳伞"), e("🎣","钓鱼"), e("🛶","龙舟"),
        },
    };

    private static ExerciseItem e(String emoji, String name) {
        return new ExerciseItem(emoji, name);
    }

    static class ExerciseItem {
        String emoji;
        String name;
        ExerciseItem(String e, String n) { emoji = e; name = n; }
    }

    // ===== 适配器 =====
    class ExerciseAdapter extends RecyclerView.Adapter<ExerciseAdapter.VH> {
        @NonNull @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new VH(LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_exercise, parent, false));
        }
        @Override
        public void onBindViewHolder(@NonNull VH h, int pos) {
            ExerciseItem item = currentExercises.get(pos);
            h.name.setText(item.emoji + "\n" + item.name);
            h.name.setTextSize(14);
            h.icon.setVisibility(View.VISIBLE);
            h.itemView.setOnClickListener(v -> onExerciseClick(item));
        }
        @Override public int getItemCount() { return currentExercises.size(); }
        class VH extends RecyclerView.ViewHolder {
            TextView name; ImageView icon;
            VH(View v) { super(v);
                name = v.findViewById(R.id.tv_ex_name);
                icon = v.findViewById(R.id.iv_ex_icon);
            }
        }
    }

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_record, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        db = DatabaseHelper.getInstance(requireContext());
        session = new SessionManager(requireContext());

        tvTodayMinutes = view.findViewById(R.id.tv_today_minutes);
        tvTodayCalories = view.findViewById(R.id.tv_today_calories);
        tvProgressPct = view.findViewById(R.id.tv_progress_pct);

        view.findViewById(R.id.btn_view_all).setOnClickListener(v ->
                startActivity(new Intent(getActivity(), WorkoutHistoryActivity.class)));

        // RecyclerView
        rvExercises = view.findViewById(R.id.rv_exercises);
        btnToggleMore = (TextView) view.findViewById(R.id.btn_toggle_more);

        btnToggleMore.setOnClickListener(v -> {
            isExpanded = !isExpanded;
            updateExerciseList();
        });
        rvExercises.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        adapter = new ExerciseAdapter();
        rvExercises.setAdapter(adapter);

        // 分类选择器
        TextView tvMainIcon = view.findViewById(R.id.tv_main_cat_icon);
        TextView tvMainName = view.findViewById(R.id.tv_main_cat_name);

        view.findViewById(R.id.btn_main_cat).setOnClickListener(v -> {
            currentCategory = (currentCategory + 1) % CAT_NAMES.length;
            tvMainIcon.setText(CAT_ICONS[currentCategory]);
            tvMainName.setText(CAT_NAMES[currentCategory]);
            updateExerciseList();
        });

        view.findViewById(R.id.btn_cat_menu).setOnClickListener(v -> {
            String[] items = new String[CAT_NAMES.length];
            for (int i = 0; i < CAT_NAMES.length; i++)
                items[i] = CAT_ICONS[i] + "  " + CAT_NAMES[i];

            android.widget.ListPopupWindow popup = new android.widget.ListPopupWindow(requireContext());
            popup.setAnchorView(v);
            popup.setAdapter(new android.widget.ArrayAdapter<String>(requireContext(),
                    R.layout.item_cat_dropdown, R.id.tv_dropdown_text, items) {
                @NonNull @Override
                public View getView(int pos, View cv, @NonNull ViewGroup parent) {
                    View v = super.getView(pos, cv, parent);
                    // 最后一项不显示分割线
                    View divider = v.findViewById(R.id.dropdown_divider);
                    if (divider != null) divider.setVisibility(
                            pos == getCount() - 1 ? android.view.View.GONE : android.view.View.VISIBLE);
                    return v;
                }
            });
            popup.setWidth(dp(190));
            popup.setBackgroundDrawable(
                    androidx.core.content.ContextCompat.getDrawable(requireContext(), R.drawable.bg_dropdown_popup));
            popup.setVerticalOffset(dp(4));
            popup.setOnItemClickListener((parent, view1, pos, id) -> {
                currentCategory = pos;
                tvMainIcon.setText(CAT_ICONS[pos]);
                tvMainName.setText(CAT_NAMES[pos]);
                updateExerciseList();
                popup.dismiss();
            });
            popup.show();
        });

        recentList = view.findViewById(R.id.recent_list);

        updateExerciseList();
        loadTodayStats();
        loadRecentRecords();
    }

    private void updateExerciseList() {
        currentExercises.clear();
        ExerciseItem[] all = EXERCISE_DATA[currentCategory];
        int count = isExpanded ? all.length : Math.min(COLLAPSED_COUNT, all.length);
        for (int i = 0; i < count; i++) currentExercises.add(all[i]);
        adapter.notifyDataSetChanged();

        int rows = (int) Math.ceil(count / 2.0);
        rvExercises.getLayoutParams().height = rows * dp(96);
        rvExercises.requestLayout();

        // 显示/隐藏 展开按钮
        if (all.length > COLLAPSED_COUNT) {
            btnToggleMore.setVisibility(View.VISIBLE);
            btnToggleMore.setText(isExpanded ? "收起 ▲" : "显示更多 ▼");
        } else {
            btnToggleMore.setVisibility(View.GONE);
        }
    }

    private void loadTodayStats() {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
        String today = sdf.format(new java.util.Date());
        int mins = 0;
        for (WorkoutRecord r : db.getRecordsByUser(session.getUserId())) {
            if (r.getRecordedAt() != null && r.getRecordedAt().startsWith(today)) {
                mins += r.getDurationMinutes();
            }
        }
        todayMinutes = mins;
        todayGoal = requireContext().getSharedPreferences("fitcore_prefs", 0)
                .getInt("daily_goal_minutes", 60);
        tvTodayMinutes.setText(mins + " / " + todayGoal + " 分钟");
        int cal = 0;
        for (WorkoutRecord r : db.getRecordsByUser(session.getUserId())) {
            if (r.getRecordedAt() != null && r.getRecordedAt().startsWith(today))
                cal += calcCalories(r.getType(), r.getDurationMinutes());
        }
        tvTodayCalories.setText("消耗 " + cal + " 千卡");
        int pct = Math.min(100, mins * 100 / todayGoal);
        tvProgressPct.setText(pct + "%");
    }

    private void loadRecentRecords() {
        recentList.removeAllViews();
        List<WorkoutRecord> all = db.getRecordsByUser(session.getUserId());
        int count = Math.min(3, all.size());
        for (int i = 0; i < count; i++) {
            WorkoutRecord r = all.get(i);
            View item = LayoutInflater.from(requireContext())
                    .inflate(R.layout.item_recent_record, recentList, false);
            ((TextView) item.findViewById(R.id.tv_rec_name)).setText(r.getType());
            ((TextView) item.findViewById(R.id.tv_rec_duration))
                    .setText(r.getDurationMinutes() + " 分钟");
            String t = r.getRecordedAt();
            if (t != null && t.length() >= 16) t = t.substring(5, 16);
            ((TextView) item.findViewById(R.id.tv_rec_time)).setText(t);

            // 点击查看详情
            item.setOnClickListener(v -> showWorkoutDetail(r));
            recentList.addView(item);
        }
    }

    private void showWorkoutDetail(WorkoutRecord r) {
        View dlg = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_workout_detail, null);

        String[] feels = {"", "😫差", "😕较差", "😐一般", "😊良好", "🔥爽"};
        int f = r.getFeeling();

        ((TextView) dlg.findViewById(R.id.detail_type)).setText(r.getType());
        ((TextView) dlg.findViewById(R.id.detail_duration))
                .setText(r.getDurationMinutes() + " 分钟");
        ((TextView) dlg.findViewById(R.id.detail_date)).setText(
                r.getRecordedAt() != null && r.getRecordedAt().length() >= 16
                        ? r.getRecordedAt().substring(0, 16) : "");
        ((TextView) dlg.findViewById(R.id.detail_feeling)).setText(
                f >= 1 && f <= 5 ? feels[f] : "无");
        ((TextView) dlg.findViewById(R.id.detail_notes)).setText(
                !TextUtils.isEmpty(r.getNotes()) ? r.getNotes() : "无备注");

        android.app.AlertDialog dialog = new android.app.AlertDialog.Builder(requireContext())
                .setView(dlg)
                .setCancelable(true)
                .create();
        if (dialog.getWindow() != null)
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        dlg.findViewById(R.id.btn_close).setOnClickListener(cv -> dialog.dismiss());
        dialog.show();
    }

    // ===== 点击运动 → BottomSheet =====
    private int dp(int d) { return (int) (d * getResources().getDisplayMetrics().density); }

    private void onExerciseClick(ExerciseItem item) {
        if (item.emoji.equals("➕")) {
            View dlgView = LayoutInflater.from(requireContext())
                    .inflate(R.layout.dialog_custom_exercise, null);
            EditText input = dlgView.findViewById(R.id.et_custom_name);

            android.app.AlertDialog dialog = new android.app.AlertDialog.Builder(requireContext())
                    .setView(dlgView)
                    .setCancelable(false)
                    .create();
            if (dialog.getWindow() != null)
                dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

            dlgView.findViewById(R.id.btn_custom_cancel).setOnClickListener(v -> dialog.dismiss());
            dlgView.findViewById(R.id.btn_custom_ok).setOnClickListener(v -> {
                String t = input.getText().toString().trim();
                if (!TextUtils.isEmpty(t)) {
                    dialog.dismiss();
                    showBottomSheet(t);
                }
            });

            dialog.show();
            return;
        }
        showBottomSheet(item.emoji + " " + item.name);
    }

    private int calcCalories(String type, int minutes) {
        double met = estimateMET(type, currentCategory);
        com.example.fitcore.model.User user = db.getUserById(session.getUserId());
        double weight = (user != null && user.getWeight() > 0) ? user.getWeight() : 70;
        return (int) (met * weight * (minutes / 60.0));
    }

    private double estimateMET(String type, int category) {
        String t = type.toLowerCase();
        switch (category) {
            case 0: // 有氧
                if (t.contains("跑") || t.contains("马拉松")) return 10.0;
                if (t.contains("骑") || t.contains("单车")) return 7.0;
                if (t.contains("游泳") || t.contains("自由泳")) return 8.0;
                if (t.contains("跳绳")) return 11.0;
                if (t.contains("走") || t.contains("徒步")) return 4.0;
                if (t.contains("舞")) return 6.0;
                if (t.contains("登山") || t.contains("攀岩") || t.contains("越野")) return 8.0;
                if (t.contains("椭圆机")) return 6.0;
                if (t.contains("划船") || t.contains("轮滑")) return 7.0;
                if (t.contains("操")) return 5.5;
                return 7.0;
            case 1: // 力量
                if (t.contains("深蹲") || t.contains("硬拉") || t.contains("腿举")) return 6.0;
                if (t.contains("卧推") || t.contains("挺举")) return 6.0;
                return 5.0;
            case 2: // 柔韧
                if (t.contains("瑜伽") || t.contains("普拉提")) return 3.5;
                if (t.contains("太极") || t.contains("冥想")) return 3.0;
                return 3.0;
            case 3: // 球类
                if (t.contains("篮球") || t.contains("足球")) return 8.0;
                if (t.contains("网球") || t.contains("羽毛球")) return 7.0;
                if (t.contains("乒乓球") || t.contains("台球")) return 4.0;
                if (t.contains("排球")) return 5.0;
                if (t.contains("橄榄球") || t.contains("冰球") || t.contains("曲棍球")) return 8.0;
                return 6.0;
            default: // 其他
                if (t.contains("拳击") || t.contains("跆拳道") || t.contains("摔跤")) return 10.0;
                if (t.contains("滑雪") || t.contains("滑板")) return 6.0;
                if (t.contains("击剑")) return 8.0;
                if (t.contains("骑马")) return 5.5;
                if (t.contains("冲浪") || t.contains("皮划艇") || t.contains("龙舟")) return 6.0;
                if (t.contains("潜水")) return 7.0;
                if (t.contains("钓鱼")) return 2.5;
                if (t.contains("跳伞") || t.contains("溜冰")) return 5.0;
                return 5.0;
        }
    }

    private void showBottomSheet(String typeName) {
        BottomSheetDialog sheet = new BottomSheetDialog(requireContext());
        View content = LayoutInflater.from(requireContext())
                .inflate(R.layout.bottom_sheet_record, null);

        ((TextView) content.findViewById(R.id.bs_title)).setText("记录 " + typeName);

        // NumberPicker
        NumberPicker np = content.findViewById(R.id.np_minutes);
        np.setMinValue(1);
        np.setMaxValue(180);
        np.setValue(30);
        np.setFormatter(i -> i + " 分钟");

        // 卡路里显示
        TextView tvCal = content.findViewById(R.id.bs_calories);
        // 初始卡路里计算
        tvCal.setText("预计消耗 " + calcCalories(typeName, 30) + " 千卡");
        np.setOnValueChangedListener((picker, oldVal, newVal) ->
                tvCal.setText("预计消耗 " + calcCalories(typeName, newVal) + " 千卡"));

        // 感受选择（5 档）
        int[] feelingResult = {3}; // 默认一般
        String[] feelIds = {"feel_bad", "feel_poor", "feel_ok", "feel_good", "feel_great"};
        int[] feelValues = {1, 2, 3, 4, 5};
        View[] feelViews = new View[5];

        View.OnClickListener feelListener = v -> {
            for (int i = 0; i < 5; i++) {
                feelViews[i].setBackgroundResource(R.drawable.bg_card);
                ((TextView) feelViews[i]).setTextColor(0xFF888888);
            }
            v.setBackgroundResource(R.drawable.bg_card_accent);
            ((TextView) v).setTextColor(0xFFCCFF00);
            for (int i = 0; i < 5; i++) {
                if (v == feelViews[i]) { feelingResult[0] = feelValues[i]; break; }
            }
        };
        for (int i = 0; i < 5; i++) {
            int rid = getResources().getIdentifier(feelIds[i], "id", requireContext().getPackageName());
            feelViews[i] = content.findViewById(rid);
            feelViews[i].setOnClickListener(feelListener);
        }
        feelViews[2].performClick(); // 默认选中"一般"

        EditText etNotes = content.findViewById(R.id.bs_notes);

        // 开始运动按钮 → 启动计时Activity
        content.findViewById(R.id.btn_start_timer).setOnClickListener(v -> {
            sheet.dismiss();
            Intent i = new Intent(getActivity(),
                    com.example.fitcore.activity.WorkoutTimerActivity.class);
            i.putExtra("plan_name", typeName);
            startActivity(i);
        });

        // 保存记录按钮
        content.findViewById(R.id.btn_save_record).setOnClickListener(v -> {
            int mins = np.getValue();
            String notes = etNotes.getText().toString().trim();
            String[] fs = {"", "😫差", "😕较差", "😐一般", "😊良好", "🔥爽"};

            View dlg = LayoutInflater.from(requireContext())
                    .inflate(R.layout.dialog_confirm, null);
            ((ImageView) dlg.findViewById(R.id.dialog_icon))
                    .setImageResource(android.R.drawable.ic_menu_edit);
            ((TextView) dlg.findViewById(R.id.dialog_title)).setText("确认记录");
            ((TextView) dlg.findViewById(R.id.dialog_message))
                    .setText(typeName + "\n" + mins + " 分钟 · " + fs[feelingResult[0]]
                            + "\n" + calcCalories(typeName, mins) + " 千卡"
                            + (notes.isEmpty() ? "" : "\n备注：" + notes));
            TextView btnPos = dlg.findViewById(R.id.dialog_positive);
            TextView btnNeg = dlg.findViewById(R.id.dialog_negative);
            btnPos.setText("保存"); btnNeg.setText("取消");

            android.app.AlertDialog dialog = new android.app.AlertDialog.Builder(requireContext())
                    .setView(dlg).setCancelable(false).create();
            if (dialog.getWindow() != null)
                dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

            btnPos.setOnClickListener(vv -> {
                dialog.dismiss();
                sheet.dismiss();
                db.insertWorkoutRecord(new WorkoutRecord(session.getUserId(), null,
                        typeName, mins, feelingResult[0], notes));
                Toast.makeText(requireContext(), "记录已保存！", Toast.LENGTH_SHORT).show();
                loadTodayStats();
                loadRecentRecords();
            });
            btnNeg.setOnClickListener(vv -> dialog.dismiss());
            dialog.show();
        });

        sheet.setContentView(content);
        sheet.show();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getView() != null && tvTodayMinutes != null) {
            loadTodayStats();
            loadRecentRecords();
        }
    }
}
