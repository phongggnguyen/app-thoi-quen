package com.example.myhabits.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.example.myhabits.utils.NotificationScheduler;
import com.example.myhabits.R;
import com.example.myhabits.database.dao.HabitDao;
import com.example.myhabits.database.dao.HabitTypeDao;
import com.example.myhabits.database.dao.UserDao;
import com.example.myhabits.database.data.DBManager;
import com.example.myhabits.models.Habit;
import com.example.myhabits.models.HabitType;
import com.example.myhabits.models.User;
import com.example.myhabits.utils.IconSpan;
import com.example.myhabits.utils.ToastUtils;
import com.google.android.material.datepicker.MaterialDatePicker;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AddHabit extends AppCompatActivity {
    private static final String TAG = "AddHabit";

    // UI Components
    private Toolbar toolbar;
    private TextView edtHabitName;
    private View viewCategoryColor;
    private TextView tvSelectedCategory;
    private LinearLayout llCategorySelector;
    private TextView tvStartDate;
    private LinearLayout llStartDate;
    private LinearLayout llStartTime, llEndTime;
    private Button btnSave;
    private EditText edtStartHour, edtStartMinute, edtEndHour, edtEndMinute;
    private EditText edtTargetDays;
    private TextView tvSelectedReminder;
    private LinearLayout llReminderSelector;
    private NotificationScheduler notificationScheduler;

    // Database components
    private HabitTypeDao habitTypeDao;
    private HabitDao habitDao;
    private UserDao userDao;
    private DBManager dbManager;

    // Data holders
    private HabitType selectedCategory;
    private Calendar selectedDate;
    private int selectedReminderMinutes = 30; // Default 30 minutes

    // Reminder options in minutes
    private final int[] REMINDER_OPTIONS = {5, 10, 15, 30, 60, 120};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_habit);

        // Initialize database manager
        dbManager = DBManager.getInstance(this);
        if (dbManager == null) {
            Log.e(TAG, "Failed to initialize DBManager");
            ToastUtils.showToast(this, "Lỗi khởi tạo database");
            finish();
            return;
        }

        // Khởi tạo NotificationScheduler
        notificationScheduler = new NotificationScheduler(this);

        initViews();
        setupToolbar();
        setupListeners();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        edtHabitName = findViewById(R.id.edt_habit_name);
        viewCategoryColor = findViewById(R.id.view_category_color);
        tvSelectedCategory = findViewById(R.id.tv_selected_category);
        llCategorySelector = findViewById(R.id.ll_category_selector);
        tvStartDate = findViewById(R.id.tv_start_date);
        llStartDate = findViewById(R.id.ll_start_date);
        llStartTime = findViewById(R.id.ll_start_time);
        llEndTime = findViewById(R.id.ll_end_time);
        btnSave = findViewById(R.id.btn_save);

        edtStartHour = findViewById(R.id.edt_start_hour);
        edtStartMinute = findViewById(R.id.edt_start_minute);
        edtEndHour = findViewById(R.id.edt_end_hour);
        edtEndMinute = findViewById(R.id.edt_end_minute);

        edtTargetDays = findViewById(R.id.edt_target_days);
        edtTargetDays.setText("15");

        tvSelectedReminder = findViewById(R.id.tv_selected_reminder);
        llReminderSelector = findViewById(R.id.ll_reminder_selector);

        // Initialize DAOs
        habitTypeDao = dbManager.getHabitTypeDao();
        habitDao = dbManager.getHabitDao();
        userDao = dbManager.getUserDao();

        if (habitTypeDao == null || habitDao == null || userDao == null) {
            Log.e(TAG, "Failed to initialize DAOs");
            ToastUtils.showToast(this, "Lỗi khởi tạo DAO");
            finish();
        }

        // Set default reminder
        updateReminderDisplay();
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Thêm thói quen");
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupListeners() {
        llCategorySelector.setOnClickListener(v -> showCategoryDialog());
        llStartDate.setOnClickListener(v -> showDatePicker());
        llStartTime.setOnClickListener(v -> showTimePicker(true));
        llEndTime.setOnClickListener(v -> showTimePicker(false));
        llReminderSelector.setOnClickListener(v -> showReminderDialog());
        btnSave.setOnClickListener(v -> {
            if (validateInput()) {
                saveHabit();
            }
        });
    }

    private void showReminderDialog() {
        View menuAnchor = findViewById(R.id.ll_reminder_selector);
        PopupMenu popup = new PopupMenu(this, menuAnchor);
        Menu menu = popup.getMenu();

        for (int i = 0; i < REMINDER_OPTIONS.length; i++) {
            int minutes = REMINDER_OPTIONS[i];
            String text = formatReminderTime(minutes);
            menu.add(Menu.NONE, i, Menu.NONE, text);
        }

        popup.setOnMenuItemClickListener(item -> {
            selectedReminderMinutes = REMINDER_OPTIONS[item.getItemId()];
            updateReminderDisplay();
            return true;
        });

        popup.show();
    }

    private String formatReminderTime(int minutes) {
        if (minutes >= 60) {
            int hours = minutes / 60;
            return hours + " giờ";
        } else {
            return minutes + " phút";
        }
    }

    private void updateReminderDisplay() {
        tvSelectedReminder.setText(formatReminderTime(selectedReminderMinutes));
    }

    private void showDatePicker() {
        Calendar minDate = Calendar.getInstance();
        MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Chọn ngày bắt đầu")
                .setSelection(minDate.getTimeInMillis())
                .build();

        datePicker.addOnPositiveButtonClickListener(selection -> {
            selectedDate = Calendar.getInstance();
            selectedDate.setTimeInMillis(selection);
            updateDateDisplay();

            edtStartHour.setText("");
            edtStartMinute.setText("");
            edtEndHour.setText("");
            edtEndMinute.setText("");
        });

        datePicker.show(getSupportFragmentManager(), "date_picker");
    }

    private void showTimePicker(boolean isStartTime) {
        final Calendar current = Calendar.getInstance();
        final Calendar selectedDate = this.selectedDate;

        if (selectedDate == null) {
            ToastUtils.showToast(this, "Vui lòng chọn ngày bắt đầu trước");
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_time_picker, null);
        builder.setView(dialogView);

        final EditText edtHour = dialogView.findViewById(R.id.edt_hour);
        final EditText edtMinute = dialogView.findViewById(R.id.edt_minute);

        builder.setTitle(isStartTime ? "Chọn giờ bắt đầu" : "Chọn giờ kết thúc");
        builder.setPositiveButton("OK", (dialog, which) -> {
            String hourStr = edtHour.getText().toString();
            String minuteStr = edtMinute.getText().toString();

            if (TextUtils.isEmpty(hourStr) || TextUtils.isEmpty(minuteStr)) {
                ToastUtils.showToast(this, "Vui lòng nhập đầy đủ giờ và phút");
                return;
            }

            int hour = Integer.parseInt(hourStr);
            int minute = Integer.parseInt(minuteStr);

            if (hour < 0 || hour > 23 || minute < 0 || minute > 59) {
                ToastUtils.showToast(this, "Giá trị giờ hoặc phút không hợp lệ");
                return;
            }

            if (isStartTime) {
                Calendar selectedTime = Calendar.getInstance();
                selectedTime.set(Calendar.HOUR_OF_DAY, hour);
                selectedTime.set(Calendar.MINUTE, minute);

                if (isSameDay(selectedDate, current) && selectedTime.before(current)) {
                    ToastUtils.showToast(this, "Không thể chọn thời gian đã qua");
                    return;
                }

                edtStartHour.setText(String.format(Locale.getDefault(), "%02d", hour));
                edtStartMinute.setText(String.format(Locale.getDefault(), "%02d", minute));

                // Reset end time if it's before start time
                String endTimeStr = getEndTimeString();
                if (!endTimeStr.isEmpty()) {
                    String[] parts = endTimeStr.split(":");
                    Calendar endTime = Calendar.getInstance();
                    endTime.set(Calendar.HOUR_OF_DAY, Integer.parseInt(parts[0]));
                    endTime.set(Calendar.MINUTE, Integer.parseInt(parts[1]));
                    if (selectedTime.compareTo(endTime) >= 0) {
                        edtEndHour.setText("");
                        edtEndMinute.setText("");
                    }
                }
            } else {
                String startTimeStr = getStartTimeString();
                if (startTimeStr.isEmpty()) {
                    ToastUtils.showToast(this, "Vui lòng chọn thời gian bắt đầu trước");
                    return;
                }

                try {
                    String[] startParts = startTimeStr.split(":");
                    Calendar startTime = Calendar.getInstance();
                    startTime.set(Calendar.HOUR_OF_DAY, Integer.parseInt(startParts[0]));
                    startTime.set(Calendar.MINUTE, Integer.parseInt(startParts[1]));

                    Calendar selectedTime = Calendar.getInstance();
                    selectedTime.set(Calendar.HOUR_OF_DAY, hour);
                    selectedTime.set(Calendar.MINUTE, minute);

                    if (selectedTime.compareTo(startTime) <= 0) {
                        ToastUtils.showToast(this, "Thời gian kết thúc phải sau thời gian bắt đầu");
                        return;
                    }

                    edtEndHour.setText(String.format(Locale.getDefault(), "%02d", hour));
                    edtEndMinute.setText(String.format(Locale.getDefault(), "%02d", minute));
                } catch (Exception e) {
                    Log.e(TAG, "Error parsing time: " + e.getMessage());
                    ToastUtils.showToast(this, "Lỗi khi xử lý thời gian");
                }
            }
        });

        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.cancel());
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void updateDateDisplay() {
        if (selectedDate != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            tvStartDate.setText(sdf.format(selectedDate.getTime()));
        }
    }

    private void updateCategoryDisplay() {
        if (selectedCategory != null) {
            tvSelectedCategory.setText(selectedCategory.getName());
            viewCategoryColor.setVisibility(View.VISIBLE);
            GradientDrawable drawable = (GradientDrawable) viewCategoryColor.getBackground();
            drawable.setColor(Color.parseColor(selectedCategory.getColor()));
        }
    }

    private boolean validateInput() {
        String name = edtHabitName.getText().toString().trim();
        if (name.isEmpty()) {
            ToastUtils.showToast(this, "Vui lòng nhập tên thói quen");
            return false;
        }

        if (selectedCategory == null) {
            ToastUtils.showToast(this, "Vui lòng chọn danh mục");
            return false;
        }

        if (selectedDate == null) {
            ToastUtils.showToast(this, "Vui lòng chọn ngày bắt đầu");
            return false;
        }

        String targetDaysStr = edtTargetDays.getText().toString().trim();
        if (targetDaysStr.isEmpty()) {
            ToastUtils.showToast(this, "Vui lòng nhập số ngày mục tiêu");
            return false;
        }

        try {
            int targetDays = Integer.parseInt(targetDaysStr);
            if (targetDays < 15) {
                ToastUtils.showToast(this, "Số ngày mục tiêu phải từ 15 ngày trở lên");
                return false;
            }
        } catch (NumberFormatException e) {
            ToastUtils.showToast(this, "Số ngày mục tiêu không hợp lệ");
            return false;
        }

        return validateTimeInput();
    }

    private boolean validateTimeInput() {
        String startTime = getStartTimeString();
        String endTime = getEndTimeString();

        if (startTime.isEmpty() || endTime.isEmpty()) {
            ToastUtils.showToast(this, "Vui lòng chọn thời gian bắt đầu và kết thúc");
            return false;
        }

        try {
            Calendar currentTime = Calendar.getInstance();
            Calendar startTimeCal = Calendar.getInstance();
            Calendar endTimeCal = Calendar.getInstance();

            String[] startParts = startTime.split(":");
            String[] endParts = endTime.split(":");

            startTimeCal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(startParts[0]));
            startTimeCal.set(Calendar.MINUTE, Integer.parseInt(startParts[1]));

            endTimeCal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(endParts[0]));
            endTimeCal.set(Calendar.MINUTE, Integer.parseInt(endParts[1]));

            if (isSameDay(selectedDate, currentTime) && startTimeCal.before(currentTime)) {
                ToastUtils.showToast(this, "Thời gian bắt đầu không thể trong quá khứ");
                return false;
            }

            if (startTimeCal.compareTo(endTimeCal) >= 0) {
                ToastUtils.showToast(this, "Thời gian kết thúc phải sau thời gian bắt đầu");
                return false;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error validating time: " + e.getMessage());
            ToastUtils.showToast(this, "Định dạng thời gian không hợp lệ");
            return false;
        }

        return true;
    }

    private void saveHabit() {
        try {
            // Get current user
            User currentUser = userDao.getFirstUser();
            if (currentUser == null) {
                Log.e(TAG, "No user found");
                ToastUtils.showToast(this, "Lỗi: Không tìm thấy thông tin người dùng");
                return;
            }

            // Validate input again before saving
            if (!validateInput()) return;

            // Create new habit object
            Habit habit = new Habit();
            habit.setName(edtHabitName.getText().toString().trim());
            habit.setTypeId(selectedCategory.getId());
            habit.setStartDate(new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    .format(selectedDate.getTime()));
            habit.setStartTime(getStartTimeString());
            habit.setEndTime(getEndTimeString());
            habit.setReminderMinutes(selectedReminderMinutes);
            habit.setTargetDays(Integer.parseInt(edtTargetDays.getText().toString().trim()));
            habit.setUserId(currentUser.getId());
            habit.setStatus(0);
            habit.setStreakCount(0);
            habit.setLastCompletedDate("");

            // Insert habit into database
            long result = habitDao.insert(habit);

            if (result > 0) {
                Log.d(TAG, "Habit saved successfully with ID: " + result);

                // Set ID for the newly created habit
                habit.setId(result);

                // Schedule notifications
                notificationScheduler.scheduleHabitNotifications(habit);

                // Prepare result intent
                Intent resultIntent = new Intent();
                resultIntent.putExtra("refresh_needed", true);
                setResult(RESULT_OK, resultIntent);

                // Show toast and finish activity
                ToastUtils.showToastAndFinish(this,
                        "Đã thêm thói quen mới",
                        () -> {
                            setResult(RESULT_OK, resultIntent);
                            finish();
                        });

            } else if (result == -1) {
                // Handle duplicate name error
                edtHabitName.setError("Tên thói quen đã tồn tại");
                ToastUtils.showToast(this, "Tên thói quen đã tồn tại");
            } else {
                // Handle other errors
                ToastUtils.showToast(this, "Có lỗi xảy ra khi lưu thói quen");
            }
        } catch (NumberFormatException e) {
            Log.e(TAG, "Error parsing numbers: " + e.getMessage());
            ToastUtils.showToast(this, "Lỗi: Dữ liệu số không hợp lệ");
        } catch (Exception e) {
            Log.e(TAG, "Error saving habit: " + e.getMessage());
            ToastUtils.showToast(this, "Có lỗi xảy ra: " + e.getMessage());
        }
    }

    private void showCategoryDialog() {
        View menuAnchor = findViewById(R.id.ll_category_selector);
        PopupMenu popup = new PopupMenu(this, menuAnchor);

        final List<HabitType> categories = habitTypeDao.getAll();
        for (HabitType category : categories) {
            popup.getMenu().add(Menu.NONE, (int) category.getId(), Menu.NONE, category.getName());
        }

        for (int i = 0; i < popup.getMenu().size(); i++) {
            final int finalI = i;
            MenuItem item = popup.getMenu().getItem(i);
            SpannableString spanString = new SpannableString("    " + item.getTitle());
            spanString.setSpan(new IconSpan(this, categories.get(finalI).getColor()), 0, 1, 0);
            item.setTitle(spanString);
        }

        popup.setOnMenuItemClickListener(item -> {
            final HabitType selected = categories.stream()
                    .filter(category -> category.getId() == item.getItemId())
                    .findFirst()
                    .orElse(null);

            if (selected != null) {
                selectedCategory = selected;
                updateCategoryDisplay();
            }
            return true;
        });

        popup.show();
    }

    private boolean isSameDay(Calendar cal1, Calendar cal2) {
        if (cal1 == null || cal2 == null) return false;
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH) &&
                cal1.get(Calendar.DAY_OF_MONTH) == cal2.get(Calendar.DAY_OF_MONTH);
    }

    private String getStartTimeString() {
        String startHour = edtStartHour.getText().toString();
        String startMinute = edtStartMinute.getText().toString();
        if (!startHour.isEmpty() && !startMinute.isEmpty()) {
            return startHour + ":" + startMinute;
        }
        return "";
    }

    private String getEndTimeString() {
        String endHour = edtEndHour.getText().toString();
        String endMinute = edtEndMinute.getText().toString();
        if (!endHour.isEmpty() && !endMinute.isEmpty()) {
            return endHour + ":" + endMinute;
        }
        return "";
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}