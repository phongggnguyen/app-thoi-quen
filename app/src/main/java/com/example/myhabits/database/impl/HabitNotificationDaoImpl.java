package com.example.myhabits.database.impl;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.myhabits.database.dao.HabitNotificationDao;
import com.example.myhabits.database.data.DatabaseConstants;
import com.example.myhabits.database.data.DatabaseHelper;
import com.example.myhabits.models.HabitNotification;

import java.util.ArrayList;
import java.util.List;

public class HabitNotificationDaoImpl implements HabitNotificationDao {
    private SQLiteDatabase database;
    private final DatabaseHelper dbHelper;

    public HabitNotificationDaoImpl(Context context) {
        dbHelper = DatabaseHelper.getInstance(context);
        database = dbHelper.getDatabase();
    }

    @Override
    public long insert(HabitNotification notification) {
        ContentValues values = new ContentValues();
        values.put(DatabaseConstants.COLUMN_NOTIFICATION_HABIT_ID, notification.getHabitId());
        values.put(DatabaseConstants.COLUMN_NOTIFICATION_TITLE, notification.getTitle());
        values.put(DatabaseConstants.COLUMN_NOTIFICATION_CONTENT, notification.getContent());
        values.put(DatabaseConstants.COLUMN_NOTIFICATION_HABIT_TIME, notification.getHabitTime());
        values.put(DatabaseConstants.COLUMN_NOTIFICATION_HABIT_START_TIME, notification.getHabitStartTime());  // Thêm mới
        values.put(DatabaseConstants.COLUMN_NOTIFICATION_HABIT_END_TIME, notification.getHabitEndTime());      // Thêm mới
        values.put(DatabaseConstants.COLUMN_NOTIFICATION_DAY_NUMBER, notification.getDayNumber());
        values.put(DatabaseConstants.COLUMN_NOTIFICATION_TIME, notification.getNotifyTime());
        values.put(DatabaseConstants.COLUMN_NOTIFICATION_IS_READ, notification.isRead() ? 1 : 0);
        values.put(DatabaseConstants.COLUMN_NOTIFICATION_TYPE, notification.getType());

        return database.insert(DatabaseConstants.TABLE_NOTIFICATIONS, null, values);
    }

    @Override
    public HabitNotification getById(long id) {
        HabitNotification notification = null;
        Cursor cursor = database.query(
                DatabaseConstants.TABLE_NOTIFICATIONS,
                null,
                DatabaseConstants.COLUMN_ID + " = ?",
                new String[]{String.valueOf(id)},
                null, null, null
        );

        if (cursor != null && cursor.moveToFirst()) {
            notification = cursorToNotification(cursor);
            cursor.close();
        }
        return notification;
    }

    @Override
    public List<HabitNotification> getAll() {
        List<HabitNotification> notifications = new ArrayList<>();
        Cursor cursor = database.query(
                DatabaseConstants.TABLE_NOTIFICATIONS,
                null, null, null, null, null,
                DatabaseConstants.COLUMN_NOTIFICATION_TIME + " DESC"
        );

        if (cursor != null && cursor.moveToFirst()) {
            do {
                notifications.add(cursorToNotification(cursor));
            } while (cursor.moveToNext());
            cursor.close();
        }
        return notifications;
    }

    @Override
    public List<HabitNotification> getUnread() {
        List<HabitNotification> notifications = new ArrayList<>();
        Cursor cursor = database.query(
                DatabaseConstants.TABLE_NOTIFICATIONS,
                null,
                DatabaseConstants.COLUMN_NOTIFICATION_IS_READ + " = 0",
                null, null, null,
                DatabaseConstants.COLUMN_NOTIFICATION_TIME + " DESC"
        );

        if (cursor != null && cursor.moveToFirst()) {
            do {
                notifications.add(cursorToNotification(cursor));
            } while (cursor.moveToNext());
            cursor.close();
        }
        return notifications;
    }

