package com.example.fitcore.activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.fitcore.R;
import com.example.fitcore.database.DatabaseHelper;
import com.example.fitcore.model.User;
import com.example.fitcore.utils.SessionManager;

public class EditProfileActivity extends AppCompatActivity {

    private DatabaseHelper db;
    private SessionManager session;
    private EditText etName, etAge, etHeight, etWeight;
    private TextView tabMale, tabFemale;
    private String gender = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        db = DatabaseHelper.getInstance(this);
        session = new SessionManager(this);

        etName = findViewById(R.id.et_edit_name);
        etAge = findViewById(R.id.et_edit_age);
        etHeight = findViewById(R.id.et_edit_height);
        etWeight = findViewById(R.id.et_edit_weight);
        tabMale = findViewById(R.id.tab_male);
        tabFemale = findViewById(R.id.tab_female);

        User user = db.getUserById(session.getUserId());
        if (user != null) {
            etName.setText(user.getName());
            etAge.setText(user.getAge() > 0 ? String.valueOf(user.getAge()) : "");
            etHeight.setText(user.getHeight() > 0 ? String.valueOf(user.getHeight()) : "");
            etWeight.setText(user.getWeight() > 0 ? String.valueOf(user.getWeight()) : "");
            gender = user.getGender();
            updateGenderToggle();
        }

        tabMale.setOnClickListener(v -> { gender = "男"; updateGenderToggle(); });
        tabFemale.setOnClickListener(v -> { gender = "女"; updateGenderToggle(); });

        findViewById(R.id.btn_cancel_edit).setOnClickListener(v -> finish());
        findViewById(R.id.btn_save_profile).setOnClickListener(v -> saveProfile(user));
    }

    private void updateGenderToggle() {
        boolean isMale = "男".equals(gender);
        tabMale.setBackground(isMale ?
                ContextCompat.getDrawable(this, R.drawable.bg_toggle_active) : null);
        tabMale.setTextColor(isMale ?
                ContextCompat.getColor(this, R.color.black) :
                ContextCompat.getColor(this, R.color.text_secondary));

        tabFemale.setBackground(isMale ? null :
                ContextCompat.getDrawable(this, R.drawable.bg_toggle_active));
        tabFemale.setTextColor(isMale ?
                ContextCompat.getColor(this, R.color.text_secondary) :
                ContextCompat.getColor(this, R.color.black));
    }

    private void saveProfile(User user) {
        if (user == null) return;

        String name = etName.getText().toString().trim();
        String ageStr = etAge.getText().toString().trim();
        String heightStr = etHeight.getText().toString().trim();
        String weightStr = etWeight.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            Toast.makeText(this, "姓名不能为空", Toast.LENGTH_SHORT).show();
            return;
        }

        user.setName(name);
        user.setGender(gender);
        user.setAge(TextUtils.isEmpty(ageStr) ? 0 : Integer.parseInt(ageStr));
        user.setHeight(TextUtils.isEmpty(heightStr) ? 0 : Double.parseDouble(heightStr));
        user.setWeight(TextUtils.isEmpty(weightStr) ? 0 : Double.parseDouble(weightStr));

        db.updateUser(user);
        session.saveLogin(user.getId(), user.getName());
        Toast.makeText(this, "资料已更新", Toast.LENGTH_SHORT).show();
        finish();
    }
}
