package com.example.myhabits.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myhabits.R;
import com.example.myhabits.utils.ToastUtils;

public class LockScreenActivity extends AppCompatActivity {
    private EditText etPassword;
    private Button btnUnlock;
    private SharedPreferences sharedPreferences;
    private int maxAttempts = 3; // Số lần nhập tối đa
    private int currentAttempts = 0; // Số lần đã nhập

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Kiểm tra xem có bật bảo mật không
        sharedPreferences = getSharedPreferences("AppSecurity", MODE_PRIVATE);
        boolean isSecurityEnabled = sharedPreferences.getBoolean("security_enabled", false);

        // Nếu không bật bảo mật, chuyển thẳng vào MainActivity
        if (!isSecurityEnabled) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_lock_screen);
        initViews();
    }

    private void initViews() {
        etPassword = findViewById(R.id.etPassword);
        btnUnlock = findViewById(R.id.btnUnlock);

        // Thiết lập button mở khóa
        btnUnlock.setOnClickListener(v -> {
            String password = etPassword.getText().toString();
            if (password.length() < 4) {
                etPassword.setError("Vui lòng nhập đủ 4 số");
                return;
            }
            validatePassword();
        });

        // Đặt focus vào ô nhập mật khẩu
        etPassword.requestFocus();
    }

    private void validatePassword() {
        String savedPassword = sharedPreferences.getString("password", "");
        String inputPassword = etPassword.getText().toString();

        if (inputPassword.equals(savedPassword)) {
            // Mật khẩu đúng
            unlockSuccess();
        } else {
            // Mật khẩu sai
            handleWrongPassword();
        }
    }

    private void unlockSuccess() {
        // Reset số lần thử
        currentAttempts = 0;

        // Chuyển đến MainActivity
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();

        ToastUtils.showToast(this, "Mở khóa thành công");
    }

    private void handleWrongPassword() {
        currentAttempts++;
        etPassword.setError("Mật khẩu không đúng");
        etPassword.setText("");

        if (currentAttempts >= maxAttempts) {
            // Đã vượt quá số lần thử cho phép
            handleMaxAttemptsReached();
        } else {
            // Còn cơ hội thử
            int remainingAttempts = maxAttempts - currentAttempts;
            ToastUtils.showToast(this,
                    "Mật khẩu không đúng. Còn " + remainingAttempts + " lần thử");
        }
    }

    private void handleMaxAttemptsReached() {
        ToastUtils.showToast(this,
                "Bạn đã nhập sai quá số lần cho phép. Ứng dụng sẽ thoát.");

        // Delay 2 giây trước khi thoát
        etPassword.postDelayed(() -> {
            // Thoát ứng dụng
            finishAffinity();
        }, 2000);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reset mật khẩu đã nhập khi quay lại màn hình
        etPassword.setText("");
        currentAttempts = 0;
    }

    @Override
    public void onBackPressed() {
        ToastUtils.showToast(this, "Thoát ứng dụng");
        super.onBackPressed();
    }
}