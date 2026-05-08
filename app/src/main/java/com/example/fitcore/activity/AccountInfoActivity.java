package com.example.fitcore.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.fitcore.LoginActivity;
import com.example.fitcore.R;
import com.example.fitcore.database.DatabaseHelper;
import com.example.fitcore.model.User;
import com.example.fitcore.utils.SessionManager;

import java.security.MessageDigest;

public class AccountInfoActivity extends AppCompatActivity {

    private DatabaseHelper db;
    private SessionManager session;
    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_info);

        db = DatabaseHelper.getInstance(this);
        session = new SessionManager(this);
        user = db.getUserById(session.getUserId());
        if (user == null) { finish(); return; }

        ((TextView) findViewById(R.id.tv_account_username)).setText(user.getUsername());
        StringBuilder dots = new StringBuilder();
        for (int i = 0; i < Math.min(user.getPasswordHash().length(), 16); i++) dots.append("•");
        ((TextView) findViewById(R.id.tv_account_password)).setText(dots.toString());

        findViewById(R.id.btn_change_pwd).setOnClickListener(v -> showChangePwdDialog());

        findViewById(R.id.btn_logout).setOnClickListener(v -> {
            View dlgView = LayoutInflater.from(this)
                    .inflate(R.layout.dialog_logout, null);
            AlertDialog dialog = new AlertDialog.Builder(this)
                    .setView(dlgView).setCancelable(false).create();
            if (dialog.getWindow() != null)
                dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            dlgView.findViewById(R.id.btn_cancel).setOnClickListener(v2 -> dialog.dismiss());
            dlgView.findViewById(R.id.btn_confirm).setOnClickListener(v2 -> {
                dialog.dismiss();
                session.logout();
                Intent i = new Intent(this, LoginActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(i);
            });
            dialog.show();
        });
    }

    private void showChangePwdDialog() {
        View dlgView = LayoutInflater.from(this)
                .inflate(R.layout.dialog_change_password, null);
        EditText etOld = dlgView.findViewById(R.id.et_old_pwd);
        EditText etNew = dlgView.findViewById(R.id.et_new_pwd);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dlgView)
                .setCancelable(true)
                .create();
        if (dialog.getWindow() != null)
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        dlgView.findViewById(R.id.btn_pwd_cancel).setOnClickListener(v -> dialog.dismiss());
        dlgView.findViewById(R.id.btn_pwd_confirm).setOnClickListener(v -> {
            String oldPwd = etOld.getText().toString().trim();
            String newPwd = etNew.getText().toString().trim();

            if (TextUtils.isEmpty(oldPwd) || TextUtils.isEmpty(newPwd)) {
                Toast.makeText(this, "请填写完整", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!user.getPasswordHash().equals(sha256(oldPwd))) {
                Toast.makeText(this, "原密码错误", Toast.LENGTH_SHORT).show();
                return;
            }
            if (newPwd.length() < 6) {
                Toast.makeText(this, "新密码至少6位", Toast.LENGTH_SHORT).show();
                return;
            }

            user.setPasswordHash(sha256(newPwd));
            db.updateUser(user);
            dialog.dismiss();

            Toast.makeText(this, "密码已修改，请重新登录", Toast.LENGTH_LONG).show();
            session.logout();
            Intent i = new Intent(this, LoginActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
        });

        dialog.show();
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
