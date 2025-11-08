package com.example.myhabits.activities;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.example.myhabits.R;
import com.example.myhabits.utils.BackupUtils;
import com.example.myhabits.utils.ToastUtils;

public class BackupActivity extends AppCompatActivity {
    private TextView tvBackupPath;
    private TextView tvBackupStatus;
    private Button btnBackup;
    private Button btnGuide;
    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_backup);
        handler = new Handler(Looper.getMainLooper());

        initViews();
        setupToolbar();
        setupListeners();
        updateBackupPath();
    }

    private void initViews() {
        tvBackupPath = findViewById(R.id.tvBackupPath);
        tvBackupStatus = findViewById(R.id.tvBackupStatus);
        btnBackup = findViewById(R.id.btnBackup);
        btnGuide = findViewById(R.id.btnGuide);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Sao lưu dữ liệu");
        }
    }

    private void setupListeners() {
        btnGuide.setOnClickListener(v -> GuideDialog.showBackupGuide(this));

        btnBackup.setOnClickListener(v -> {
            btnBackup.setEnabled(false);
            tvBackupStatus.setText("Đang sao lưu...");

            new Thread(() -> {
                String backupPath = BackupUtils.createBackup(this);
                handler.post(() -> {
                    btnBackup.setEnabled(true);
                    if (backupPath != null) {
                        tvBackupStatus.setText("Sao lưu thành công: " + backupPath);
                        ToastUtils.showToast(this, "Sao lưu thành công!");
                    } else {
                        tvBackupStatus.setText("Sao lưu thất bại!");
                        ToastUtils.showToast(this, "Sao lưu thất bại!");
                    }
                });
            }).start();
        });
    }

    private void updateBackupPath() {
        tvBackupPath.setText("Vị trí: " + BackupUtils.getBackupFolder().getAbsolutePath());
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}