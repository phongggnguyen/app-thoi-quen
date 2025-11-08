package com.example.myhabits.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.example.myhabits.R;
import com.example.myhabits.database.dao.UserDao;
import com.example.myhabits.database.data.DBManager;
import com.example.myhabits.models.User;
import com.example.myhabits.utils.SelectAvatarDialog;
import com.example.myhabits.utils.ToastUtils;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class ProfileUpdateActivity extends AppCompatActivity {
    private ImageView imgAvatar;
    private EditText edtName;
    private ImageButton btnUpdate;
    private ImageButton btnDelete;
    private FloatingActionButton fabChangeAvatar;
    private Toolbar toolbar;
    private Uri selectedImageUri;
    private UserDao userDao;
    private User currentUser;
    private boolean isDefaultAvatar = true;
    private TextView tvCreatedTime;

    private final ActivityResultLauncher<String> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    selectedImageUri = uri;
                    imgAvatar.setImageURI(uri);
                    isDefaultAvatar = false;
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_update);

        userDao = DBManager.getInstance(this).getUserDao();
        initViews();
        setupToolbar();
        loadCurrentUser();
        setupListeners();
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Cập nhật hồ sơ");
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }


    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        imgAvatar = findViewById(R.id.img_avatar);
        edtName = findViewById(R.id.edt_name);
        btnUpdate = findViewById(R.id.btn_update);
        fabChangeAvatar = findViewById(R.id.fab_change_avatar);
        btnDelete = findViewById(R.id.btn_delete);
        tvCreatedTime = findViewById(R.id.tvCreatedTime);
    }


    private void loadCurrentUser() {
        currentUser = userDao.getFirstUser();
        if (currentUser != null) {
            edtName.setText(currentUser.getName());
            String currentAvatarUri = currentUser.getAvatar();

            // Xử lý hiển thị avatar
            if (currentAvatarUri != null) {
                Uri uri = Uri.parse(currentAvatarUri);
                int resourceId = Integer.parseInt(uri.getLastPathSegment());
                imgAvatar.setImageResource(resourceId);
                selectedImageUri = uri;
            } else {
                imgAvatar.setImageResource(R.drawable.ic_profile);
                selectedImageUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.drawable.ic_profile);
            }

            // Hiển thị thời gian tạo
            long createdTime = currentUser.getCreatedAt();
            if (createdTime > 0) {
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault());
                tvCreatedTime.setText("Tạo lúc: " + sdf.format(new java.util.Date(createdTime)));
                tvCreatedTime.setVisibility(View.VISIBLE);
            } else {
                tvCreatedTime.setVisibility(View.GONE);
            }
        } else {
            ToastUtils.showToast(this, "Không thể tải thông tin người dùng");
            finish();
        }
    }

    private void setupListeners() {
        fabChangeAvatar.setOnClickListener(v -> showAvatarSelectionDialog());
        btnUpdate.setOnClickListener(v -> {
            if (validateInput()) {
                updateProfile();
            }
        });
        btnDelete.setOnClickListener(v -> showDeleteConfirmationDialog());
    }

    private void showAvatarSelectionDialog() {
        SelectAvatarDialog dialog = SelectAvatarDialog.newInstance();
        dialog.setOnAvatarSelectedListener(resourceId -> {
            imgAvatar.setImageResource(resourceId);
            selectedImageUri = Uri.parse("android.resource://" + getPackageName() + "/" + resourceId);
            isDefaultAvatar = false;
        });
        dialog.show(getSupportFragmentManager(), "select_avatar");
    }

    private boolean validateInput() {
        String name = edtName.getText().toString().trim();
        if (name.isEmpty()) {
            edtName.setError("Vui lòng nhập tên của bạn");
            return false;
        }
        return true;
    }


    private void updateProfile() {
        if (!validateInput()) return;

        if (currentUser != null) {
            String newName = edtName.getText().toString().trim();
            currentUser.setName(newName);
            currentUser.setAvatar(selectedImageUri.toString());

            int result = userDao.update(currentUser);
            if (result > 0) {
                ToastUtils.showToast(this, "Cập nhật thành công!");
                setResult(RESULT_OK);
                finish();
            } else {
                ToastUtils.showToast(this, "Cập nhật thất bại!");
            }
        }
    }

    private void showDeleteConfirmationDialog() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc chắn muốn xóa hồ sơ? Tất cả dữ liệu sẽ bị xóa và không thể khôi phục.")
                .setPositiveButton("Xóa", (dialog, which) -> deleteUserProfile())
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void deleteUserProfile() {
        if (currentUser != null) {
            try {
                // Xóa tất cả dữ liệu liên quan đến user
                DBManager dbManager = DBManager.getInstance(this);

                // Xóa tất cả habits
                dbManager.getHabitDao().deleteAll();

                // Xóa tất cả habit types
                dbManager.getHabitTypeDao().deleteAll();

                // Xóa user
                boolean isDeleted = dbManager.getUserDao().delete(currentUser.getId());

                if (isDeleted) {
                    // Chuyển về màn hình UserSetupActivity
                    Intent intent = new Intent(this, UserSetupActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                    ToastUtils.showToast(this, "Đã xóa hồ sơ thành công");
                } else {
                    Toast.makeText(this, "Không thể xóa hồ sơ", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Có lỗi xảy ra khi xóa hồ sơ", Toast.LENGTH_SHORT).show();
            }
        }
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}