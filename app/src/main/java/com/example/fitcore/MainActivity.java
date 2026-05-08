package com.example.fitcore;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.example.fitcore.fragment.AnalyticsFragment;
import com.example.fitcore.fragment.HomeFragment;
import com.example.fitcore.fragment.ProfileFragment;
import com.example.fitcore.fragment.RecordFragment;
import com.example.fitcore.fragment.ReminderFragment;
import com.example.fitcore.utils.SessionManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        session = new SessionManager(this);
        if (!session.isLoggedIn()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        // 处理系统栏内边距，防止内容被系统导航栏遮挡
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.root_layout), (v, insets) -> {
            int bottomInset = insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom;
            v.setPadding(v.getPaddingLeft(), v.getPaddingTop(), v.getPaddingRight(), bottomInset);
            return insets;
        });

        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);
        bottomNav.setLabelVisibilityMode(
                com.google.android.material.bottomnavigation.BottomNavigationView.LABEL_VISIBILITY_LABELED);
        bottomNav.setItemIconSize(dp2px(24));

        // 铃铛图标切换：默认端正
        final android.view.MenuItem bellItem = bottomNav.getMenu().findItem(R.id.nav_reminder);
        bellItem.setIcon(R.drawable.ic_bell_straight);
        final int[] prevId = {R.id.nav_home};
        bottomNav.setOnItemSelectedListener(item -> {
            int clickedId = item.getItemId();
            // 恢复上一个铃铛
            if (prevId[0] == R.id.nav_reminder && clickedId != R.id.nav_reminder) {
                bellItem.setIcon(R.drawable.ic_bell_straight);
            }
            // 切换到提醒→倾斜（用系统自带铃铛，天然斜角）
            if (clickedId == R.id.nav_reminder) {
                bellItem.setIcon(android.R.drawable.ic_popup_reminder);
            }
            prevId[0] = clickedId;

            Fragment f;
            int id = clickedId;
            if (id == R.id.nav_home) f = new HomeFragment();
            else if (id == R.id.nav_record) f = new RecordFragment();
            else if (id == R.id.nav_reminder) f = new ReminderFragment();
            else if (id == R.id.nav_analytics) f = new AnalyticsFragment();
            else if (id == R.id.nav_profile) f = new ProfileFragment();
            else return false;

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, f).commit();
            return true;
        });

        bottomNav.setSelectedItemId(R.id.nav_home);
    }

    private int dp2px(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density + 0.5f);
    }
}
