package com.example.myhabits.database.impl;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.myhabits.database.dao.HabitTypeDao;
import com.example.myhabits.database.data.DatabaseConstants;
import com.example.myhabits.database.data.DatabaseHelper;
import com.example.myhabits.models.HabitType;
import com.example.myhabits.utils.CursorUtils;

import java.util.ArrayList;
import java.util.List;

public class HabitTypeDaoImpl implements HabitTypeDao {
    private SQLiteDatabase database;
    private final DatabaseHelper dbHelper;

    public HabitTypeDaoImpl(Context context) {
        dbHelper = DatabaseHelper.getInstance(context);
        database = dbHelper.getDatabase();
    }

    @Override
    public long insert(HabitType type) {
        // Kiểm tra tên trống
        if (type.getName() == null || type.getName().trim().isEmpty()) {
            return -1;
        }

        // Kiểm tra trùng tên
        Cursor cursor = database.query(
                DatabaseConstants.TABLE_HABIT_TYPES,
                new String[]{DatabaseConstants.COLUMN_ID},
                DatabaseConstants.COLUMN_TYPE_NAME + " = ?",
                new String[]{type.getName().trim()},
                null, null, null
        );

        boolean exists = cursor != null && cursor.moveToFirst();
        if (cursor != null) cursor.close();

        if (exists) {
            return -2; // Trả về -2 nếu tên đã tồn tại
        }

        ContentValues values = new ContentValues();
        values.put(DatabaseConstants.COLUMN_TYPE_NAME, type.getName().trim());
        values.put(DatabaseConstants.COLUMN_TYPE_COLOR, type.getColor());

        return database.insert(DatabaseConstants.TABLE_HABIT_TYPES, null, values);
    }

    @Override
    public HabitType getById(long id) {
        HabitType type = null;
        Cursor cursor = database.query(
                DatabaseConstants.TABLE_HABIT_TYPES,
                null,
                DatabaseConstants.COLUMN_ID + " = ?",
                new String[]{String.valueOf(id)},
                null, null, null
        );

        if (cursor != null && cursor.moveToFirst()) {
            type = cursorToHabitType(cursor);
            cursor.close();
        }
        return type;
    }

    @Override
    public int update(HabitType type) {
        ContentValues values = new ContentValues();
        values.put(DatabaseConstants.COLUMN_TYPE_NAME, type.getName());
        values.put(DatabaseConstants.COLUMN_TYPE_COLOR, type.getColor());

        return database.update(
                DatabaseConstants.TABLE_HABIT_TYPES,
                values,
                DatabaseConstants.COLUMN_ID + " = ?",
                new String[]{String.valueOf(type.getId())}
        );
    }

    @Override
    public boolean delete(long id) {
        // Kiểm tra xem có habits nào thuộc type này không
        Cursor cursor = database.query(
                DatabaseConstants.TABLE_HABITS,
                new String[]{DatabaseConstants.COLUMN_ID},
                DatabaseConstants.COLUMN_HABIT_TYPE_ID + " = ?",
                new String[]{String.valueOf(id)},
                null, null, null
        );

        boolean hasHabits = cursor != null && cursor.moveToFirst();
        if (cursor != null) cursor.close();

        if (hasHabits) {
            return false; // Không thể xóa vì có habits
        }

        return database.delete(
                DatabaseConstants.TABLE_HABIT_TYPES,
                DatabaseConstants.COLUMN_ID + " = ?",
                new String[]{String.valueOf(id)}
        ) > 0;
    }

    @Override
    public List<HabitType> getAll() {
        List<HabitType> types = new ArrayList<>();
        Cursor cursor = database.query(
                DatabaseConstants.TABLE_HABIT_TYPES,
                null,
                null,
                null,
                null,
                null,
                DatabaseConstants.COLUMN_TYPE_NAME + " ASC"
        );

        if (cursor != null && cursor.moveToFirst()) {
            do {
                types.add(cursorToHabitType(cursor));
            } while (cursor.moveToNext());
            cursor.close();
        }
        return types;
    }

    private HabitType cursorToHabitType(Cursor cursor) {
        HabitType type = new HabitType();

        type.setId(CursorUtils.getLongOrDefault(cursor, DatabaseConstants.COLUMN_ID, 0));
        type.setName(CursorUtils.getStringOrNull(cursor, DatabaseConstants.COLUMN_TYPE_NAME));
        type.setColor(CursorUtils.getStringOrNull(cursor, DatabaseConstants.COLUMN_TYPE_COLOR));

        return type;
    }

    @Override
    public void deleteAll() {
        database.delete(DatabaseConstants.TABLE_HABIT_TYPES, null, null);
    }
}
