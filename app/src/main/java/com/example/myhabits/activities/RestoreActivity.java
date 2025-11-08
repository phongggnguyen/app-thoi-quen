package com.example.myhabits.activities;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.OpenableColumns;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.example.myhabits.R;
import com.example.myhabits.utils.BackupUtils;
import com.example.myhabits.utils.ToastUtils;

public class RestoreActivity extends AppCompatActivity {
    private TextView tvSelectedFile;
    private TextView tvRestoreStatus;
    private Button btnChooseFile;
    private Button btnRestore;
    private Button btnGuide;
    private Uri selectedFileUri;
    private Handler handler;

    private final ActivityResultLauncher<Intent> filePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == Activity.RESULT_OK &&
                                result.getData() != null) {
                            validateAndEnableRestore(result.getData().getData());
                        }
                    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restore);
        handler = new Handler(Looper.getMainLooper());

        initViews();
        setupToolbar();
        setupListeners();
    }

    private void initViews() {
        tvSelectedFile = findViewById(R.id.tvSelectedFile);
        tvRestoreStatus = findViewById(R.id.tvRestoreStatus);
        btnChooseFile = findViewById(R.id.btnChooseFile);
        btnRestore = findViewById(R.id.btnRestore);
        btnGuide = findViewById(R.id.btnGuide);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Khôi phục dữ liệu");
        }
    }

    private void setupListeners() {
        btnGuide.setOnClickListener(v -> GuideDialog.showRestoreGuide(this));

        btnChooseFile.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("*/*");
            filePickerLauncher.launch(intent);
        });

        btnRestore.setOnClickListener(v -> {
            if (selectedFileUri != null) {
                showRestoreConfirmation();
            }
        });
    }

    private void validateAndEnableRestore(Uri uri) {
        String fileName = getFileName(uri);
        if (fileName != null && BackupUtils.isValidBackupFile(fileName)) {
            selectedFileUri = uri;
            tvSelectedFile.setText("File đã chọn: " + fileName);
            btnRestore.setEnabled(true);
            tvRestoreStatus.setText("");
        } else {
            selectedFileUri = null;
            tvSelectedFile.setText("File không hợp lệ. Vui lòng chọn file backup hợp lệ");
            btnRestore.setEnabled(false);
            tvRestoreStatus.setText("");
        }
    }

    private String getFileName(Uri uri) {
        String fileName = null;
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
            if (nameIndex != -1) {
                fileName = cursor.getString(nameIndex);
            }
            cursor.close();
        }
        return fileName;
    }

    private void showRestoreConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận khôi phục")
                .setMessage("Dữ liệu hiện tại sẽ bị thay thế hoàn toàn. " +
                        "Bạn có chắc chắn muốn khôi phục?")
                .setPositiveButton("Đồng ý", (dialog, which) -> performRestore())
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void performRestore() {
        btnRestore.setEnabled(false);
        btnChooseFile.setEnabled(false);
        tvRestoreStatus.setText("Đang khôi phục...");

        new Thread(() -> {
            try {
                boolean success = BackupUtils.restoreBackup(this, selectedFileUri);
                handler.post(() -> {
                    if (success) {
                        tvRestoreStatus.setText("Khôi phục thành công!");
                        ToastUtils.showToast(this, "Khôi phục thành công! Ứng dụng sẽ tự động thoát.");

                        new Handler().postDelayed(() -> {
                            // Đóng tất cả activities và kill process
                            finishAffinity();
                            System.exit(0);
                        }, 2000);
                    } else {
                        btnRestore.setEnabled(true);
                        btnChooseFile.setEnabled(true);
                        tvRestoreStatus.setText("Khôi phục thất bại!");
                        Toast.makeText(this, "Khôi phục thất bại!", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception e) {
                Log.e("RestoreActivity", "Error during restore: " + e.getMessage());
                handler.post(() -> {
                    btnRestore.setEnabled(true);
                    btnChooseFile.setEnabled(true);
                    tvRestoreStatus.setText("Khôi phục thất bại: " + e.getMessage());
                    Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }
}