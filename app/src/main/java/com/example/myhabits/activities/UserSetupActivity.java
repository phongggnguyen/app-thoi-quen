package com.example.myhabits.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.myhabits.R;
import com.example.myhabits.database.dao.UserDao;
import com.example.myhabits.database.data.DBManager;
import com.example.myhabits.models.User;
import com.example.myhabits.utils.SelectAvatarDialog;
import com.example.myhabits.utils.ToastUtils;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class UserSetupActivity extends AppCompatActivity {
    private static final int[] AVATAR_RESOURCES = {
            R.drawable.ic_profile,  // Ảnh mặc định
            R.drawable.avatar_1,
            R.drawable.avatar_2,
            R.drawable.avatar_3,
            R.drawable.avatar_4,
            R.drawable.avatar_5,

    };

    private ImageView imgAvatar;
    private EditText edtName;
    private Button btnStart;
    private FloatingActionButton fabChangeAvatar;
    private TextView tvWelcome, tvCreateProfile;
    private UserDao userDao;

    // Theo dõi ảnh đại diện được chọn
    private int selectedAvatarResource = R.drawable.ic_profile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_setup);

        // Khởi tạo UserDao
        userDao = DBManager.getInstance(this).getUserDao();
        if (userDao == null) {
            Toast.makeText(this, "Lỗi kết nối database", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        initViews();
        setupListeners();
    }

    private void initViews() {
        imgAvatar = findViewById(R.id.img_avatar);
        edtName = findViewById(R.id.edt_name);
        btnStart = findViewById(R.id.btn_start);
        fabChangeAvatar = findViewById(R.id.fab_change_avatar);
        tvWelcome = findViewById(R.id.tv_welcome);
        tvCreateProfile = findViewById(R.id.tv_create_profile);

        // Thiết lập ảnh đại diện mặc định
        imgAvatar.setImageResource(selectedAvatarResource);

        // Thiết lập icon cho nút chọn ảnh
        fabChangeAvatar.setImageResource(R.drawable.ic_image);
    }

    private void setupListeners() {
        // Xử lý sự kiện chọn ảnh đại diện
        fabChangeAvatar.setOnClickListener(v -> showAvatarSelectionDialog());

        // Xử lý sự kiện nút Bắt đầu
        btnStart.setOnClickListener(v -> {
            if (validateInput()) {
                saveUser();
            }
        });
    }

    private void showAvatarSelectionDialog() {
        SelectAvatarDialog dialog = SelectAvatarDialog.newInstance();
        dialog.setAvatarResources(AVATAR_RESOURCES);
        dialog.setOnAvatarSelectedListener(resourceId -> {
            selectedAvatarResource = resourceId;
            imgAvatar.setImageResource(resourceId);
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

    private void saveUser() {
        try {
            // Tạo đối tượng User mới
            User user = new User();
            user.setName(edtName.getText().toString().trim());

            // Tạo URI cho ảnh đại diện từ resource
            String avatarUri = "android.resource://" + getPackageName() + "/" + selectedAvatarResource;
            user.setAvatar(avatarUri);

            // Thiết lập thời gian tạo
            user.setCreatedAt(System.currentTimeMillis());

            // Lưu user vào database
            long userId = userDao.insert(user);

            if (userId > 0) {
                // Log thông tin user đã tạo
                logUserInfo(userId);

                // Thông báo thành công
                ToastUtils.showToast(this, "Đã tạo hồ sơ thành công!");

                // Chuyển đến MainActivity
                startMainActivity();
            } else {
                Toast.makeText(this, "Có lỗi xảy ra khi lưu thông tin", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Có lỗi xảy ra: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void logUserInfo(long userId) {
        User user = userDao.getById(userId);
        if (user != null) {
            String logMessage = String.format(
                    "User Info:\nID: %d\nName: %s\nAvatar: %s\nCreated At: %s",
                    user.getId(),
                    user.getName(),
                    user.getAvatar(),
                    new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss",
                            java.util.Locale.getDefault())
                            .format(new java.util.Date(user.getCreatedAt()))
            );
            android.util.Log.d("UserSetup", logMessage);
        }
    }

    private void startMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        moveTaskToBack(true);
    }
}