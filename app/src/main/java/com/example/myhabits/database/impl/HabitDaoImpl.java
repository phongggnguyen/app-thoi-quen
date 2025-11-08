package com.example.myhabits.database.impl;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.myhabits.database.dao.HabitDao;
import com.example.myhabits.database.data.DatabaseConstants;
import com.example.myhabits.database.data.DatabaseHelper;
import com.example.myhabits.models.Habit;

import java.util.ArrayList;
import java.util.List;

public class HabitDaoImpl implements HabitDao {
    private static final String TAG = "HabitDaoImpl";
    private SQLiteDatabase database;
    private final DatabaseHelper dbHelper;

    public HabitDaoImpl(Context context) {
        dbHelper = DatabaseHelper.getInstance(context);
        database = dbHelper.getWritableDatabase();
    }

    private SQLiteDatabase getDatabase() {
        if (database == null || !database.isOpen()) {
            database = dbHelper.getWritableDatabase();
        }
        return database;
    }

    public boolean isHabitNameExists(String name, long userId) {
        Cursor cursor = database.query(
                DatabaseConstants.TABLE_HABITS,
                new String[]{DatabaseConstants.COLUMN_ID},
                DatabaseConstants.COLUMN_HABIT_NAME + " = ? AND " +
                        DatabaseConstants.COLUMN_HABIT_USER_ID + " = ?",
                new String[]{name.trim(), String.valueOf(userId)},
                null, null, null
        );

        boolean exists = cursor != null && cursor.moveToFirst();
        if (cursor != null) cursor.close();
        return exists;
    }

    @Override
    public long insert(Habit habit) {
        // Kiểm tra tên trùng
        if (isHabitNameExists(habit.getName(), habit.getUserId())) {
            return -1; // Trả về -1 nếu tên đã tồn tại
        }

        ContentValues values = new ContentValues();
        values.put(DatabaseConstants.COLUMN_HABIT_USER_ID, habit.getUserId());
        values.put(DatabaseConstants.COLUMN_HABIT_TYPE_ID, habit.getTypeId());
        values.put(DatabaseConstants.COLUMN_HABIT_NAME, habit.getName());
        values.put(DatabaseConstants.COLUMN_HABIT_START_DATE, habit.getStartDate());
        values.put(DatabaseConstants.COLUMN_HABIT_START_TIME, habit.getStartTime());
        values.put(DatabaseConstants.COLUMN_HABIT_END_TIME, habit.getEndTime());
        values.put(DatabaseConstants.COLUMN_HABIT_REMINDER_MINUTES, habit.getReminderMinutes());
        values.put(DatabaseConstants.COLUMN_HABIT_TARGET_DAYS, habit.getTargetDays());
        values.put(DatabaseConstants.COLUMN_HABIT_STATUS, habit.getStatus());
        values.put(DatabaseConstants.COLUMN_HABIT_STREAK_COUNT, habit.getStreakCount());
        values.put(DatabaseConstants.COLUMN_HABIT_LAST_COMPLETED_DATE, habit.getLastCompletedDate());

        return database.insert(DatabaseConstants.TABLE_HABITS, null, values);
    }

    @Override
    public Habit getById(long id) {
        Habit habit = null;
        Cursor cursor = getDatabase().query(
                DatabaseConstants.TABLE_HABITS,
                null,
                DatabaseConstants.COLUMN_ID + " = ?",
                new String[]{String.valueOf(id)},
                null, null, null
        );

        if (cursor != null && cursor.moveToFirst()) {
            habit = cursorToHabit(cursor);
            cursor.close();
        }
        return habit;
    }

    @Override
    public List<Habit> getByUserId(long userId) {
        return getByUserId(userId, false);
    }