    @Override
    public int update(HabitNotification notification) {
        ContentValues values = new ContentValues();
        values.put(DatabaseConstants.COLUMN_NOTIFICATION_TITLE, notification.getTitle());
        values.put(DatabaseConstants.COLUMN_NOTIFICATION_CONTENT, notification.getContent());
        values.put(DatabaseConstants.COLUMN_NOTIFICATION_HABIT_TIME, notification.getHabitTime());
        values.put(DatabaseConstants.COLUMN_NOTIFICATION_IS_READ, notification.isRead() ? 1 : 0);

        return database.update(
                DatabaseConstants.TABLE_NOTIFICATIONS,
                values,
                DatabaseConstants.COLUMN_ID + " = ?",
                new String[]{String.valueOf(notification.getId())}
        );
    }

    @Override
    public boolean delete(long id) {
        return database.delete(
                DatabaseConstants.TABLE_NOTIFICATIONS,
                DatabaseConstants.COLUMN_ID + " = ?",
                new String[]{String.valueOf(id)}
        ) > 0;
    }

    @Override
    public void deleteAll() {
        database.delete(DatabaseConstants.TABLE_NOTIFICATIONS, null, null);
    }

    @Override
    public void markAsRead(long id) {
        ContentValues values = new ContentValues();
        values.put(DatabaseConstants.COLUMN_NOTIFICATION_IS_READ, 1);

        database.update(
                DatabaseConstants.TABLE_NOTIFICATIONS,
                values,
                DatabaseConstants.COLUMN_ID + " = ?",
                new String[]{String.valueOf(id)}
        );
    }
    @Override
    public boolean hasUnreadNotifications() {
        Cursor cursor = database.query(
                DatabaseConstants.TABLE_NOTIFICATIONS,
                null,
                DatabaseConstants.COLUMN_NOTIFICATION_IS_READ + " = 0",
                null, null, null, null
        );
        boolean hasUnread = cursor != null && cursor.getCount() > 0;
        if (cursor != null) {
            cursor.close();
        }
        return hasUnread;
    }
    @Override
    public int getUnreadCount() {
        Cursor cursor = database.query(
                DatabaseConstants.TABLE_NOTIFICATIONS,
                new String[]{"COUNT(*) as count"},
                DatabaseConstants.COLUMN_NOTIFICATION_IS_READ + " = 0",
                null, null, null, null
        );

        int count = 0;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                count = cursor.getInt(0);
            }
            cursor.close();
        }
        return count;
    }


    private HabitNotification cursorToNotification(Cursor cursor) {
        HabitNotification notification = new HabitNotification();
        notification.setId(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseConstants.COLUMN_ID)));
        notification.setHabitId(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseConstants.COLUMN_NOTIFICATION_HABIT_ID)));
        notification.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseConstants.COLUMN_NOTIFICATION_TITLE)));
        notification.setContent(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseConstants.COLUMN_NOTIFICATION_CONTENT)));
        notification.setHabitTime(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseConstants.COLUMN_NOTIFICATION_HABIT_TIME)));
        notification.setDayNumber(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseConstants.COLUMN_NOTIFICATION_DAY_NUMBER)));
        notification.setNotifyTime(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseConstants.COLUMN_NOTIFICATION_TIME)));
        notification.setRead(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseConstants.COLUMN_NOTIFICATION_IS_READ)) == 1);
        notification.setType(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseConstants.COLUMN_NOTIFICATION_TYPE)));

        // Kiểm tra và đọc các cột mới nếu tồn tại
        int startTimeIndex = cursor.getColumnIndex(DatabaseConstants.COLUMN_NOTIFICATION_HABIT_START_TIME);
        if (startTimeIndex != -1) {
            notification.setHabitStartTime(cursor.getString(startTimeIndex));
        }

        int endTimeIndex = cursor.getColumnIndex(DatabaseConstants.COLUMN_NOTIFICATION_HABIT_END_TIME);
        if (endTimeIndex != -1) {
            notification.setHabitEndTime(cursor.getString(endTimeIndex));
        }

        return notification;
    }
}