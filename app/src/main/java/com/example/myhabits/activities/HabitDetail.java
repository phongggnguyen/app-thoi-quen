package com.example.myhabits.activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;
import androidx.appcompat.app.AppCompatActivity;
import com.example.myhabits.R;
import com.example.myhabits.database.dao.DailyStatusDao;
import com.example.myhabits.database.dao.HabitDao;
import com.example.myhabits.database.dao.HabitTypeDao;
import com.example.myhabits.database.data.DBManager;
import com.example.myhabits.database.data.DatabaseConstants;
import com.example.myhabits.models.DailyStatus;
import com.example.myhabits.models.Habit;
import com.example.myhabits.models.HabitType;
import com.example.myhabits.utils.DateUtils;
import com.example.myhabits.utils.ToastUtils;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HabitDetail extends AppCompatActivity {
    private static final String TAG = "HabitDetail";
    private static final String EXTRA_HABIT_ID = "habit_id";
    private static final int DELETE_RESULT_CODE = 100;

    private HabitDao habitDao;
    private HabitTypeDao habitTypeDao;
    private Habit habit;
    private HabitType habitType;
    private GridLayout checkboxContainer;
    private DailyStatusDao dailyStatusDao;

    public static void start(Context context, long habitId) {
        Intent intent = new Intent(context, HabitDetail.class);
        intent.putExtra(EXTRA_HABIT_ID, habitId);
        ((AppCompatActivity) context).startActivityForResult(intent, DELETE_RESULT_CODE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_habit_detail);

        initDatabase();

        initViews();

        loadData();
    }

    private void initDatabase() {
        DBManager dbManager = DBManager.getInstance(this);
        habitDao = dbManager.getHabitDao();
        habitTypeDao = dbManager.getHabitTypeDao();
        dailyStatusDao = dbManager.getDailyStatusDao();
    }

    private void initViews() {
        // Init các view hiện tại
        ImageView btnDelete = findViewById(R.id.btnDelete);
        btnDelete.setOnClickListener(v -> showDeleteConfirmDialog());
        setupToolbar();

        checkboxContainer = findViewById(R.id.checkboxContainer);
    }

    private void loadData() {
        long habitId = getIntent().getLongExtra(EXTRA_HABIT_ID, -1);
        if (habitId == -1) {
            ToastUtils.showToast(this, "Không tìm thấy thói quen");
            finish();
            return;
        }

        habit = habitDao.getById(habitId);
        if (habit == null) {
            ToastUtils.showToast(this, "Không tìm thấy thông tin thói quen");
            finish();
            return;
        }

        habitType = habitTypeDao.getById(habit.getTypeId());
        if (habitType == null) {
            Log.e(TAG, "Không tìm thấy danh mục của thói quen");
        }

        setupViews();
        setupCheckboxes();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Chi tiết thói quen");
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupViews() {
        TextView tvHabitName = findViewById(R.id.tvHabitName);
        TextView tvCategory = findViewById(R.id.tvCategory);
        TextView tvTime = findViewById(R.id.tvTime);
        TextView tvStartDate = findViewById(R.id.tvStartDate);
        TextView tvTargetDays = findViewById(R.id.tvTargetDays);
        TextView tvStatus = findViewById(R.id.tvStatus);
        TextView tvReminder = findViewById(R.id.tvReminder);

        tvHabitName.setText(habit.getName());

        if (habitType != null) {
            tvCategory.setText(habitType.getName());
        } else {
            tvCategory.setText("Không có danh mục");
        }

        String timeText = String.format("%s - %s", habit.getStartTime(), habit.getEndTime());
        tvTime.setText(timeText);

        tvTargetDays.setText(String.format("%d ngày", habit.getTargetDays()));

        String reminderText = formatReminderTime(habit.getReminderMinutes());
        tvReminder.setText(reminderText);

        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            Date date = inputFormat.parse(habit.getStartDate());
            tvStartDate.setText(outputFormat.format(date));
        } catch (Exception e) {
            Log.e(TAG, "Error formatting date: " + e.getMessage());
            tvStartDate.setText(habit.getStartDate());
        }

        String statusText;
        int statusColor;

        if (habit.getStatus() == 1) {
            statusText = "Đã hoàn thành";
            statusColor = Color.parseColor("#4CAF50");
        } else {
            statusText = "Chưa hoàn thành";
            statusColor = Color.parseColor("#FFA500");
        }

        tvStatus.setText(statusText);
        tvStatus.setTextColor(statusColor);

        TextView tvProgress = findViewById(R.id.tvProgress);
        ProgressBar progressBar = findViewById(R.id.progressBar);
        calculateProgress(habit, tvProgress, progressBar);
    }
    private void setupCheckboxes() {
        checkboxContainer.removeAllViews();

        List<String> daysToShow = DateUtils.getDailyDaysWithTarget(habit.getStartDate(), habit.getTargetDays());

        int numRows = (daysToShow.size() + 4) / 5;
        checkboxContainer.setRowCount(numRows);

        for (int i = 0; i < daysToShow.size(); i++) {
            String date = daysToShow.get(i);
            View checkboxView = createCheckboxView(date);

            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 0;
            params.height = GridLayout.LayoutParams.WRAP_CONTENT;
            params.columnSpec = GridLayout.spec(i % 5, 1f);
            params.rowSpec = GridLayout.spec(i / 5);
            params.setMargins(8, 8, 8, 8);

            checkboxView.setLayoutParams(params);
            checkboxContainer.addView(checkboxView);
        }
    }

    private View createCheckboxView(String date) {
        View view = LayoutInflater.from(this).inflate(R.layout.checkbox_day_item, null);
        TextView tvDay = view.findViewById(R.id.tvDay);
        CheckBox checkbox = view.findViewById(R.id.checkbox);

        DailyStatus status = dailyStatusDao.getByHabitIdAndDate(habit.getId(), date);
        int statusCode = status != null ? status.getStatus() : DatabaseConstants.CHECK_STATUS_PENDING;

        String dayText = "Ngày " + DateUtils.getDayNumberFromStartDate(habit.getStartDate(), date);
        tvDay.setText(dayText);

        if (statusCode == DatabaseConstants.CHECK_STATUS_COMPLETED) {
            checkbox.setChecked(true);
            checkbox.setButtonTintList(ColorStateList.valueOf(getColor(R.color.checkGreen)));
        } else if (statusCode == DatabaseConstants.CHECK_STATUS_MISSED) {
            checkbox.setChecked(true);
            checkbox.setButtonTintList(ColorStateList.valueOf(getColor(R.color.checkRed)));
        } else {
            checkbox.setChecked(false);
            checkbox.setButtonTintList(ColorStateList.valueOf(getColor(R.color.checkNormal)));
        }

        checkbox.setEnabled(false);

        return view;
    }

    private String formatReminderTime(int minutes) {
        if (minutes >= 60) {
            int hours = minutes / 60;
            return String.format("Nhắc nhở trước %d giờ", hours);
        } else {
            return String.format("Nhắc nhở trước %d phút", minutes);
        }
    }

    private void calculateProgress(Habit habit, TextView tvProgress, ProgressBar progressBar) {
        try {
            DailyStatusDao dailyStatusDao = DBManager.getInstance(this).getDailyStatusDao();

            int targetDays = habit.getTargetDays();
            int completedDays = 0;

            // Lấy danh sách các ngày cần kiểm tra từ ngày bắt đầu
            List<String> daysToCheck = DateUtils.getDailyDaysWithTarget(habit.getStartDate(), targetDays);

            // Đếm số ngày đã hoàn thành
            for (String date : daysToCheck) {
                DailyStatus status = dailyStatusDao.getByHabitIdAndDate(habit.getId(), date);
                if (status != null && status.getStatus() == DatabaseConstants.CHECK_STATUS_COMPLETED) {
                    completedDays++;
                }
            }

            // Tính toán và hiển thị tiến độ
            int progressPercent = targetDays > 0 ? (completedDays * 100) / targetDays : 0;
            tvProgress.setText(String.format("%d%% (%d/%d)", progressPercent, completedDays, targetDays));
            progressBar.setProgress(progressPercent);

            // Cập nhật trạng thái nếu đã hoàn thành
            if (progressPercent == 100 && habit.getStatus() != 1) {
                habit.setStatus(1);
                habitDao.update(habit);
                TextView tvStatus = findViewById(R.id.tvStatus);
                tvStatus.setText("Đã hoàn thành");
                tvStatus.setTextColor(Color.parseColor("#4CAF50"));
            }

        } catch (Exception e) {
            Log.e(TAG, "Error calculating progress: " + e.getMessage());
            tvProgress.setText("0%");
            progressBar.setProgress(0);
        }
    }

    private void showDeleteConfirmDialog() {
        String message;
        if (habit.getStatus() == 0) {
            message = "Thói quen vẫn chưa hoàn thành. Bạn chắc chắn xóa?";
        } else {
            message = "Khi xóa thói quen, các thông tin liên quan của thói quen đều bị xóa, " +
                    "bao gồm tất cả báo cáo thống kê. Bạn chắc chắn muốn xóa?";
        }

        new AlertDialog.Builder(this)
                .setTitle("Xác nhận xóa")
                .setMessage(message)
                .setPositiveButton("Xóa", (dialog, which) -> deleteHabit())
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void deleteHabit() {
        if (habit != null) {
            boolean deleted = habitDao.delete(habit.getId());
            if (deleted) {
                Log.d(TAG, "Habit deleted successfully: " + habit.getId());
                Toast.makeText(this, "Xóa thói quen thành công", Toast.LENGTH_SHORT).show();

                Intent resultIntent = new Intent();
                resultIntent.putExtra("refresh_needed", true);
                setResult(RESULT_OK, resultIntent);
                finish();
            } else {
                Log.e(TAG, "Failed to delete habit: " + habit.getId());
                Toast.makeText(this, "Xóa thói quen thất bại", Toast.LENGTH_SHORT).show();
            }
        }
    }
}