    @Override
    public List<Habit> getByUserId(long userId, boolean includeArchived) {
        List<Habit> habits = new ArrayList<>();
        try {
            if (!getDatabase().isOpen()) {
                Log.e(TAG, "Database is not open!");
                return habits;
            }

            String query;
            String[] selectionArgs;
            if (includeArchived) {
                // Lấy tất cả thói quen
                query = "SELECT * FROM " + DatabaseConstants.TABLE_HABITS +
                        " WHERE " + DatabaseConstants.COLUMN_HABIT_USER_ID + " = ?" +
                        " ORDER BY " + DatabaseConstants.COLUMN_HABIT_START_TIME + " ASC";
                selectionArgs = new String[]{String.valueOf(userId)};
            } else {
                // Chỉ lấy thói quen chưa kết thúc hoặc chưa hoàn thành
                query = "SELECT * FROM " + DatabaseConstants.TABLE_HABITS +
                        " WHERE " + DatabaseConstants.COLUMN_HABIT_USER_ID + " = ?" +
                        " AND ((" + DatabaseConstants.COLUMN_HABIT_STATUS + " = 0) OR " +
                        "(JULIANDAY(" + DatabaseConstants.COLUMN_HABIT_START_DATE + ") + " +
                        DatabaseConstants.COLUMN_HABIT_TARGET_DAYS + " >= JULIANDAY('now')))" +
                        " ORDER BY " + DatabaseConstants.COLUMN_HABIT_START_TIME + " ASC";
                selectionArgs = new String[]{String.valueOf(userId)};
            }

            Cursor cursor = getDatabase().rawQuery(query, selectionArgs);

            if (cursor != null) {
                Log.d(TAG, "Cursor count: " + cursor.getCount());

                if (cursor.moveToFirst()) {
                    do {
                        try {
                            Habit habit = cursorToHabit(cursor);
                            Log.d(TAG, String.format("Found habit: ID=%d, Name=%s, UserID=%d",
                                    habit.getId(), habit.getName(), habit.getUserId()));
                            habits.add(habit);
                        } catch (Exception e) {
                            Log.e(TAG, "Error converting cursor to habit: " + e.getMessage());
                        }
                    } while (cursor.moveToNext());
                }
                cursor.close();
            }

            Log.d(TAG, "Total habits found: " + habits.size());

        } catch (Exception e) {
            Log.e(TAG, "Error querying habits: " + e.getMessage());
        }
        return habits;
    }

    @Override
    public List<Habit> getByType(long typeId) {
        List<Habit> habits = new ArrayList<>();
        Cursor cursor = getDatabase().query(
                DatabaseConstants.TABLE_HABITS,
                null,
                DatabaseConstants.COLUMN_HABIT_TYPE_ID + " = ?",
                new String[]{String.valueOf(typeId)},
                null,
                null,
                DatabaseConstants.COLUMN_HABIT_START_TIME + " ASC"
        );

        if (cursor != null && cursor.moveToFirst()) {
            do {
                habits.add(cursorToHabit(cursor));
            } while (cursor.moveToNext());
            cursor.close();
        }
        return habits;
    }

    @Override
    public int update(Habit habit) {
        ContentValues values = new ContentValues();
        values.put(DatabaseConstants.COLUMN_HABIT_NAME, habit.getName());
        values.put(DatabaseConstants.COLUMN_HABIT_TYPE_ID, habit.getTypeId());
        values.put(DatabaseConstants.COLUMN_HABIT_START_DATE, habit.getStartDate());
        values.put(DatabaseConstants.COLUMN_HABIT_START_TIME, habit.getStartTime());
        values.put(DatabaseConstants.COLUMN_HABIT_END_TIME, habit.getEndTime());
        values.put(DatabaseConstants.COLUMN_HABIT_REMINDER_MINUTES, habit.getReminderMinutes());
        values.put(DatabaseConstants.COLUMN_HABIT_TARGET_DAYS, habit.getTargetDays());
        values.put(DatabaseConstants.COLUMN_HABIT_STATUS, habit.getStatus());
        values.put(DatabaseConstants.COLUMN_HABIT_STREAK_COUNT, habit.getStreakCount());
        values.put(DatabaseConstants.COLUMN_HABIT_LAST_COMPLETED_DATE, habit.getLastCompletedDate());

        return getDatabase().update(
                DatabaseConstants.TABLE_HABITS,
                values,
                DatabaseConstants.COLUMN_ID + " = ?",
                new String[]{String.valueOf(habit.getId())}
        );
    }

