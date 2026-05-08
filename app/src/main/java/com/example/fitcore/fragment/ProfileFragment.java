package com.example.fitcore.fragment;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.fitcore.LoginActivity;
import com.example.fitcore.R;
import com.example.fitcore.activity.AccountInfoActivity;
import com.example.fitcore.activity.BMIGuideActivity;
import com.example.fitcore.activity.CalorieGuideActivity;
import com.example.fitcore.activity.EditProfileActivity;
import com.example.fitcore.activity.WorkoutHistoryActivity;
import com.example.fitcore.database.DatabaseHelper;
import com.example.fitcore.model.User;
import com.example.fitcore.utils.SessionManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class ProfileFragment extends Fragment {

    private DatabaseHelper db;
    private SessionManager session;
    private ImageView ivAvatar;
    private ImageView ivProfileBackground;
    private View viewHeaderGradient;
    private TextView tvFitcoreWatermark;
    private ActivityResultLauncher<String> pickImageLauncher;
    private ActivityResultLauncher<String> pickBgImageLauncher;

    private File getAvatarFile() {
        return new File(requireContext().getFilesDir(), "avatar.jpg");
    }

    private boolean hasAvatar() {
        return getAvatarFile().exists();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = DatabaseHelper.getInstance(requireContext());
        session = new SessionManager(requireContext());
        ivAvatar = view.findViewById(R.id.iv_avatar);
        // 裁剪为圆形
        ivAvatar.setClipToOutline(true);
        ivAvatar.setOutlineProvider(new android.view.ViewOutlineProvider() {
            @Override
            public void getOutline(View view, android.graphics.Outline outline) {
                outline.setOval(0, 0, view.getWidth(), view.getHeight());
            }
        });

        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        saveImageToFile(uri);
                        loadAvatar();
                    }
                });

        // ----- 背景图片 -----
        ivProfileBackground = view.findViewById(R.id.iv_profile_background);
        viewHeaderGradient = view.findViewById(R.id.view_header_gradient);
        tvFitcoreWatermark = view.findViewById(R.id.tv_fitcore_watermark);

        pickBgImageLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        saveBackgroundToFile(uri);
                        loadBackground();
                    }
                });

        View headerArea = view.findViewById(R.id.layout_profile_header);
        headerArea.setOnClickListener(v -> onBackgroundClick());
        loadBackground();

        ivAvatar.setOnClickListener(v -> onAvatarClick());
        loadAvatar();
        loadUserInfo(view);

        view.findViewById(R.id.btn_edit_profile).setOnClickListener(v ->
                startActivity(new Intent(getActivity(), EditProfileActivity.class)));
        view.findViewById(R.id.btn_calorie_guide).setOnClickListener(v ->
                startActivity(new Intent(getActivity(), CalorieGuideActivity.class)));
        view.findViewById(R.id.btn_bmi_guide).setOnClickListener(v ->
                startActivity(new Intent(getActivity(), BMIGuideActivity.class)));
        view.findViewById(R.id.btn_account_info).setOnClickListener(v ->
                startActivity(new Intent(getActivity(), AccountInfoActivity.class)));
        view.findViewById(R.id.btn_history).setOnClickListener(v ->
                startActivity(new Intent(getActivity(), WorkoutHistoryActivity.class)));
    }

    private void onAvatarClick() {
        if (!hasAvatar()) {
            showPickConfirm();
            return;
        }

        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_avatar_options, null);

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        dialogView.findViewById(R.id.btn_remove).setOnClickListener(v -> {
            dialog.dismiss();
            removeAvatar();
        });
        dialogView.findViewById(R.id.btn_view).setOnClickListener(v -> {
            dialog.dismiss();
            viewAvatar();
        });
        dialogView.findViewById(R.id.btn_change).setOnClickListener(v -> {
            dialog.dismiss();
            showPickConfirm();
        });

        dialog.show();
    }

    private void showPickConfirm() {
        View dlg = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_confirm, null);
        ((ImageView) dlg.findViewById(R.id.dialog_icon))
                .setImageResource(android.R.drawable.ic_menu_gallery);
        ((TextView) dlg.findViewById(R.id.dialog_title)).setText("选择头像");
        ((TextView) dlg.findViewById(R.id.dialog_message)).setText("将从手机相册选择图片");
        TextView btnPos = dlg.findViewById(R.id.dialog_positive);
        TextView btnNeg = dlg.findViewById(R.id.dialog_negative);
        btnPos.setText("确定");
        btnNeg.setText("取消");

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(dlg).setCancelable(false).create();
        if (dialog.getWindow() != null)
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        btnPos.setOnClickListener(v -> { dialog.dismiss(); pickImageLauncher.launch("image/*"); });
        btnNeg.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void removeAvatar() {
        getAvatarFile().delete();
        loadAvatar();
    }

    private void viewAvatar() {
        File file = getAvatarFile();
        if (!file.exists()) return;

        FrameLayout fl = new FrameLayout(requireContext());
        fl.setBackgroundColor(0xEE000000);

        ImageView iv = new ImageView(requireContext());
        iv.setImageURI(Uri.fromFile(file));
        iv.setScaleType(ImageView.ScaleType.FIT_CENTER);
        int pad = dp(8);
        iv.setPadding(pad, pad, pad, dp(80));
        FrameLayout.LayoutParams ivlp = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        fl.addView(iv, ivlp);

        // 关闭按钮
        TextView btnClose = new TextView(requireContext());
        btnClose.setText("✕");
        btnClose.setTextColor(0xFFFFFFFF);
        btnClose.setTextSize(22);
        btnClose.setGravity(Gravity.CENTER);
        btnClose.setBackgroundResource(R.drawable.bg_card);
        btnClose.setPadding(0, 0, 0, 0);
        FrameLayout.LayoutParams blp = new FrameLayout.LayoutParams(dp(40), dp(40));
        blp.gravity = Gravity.TOP | Gravity.END;
        blp.topMargin = dp(48);
        blp.rightMargin = dp(16);
        fl.addView(btnClose, blp);

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(fl)
                .setCancelable(true)
                .create();
        if (dialog.getWindow() != null)
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        btnClose.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private int dp(int d) { return (int) (d * getResources().getDisplayMetrics().density); }

    private void saveImageToFile(Uri uri) {
        File file = getAvatarFile();
        try (InputStream in = requireContext().getContentResolver().openInputStream(uri);
             FileOutputStream out = new FileOutputStream(file)) {
            byte[] buf = new byte[8192];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
        } catch (Exception ignored) {}
    }

    private void loadAvatar() {
        ivAvatar.setImageDrawable(null);
        ivAvatar.clearColorFilter();
        if (hasAvatar()) {
            ivAvatar.setImageURI(Uri.fromFile(getAvatarFile()));
            ivAvatar.setBackground(null);
        } else {
            ivAvatar.setBackgroundResource(R.drawable.bg_avatar_ring);
            ivAvatar.setImageResource(R.drawable.ic_gender);
            ivAvatar.setColorFilter(0xFFFFFFFF, android.graphics.PorterDuff.Mode.SRC_IN);
        }
    }

    private void loadUserInfo(View view) {
        User user = db.getUserById(session.getUserId());
        if (user != null) {
            ((TextView) view.findViewById(R.id.tv_profile_name)).setText(user.getName());
            ((TextView) view.findViewById(R.id.tv_info_gender)).setText(
                    !TextUtils.isEmpty(user.getGender()) ? user.getGender() : "未填写");
            ((TextView) view.findViewById(R.id.tv_info_age)).setText(
                    user.getAge() > 0 ? user.getAge() + " 岁" : "--");
            ((TextView) view.findViewById(R.id.tv_info_height)).setText(
                    user.getHeight() > 0 ? user.getHeight() + " cm" : "--");
            ((TextView) view.findViewById(R.id.tv_info_weight)).setText(
                    user.getWeight() > 0 ? user.getWeight() + " kg" : "--");

            // BMI 计算
            double h = user.getHeight();
            double w = user.getWeight();
            TextView tvBmi = view.findViewById(R.id.tv_bmi_value);
            TextView tvBmiDesc = view.findViewById(R.id.tv_bmi_desc);
            View barUnder = view.findViewById(R.id.bmi_bar_under);
            View barNormal = view.findViewById(R.id.bmi_bar_normal);
            View barOver = view.findViewById(R.id.bmi_bar_over);
            View barObese = view.findViewById(R.id.bmi_bar_obese);
            barUnder.setAlpha(0.35f); barNormal.setAlpha(0.35f);
            barOver.setAlpha(0.35f); barObese.setAlpha(0.35f);

            if (h > 0 && w > 0) {
                double bmi = w / ((h / 100) * (h / 100));
                tvBmi.setText(String.format("%.1f", bmi));
                if (bmi < 18.5) { tvBmiDesc.setText("偏瘦 · 建议增重"); barUnder.setAlpha(1.0f); }
                else if (bmi < 24) { tvBmiDesc.setText("正常 · 保持健康"); barNormal.setAlpha(1.0f); }
                else if (bmi < 28) { tvBmiDesc.setText("偏胖 · 注意饮食"); barOver.setAlpha(1.0f); }
                else { tvBmiDesc.setText("肥胖 · 建议减脂"); barObese.setAlpha(1.0f); }
            } else {
                tvBmi.setText("--");
                tvBmiDesc.setText("请填写身高体重");
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getView() != null) {
            loadAvatar();
            loadBackground();
            loadUserInfo(getView());
        }
    }

    // ==================== 背景图片 ====================

    private File getBackgroundFile() {
        return new File(requireContext().getFilesDir(), "background.jpg");
    }

    private boolean hasBackground() {
        return getBackgroundFile().exists();
    }

    private void onBackgroundClick() {
        if (!hasBackground()) {
            showBgPickConfirm();
            return;
        }

        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_background_options, null);

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        dialogView.findViewById(R.id.btn_remove).setOnClickListener(v -> {
            dialog.dismiss();
            removeBackground();
        });
        dialogView.findViewById(R.id.btn_view).setOnClickListener(v -> {
            dialog.dismiss();
            viewBackground();
        });
        dialogView.findViewById(R.id.btn_change).setOnClickListener(v -> {
            dialog.dismiss();
            showBgPickConfirm();
        });

        dialog.show();
    }

    private void showBgPickConfirm() {
        View dlg = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_confirm, null);
        ((ImageView) dlg.findViewById(R.id.dialog_icon))
                .setImageResource(android.R.drawable.ic_menu_gallery);
        ((TextView) dlg.findViewById(R.id.dialog_title)).setText("选择背景");
        ((TextView) dlg.findViewById(R.id.dialog_message)).setText("将从手机相册选择图片");
        TextView btnPos = dlg.findViewById(R.id.dialog_positive);
        TextView btnNeg = dlg.findViewById(R.id.dialog_negative);
        btnPos.setText("确定");
        btnNeg.setText("取消");

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(dlg).setCancelable(false).create();
        if (dialog.getWindow() != null)
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        btnPos.setOnClickListener(v -> { dialog.dismiss(); pickBgImageLauncher.launch("image/*"); });
        btnNeg.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void saveBackgroundToFile(Uri uri) {
        File file = getBackgroundFile();
        try (InputStream in = requireContext().getContentResolver().openInputStream(uri);
             FileOutputStream out = new FileOutputStream(file)) {
            byte[] buf = new byte[8192];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
        } catch (Exception ignored) {}
    }

    private void loadBackground() {
        if (hasBackground()) {
            int targetW = ivProfileBackground.getWidth();
            int targetH = ivProfileBackground.getHeight();
            if (targetW <= 0) {
                targetW = getResources().getDisplayMetrics().widthPixels;
                targetH = dp(280);
            }
            Bitmap blurred = decodeAndBlurBackground(getBackgroundFile(), targetW, targetH);
            if (blurred != null) {
                ivProfileBackground.setImageBitmap(blurred);
                ivProfileBackground.setVisibility(View.VISIBLE);
                viewHeaderGradient.setVisibility(View.GONE);
                tvFitcoreWatermark.setVisibility(View.GONE);
            }
        } else {
            ivProfileBackground.setImageDrawable(null);
            ivProfileBackground.setVisibility(View.GONE);
            viewHeaderGradient.setVisibility(View.VISIBLE);
            tvFitcoreWatermark.setVisibility(View.VISIBLE);
        }
    }

    private void removeBackground() {
        getBackgroundFile().delete();
        loadBackground();
    }

    private void viewBackground() {
        File file = getBackgroundFile();
        if (!file.exists()) return;

        FrameLayout fl = new FrameLayout(requireContext());
        fl.setBackgroundColor(0xEE000000);

        ImageView iv = new ImageView(requireContext());
        iv.setImageURI(Uri.fromFile(file));
        iv.setScaleType(ImageView.ScaleType.FIT_CENTER);
        int pad = dp(8);
        iv.setPadding(pad, pad, pad, dp(80));
        FrameLayout.LayoutParams ivlp = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        fl.addView(iv, ivlp);

        TextView btnClose = new TextView(requireContext());
        btnClose.setText("✕");
        btnClose.setTextColor(0xFFFFFFFF);
        btnClose.setTextSize(22);
        btnClose.setGravity(Gravity.CENTER);
        btnClose.setBackgroundResource(R.drawable.bg_card);
        btnClose.setPadding(0, 0, 0, 0);
        FrameLayout.LayoutParams blp = new FrameLayout.LayoutParams(dp(40), dp(40));
        blp.gravity = Gravity.TOP | Gravity.END;
        blp.topMargin = dp(48);
        blp.rightMargin = dp(16);
        fl.addView(btnClose, blp);

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(fl)
                .setCancelable(true)
                .create();
        if (dialog.getWindow() != null)
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        btnClose.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private Bitmap decodeAndBlurBackground(File file, int targetWidth, int targetHeight) {
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(file.getAbsolutePath(), opts);

        int sampleSize = 1;
        int maxDim = Math.max(opts.outWidth, opts.outHeight);
        while (maxDim / sampleSize > 800) {
            sampleSize *= 2;
        }
        opts.inSampleSize = sampleSize;
        opts.inJustDecodeBounds = false;

        Bitmap source = BitmapFactory.decodeFile(file.getAbsolutePath(), opts);
        if (source == null) return null;

        float aspect = (float) source.getWidth() / source.getHeight();
        int smallW = 250;
        int smallH = Math.round(smallW / aspect);
        if (smallH <= 0) smallH = 1;
        Bitmap small = Bitmap.createScaledBitmap(source, smallW, smallH, true);

        int outW = targetWidth > 0 ? targetWidth : source.getWidth();
        int outH = targetHeight > 0 ? targetHeight : source.getHeight();
        Bitmap blurred = Bitmap.createScaledBitmap(small, outW, outH, true);

        if (small != blurred && small != source) small.recycle();
        if (source != blurred) source.recycle();

        return blurred;
    }
}
