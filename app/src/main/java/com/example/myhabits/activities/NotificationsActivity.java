package com.example.myhabits.activities;

import androidx.appcompat.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myhabits.R;
import com.example.myhabits.adapters.NotificationAdapter;
import com.example.myhabits.database.dao.HabitNotificationDao;
import com.example.myhabits.database.data.DBManager;
import com.example.myhabits.models.Habit;
import com.example.myhabits.models.HabitNotification;
import com.example.myhabits.utils.DateUtils;
import com.example.myhabits.utils.ToastUtils;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NotificationsActivity extends AppCompatActivity {
    private static final String TAG = "NotificationsActivity";
    private RecyclerView rvNotifications;
    private TextView tvEmpty;
    private NotificationAdapter adapter;
    private HabitNotificationDao notificationDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        initViews();
        setupToolbar();
        setupRecyclerView();
        loadNotifications();
    }

    private void initViews() {
        rvNotifications = findViewById(R.id.rvNotifications);
        tvEmpty = findViewById(R.id.tvEmpty);
        notificationDao = DBManager.getInstance(this).getHabitNotificationDao();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Thông báo");
        }
    }

    private void setupRecyclerView() {
        adapter = new NotificationAdapter(this);
        adapter.setOnNotificationClickListener(this::showNotificationDetail);

        adapter.setOnMoreClickListener(new NotificationAdapter.OnMoreClickListener() {
            @Override
            public void onDelete(HabitNotification notification) {
                new AlertDialog.Builder(NotificationsActivity.this)
                        .setTitle("Xác nhận xóa")
                        .setMessage("Bạn có chắc chắn muốn xóa thông báo này?")
                        .setPositiveButton("Xóa", (dialog, which) -> {
                            if (notificationDao.delete(notification.getId())) {
                                loadNotifications();
                                ToastUtils.showToast(NotificationsActivity.this, "Đã xóa thông báo");
                            }
                        })
                        .setNegativeButton("Hủy", null)
                        .show();
            }
        });

        rvNotifications.setLayoutManager(new LinearLayoutManager(this));
        rvNotifications.setAdapter(adapter);
    }

    private void loadNotifications() {
        try {
            List<HabitNotification> notifications = notificationDao.getAll();
            if (notifications.isEmpty()) {
                rvNotifications.setVisibility(View.GONE);
                tvEmpty.setVisibility(View.VISIBLE);
            } else {
                rvNotifications.setVisibility(View.VISIBLE);
                tvEmpty.setVisibility(View.GONE);
                adapter.setNotifications(notifications);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading notifications: " + e.getMessage());
            Toast.makeText(this, "Lỗi khi tải thông báo", Toast.LENGTH_SHORT).show();
        }
    }

    private void showNotificationDetail(HabitNotification notification) {
        try {
            View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_notification_detail, null);

            // Custom title layout với icon close
            View titleView = LayoutInflater.from(this).inflate(R.layout.dialog_title_with_close, null);
            TextView titleText = titleView.findViewById(R.id.dialogTitle);
            ImageView closeButton = titleView.findViewById(R.id.closeButton);

            // Set title từ notification
            titleText.setText(notification.getTitle());

            TextView tvContent = dialogView.findViewById(R.id.tvContent);
            TextView tvHabitTime = dialogView.findViewById(R.id.tvHabitTime);
            TextView tvDayNumber = dialogView.findViewById(R.id.tvDayNumber);
            TextView tvStartDate = dialogView.findViewById(R.id.tvStartDate); // Thêm reference

            // Set nội dung
            tvContent.setText(notification.getContent());

            // Lấy thông tin habit
            Habit habit = DBManager.getInstance(this).getHabitDao().getById(notification.getHabitId());
            if (habit != null) {
                // Hiển thị thời gian thực hiện
                String timeRange = String.format("%s - %s", habit.getStartTime(), habit.getEndTime());
                tvHabitTime.setText(timeRange);

                // Hiển thị ngày bắt đầu đã format
                SimpleDateFormat displayFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                try {
                    Date startDate = DateUtils.parseDate(habit.getStartDate());
                    tvStartDate.setText(displayFormat.format(startDate));
                } catch (Exception e) {
                    tvStartDate.setText(habit.getStartDate());
                }
            } else {
                tvHabitTime.setText("Không có thông tin thời gian");
                tvStartDate.setText("Không có thông tin");
            }

            // Set day number
            if (notification.getDayNumber() > 0) {
                tvDayNumber.setText(String.valueOf(notification.getDayNumber()));
            }

            AlertDialog dialog = new MaterialAlertDialogBuilder(this, R.style.MaterialAlertDialog_Rounded)
                    .setCustomTitle(titleView)
                    .setView(dialogView)
                    .create();

            // Xử lý sự kiện đóng dialog
            closeButton.setOnClickListener(v -> {
                try {
                    // Đánh dấu đã đọc
                    notification.setRead(true);
                    notificationDao.update(notification);
                    adapter.notifyDataSetChanged();

                    // Gửi broadcast để cập nhật badge
                    sendBroadcast(new Intent("NOTIFICATION_UPDATED"));

                    dialog.dismiss();
                } catch (Exception e) {
                    Log.e(TAG, "Error updating notification status: " + e.getMessage());
                    Toast.makeText(this, "Lỗi khi cập nhật trạng thái", Toast.LENGTH_SHORT).show();
                }
            });

            dialog.show();

        } catch (Exception e) {
            Log.e(TAG, "Error showing notification detail: " + e.getMessage());
            Toast.makeText(this, "Lỗi khi hiển thị chi tiết thông báo", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadNotifications();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(0, 0);
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, 0);
    }
}