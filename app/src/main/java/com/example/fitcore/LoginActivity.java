package com.example.fitcore;

import android.content.Intent;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.fitcore.database.DatabaseHelper;
import com.example.fitcore.model.ReminderSettings;
import com.example.fitcore.model.User;
import com.example.fitcore.utils.NotificationHelper;
import com.example.fitcore.utils.SessionManager;

import java.security.MessageDigest;

public class LoginActivity extends AppCompatActivity {

    private boolean isLoginMode = true;
    private boolean updatingProgrammatically;

    private LinearLayout layoutName, layoutConfirm, layoutWarning;
    private EditText etName, etUsername, etPassword, etConfirm;
    private TextView tabLogin, tabSignup, btnAction, tvWarning;

    private DatabaseHelper db;
    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        try {
            db = DatabaseHelper.getInstance(this);
        } catch (Exception e) {
            Toast.makeText(this, "数据库初始化失败，请重装应用", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        session = new SessionManager(this);
        NotificationHelper.createChannel(this);

        if (session.isLoggedIn()) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        initViews();
        setupTextWatchers();
    }

    private void initViews() {
        layoutName = findViewById(R.id.layout_name);
        layoutConfirm = findViewById(R.id.layout_confirm);
        layoutWarning = findViewById(R.id.layout_warning);
        tvWarning = findViewById(R.id.tv_warning);
        etName = findViewById(R.id.et_name);
        etUsername = findViewById(R.id.et_username);
        etPassword = findViewById(R.id.et_password);
        etConfirm = findViewById(R.id.et_confirm);

        tabLogin = findViewById(R.id.tab_login);
        tabSignup = findViewById(R.id.tab_signup);
        btnAction = findViewById(R.id.btn_action);

        tabLogin.setOnClickListener(v -> switchMode(true));
        tabSignup.setOnClickListener(v -> switchMode(false));
        btnAction.setOnClickListener(v -> {
            if (isLoginMode) login();
            else register();
        });
    }

    private void setupTextWatchers() {
        TextWatcher tw = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void onTextChanged(CharSequence s, int st, int b, int c) {}
            @Override public void afterTextChanged(Editable s) {
                if (!updatingProgrammatically) updateButtonState();
            }
        };
        etUsername.addTextChangedListener(tw);
        etPassword.addTextChangedListener(tw);
        etName.addTextChangedListener(tw);
        etConfirm.addTextChangedListener(tw);
    }

    private void updateButtonState() {
        String user = etUsername.getText().toString().trim();
        String pass = etPassword.getText().toString().trim();
        boolean valid;
        if (isLoginMode) {
            valid = !TextUtils.isEmpty(user) && !TextUtils.isEmpty(pass);
        } else {
            String name = etName.getText().toString().trim();
            String confirm = etConfirm.getText().toString().trim();
            valid = !TextUtils.isEmpty(name) && !TextUtils.isEmpty(user)
                    && !TextUtils.isEmpty(pass) && !TextUtils.isEmpty(confirm);
        }
        btnAction.setEnabled(valid);
        btnAction.setBackground(ContextCompat.getDrawable(this,
                valid ? R.drawable.bg_accent_btn : R.drawable.bg_btn_disabled));
        btnAction.setTextColor(ContextCompat.getColor(this,
                valid ? R.color.black : R.color.text_muted));
        try { layoutWarning.setVisibility(View.GONE); } catch (Exception ignored) {}
    }

    private void switchMode(boolean login) {
        isLoginMode = login;
        layoutName.setVisibility(login ? View.GONE : View.VISIBLE);
        layoutConfirm.setVisibility(login ? View.GONE : View.VISIBLE);
        btnAction.setText(login ? "登录" : "注册");
        try { layoutWarning.setVisibility(View.GONE); } catch (Exception ignored) {}

        tabLogin.setBackground(login ?
                ContextCompat.getDrawable(this, R.drawable.bg_toggle_active) : null);
        tabLogin.setTextColor(login ?
                ContextCompat.getColor(this, R.color.black) :
                ContextCompat.getColor(this, R.color.text_secondary));

        tabSignup.setBackground(login ? null :
                ContextCompat.getDrawable(this, R.drawable.bg_toggle_active));
        tabSignup.setTextColor(login ?
                ContextCompat.getColor(this, R.color.text_secondary) :
                ContextCompat.getColor(this, R.color.black));

        updateButtonState();
    }

    private void login() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) return;
        try {
            User user = db.getUserByUsername(username);
            if (user == null) { showWarning("用户不存在，请先注册"); return; }
            if (!user.getPasswordHash().equals(sha256(password))) {
                showWarning("密码错误"); return;
            }
            session.saveLogin(user.getId(), user.getName());
            Toast.makeText(this, "欢迎，" + user.getName() + "！", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, MainActivity.class));
            finish();
        } catch (SQLiteException e) {
            showWarning("数据异常，请卸载后重装");
        }
    }

    private void register() {
        String name = etName.getText().toString().trim();
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirm = etConfirm.getText().toString().trim();
        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(username)
                || TextUtils.isEmpty(password) || TextUtils.isEmpty(confirm)) return;
        if (password.length() < 6) { showWarning("密码至少6位"); return; }
        if (!password.equals(confirm)) { showWarning("两次密码输入不一致"); return; }
        try {
            if (db.getUserByUsername(username) != null) {
                showWarning("用户名已存在"); return;
            }
            User user = new User(name, username, sha256(password));
            long userId = db.insertUser(user);
            if (userId == -1) { showWarning("注册失败，请重试"); return; }
            ReminderSettings rs = new ReminderSettings((int) userId, 7, 0, false);
            db.insertReminderSettings(rs);

            updatingProgrammatically = true;
            etName.setText("");
            etUsername.setText("");
            etPassword.setText("");
            etConfirm.setText("");
            updatingProgrammatically = false;

            Toast.makeText(this, "注册成功，请登录", Toast.LENGTH_SHORT).show();
            switchMode(true);
        } catch (SQLiteException e) {
            showWarning("数据异常，请先卸载旧版本再安装");
        }
    }

    private void showWarning(String msg) {
        tvWarning.setText(msg);
        layoutWarning.setVisibility(View.VISIBLE);
    }

    private String sha256(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(input.getBytes("UTF-8"));
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) hex.append(String.format("%02x", b));
            return hex.toString();
        } catch (Exception e) { return input; }
    }
}
