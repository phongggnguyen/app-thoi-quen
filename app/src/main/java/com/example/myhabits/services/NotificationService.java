package com.example.myhabits.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

import com.example.myhabits.database.dao.HabitDao;
import com.example.myhabits.database.dao.UserDao;
import com.example.myhabits.database.data.DBManager;
import com.example.myhabits.models.Habit;
import com.example.myhabits.models.User;
import com.example.myhabits.utils.NotificationScheduler;

import java.util.List;

public class NotificationService extends Service {
    private static final String TAG = "NotificationService";
    private PowerManager.WakeLock wakeLock;
    private static final long WAKE_LOCK_TIMEOUT = 5 * 60 * 1000L; // 5 phút

    @Override
    public void onCreate() {
        super.onCreate();
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK,
                "MyHabits:NotificationWakeLock"
        );
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && "RESCHEDULE_NOTIFICATIONS".equals(intent.getAction())) {
            rescheduleNotifications();
        }
        return START_NOT_STICKY; // Thay đổi thành START_NOT_STICKY
    }

    private void rescheduleNotifications() {
        // Acquire wake lock để ngăn thiết bị sleep trong quá trình xử lý
        if (!wakeLock.isHeld()) {
            wakeLock.acquire(WAKE_LOCK_TIMEOUT);
        }

        try {
            Log.d(TAG, "Starting to reschedule notifications");

            DBManager dbManager = DBManager.getInstance(this);
            UserDao userDao = dbManager.getUserDao();
            HabitDao habitDao = dbManager.getHabitDao();

            // Lấy user hiện tại
            User currentUser = userDao.getFirstUser();
            if (currentUser == null) {
                Log.e(TAG, "No user found");
                return;
            }

            // Lấy danh sách habits của user hiện tại
            List<Habit> habits = habitDao.getByUserId(currentUser.getId());

            NotificationScheduler scheduler = new NotificationScheduler(this);

            for (Habit habit : habits) {
                scheduler.scheduleHabitNotifications(habit);
            }

            Log.d(TAG, "Successfully rescheduled notifications for " + habits.size() + " habits");

        } catch (Exception e) {
            Log.e(TAG, "Error rescheduling notifications: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (wakeLock.isHeld()) {
                wakeLock.release();
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (wakeLock.isHeld()) {
            wakeLock.release();
        }
    }
}