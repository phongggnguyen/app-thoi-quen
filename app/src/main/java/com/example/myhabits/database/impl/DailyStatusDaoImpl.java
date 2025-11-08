package com.example.myhabits.database.impl;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.myhabits.database.dao.DailyStatusDao;
import com.example.myhabits.database.data.DBManager;
import com.example.myhabits.database.data.DatabaseConstants;
import com.example.myhabits.database.data.DatabaseHelper;
import com.example.myhabits.models.DailyStatus;
import com.example.myhabits.utils.DateUtils;
import com.example.myhabits.models.Habit;
import java.util.ArrayList;
import java.util.List;

public class DailyStatusDaoImpl implements DailyStatusDao {
    private SQLiteDatabase database;
    private final DatabaseHelper dbHelper;
    private Context context;

    public DailyStatusDaoImpl(Context context) {
        this.context = context;
        dbHelper = DatabaseHelper.getInstance(context);
        database = dbHelper.getDatabase();
    }

    @Override
    public long insert(DailyStatus status) {
        ContentValues values = new ContentValues();
        values.put(DatabaseConstants.COLUMN_DAILY_HABIT_ID, status.getHabitId());
        values.put(DatabaseConstants.COLUMN_DAILY_DATE, status.getDate());
        values.put(DatabaseConstants.COLUMN_DAILY_STATUS, status.getStatus());
        values.put(DatabaseConstants.COLUMN_DAILY_CHECK_TIME, status.getCheckTime());
        values.put(DatabaseConstants.COLUMN_DAILY_NOTE, status.getNote());
        values.put(DatabaseConstants.COLUMN_DAILY_NOTE_UPDATED_TIME, status.getNoteUpdatedTime());
        values.put(DatabaseConstants.COLUMN_DAILY_DAY_NUMBER, status.getDayNumber());
        return database.insert(DatabaseConstants.TABLE_DAILY_STATUS, null, values);
    }

    @Override
    public DailyStatus getById(long id) {
        DailyStatus status = null;
        Cursor cursor = database.query(
                DatabaseConstants.TABLE_DAILY_STATUS,
                null,
                DatabaseConstants.COLUMN_ID + " = ?",
                new String[]{String.valueOf(id)},
                null, null, null
        );

        if (cursor != null && cursor.moveToFirst()) {
            status = cursorToDailyStatus(cursor);
            cursor.close();
        }
        return status;
    }


    @Override
    public List<DailyStatus> getByHabitId(long habitId) {
        List<DailyStatus> statuses = new ArrayList<>();
        Cursor cursor = database.query(
                DatabaseConstants.TABLE_DAILY_STATUS,
                null,
                DatabaseConstants.COLUMN_DAILY_HABIT_ID + " = ?",
                new String[]{String.valueOf(habitId)},
                null,
                null,
                DatabaseConstants.COLUMN_DAILY_DATE + " ASC"
        );

        if (cursor != null && cursor.moveToFirst()) {
            do {
                statuses.add(cursorToDailyStatus(cursor));
            } while (cursor.moveToNext());
            cursor.close();
        }
        return statuses;
    }


    @Override
    public DailyStatus getByHabitIdAndDate(long habitId, String date) {
        DailyStatus status = null;
        String selection = DatabaseConstants.COLUMN_DAILY_HABIT_ID + " = ? AND " +
                DatabaseConstants.COLUMN_DAILY_DATE + " = ?";
        String[] selectionArgs = {String.valueOf(habitId), date};

        Cursor cursor = database.query(
                DatabaseConstants.TABLE_DAILY_STATUS,
                null,
                selection,
                selectionArgs,
                null,
                null,
                null
        );

        if (cursor != null && cursor.moveToFirst()) {
            status = cursorToDailyStatus(cursor);
            cursor.close();
        }
        return status;
    }

    @Override
    public int update(DailyStatus status) {
        ContentValues values = new ContentValues();
        values.put(DatabaseConstants.COLUMN_DAILY_STATUS, status.getStatus());
        values.put(DatabaseConstants.COLUMN_DAILY_CHECK_TIME, status.getCheckTime());
        values.put(DatabaseConstants.COLUMN_DAILY_NOTE, status.getNote());
        values.put(DatabaseConstants.COLUMN_DAILY_NOTE_UPDATED_TIME, status.getNoteUpdatedTime());

        return database.update(
                DatabaseConstants.TABLE_DAILY_STATUS,
                values,
                DatabaseConstants.COLUMN_ID + " = ?",
                new String[]{String.valueOf(status.getId())}
        );
    }

    @Override
    public boolean delete(long id) {
        return database.delete(
                DatabaseConstants.TABLE_DAILY_STATUS,
                DatabaseConstants.COLUMN_ID + " = ?",
                new String[]{String.valueOf(id)}
        ) > 0;
    }


    @Override
    public void markMissedDays(long habitId) {
        // Lấy thông tin thói quen
        Habit habit = DBManager.getInstance(context).getHabitDao().getById(habitId);
        if (habit == null) return;

        String currentDate = DateUtils.getCurrentDate();
        List<String> daysToCheck = DateUtils.getDailyDaysWithTarget(
                habit.getStartDate(),
                habit.getTargetDays()
        );

        for (String date : daysToCheck) {
            // Bỏ qua các ngày trong tương lai hoặc ngày hiện tại
            if (DateUtils.isDatePassed(date) && !DateUtils.isSameDay(date, currentDate)) {
                DailyStatus status = getByHabitIdAndDate(habitId, date);

                if (status == null) {
                    // Tạo status mới cho ngày bỏ lỡ
                    status = new DailyStatus(
                            habitId,
                            date,
                            DatabaseConstants.CHECK_STATUS_MISSED,
                            DateUtils.getDayNumberFromStartDate(habit.getStartDate(), date)
                    );
                    insert(status);
                } else if (status.getStatus() == DatabaseConstants.CHECK_STATUS_PENDING) {
                    // Cập nhật status từ pending sang missed
                    status.setStatus(DatabaseConstants.CHECK_STATUS_MISSED);
                    update(status);
                }
            }
        }
    }

    private DailyStatus cursorToDailyStatus(Cursor cursor) {
        DailyStatus status = new DailyStatus();
        status.setId(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseConstants.COLUMN_ID)));
        status.setHabitId(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseConstants.COLUMN_DAILY_HABIT_ID)));
        status.setDate(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseConstants.COLUMN_DAILY_DATE)));
        status.setStatus(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseConstants.COLUMN_DAILY_STATUS)));
        status.setCheckTime(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseConstants.COLUMN_DAILY_CHECK_TIME)));
        status.setNote(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseConstants.COLUMN_DAILY_NOTE)));
        status.setNoteUpdatedTime(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseConstants.COLUMN_DAILY_NOTE_UPDATED_TIME)));
        status.setDayNumber(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseConstants.COLUMN_DAILY_DAY_NUMBER)));
        return status;
    }

}