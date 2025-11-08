package com.example.myhabits.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.example.myhabits.database.dao.UserDao;
import com.example.myhabits.database.data.DBManager;

public class SplashActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        UserDao userDao = DBManager.getInstance(this).getUserDao();
        SharedPreferences sharedPreferences = getSharedPreferences("AppSecurity", MODE_PRIVATE);

        // Kiểm tra người dùng đã tồn tại chưa
        if (userDao.getFirstUser() == null) {
            startActivity(new Intent(this, UserSetupActivity.class));
        } else {
            // Kiểm tra có bật bảo mật không
            boolean isSecurityEnabled = sharedPreferences.getBoolean("security_enabled", false);

            if (isSecurityEnabled) {
                // Nếu có bảo mật, chuyển đến màn hình khóa
                startActivity(new Intent(this, LockScreenActivity.class));
            } else {
                // Nếu không có bảo mật, chuyển đến màn hình chính
                startActivity(new Intent(this, MainActivity.class));
            }
        }
        finish();
    }
}