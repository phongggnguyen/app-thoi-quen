package com.example.myhabits.utils;

import android.app.AlarmManager;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import com.example.myhabits.database.data.DatabaseConstants;
import com.example.myhabits.database.dao.DailyStatusDao;
import com.example.myhabits.database.data.DBManager;
import com.example.myhabits.models.DailyStatus;
import com.example.myhabits.models.Habit;
import com.example.myhabits.models.User;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NotificationScheduler {
    private static final String TAG = "NotificationScheduler";
    private final Context context;
    private final NotificationHelper notificationHelper;
    private final DailyStatusDao dailyStatusDao;

    public NotificationScheduler(Context context) {
        this.context = context;
        this.notificationHelper = new NotificationHelper(context);
        this.dailyStatusDao = DBManager.getInstance(context).getDailyStatusDao();
    }

    public void scheduleHabitNotifications(Habit habit) {
        try {
            // Lấy danh sách các ngày cần lập lịch
            List<String> daysToSchedule = getUpcomingDays(habit);

            if (daysToSchedule.isEmpty()) {
                Log.d(TAG, "No days to schedule notifications for habit: " + habit.getName());
                return;
            }

            // Schedule reminder notifications
            notificationHelper.scheduleHabitReminder(habit, daysToSchedule);

            // Schedule completion check notifications
            notificationHelper.scheduleCompletionCheck(habit, daysToSchedule);

            Log.d(TAG, "Scheduled notifications for " + daysToSchedule.size() + " days");
        } catch (Exception e) {
            Log.e(TAG, "Error scheduling notifications: " + e.getMessage());
        }
    }
    private List<String> getUpcomingDays(Habit habit) {
        List<String> days = new ArrayList<>();
        try {
            String currentDate = DateUtils.getCurrentDate();
            Date startDate = DateUtils.parseDate(habit.getStartDate());
            Date today = DateUtils.parseDate(currentDate);

            // Nếu ngày bắt đầu chưa tới, bắt đầu từ ngày đó
            // Nếu đã qua ngày bắt đầu, bắt đầu từ ngày hiện tại
            Date effectiveStartDate = today.after(startDate) ? today : startDate;

            // Tính số ngày còn lại từ ngày hiện tại
            int targetDays = habit.getTargetDays();
            int daysPassed = DateUtils.getDayNumberFromStartDate(habit.getStartDate(), currentDate) - 1;
            int remainingDays = Math.max(0, targetDays - daysPassed);

            // Tạo danh sách các ngày
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(effectiveStartDate);

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

            for (int i = 0; i < remainingDays; i++) {
                String dateStr = sdf.format(calendar.getTime());

                // Kiểm tra xem thói quen đã hoàn thành cho ngày này chưa
                DailyStatus status = dailyStatusDao.getByHabitIdAndDate(
                        habit.getId(),
                        dateStr
                );

                if (status == null ||
                        status.getStatus() != DatabaseConstants.CHECK_STATUS_COMPLETED) {
                    days.add(dateStr);
                }

                calendar.add(Calendar.DAY_OF_MONTH, 1);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting upcoming days: " + e.getMessage());
        }
        return days;
    }


}