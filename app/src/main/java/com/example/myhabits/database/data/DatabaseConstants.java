package com.example.myhabits.database.data;

public class DatabaseConstants {
    // Database Version and Name
    public static final String DATABASE_NAME = "Test1.db";
    public static final int DATABASE_VERSION = 4; // Tăng version lên 1

    // Status Constants
    public static final int CHECK_STATUS_PENDING = 0;   // Chờ thực hiện
    public static final int CHECK_STATUS_MISSED = 1;    // Bỏ lỡ
    public static final int CHECK_STATUS_COMPLETED = 3; // Đã hoàn thành
    public static final String COLUMN_NOTIFICATION_HABIT_START_TIME = "habit_start_time";
    public static final String COLUMN_NOTIFICATION_HABIT_END_TIME = "habit_end_time";

    public static final String COLUMN_NOTIFICATION_HABIT_ID = "habit_id";
    public static final String COLUMN_NOTIFICATION_TITLE = "title";
    public static final String COLUMN_NOTIFICATION_CONTENT = "content";
    public static final String COLUMN_NOTIFICATION_HABIT_TIME = "habit_time";
    public static final String COLUMN_NOTIFICATION_DAY_NUMBER = "day_number";
    public static final String COLUMN_NOTIFICATION_TIME = "notify_time";
    public static final String COLUMN_NOTIFICATION_IS_READ = "is_read";
    public static final String COLUMN_NOTIFICATION_TYPE = "type";

    // Table Names
    public static final String TABLE_USERS = "users";
    public static final String TABLE_HABIT_TYPES = "habit_types";
    public static final String TABLE_HABITS = "habits";
    public static final String TABLE_DAILY_STATUS = "daily_status";
    public static final String TABLE_NOTES = "notes";
    public static final String TABLE_NOTIFICATIONS = "notifications";

    // Common Column Names
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_CREATED_AT = "created_at";
    public static final String COLUMN_UPDATED_AT = "updated_at";

    // Users Table Columns
    public static final String COLUMN_USER_NAME = "name";
    public static final String COLUMN_USER_AVATAR = "avatar";

    // Habit Types Table Columns
    public static final String COLUMN_TYPE_NAME = "name";
    public static final String COLUMN_TYPE_COLOR = "color";

    // Habits Table Columns
    public static final String COLUMN_HABIT_USER_ID = "user_id";
    public static final String COLUMN_HABIT_TYPE_ID = "type_id";
    public static final String COLUMN_HABIT_NAME = "name";
    public static final String COLUMN_HABIT_START_DATE = "start_date";
    public static final String COLUMN_HABIT_START_TIME = "start_time";
    public static final String COLUMN_HABIT_END_TIME = "end_time";
    public static final String COLUMN_HABIT_REMINDER_MINUTES = "reminder_minutes";
    public static final String COLUMN_HABIT_STATUS = "status";
    public static final String COLUMN_HABIT_STREAK_COUNT = "streak_count";
    public static final String COLUMN_HABIT_LAST_COMPLETED_DATE = "last_completed_date";
    public static final String COLUMN_HABIT_TARGET_DAYS = "target_days";

    // Daily Status Table Columns
    public static final String COLUMN_DAILY_HABIT_ID = "habit_id";
    public static final String COLUMN_DAILY_DATE = "date";
    public static final String COLUMN_DAILY_STATUS = "status";
    public static final String COLUMN_DAILY_CHECK_TIME = "check_time";
    public static final String COLUMN_DAILY_NOTE = "note";
    public static final String COLUMN_DAILY_DAY_NUMBER = "day_number";
    public static final String COLUMN_DAILY_NOTE_UPDATED_TIME = "note_updated_time";

    // Notes Table Columns
    public static final String COLUMN_NOTE_USER_ID = "user_id";
    public static final String COLUMN_NOTE_TITLE = "title";
    public static final String COLUMN_NOTE_CONTENT = "content";

    // Create Table Statements
    public static final String CREATE_TABLE_USERS = "CREATE TABLE " + TABLE_USERS + "("
            + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + COLUMN_USER_NAME + " TEXT NOT NULL,"
            + COLUMN_USER_AVATAR + " TEXT,"
            + COLUMN_CREATED_AT + " INTEGER"
            + ")";

    public static final String CREATE_TABLE_HABIT_TYPES = "CREATE TABLE " + TABLE_HABIT_TYPES + "("
            + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + COLUMN_TYPE_NAME + " TEXT NOT NULL,"
            + COLUMN_TYPE_COLOR + " TEXT"
            + ")";

