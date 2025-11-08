package com.example.myhabits.activities;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.myhabits.R;
import com.example.myhabits.utils.ToastUtils;

public class PasswordSetting extends AppCompatActivity {
    private Switch switchSecurity;
    private Button btnChangePassword;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password_setting);

        setupToolbar();
        initViews();
        loadSecurityStatus();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Cài đặt bảo mật");
        }
    }

    private void initViews() {
        sharedPreferences = getSharedPreferences("AppSecurity", MODE_PRIVATE);
        switchSecurity = findViewById(R.id.switchSecurity);
        btnChangePassword = findViewById(R.id.btnChangePassword);

        switchSecurity.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked && !sharedPreferences.getBoolean("security_enabled", false)) {
                // Chỉ hiển thị dialog tạo mật khẩu khi chuyển từ tắt sang bật
                showSetPasswordDialog();
            } else if (!isChecked) {
                // Tắt bảo mật
                sharedPreferences.edit()
                        .putBoolean("security_enabled", false)
                        .putString("password", "")
                        .apply();
                updateUI(false);
            }
        });

        btnChangePassword.setOnClickListener(v -> showChangePasswordDialog());
    }

    private void showChangePasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_change_password, null);

        EditText etCurrentPassword = view.findViewById(R.id.etCurrentPassword);
        EditText etNewPassword = view.findViewById(R.id.etNewPassword);
        EditText etConfirmNewPassword = view.findViewById(R.id.etConfirmNewPassword);

        builder.setView(view)
                .setTitle("Đổi mật khẩu")
                .setPositiveButton("Xác nhận", null)
                .setNegativeButton("Hủy", null);

        AlertDialog dialog = builder.create();
        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String currentPassword = etCurrentPassword.getText().toString();
            String newPassword = etNewPassword.getText().toString();
            String confirmPassword = etConfirmNewPassword.getText().toString();
            String savedPassword = sharedPreferences.getString("password", "");

            if (!currentPassword.equals(savedPassword)) {
                etCurrentPassword.setError("Mật khẩu hiện tại không đúng");
                return;
            }

            if (newPassword.length() != 4) {
                etNewPassword.setError("Mật khẩu phải có 4 số");
                return;
            }

            if (!newPassword.equals(confirmPassword)) {
                etConfirmNewPassword.setError("Mật khẩu không khớp");
                return;
            }

            // Lưu mật khẩu mới
            sharedPreferences.edit()
                    .putString("password", newPassword)
                    .apply();

            dialog.dismiss();
            ToastUtils.showToast(this, "Đổi mật khẩu thành công");
        });
    }

    private void showSetPasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_set_password, null);
        EditText etPassword = view.findViewById(R.id.etPassword);
        EditText etConfirmPassword = view.findViewById(R.id.etConfirmPassword);

        builder.setView(view)
                .setTitle("Tạo mật khẩu")
                .setPositiveButton("Xác nhận", null)
                .setNegativeButton("Hủy", (dialog, which) -> {
                    switchSecurity.setChecked(false);
                });

        AlertDialog dialog = builder.create();
        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String password = etPassword.getText().toString();
            String confirmPassword = etConfirmPassword.getText().toString();

            if (password.length() != 4) {
                etPassword.setError("Mật khẩu phải có 4 số");
                return;
            }

            if (!password.equals(confirmPassword)) {
                etConfirmPassword.setError("Mật khẩu không khớp");
                return;
            }

            // Lưu mật khẩu và bật bảo mật
            sharedPreferences.edit()
                    .putBoolean("security_enabled", true)
                    .putString("password", password)
                    .apply();

            dialog.dismiss();
            updateUI(true);
            ToastUtils.showToast(this, "Đã thiết lập mật khẩu");
        });
    }

    private void loadSecurityStatus() {
        boolean isSecurityEnabled = sharedPreferences.getBoolean("security_enabled", false);
        switchSecurity.setChecked(isSecurityEnabled);
        updateUI(isSecurityEnabled);
    }

    private void updateUI(boolean isSecurityEnabled) {
        btnChangePassword.setVisibility(isSecurityEnabled ? View.VISIBLE : View.GONE);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}