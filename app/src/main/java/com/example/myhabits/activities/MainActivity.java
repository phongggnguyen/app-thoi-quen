package com.example.myhabits.activities;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import com.example.myhabits.R;
import com.example.myhabits.adapters.HabitListAdapter;
import com.example.myhabits.database.dao.DailyStatusDao;
import com.example.myhabits.database.dao.HabitDao;
import com.example.myhabits.database.dao.UserDao;
import com.example.myhabits.database.data.DBManager;
import com.example.myhabits.database.data.DataChangeListener;
import com.example.myhabits.database.data.DataChangeManager;
import com.example.myhabits.database.data.DatabaseConstants;
import com.example.myhabits.interfaces.BadgeUpdateListener;
import com.example.myhabits.models.DailyStatus;
import com.example.myhabits.models.Habit;
import com.example.myhabits.models.HabitType;
import com.example.myhabits.models.User;
import com.example.myhabits.utils.DateUtils;
import com.google.android.material.navigation.NavigationView;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import android.app.AlarmManager;
import com.example.myhabits.services.NotificationService;
import android.widget.RelativeLayout;
import android.view.LayoutInflater;
import com.example.myhabits.database.dao.HabitNotificationDao;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        HabitListAdapter.OnHabitClickListener,
        DataChangeListener,
        BadgeUpdateListener {

    private static final String TAG = "MainActivity";
    private static final int STORAGE_PERMISSION_CODE = 2296;
    private static final int NOTIFICATION_PERMISSION_CODE = 2297;
    private static final int SCHEDULE_EXACT_ALARM_CODE = 2298;

    // Request Codes for activities
    public static final class RequestCode {
        public static final int UPDATE_PROFILE = 100;
        public static final int ADD_CATEGORY = 101;
        public static final int LIST_CATEGORY = 102;
        public static final int ADD_HABIT = 103;
        public static final int HABIT_DETAIL = 104;
        public static final int ADD_NOTE = 106;
        public static final int STORAGE_PERMISSION = 2296;
        public static final int BACKUP = 108;
        public static final int RESTORE = 109;
        public static final int PASSWORD = 110;
        public static final int LIST_HABIT = 111;
        public static final int LIST_NOTE = 112;
        public static final int REPORT = 113;
    }

    // Views
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;
    private RecyclerView rvHabits;
    private TextView tvMessage;
    private TextView tvTodayHabits;

    // Database
    private UserDao userDao;
    private HabitDao habitDao;
    private DailyStatusDao dailyStatusDao;

    // Adapter
    private HabitListAdapter habitAdapter;
    private List<Habit> habitList;

    // Data
    private User user;
    private volatile boolean isRefreshing = false;

    private Calendar currentDate = Calendar.getInstance();
    private LinearLayout weekdayContainer;
    private LinearLayout dateContainer;
    private TextView tvMonthYear;
    private ImageButton btnPrevWeek, btnNextWeek;
    private List<TextView> dateViews = new ArrayList<>();
    private int selectedDatePosition = -1;

    private MenuItem menuItemNotification;
    private TextView badgeCounter;
    private HabitNotificationDao notificationDao;
    private BroadcastReceiver notificationUpdateReceiver;

    private static BadgeUpdateListener badgeUpdateListener;

    public static BadgeUpdateListener getBadgeUpdateListener() {
        return badgeUpdateListener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        badgeUpdateListener = this;

        initDatabase();
        DataChangeManager.getInstance().addListener(this);

        // Khởi tạo notificationDao
        notificationDao = DBManager.getInstance(this).getHabitNotificationDao();

        // Đăng ký broadcast receiver
        registerNotificationReceiver();

        initViews();
        setupViews();
        setupRecyclerView();

        if (navigationView != null) {
            navigationView.getMenu().getItem(0).setChecked(false);
        }

        checkPermissions();
        startNotificationService();
        refreshData();
        updateNotificationBadge();
    }

    public void refreshData() {
        if (isRefreshing) return;

        try {
            isRefreshing = true;
            runOnUiThread(() -> {
                try {
                    List<Habit> allHabits = habitDao.getByUserId(getCurrentUserId());
                    List<Habit> todayHabits = filterTodayHabits(allHabits);
                    User currentUser = userDao.getFirstUser();

                    updateHabitList(allHabits, todayHabits);
                    updateUserInfo(currentUser);
                    invalidateOptionsMenu();
                } catch (Exception e) {
                    Log.e(TAG, "Error updating UI: " + e.getMessage());
                    showError("Lỗi cập nhật giao diện");
                } finally {
                    isRefreshing = false;
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error in refreshData: " + e.getMessage());
            isRefreshing = false;
            showError("Đã xảy ra lỗi");
        }
    }

    private void checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        NOTIFICATION_PERMISSION_CODE
                );
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            if (!alarmManager.canScheduleExactAlarms()) {
                Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                startActivity(intent);
            }
        }
    }

    private void checkScheduleExactAlarmPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            if (!alarmManager.canScheduleExactAlarms()) {
                Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivityForResult(intent, SCHEDULE_EXACT_ALARM_CODE);
            }
        }
    }

    private void startNotificationService() {
        try {
            Intent serviceIntent = new Intent(this, NotificationService.class)
                    .setAction("RESCHEDULE_NOTIFICATIONS");

            // Thay vì dùng startForegroundService, dùng startService thông thường
            startService(serviceIntent);

            Log.d(TAG, "Notification service started successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error starting notification service: " + e.getMessage());
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == STORAGE_PERMISSION_CODE && grantResults.length > 0) {
            boolean readGranted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
            boolean writeGranted = grantResults[1] == PackageManager.PERMISSION_GRANTED;

            if (readGranted && writeGranted) {
                showError("Đã được cấp quyền truy cập bộ nhớ");
            } else {
                showError("Ứng dụng cần quyền truy cập bộ nhớ để sao lưu dữ liệu");
            }
        } else if (requestCode == NOTIFICATION_PERMISSION_CODE && grantResults.length > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                showError("Đã được cấp quyền thông báo");
            } else {
                showError("Ứng dụng cần quyền thông báo để gửi nhắc nhở");
            }
        }
    }

    private void registerNotificationReceiver() {
        notificationUpdateReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if ("NOTIFICATION_UPDATED".equals(intent.getAction())) {
                    updateNotificationBadge();
                }
            }
        };

        IntentFilter filter = new IntentFilter("NOTIFICATION_UPDATED");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(notificationUpdateReceiver, filter,
                    Context.RECEIVER_NOT_EXPORTED);
        } else {
            registerReceiver(notificationUpdateReceiver, filter);
        }
    }

    @Override
    public void onBadgeUpdate() {
        runOnUiThread(() -> updateNotificationBadge());
    }

    private void updateNotificationBadge() {
        if (badgeCounter == null || notificationDao == null) return;

        try {
            int unreadCount = notificationDao.getUnreadCount();
            runOnUiThread(() -> {
                try {
                    if (unreadCount > 0) {
                        badgeCounter.setVisibility(View.VISIBLE);
                        if (unreadCount > 99) {
                            badgeCounter.setText("99+");
                        } else {
                            badgeCounter.setText(String.valueOf(unreadCount));
                        }
                    } else {
                        badgeCounter.setVisibility(View.GONE);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error updating badge UI: " + e.getMessage());
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error getting unread count: " + e.getMessage());
        }
    }

    private void updateHabitList(List<Habit> allHabits, List<Habit> todayHabits) {
        if (allHabits == null || allHabits.isEmpty()) {
            rvHabits.setVisibility(View.GONE);
            tvMessage.setVisibility(View.VISIBLE);
            tvMessage.setText("Bạn chưa có thói quen nào.\nHãy thêm một thói quen mới!");
            return;
        }

        // Nếu chưa chọn ngày nào thì mặc định hiển thị thói quen ngày hôm nay
        if (selectedDatePosition == -1) {
            if (todayHabits.isEmpty()) {
                rvHabits.setVisibility(View.GONE);
                tvMessage.setVisibility(View.VISIBLE);
                tvMessage.setText("Hôm nay không có thói quen nào");
            } else {
                rvHabits.setVisibility(View.VISIBLE);
                tvMessage.setVisibility(View.GONE);
                habitList.clear();
                habitList.addAll(todayHabits);
                updateHabitStatuses(todayHabits);
                habitAdapter.notifyDataSetChanged();
            }
        }
    }

    private void updateUserInfo(User user) {
        if (user != null && navigationView != null) {
            View headerView = navigationView.getHeaderView(0);
            if (headerView != null) {
                ImageView imgAvatar = headerView.findViewById(R.id.img_user_avatar);
                TextView tvUserName = headerView.findViewById(R.id.tv_user_name);

                loadUserAvatar(user.getAvatar(), imgAvatar);
                tvUserName.setText(user.getName());
            }
        }
    }

    private void showError(String message) {
        runOnUiThread(() -> Toast.makeText(this, message, Toast.LENGTH_SHORT).show());
    }

    private void initDatabase() {
        try {
            DBManager dbManager = DBManager.getInstance(this);
            if (dbManager != null) {
                userDao = dbManager.getUserDao();
                habitDao = dbManager.getHabitDao();
                dailyStatusDao = dbManager.getDailyStatusDao();

                user = userDao.getFirstUser();
                if (user == null) {
                    user = new User();
                    user.setName("Default User");
                    user.setAvatar("android.resource://" + getPackageName() + "/" + R.drawable.ic_profile);
                    long userId = userDao.insert(user);
                    if (userId != -1) {
                        user = userDao.getFirstUser();
                    }
                }

                if (user == null) {
                    Log.e(TAG, "Failed to create or get user");
                    Toast.makeText(this, "Lỗi khởi tạo người dùng", Toast.LENGTH_LONG).show();
                    finish();
                }
            } else {
                Log.e(TAG, "DBManager is null");
                Toast.makeText(this, "Không thể kết nối database", Toast.LENGTH_LONG).show();
                finish();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error initializing database: " + e.getMessage());
            Toast.makeText(this, "Lỗi khởi tạo database: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
            finish();
        }
    }

    private void initViews() {
        try {
            drawerLayout = findViewById(R.id.drawer_layout);
            navigationView = findViewById(R.id.nav_view);
            toolbar = findViewById(R.id.toolbar);
            rvHabits = findViewById(R.id.rvHabits);
            tvMessage = findViewById(R.id.tvMessage);
            weekdayContainer = findViewById(R.id.weekdayContainer);
            dateContainer = findViewById(R.id.dateContainer);
            tvMonthYear = findViewById(R.id.tvMonthYear);
            btnPrevWeek = findViewById(R.id.btnPrevWeek);
            btnNextWeek = findViewById(R.id.btnNextWeek);

            setupCalendar();

            if (drawerLayout == null || navigationView == null ||
                    toolbar == null || rvHabits == null || tvMessage == null) {
                Log.e(TAG, "Error initializing views");
                showError("Lỗi khởi tạo giao diện");
                finish();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error initializing views: " + e.getMessage());
            showError("Lỗi khởi tạo giao diện");
            finish();
        }
    }

    private void setupViews() {
        setupToolbar();
        setupDrawer();
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
    }

    private void setupDrawer() {
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close) {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                super.onDrawerSlide(drawerView, 0);
            }
        };

        drawerLayout.addDrawerListener(toggle);

        drawerLayout.setScrimColor(Color.TRANSPARENT);
        drawerLayout.setDrawerElevation(0);

        drawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerOpened(View drawerView) {
                clearNavigationSelection();
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                clearNavigationSelection();
            }

            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {}

            @Override
            public void onDrawerStateChanged(int newState) {}
        });

        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);
    }

    private void setupCalendar() {
        // Setup listeners
        btnPrevWeek.setOnClickListener(v -> navigateWeek(-7));
        btnNextWeek.setOnClickListener(v -> navigateWeek(7));

        // Setup weekday labels
        String[] weekdays = {"T2", "T3", "T4", "T5", "T6", "T7", "CN"};
        weekdayContainer.removeAllViews();
        for (String weekday : weekdays) {
            TextView tvWeekday = createTextView(weekday, true);
            weekdayContainer.addView(tvWeekday);
        }

        // Initialize date views
        dateContainer.removeAllViews();
        dateViews.clear();
        for (int i = 0; i < 7; i++) {
            TextView tvDate = createTextView("", false);
            int position = i;
            tvDate.setOnClickListener(v -> onDateSelected(position));
            dateViews.add(tvDate);
            dateContainer.addView(tvDate);
        }

        updateCalendarDates();
    }

    private TextView createTextView(String text, boolean isWeekday) {
        TextView textView = new TextView(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1
        );
        textView.setLayoutParams(params);
        textView.setText(text);
        textView.setGravity(Gravity.CENTER);
        textView.setPadding(0, 16, 0, 16);
        textView.setTextColor(getColor(R.color.black));
        textView.setTextSize(18);

        if (!isWeekday) {
            textView.setBackgroundResource(R.drawable.bg_date_selector);
        }

        return textView;
    }

    private void updateCalendarDates() {
        // Update month/year display
        SimpleDateFormat monthYearFormat = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
        tvMonthYear.setText(monthYearFormat.format(currentDate.getTime()));

        // Get start of week (Monday)
        Calendar calendar = (Calendar) currentDate.clone();
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);

        // Update date buttons
        for (int i = 0; i < 7; i++) {
            TextView tvDate = dateViews.get(i);
            tvDate.setText(String.valueOf(calendar.get(Calendar.DAY_OF_MONTH)));

            // Highlight today
            if (DateUtils.isSameDay(calendar.getTime(), new Date())) {
                tvDate.setTextColor(getColor(R.color.red));
                tvDate.setTypeface(null, Typeface.BOLD);
            } else {
                tvDate.setTextColor(getColor(R.color.black));
                tvDate.setTypeface(null, Typeface.NORMAL);
            }

            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }
    }

    private void navigateWeek(int days) {
        currentDate.add(Calendar.DAY_OF_MONTH, days);
        updateCalendarDates();
        if (selectedDatePosition != -1) {
            onDateSelected(selectedDatePosition);
        }
    }

    private void onDateSelected(int position) {
        // Unhighlight previous selection
        if (selectedDatePosition != -1 && selectedDatePosition < dateViews.size()) {
            dateViews.get(selectedDatePosition).setSelected(false);
        }

        // Highlight new selection
        selectedDatePosition = position;
        dateViews.get(position).setSelected(true);

        // Calculate selected date
        Calendar selectedDate = (Calendar) currentDate.clone();
        selectedDate.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        selectedDate.add(Calendar.DAY_OF_MONTH, position);

        String formattedDate = DateUtils.formatDate(selectedDate.getTime());

        // Lấy danh sách thói quen cho ngày được chọn
        List<Habit> allHabits = habitDao.getByUserId(getCurrentUserId());
        List<Habit> selectedDateHabits = filterHabitsByDate(allHabits, formattedDate);

        // Format ngày để hiển thị thông báo
        SimpleDateFormat displayFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        String displayDate = displayFormat.format(selectedDate.getTime());

        if (selectedDateHabits.isEmpty()) {
            rvHabits.setVisibility(View.GONE);
            tvMessage.setVisibility(View.VISIBLE);
            tvMessage.setText("Hôm nay không có thói quen");
        } else {
            rvHabits.setVisibility(View.VISIBLE);
            tvMessage.setVisibility(View.GONE);
            habitList.clear();
            habitList.addAll(selectedDateHabits);
            updateHabitStatuses(selectedDateHabits);
            habitAdapter.notifyDataSetChanged();
        }
    }

    private List<Habit> filterHabitsByDate(List<Habit> habits, String date) {
        List<Habit> filteredHabits = new ArrayList<>();
        for (Habit habit : habits) {
            try {
                Date habitStart = DateUtils.parseDate(habit.getStartDate());
                Date targetDate = DateUtils.parseDate(date);

                if (!targetDate.before(habitStart)) {
                    int daysPassed = DateUtils.getDayNumberFromStartDate(habit.getStartDate(), date);
                    if (daysPassed <= habit.getTargetDays()) {
                        filteredHabits.add(habit);
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error filtering habit: " + e.getMessage());
            }
        }
        return filteredHabits;
    }

    private void clearNavigationSelection() {
        if (navigationView != null) {
            int size = navigationView.getMenu().size();
            for (int i = 0; i < size; i++) {
                navigationView.getMenu().getItem(i).setChecked(false);
            }
        }
    }

    private void setupRecyclerView() {
        habitList = new ArrayList<>();
        habitAdapter = new HabitListAdapter(this, habitList, dailyStatusDao, this);
        rvHabits.setLayoutManager(new LinearLayoutManager(this));
        rvHabits.setAdapter(habitAdapter);
    }

    private List<Habit> filterTodayHabits(List<Habit> habits) {
        List<Habit> todayHabits = new ArrayList<>();
        String today = DateUtils.getCurrentDate();

        for (Habit habit : habits) {
            try {
                Date startDate = DateUtils.parseDate(habit.getStartDate());
                Date currentDate = DateUtils.parseDate(today);

                // Chỉ xem xét các thói quen đã đến ngày bắt đầu
                if (currentDate.before(startDate)) {
                    continue;
                }

                // Kiểm tra xem đã đạt đủ số ngày mục tiêu chưa
                int daysPassed = DateUtils.getDayNumberFromStartDate(habit.getStartDate(), today);
                if (daysPassed <= habit.getTargetDays()) {
                    todayHabits.add(habit);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error processing habit: " + e.getMessage());
            }
        }

        return todayHabits;
    }

    private void updateHabitStatuses(List<Habit> habits) {
        // Chỉ cần đánh dấu các ngày đã bỏ lỡ
        for (Habit habit : habits) {
            try {
                dailyStatusDao.markMissedDays(habit.getId());
            } catch (Exception e) {
                Log.e(TAG, "Error updating habit status: " + habit.getId() + ", " + e.getMessage());
            }
        }
        habitAdapter.notifyDataSetChanged();
    }

    private void startActivityWithoutAnimation(Intent intent, int requestCode) {
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        if (requestCode != -1) {
            startActivityForResult(intent, requestCode);
        } else {
            startActivity(intent);
        }
        overridePendingTransition(0, 0);
    }

    private void setupPopupMenuIcons(PopupMenu popup) {
        try {
            Field field = popup.getClass().getDeclaredField("mPopup");
            field.setAccessible(true);
            Object menuPopupHelper = field.get(popup);
            Class<?> classPopupHelper = Class.forName(menuPopupHelper.getClass().getName());
            Method setForceIcons = classPopupHelper.getMethod("setForceShowIcon", boolean.class);
            setForceIcons.invoke(menuPopupHelper, true);
        } catch (Exception e) {
            Log.e(TAG, "Error setting up popup menu icons: " + e.getMessage());
        }
    }

    private void showSettingOptions() {
        View menuItemView = navigationView.findViewById(R.id.nav_setting);
        Context wrapper = new ContextThemeWrapper(this, R.style.CustomPopupTheme);
        PopupMenu popup = new PopupMenu(wrapper, menuItemView);

        try {
            Field field = PopupMenu.class.getDeclaredField("mPopup");
            field.setAccessible(true);
            Object menuPopupHelper = field.get(popup);
            Class<?> classPopupHelper = Class.forName(menuPopupHelper.getClass().getName());
            Method setAnimationStyle = classPopupHelper.getMethod("setAnimationStyle", int.class);
            setAnimationStyle.invoke(menuPopupHelper, 0);
        } catch (Exception e) {
            Log.e(TAG, "Error setting popup animation: " + e.getMessage());
        }

        Menu menu = popup.getMenu();
        menu.add(Menu.NONE, 1, Menu.NONE, "Sao lưu")
                .setIcon(R.drawable.ic_backup);
        menu.add(Menu.NONE, 2, Menu.NONE, "Khôi phục")
                .setIcon(R.drawable.ic_restore);
        menu.add(Menu.NONE, 3, Menu.NONE, "Đặt mật khẩu")
                .setIcon(R.drawable.ic_password);

        setupPopupMenuIcons(popup);

        popup.setOnMenuItemClickListener(item -> {
            drawerLayout.closeDrawer(GravityCompat.START);
            Intent intent;
            switch (item.getItemId()) {
                case 1:
                    intent = new Intent(this, BackupActivity.class);
                    startActivityWithoutAnimation(intent, RequestCode.BACKUP);
                    return true;
                case 2:
                    intent = new Intent(this, RestoreActivity.class);
                    startActivityWithoutAnimation(intent, RequestCode.RESTORE);
                    return true;
                case 3:
                    intent = new Intent(this, PasswordSetting.class);
                    startActivityWithoutAnimation(intent, RequestCode.PASSWORD);
                    return true;
                default:
                    return false;
            }
        });

        popup.show();
    }

    private void showManageOptions() {
        View menuItemView = navigationView.findViewById(R.id.nav_manage);
        Context wrapper = new ContextThemeWrapper(this, R.style.CustomPopupTheme);
        PopupMenu popup = new PopupMenu(wrapper, menuItemView);

        try {
            Field field = PopupMenu.class.getDeclaredField("mPopup");
            field.setAccessible(true);
            Object menuPopupHelper = field.get(popup);
            Class<?> classPopupHelper = Class.forName(menuPopupHelper.getClass().getName());
            Method setAnimationStyle = classPopupHelper.getMethod("setAnimationStyle", int.class);
            setAnimationStyle.invoke(menuPopupHelper, 0);
        } catch (Exception e) {
            Log.e(TAG, "Error setting popup animation: " + e.getMessage());
        }

        Menu menu = popup.getMenu();
        menu.add(Menu.NONE, 1, Menu.NONE, "Hồ sơ")
                .setIcon(R.drawable.ic_manage_profile);
        menu.add(Menu.NONE, 2, Menu.NONE, "Danh mục")
                .setIcon(R.drawable.ic_category);
        menu.add(Menu.NONE, 3, Menu.NONE, "Thói quen")
                .setIcon(R.drawable.ic_habit);
        menu.add(Menu.NONE, 4, Menu.NONE, "Ghi chú")
                .setIcon(R.drawable.ic_list_note);
        menu.add(Menu.NONE, 5, Menu.NONE, "Thống kê")
                .setIcon(R.drawable.ic_report);

        setupPopupMenuIcons(popup);
        popup.setOnMenuItemClickListener(item -> {
            drawerLayout.closeDrawer(GravityCompat.START);
            Intent intent;
            switch (item.getItemId()) {
                case 1:
                    intent = new Intent(this, ProfileUpdateActivity.class);
                    startActivityWithoutAnimation(intent, RequestCode.UPDATE_PROFILE);
                    return true;
                case 2:
                    intent = new Intent(this, ListCategory.class);
                    startActivityWithoutAnimation(intent, RequestCode.LIST_CATEGORY);
                    return true;
                case 3:
                    intent = new Intent(this, ListHabit.class);
                    startActivityWithoutAnimation(intent, RequestCode.LIST_HABIT);
                    return true;
                case 4:
                    intent = new Intent(this, ListNote.class);
                    startActivityWithoutAnimation(intent, RequestCode.LIST_NOTE);
                    return true;
                case 5:
                    intent = new Intent(this, Report.class);
                    startActivityWithoutAnimation(intent, RequestCode.REPORT);
                    return true;
                default:
                    return false;
            }
        });

        popup.show();
    }

    private void handleSearch() {
        SearchDialog.showSearchDialog(this, habitList, rvHabits);
    }

    private void handleAddCategory() {
        startActivityWithoutAnimation(
                new Intent(this, Add_Update_Category.class),
                RequestCode.ADD_CATEGORY
        );
    }

    private void handleAddHabit() {
        List<HabitType> categories = DBManager.getInstance(this).getHabitTypeDao().getAll();
        if (categories.isEmpty()) {
            new AlertDialog.Builder(this)
                    .setTitle("Thông báo")
                    .setMessage("Bạn cần tạo ít nhất một danh mục trước khi thêm thói quen mới.")
                    .setPositiveButton("Tạo danh mục", (dialog, which) -> {
                        startActivityWithoutAnimation(
                                new Intent(this, Add_Update_Category.class),
                                RequestCode.ADD_CATEGORY
                        );
                    })
                    .setNegativeButton("Hủy", null)
                    .show();
        } else {
            startActivityWithoutAnimation(
                    new Intent(this, AddHabit.class),
                    RequestCode.ADD_HABIT
            );
        }
    }

    private void handleAddNote() {
        startActivityWithoutAnimation(
                new Intent(this, AddEditNote.class),
                RequestCode.ADD_NOTE
        );
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.nav_setting) {
            showSettingOptions();
            return true;
        } else if (id == R.id.nav_manage) {
            showManageOptions();
            return true;
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);

        // Lấy reference đến menu item notification
        menuItemNotification = menu.findItem(R.id.action_notification);

        // Setup badge cho notification
        setupNotificationBadge();

        return true;
    }

    private void setupNotificationBadge() {
        // Inflate custom layout cho notification icon
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        RelativeLayout notificationLayout = (RelativeLayout) inflater.inflate(R.layout.notification_badge_layout, null);

        badgeCounter = notificationLayout.findViewById(R.id.badge_counter);

        // Set custom view cho menu item
        menuItemNotification.setActionView(notificationLayout);

        // Set click listener cho toàn bộ layout
        notificationLayout.setOnClickListener(v -> {
            Intent intent = new Intent(this, NotificationsActivity.class);
            startActivityWithoutAnimation(intent, -1);
        });

        updateNotificationBadge();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_notification) {
            Intent intent = new Intent(this, NotificationsActivity.class);
            startActivityWithoutAnimation(intent, -1);
            return true;
        } else if (id == R.id.action_search) {
            handleSearch();
            return true;
        } else if (id == R.id.action_add_category) {
            handleAddCategory();
            return true;
        } else if (id == R.id.action_add_habit) {
            handleAddHabit();
            return true;
        } else if (id == R.id.action_add_note) {
            handleAddNote();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadUserAvatar(String avatarUri, ImageView imgAvatar) {
        try {
            if (avatarUri != null) {
                Uri uri = Uri.parse(avatarUri);
                // Lấy resource ID từ URI
                int resourceId = Integer.parseInt(uri.getLastPathSegment());
                imgAvatar.setImageResource(resourceId);
            } else {
                imgAvatar.setImageResource(R.drawable.ic_profile);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading avatar: " + e.getMessage());
            imgAvatar.setImageResource(R.drawable.ic_profile);
        }
    }

    private long getCurrentUserId() {
        try {
            if (userDao != null) {
                User currentUser = userDao.getFirstUser();
                if (currentUser != null) {
                    return currentUser.getId();
                }
            }
            Log.e(TAG, "UserDao or current user is null");
        } catch (Exception e) {
            Log.e(TAG, "Error getting current user ID: " + e.getMessage());
        }
        return -1;
    }

    private void checkStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                try {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                            .addCategory("android.intent.category.DEFAULT")
                            .setData(Uri.parse("package:" + getPackageName()));
                    startActivityForResult(intent, RequestCode.STORAGE_PERMISSION);
                } catch (Exception e) {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                    startActivityForResult(intent, RequestCode.STORAGE_PERMISSION);
                }
            }
        } else {
            String[] permissions = {
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            };
            ActivityCompat.requestPermissions(this, permissions, RequestCode.STORAGE_PERMISSION);
        }
    }

    @Override
    public void onCheckboxClick(Habit habit, String date, boolean isChecked) {
        try {
            int dayNumber = DateUtils.getDayNumberFromStartDate(habit.getStartDate(), date);

            DailyStatus status = dailyStatusDao.getByHabitIdAndDate(habit.getId(), date);
            if (status == null) {
                status = new DailyStatus(habit.getId(), date, DatabaseConstants.CHECK_STATUS_COMPLETED, dayNumber);
                status.setCheckTime(System.currentTimeMillis());
            } else {
                status.setStatus(DatabaseConstants.CHECK_STATUS_COMPLETED);
                status.setCheckTime(System.currentTimeMillis());
            }

            if (status.getId() > 0) {
                dailyStatusDao.update(status);
            } else {
                dailyStatusDao.insert(status);
            }

            checkHabitCompletion(habit);
            refreshData();

        } catch (Exception e) {
            Log.e(TAG, "Error handling checkbox click: " + e.getMessage());
            showError("Lỗi cập nhật trạng thái");
        }
    }

    private void checkHabitCompletion(Habit habit) {
        try {
            int targetDays = habit.getTargetDays();
            List<String> daysToCheck = DateUtils.getDailyDaysWithTarget(habit.getStartDate(), targetDays);

            int completedCount = 0;
            for (String date : daysToCheck) {
                DailyStatus status = dailyStatusDao.getByHabitIdAndDate(habit.getId(), date);
                if (status != null && status.getStatus() == DatabaseConstants.CHECK_STATUS_COMPLETED) {
                    completedCount++;
                }
            }

            if (completedCount == targetDays) {
                habit.setStatus(1);
                habitDao.update(habit);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error checking habit completion: " + e.getMessage());
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (menu.getClass().getSimpleName().equals("MenuBuilder")) {
            try {
                Method method = menu.getClass()
                        .getDeclaredMethod("setOptionalIconsVisible", Boolean.TYPE);
                method.setAccessible(true);
                method.invoke(menu, true);
            } catch (Exception e) {
                Log.e(TAG, "Error showing menu icons: " + e.getMessage());
            }
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SCHEDULE_EXACT_ALARM_CODE) {
            checkScheduleExactAlarmPermission();
        }

        // Xử lý kết quả restore thành công
        if (requestCode == RequestCode.RESTORE && resultCode == RESULT_OK && data != null) {
            if (data.getBooleanExtra("restore_success", false)) {
                initDatabase();
            }
        }

        // Refresh data cho mọi request thành công
        if (resultCode == RESULT_OK) {
            refreshData();
        }

        overridePendingTransition(0, 0);
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshData();
    }

    @Override
    public void onDataChanged() {
        refreshData();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent != null && intent.getBooleanExtra("restore_success", false)) {
            initDatabase();
            refreshData();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            if (notificationUpdateReceiver != null) {
                unregisterReceiver(notificationUpdateReceiver);
                notificationUpdateReceiver = null;
            }
            badgeUpdateListener = null;
        } catch (Exception e) {
            Log.e(TAG, "Error unregistering receiver: " + e.getMessage());
        }
        DataChangeManager.getInstance().removeListener(this);
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
            overridePendingTransition(0, 0);
        }
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, 0);
    }
}