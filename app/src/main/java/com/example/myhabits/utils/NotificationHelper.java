package com.example.myhabits.utils;

import android.Manifest;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.RingtoneManager;
import android.os.Build;
import android.util.Log;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import com.example.myhabits.R;
import com.example.myhabits.models.Habit;
import com.example.myhabits.receivers.HabitReminderReceiver;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NotificationHelper {
    private static final String TAG = "NotificationHelper";
    private static final String CHANNEL_ID = "habit_reminders";
    private final Context context;
    private final AlarmManager alarmManager;
    private final NotificationManager notificationManager;

    public NotificationHelper(Context context) {
        this.context = context;
        this.alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        this.notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        createNotificationChannel();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Habit Reminders",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Thông báo nhắc nhở thói quen");
            channel.enableVibration(true);
            channel.setVibrationPattern(new long[]{0, 500, 200, 500});
            notificationManager.createNotificationChannel(channel);
        }
    }

    public void scheduleHabitReminder(Habit habit, List<String> dates) {
        for (String date : dates) {
            try {
                Calendar reminderTime = getTimeFromStringWithDate(habit.getStartTime(), date);
                reminderTime.add(Calendar.MINUTE, -habit.getReminderMinutes());

                if (reminderTime.getTimeInMillis() > System.currentTimeMillis()) {
                    scheduleNotification(
                            habit.getId(),
                            "Nhắc nhở thực hiện",
                            String.format("Thói quen '%s' sẽ bắt đầu sau %d phút",
                                    habit.getName(), habit.getReminderMinutes()),
                            reminderTime.getTimeInMillis(),
                            0,
                            date
                    );
                }
            } catch (Exception e) {
                Log.e(TAG, "Error scheduling reminder for date " + date + ": " + e.getMessage());
            }
        }
    }

    public void scheduleCompletionCheck(Habit habit, List<String> dates) {
        for (String date : dates) {
            try {
                Calendar checkTime = getTimeFromStringWithDate(habit.getEndTime(), date);
                checkTime.add(Calendar.MINUTE, 30);

                if (checkTime.getTimeInMillis() > System.currentTimeMillis()) {
                    scheduleNotification(
                            habit.getId(),
                            "Nhắc nhở hoàn thành",
                            String.format("Bạn đã hoàn thành thói quen '%s' chưa?",
                                    habit.getName()),
                            checkTime.getTimeInMillis(),
                            1,
                            date  // Thêm tham số date
                    );
                }
            } catch (Exception e) {
                Log.e(TAG, "Error scheduling completion check for date " + date + ": " + e.getMessage());
            }
        }
    }

    private void scheduleNotification(long habitId, String title, String content,
                                      long triggerTime, int notificationType, String date) {
        Intent intent = new Intent(context, HabitReminderReceiver.class);
        intent.putExtra("habitId", habitId);
        intent.putExtra("title", title);
        intent.putExtra("content", content);
        intent.putExtra("type", notificationType);
        intent.putExtra("date", date);

        int requestCode = generateRequestCode(habitId, date, notificationType);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Kiểm tra version và quyền
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                try {
                    alarmManager.setAlarmClock(
                            new AlarmManager.AlarmClockInfo(triggerTime, pendingIntent),
                            pendingIntent
                    );
                    Log.d(TAG, "Scheduled notification for habit " + habitId + " on " + date);
                } catch (Exception e) {
                    Log.e(TAG, "Error scheduling notification: " + e.getMessage());
                }
            } else {
                alarmManager.set(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        pendingIntent
                );
            }
        } else {
            alarmManager.setAlarmClock(
                    new AlarmManager.AlarmClockInfo(triggerTime, pendingIntent),
                    pendingIntent
            );
        }
    }

    private Calendar getTimeFromStringWithDate(String timeString, String dateString) {
        Calendar calendar = Calendar.getInstance();

        // Set date
        try {
            Date date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(dateString);
            calendar.setTime(date);
        } catch (ParseException e) {
            Log.e(TAG, "Error parsing date: " + e.getMessage());
        }

        // Set time
        String[] parts = timeString.split(":");
        calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(parts[0]));
        calendar.set(Calendar.MINUTE, Integer.parseInt(parts[1]));
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        return calendar;
    }

    private int generateRequestCode(long habitId, String date, int type) {
        // Tạo requestCode unique cho mỗi notification
        String dateStr = date.replace("-", "");
        return (int) (habitId * 10000000 + Long.parseLong(dateStr) * 10 + type);
    }

    public void cancelHabitNotifications(long habitId, String date) {
        // Hủy cả reminder và completion check
        cancelSpecificNotification(habitId, date, 0);
        cancelSpecificNotification(habitId, date, 1);
    }

    private void cancelSpecificNotification(long habitId, String date, int type) {
        Intent intent = new Intent(context, HabitReminderReceiver.class);
        int requestCode = generateRequestCode(habitId, date, type);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        alarmManager.cancel(pendingIntent);
    }

    public void showNotification(String title, String content, int notificationId) {
        // Kiểm tra quyền POST_NOTIFICATIONS
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                Log.e(TAG, "Không có quyền POST_NOTIFICATIONS");
                return;
            }
        }

        try {
            // Đảm bảo channel đã được tạo
            createNotificationChannel();

            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_notification)
                    .setContentTitle(title)
                    .setContentText(content)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setCategory(NotificationCompat.CATEGORY_REMINDER)
                    .setAutoCancel(true)
                    .setDefaults(NotificationCompat.DEFAULT_ALL); // Thêm defaults

            // Thêm âm thanh và rung động
            builder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
            builder.setVibrate(new long[]{0, 500, 200, 500});

            // Sử dụng NotificationManagerCompat thay vì NotificationManager
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            notificationManager.notify(notificationId, builder.build());

            Log.d(TAG, "Đã hiển thị thông báo: " + title);
        } catch (Exception e) {
            Log.e(TAG, "Lỗi khi hiển thị thông báo: " + e.getMessage());
        }
    }
}