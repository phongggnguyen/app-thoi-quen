package com.example.myhabits.database.impl;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.myhabits.database.dao.UserDao;
import com.example.myhabits.database.data.DatabaseConstants;
import com.example.myhabits.database.data.DatabaseHelper;
import com.example.myhabits.models.User;
import com.example.myhabits.utils.CursorUtils;

import java.util.ArrayList;
import java.util.List;

public class UserDaoImpl implements UserDao {
    private static final String TAG = "UserDaoImpl";
    private final SQLiteDatabase database;
    private final DatabaseHelper dbHelper;

    public UserDaoImpl(Context context) {
        dbHelper = DatabaseHelper.getInstance(context);
        database = dbHelper.getWritableDatabase();
    }

    @Override
    public boolean delete(long userId) {
        database.beginTransaction();
        try {
            // Log the operation
            Log.d(TAG, "Starting deletion process for user: " + userId);

            // Delete all notifications for user's habits
            String deleteNotifications = String.format(
                    "DELETE FROM %s WHERE %s IN (SELECT %s FROM %s WHERE %s = ?)",
                    DatabaseConstants.TABLE_NOTIFICATIONS,
                    DatabaseConstants.COLUMN_NOTIFICATION_HABIT_ID,
                    DatabaseConstants.COLUMN_ID,
                    DatabaseConstants.TABLE_HABITS,
                    DatabaseConstants.COLUMN_HABIT_USER_ID
            );
            database.execSQL(deleteNotifications, new String[]{String.valueOf(userId)});
            Log.d(TAG, "Deleted notifications for user's habits");

            // Delete daily status for user's habits
            String deleteDailyStatus = String.format(
                    "DELETE FROM %s WHERE %s IN (SELECT %s FROM %s WHERE %s = ?)",
                    DatabaseConstants.TABLE_DAILY_STATUS,
                    DatabaseConstants.COLUMN_DAILY_HABIT_ID,
                    DatabaseConstants.COLUMN_ID,
                    DatabaseConstants.TABLE_HABITS,
                    DatabaseConstants.COLUMN_HABIT_USER_ID
            );
            database.execSQL(deleteDailyStatus, new String[]{String.valueOf(userId)});
            Log.d(TAG, "Deleted daily status for user's habits");

            // Delete habits
            int habitsDeleted = database.delete(
                    DatabaseConstants.TABLE_HABITS,
                    DatabaseConstants.COLUMN_HABIT_USER_ID + " = ?",
                    new String[]{String.valueOf(userId)}
            );
            Log.d(TAG, "Deleted " + habitsDeleted + " habits");

            // Delete notes
            int notesDeleted = database.delete(
                    DatabaseConstants.TABLE_NOTES,
                    DatabaseConstants.COLUMN_NOTE_USER_ID + " = ?",
                    new String[]{String.valueOf(userId)}
            );
            Log.d(TAG, "Deleted " + notesDeleted + " notes");

            // Finally delete user
            int result = database.delete(
                    DatabaseConstants.TABLE_USERS,
                    DatabaseConstants.COLUMN_ID + " = ?",
                    new String[]{String.valueOf(userId)}
            );
            Log.d(TAG, "User deletion result: " + result);

            database.setTransactionSuccessful();
            return result > 0;
        } catch (Exception e) {
            Log.e(TAG, "Error deleting user: " + e.getMessage());
            return false;
        } finally {
            database.endTransaction();
        }
    }


    @Override
    public long insert(User user) {
        ContentValues values = new ContentValues();
        values.put(DatabaseConstants.COLUMN_USER_NAME, user.getName());
        values.put(DatabaseConstants.COLUMN_USER_AVATAR, user.getAvatar());
        values.put(DatabaseConstants.COLUMN_CREATED_AT, System.currentTimeMillis());

        return database.insert(DatabaseConstants.TABLE_USERS, null, values);
    }

    @Override
    public User getById(long id) {
        User user = null;
        Cursor cursor = database.query(
                DatabaseConstants.TABLE_USERS,
                null,
                DatabaseConstants.COLUMN_ID + " = ?",
                new String[]{String.valueOf(id)},
                null, null, null
        );

        if (cursor != null && cursor.moveToFirst()) {
            user = cursorToUser(cursor);
            cursor.close();
        }
        return user;
    }

    @Override
    public List<User> getAll() {
        List<User> users = new ArrayList<>();
        Cursor cursor = database.query(
                DatabaseConstants.TABLE_USERS,
                null,
                null,
                null,
                null,
                null,
                DatabaseConstants.COLUMN_CREATED_AT + " DESC"
        );

        if (cursor != null && cursor.moveToFirst()) {
            do {
                users.add(cursorToUser(cursor));
            } while (cursor.moveToNext());
            cursor.close();
        }
        return users;
    }

    @Override
    public int update(User user) {
        ContentValues values = new ContentValues();
        values.put(DatabaseConstants.COLUMN_USER_NAME, user.getName());
        values.put(DatabaseConstants.COLUMN_USER_AVATAR, user.getAvatar());

        return database.update(
                DatabaseConstants.TABLE_USERS,
                values,
                DatabaseConstants.COLUMN_ID + " = ?",
                new String[]{String.valueOf(user.getId())}
        );
    }


    @Override
    public User getFirstUser() {
        User user = null;
        Cursor cursor = database.query(
                DatabaseConstants.TABLE_USERS,
                null,
                null,
                null,
                null,
                null,
                DatabaseConstants.COLUMN_CREATED_AT + " ASC", // Lấy user được tạo sớm nhất
                "1" // LIMIT 1
        );

        if (cursor != null && cursor.moveToFirst()) {
            user = cursorToUser(cursor);
            cursor.close();
        }
        return user;
    }

    private User cursorToUser(Cursor cursor) {
        User user = new User();
        user.setId(CursorUtils.getLongOrDefault(cursor, DatabaseConstants.COLUMN_ID, 0));
        user.setName(CursorUtils.getStringOrNull(cursor, DatabaseConstants.COLUMN_USER_NAME));
        user.setAvatar(CursorUtils.getStringOrNull(cursor, DatabaseConstants.COLUMN_USER_AVATAR));
        user.setCreatedAt(CursorUtils.getLongOrDefault(cursor, DatabaseConstants.COLUMN_CREATED_AT, 0));
        return user;
    }
}