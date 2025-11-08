package com.example.myhabits.receivers;

import static android.content.ContentValues.TAG;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.myhabits.activities.MainActivity;
import com.example.myhabits.database.dao.DailyStatusDao;
import com.example.myhabits.database.dao.HabitNotificationDao;
import com.example.myhabits.database.data.DBManager;
import com.example.myhabits.database.data.DatabaseConstants;
import com.example.myhabits.models.DailyStatus;
import com.example.myhabits.models.Habit;
import com.example.myhabits.models.HabitNotification;
import com.example.myhabits.utils.DateUtils;
import com.example.myhabits.utils.NotificationHelper;
import com.example.myhabits.interfaces.BadgeUpdateListener;

public class HabitReminderReceiver extends BroadcastReceiver {
    private static final String TAG = "HabitReminderReceiver";
    private HabitNotificationDao notificationDao;

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            notificationDao = DBManager.getInstance(context).getHabitNotificationDao();

            long habitId = intent.getLongExtra("habitId", -1);
            String title = intent.getStringExtra("title");
            String content = intent.getStringExtra("content");
            int notificationType = intent.getIntExtra("type", -1);
            String date = intent.getStringExtra("date"); // Lấy thêm thông tin ngày

            Log.d(TAG, "Received notification for habit " + habitId + " on " + date);

            Habit habit = DBManager.getInstance(context).getHabitDao().getById(habitId);
            if (habit == null) {
                Log.e(TAG, "Habit not found");
                return;
            }

            // Kiểm tra xem thói quen đã hoàn thành cho ngày này chưa
            DailyStatus status = DBManager.getInstance(context)
                    .getDailyStatusDao()
                    .getByHabitIdAndDate(habitId, date);

            if (status != null &&
                    status.getStatus() == DatabaseConstants.CHECK_STATUS_COMPLETED) {
                Log.d(TAG, "Habit already completed for date " + date);
                return;
            }

            // Tạo notification mới
            HabitNotification notification = new HabitNotification();
            notification.setHabitId(habitId);
            notification.setTitle(title);
            notification.setContent(content);
            notification.setType(notificationType);
            notification.setHabitTime(date);  // Sử dụng date từ intent
            notification.setHabitStartTime(habit.getStartTime());
            notification.setHabitEndTime(habit.getEndTime());
            notification.setDayNumber(getDayNumber(context, habitId, date)); // Thêm date parameter
            notification.setNotifyTime(System.currentTimeMillis());
            notification.setRead(false);

            long notificationId = notificationDao.insert(notification);

            if (notificationId > 0) {
                NotificationHelper notificationHelper = new NotificationHelper(context);
                notificationHelper.showNotification(
                        title,
                        content,
                        (int) (habitId + notificationType)
                );

                BadgeUpdateListener listener = MainActivity.getBadgeUpdateListener();
                if (listener != null) {
                    listener.onBadgeUpdate();
                }

                context.sendBroadcast(new Intent("NOTIFICATION_UPDATED"));
            }
        } catch (Exception e) {
            Log.e(TAG, "Error processing notification: " + e.getMessage());
        }
    }

    private int getDayNumber(Context context, long habitId, String date) {
        try {
            DailyStatus status = DBManager.getInstance(context)
                    .getDailyStatusDao()
                    .getByHabitIdAndDate(habitId, date);
            return status != null ? status.getDayNumber() : 1;
        } catch (Exception e) {
            Log.e(TAG, "Error getting day number: " + e.getMessage());
            return 1;
        }
    }
}