    public static final String CREATE_TABLE_HABITS = "CREATE TABLE " + TABLE_HABITS + "("
            + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + COLUMN_HABIT_USER_ID + " INTEGER NOT NULL,"
            + COLUMN_HABIT_TYPE_ID + " INTEGER NOT NULL,"
            + COLUMN_HABIT_NAME + " TEXT NOT NULL,"
            + COLUMN_HABIT_START_DATE + " TEXT NOT NULL,"
            + COLUMN_HABIT_START_TIME + " TEXT NOT NULL,"
            + COLUMN_HABIT_END_TIME + " TEXT NOT NULL,"
            + COLUMN_HABIT_REMINDER_MINUTES + " INTEGER NOT NULL,"
            + COLUMN_HABIT_STATUS + " INTEGER DEFAULT 0,"
            + COLUMN_HABIT_STREAK_COUNT + " INTEGER DEFAULT 0,"
            + COLUMN_HABIT_LAST_COMPLETED_DATE + " TEXT,"
            + COLUMN_HABIT_TARGET_DAYS + " INTEGER NOT NULL,"
            + COLUMN_CREATED_AT + " DATETIME DEFAULT CURRENT_TIMESTAMP,"
            + COLUMN_UPDATED_AT + " DATETIME DEFAULT CURRENT_TIMESTAMP,"
            + "FOREIGN KEY(" + COLUMN_HABIT_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_ID + "),"
            + "FOREIGN KEY(" + COLUMN_HABIT_TYPE_ID + ") REFERENCES " + TABLE_HABIT_TYPES + "(" + COLUMN_ID + ") ON DELETE CASCADE"
            + ")";


    public static final String CREATE_TABLE_DAILY_STATUS = "CREATE TABLE " + TABLE_DAILY_STATUS + "("
            + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + COLUMN_DAILY_HABIT_ID + " INTEGER,"
            + COLUMN_DAILY_DATE + " TEXT NOT NULL,"
            + COLUMN_DAILY_STATUS + " INTEGER DEFAULT 0,"
            + COLUMN_DAILY_CHECK_TIME + " INTEGER,"
            + COLUMN_DAILY_NOTE + " TEXT,"
            + COLUMN_DAILY_NOTE_UPDATED_TIME + " INTEGER DEFAULT 0,"
            + COLUMN_DAILY_DAY_NUMBER + " INTEGER,"
            + "FOREIGN KEY(" + COLUMN_DAILY_HABIT_ID + ") REFERENCES habits(" + COLUMN_ID + ")"
            + "FOREIGN KEY(" + COLUMN_DAILY_HABIT_ID + ") REFERENCES " + TABLE_HABITS
            + "(" + COLUMN_ID + ") ON DELETE CASCADE"
            + ")";

    // Create Table Statement for Notes
    public static final String CREATE_TABLE_NOTES = "CREATE TABLE " + TABLE_NOTES + "("
            + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + COLUMN_NOTE_USER_ID + " INTEGER NOT NULL,"
            + COLUMN_NOTE_TITLE + " TEXT NOT NULL,"
            + COLUMN_NOTE_CONTENT + " TEXT,"
            + COLUMN_CREATED_AT + " INTEGER NOT NULL,"
            + COLUMN_UPDATED_AT + " INTEGER NOT NULL,"
            + "FOREIGN KEY(" + COLUMN_NOTE_USER_ID + ") REFERENCES "
            + TABLE_USERS + "(" + COLUMN_ID + ")"
            + ")";

    public static final String CREATE_TABLE_NOTIFICATIONS = "CREATE TABLE " + TABLE_NOTIFICATIONS + "("
            + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + COLUMN_NOTIFICATION_HABIT_ID + " INTEGER,"
            + COLUMN_NOTIFICATION_TITLE + " TEXT,"
            + COLUMN_NOTIFICATION_CONTENT + " TEXT,"
            + COLUMN_NOTIFICATION_HABIT_TIME + " TEXT,"
            + COLUMN_NOTIFICATION_HABIT_START_TIME + " TEXT,"
            + COLUMN_NOTIFICATION_HABIT_END_TIME + " TEXT,"
            + COLUMN_NOTIFICATION_DAY_NUMBER + " INTEGER,"
            + COLUMN_NOTIFICATION_TIME + " INTEGER,"
            + COLUMN_NOTIFICATION_IS_READ + " INTEGER DEFAULT 0,"
            + COLUMN_NOTIFICATION_TYPE + " INTEGER,"
            + "FOREIGN KEY(" + COLUMN_NOTIFICATION_HABIT_ID + ") REFERENCES habits(" + COLUMN_ID + ")"
            + ")";
}