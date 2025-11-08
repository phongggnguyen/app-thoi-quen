package com.example.myhabits.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myhabits.R;
import com.example.myhabits.adapters.HabitAdapter;
import com.example.myhabits.database.dao.DailyStatusDao;
import com.example.myhabits.database.data.DBManager;
import com.example.myhabits.database.dao.HabitDao;
import com.example.myhabits.database.dao.UserDao;
import com.example.myhabits.database.data.DatabaseConstants;
import com.example.myhabits.models.DailyStatus;
import com.example.myhabits.models.Habit;
import com.example.myhabits.models.User;
import com.example.myhabits.utils.DateUtils;
import com.example.myhabits.utils.ToastUtils;

import java.util.List;

public class ListHabit extends AppCompatActivity {
    private static final String TAG = "ListHabit";
    private static final int REQUEST_CODE_DETAIL = 100;
    private static final int REQUEST_CODE_ADD = 101;

    private RecyclerView rvHabits;
    private TextView tvNoHabits;
    private ImageView btnAdd;
    private HabitAdapter habitAdapter;
    private HabitDao habitDao;
    private UserDao userDao;
    private DBManager dbManager;
    private TextView tvTotalHabits;
    private TextView tvCompletedHabits;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_habit);

        // Khởi tạo DBManager và các DAO
        dbManager = DBManager.getInstance(this);
        habitDao = dbManager.getHabitDao();
        userDao = dbManager.getUserDao();

        if (dbManager != null) {
            Log.d(TAG, "DBManager initialized successfully");
        } else {
            Log.e(TAG, "DBManager initialization failed");
            ToastUtils.showToast(this, "Lỗi kết nối database");
            finish();
            return;
        }

        setupToolbar();
        initViews();
        loadHabits();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Danh sách thói quen");
        }

        btnAdd = findViewById(R.id.btnAdd);
        btnAdd.setOnClickListener(v -> {
            Intent intent = new Intent(ListHabit.this, AddHabit.class);
            startActivityForResult(intent, REQUEST_CODE_ADD);
        });
    }

    private void initViews() {
        rvHabits = findViewById(R.id.rvHabits);
        tvNoHabits = findViewById(R.id.tvNoHabits);
        tvTotalHabits = findViewById(R.id.tvTotalHabits);
        tvCompletedHabits = findViewById(R.id.tvCompletedHabits);

        rvHabits.setLayoutManager(new LinearLayoutManager(this));
        habitAdapter = new HabitAdapter(this);
        rvHabits.setAdapter(habitAdapter);
    }

    private void loadHabits() {
        if (userDao == null || habitDao == null) {
            Log.e(TAG, "DAOs not initialized");
            ToastUtils.showToast(this, "Lỗi: Không thể kết nối database");
            return;
        }

        User currentUser = userDao.getFirstUser();
        if (currentUser != null) {
            Log.d(TAG, "Loading habits for user: " + currentUser.getId());
            List<Habit> habits = habitDao.getByUserId(currentUser.getId());

            if (habits != null && !habits.isEmpty()) {
                Log.d(TAG, "Found " + habits.size() + " habits");
                tvNoHabits.setVisibility(View.GONE);
                rvHabits.setVisibility(View.VISIBLE);

                int completedCount = 0;
                DailyStatusDao dailyStatusDao = dbManager.getDailyStatusDao();

                for (Habit habit : habits) {
                    int targetDays = habit.getTargetDays();
                    int completedBoxes = 0;
                    List<String> daysToCheck = DateUtils.getDailyDaysWithTarget(habit.getStartDate(), targetDays);

                    // Đếm số ngày đã hoàn thành
                    for (String date : daysToCheck) {
                        DailyStatus status = dailyStatusDao.getByHabitIdAndDate(habit.getId(), date);
                        if (status != null && status.getStatus() == DatabaseConstants.CHECK_STATUS_COMPLETED) {
                            completedBoxes++;
                        }
                    }

                    // Kiểm tra và cập nhật trạng thái hoàn thành
                    int progressPercent = (completedBoxes * 100) / targetDays;
                    if (progressPercent == 100) {
                        completedCount++;
                        // Cập nhật trạng thái nếu chưa được cập nhật
                        if (habit.getStatus() != 1) {
                            habit.setStatus(1);
                            habitDao.update(habit);
                        }
                    }
                }

                habitAdapter.setHabits(habits);
                updateHabitsCount(habits.size(), completedCount);

            } else {
                Log.d(TAG, "No habits found");
                tvNoHabits.setVisibility(View.VISIBLE);
                rvHabits.setVisibility(View.GONE);
                updateHabitsCount(0, 0);
            }
        } else {
            Log.e(TAG, "No user found");
            ToastUtils.showToast(this, "Lỗi: Không tìm thấy thông tin người dùng");
            tvNoHabits.setVisibility(View.VISIBLE);
            rvHabits.setVisibility(View.GONE);
            updateHabitsCount(0, 0);
        }
    }

    private void updateHabitsCount(int totalCount, int completedCount) {
        if (tvTotalHabits != null && tvCompletedHabits != null) {
            if (totalCount == 0) {
                tvTotalHabits.setVisibility(View.GONE);
                tvCompletedHabits.setVisibility(View.GONE);
            } else {
                tvTotalHabits.setVisibility(View.VISIBLE);
                tvCompletedHabits.setVisibility(View.VISIBLE);
                tvTotalHabits.setText("Tổng: " + totalCount + " thói quen");
                tvCompletedHabits.setText("Hoàn thành: " + completedCount + " thói quen");
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_CODE_DETAIL && data != null &&
                    data.getBooleanExtra("refresh_needed", false)) {
                Log.d(TAG, "Reloading habits after delete");
                loadHabits();
            } else if (requestCode == REQUEST_CODE_ADD) {
                Log.d(TAG, "Reloading habits after add");
                loadHabits();
                ToastUtils.showToast(this, "Thêm thói quen thành công");
            }
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
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadHabits();
    }
}