    @Override
    public boolean delete(long habitId) {
        getDatabase().beginTransaction();
        try {
            Log.d(TAG, "Starting deletion process for habit: " + habitId);

            // Delete notifications
            int notificationsDeleted = getDatabase().delete(
                    DatabaseConstants.TABLE_NOTIFICATIONS,
                    DatabaseConstants.COLUMN_NOTIFICATION_HABIT_ID + " = ?",
                    new String[]{String.valueOf(habitId)}
            );
            Log.d(TAG, "Deleted " + notificationsDeleted + " notifications");

            // Delete daily status
            int statusDeleted = getDatabase().delete(
                    DatabaseConstants.TABLE_DAILY_STATUS,
                    DatabaseConstants.COLUMN_DAILY_HABIT_ID + " = ?",
                    new String[]{String.valueOf(habitId)}
            );
            Log.d(TAG, "Deleted " + statusDeleted + " daily status records");

            // Delete the habit
            int result = getDatabase().delete(
                    DatabaseConstants.TABLE_HABITS,
                    DatabaseConstants.COLUMN_ID + " = ?",
                    new String[]{String.valueOf(habitId)}
            );
            Log.d(TAG, "Habit deletion result: " + result);

            getDatabase().setTransactionSuccessful();
            return result > 0;
        } catch (Exception e) {
            Log.e(TAG, "Error deleting habit: " + e.getMessage());
            return false;
        } finally {
            getDatabase().endTransaction();
        }
    }

    @Override
    public void deleteAll() {
        getDatabase().beginTransaction();
        try {
            // Delete all related data first
            getDatabase().delete(DatabaseConstants.TABLE_NOTIFICATIONS, null, null);
            getDatabase().delete(DatabaseConstants.TABLE_DAILY_STATUS, null, null);
            getDatabase().delete(DatabaseConstants.TABLE_HABITS, null, null);
            getDatabase().setTransactionSuccessful();
        } catch (Exception e) {
            Log.e(TAG, "Error in deleteAll: " + e.getMessage());
        } finally {
            getDatabase().endTransaction();
        }
    }

    private Habit cursorToHabit(Cursor cursor) {
        try {
            Habit habit = new Habit();
            habit.setId(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseConstants.COLUMN_ID)));
            habit.setUserId(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseConstants.COLUMN_HABIT_USER_ID)));
            habit.setTypeId(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseConstants.COLUMN_HABIT_TYPE_ID)));
            habit.setName(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseConstants.COLUMN_HABIT_NAME)));
            habit.setStartDate(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseConstants.COLUMN_HABIT_START_DATE)));
            habit.setStartTime(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseConstants.COLUMN_HABIT_START_TIME)));
            habit.setEndTime(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseConstants.COLUMN_HABIT_END_TIME)));
            habit.setReminderMinutes(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseConstants.COLUMN_HABIT_REMINDER_MINUTES)));
            habit.setTargetDays(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseConstants.COLUMN_HABIT_TARGET_DAYS)));
            habit.setStatus(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseConstants.COLUMN_HABIT_STATUS)));
            habit.setStreakCount(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseConstants.COLUMN_HABIT_STREAK_COUNT)));
            habit.setLastCompletedDate(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseConstants.COLUMN_HABIT_LAST_COMPLETED_DATE)));
            return habit;
        } catch (Exception e) {
            Log.e(TAG, "Error converting cursor to habit: " + e.getMessage());
            throw e;
        }
    }
}