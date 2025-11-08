package com.example.myhabits.database.data;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String TAG = "DatabaseHelper";
    private static DatabaseHelper instance;
    private SQLiteDatabase database;

    private DatabaseHelper(Context context) {
        super(context.getApplicationContext(), DatabaseConstants.DATABASE_NAME, null, DatabaseConstants.DATABASE_VERSION);
    }

    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context);
        }
        return instance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            Log.d(TAG, "Creating database tables...");
            createAllTables(db);
            Log.d(TAG, "Database tables created successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error creating database tables: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void createAllTables(SQLiteDatabase db) {
        db.execSQL(DatabaseConstants.CREATE_TABLE_USERS);
        db.execSQL(DatabaseConstants.CREATE_TABLE_HABIT_TYPES);
        db.execSQL(DatabaseConstants.CREATE_TABLE_HABITS);
        db.execSQL(DatabaseConstants.CREATE_TABLE_DAILY_STATUS);
        db.execSQL(DatabaseConstants.CREATE_TABLE_NOTES);
        db.execSQL(DatabaseConstants.CREATE_TABLE_NOTIFICATIONS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion);

        try {
            // Version 2: Thêm note_updated_time
            if (oldVersion < 2) {
                upgradeToVersion12(db);
            }

            // Version 3: Thêm bảng notifications
            if (oldVersion < 3) {
                if (!isTableExists(db, DatabaseConstants.TABLE_NOTIFICATIONS)) {
                    db.execSQL(DatabaseConstants.CREATE_TABLE_NOTIFICATIONS);
                }
            }

            // Version 4: Thêm các cột mới cho notifications
            if (oldVersion < 4) {
                try {
                    db.execSQL("ALTER TABLE " + DatabaseConstants.TABLE_NOTIFICATIONS +
                            " ADD COLUMN " + DatabaseConstants.COLUMN_NOTIFICATION_HABIT_START_TIME + " TEXT");
                    db.execSQL("ALTER TABLE " + DatabaseConstants.TABLE_NOTIFICATIONS +
                            " ADD COLUMN " + DatabaseConstants.COLUMN_NOTIFICATION_HABIT_END_TIME + " TEXT");
                } catch (Exception e) {
                    Log.e(TAG, "Error adding notification time columns: " + e.getMessage());
                }
            }

        } catch (Exception e) {
            Log.e(TAG, "Error during database upgrade: " + e.getMessage());
            throw new RuntimeException("Database upgrade failed");
        }
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Không cho phép hạ cấp database
        throw new RuntimeException("Database downgrade is not supported");
    }

    /**
     * Nâng cấp lên version 12: Thêm cột note_updated_time vào bảng daily_status
     */
    private void upgradeToVersion12(SQLiteDatabase db) {
        Log.d(TAG, "Starting upgrade to version 12...");

        if (!isColumnExists(db, DatabaseConstants.TABLE_DAILY_STATUS,
                DatabaseConstants.COLUMN_DAILY_NOTE_UPDATED_TIME)) {

            try {
                // Thêm cột mới bằng ALTER TABLE
                String alterTableSQL = "ALTER TABLE " + DatabaseConstants.TABLE_DAILY_STATUS +
                        " ADD COLUMN " + DatabaseConstants.COLUMN_DAILY_NOTE_UPDATED_TIME +
                        " INTEGER DEFAULT 0";

                db.execSQL(alterTableSQL);
                Log.d(TAG, "Added column note_updated_time to daily_status table");

            } catch (Exception e) {
                Log.e(TAG, "Error adding column note_updated_time: " + e.getMessage());
                throw e;
            }
        } else {
            Log.d(TAG, "Column note_updated_time already exists in daily_status table");
        }
    }

    private boolean isColumnExists(SQLiteDatabase db, String tableName, String columnName) {
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("PRAGMA table_info(" + tableName + ")", null);
            if (cursor != null) {
                int nameIndex = cursor.getColumnIndex("name");
                if (nameIndex == -1) {
                    Log.e(TAG, "Column 'name' not found in PRAGMA result");
                    return false;
                }
                while (cursor.moveToNext()) {
                    String name = cursor.getString(nameIndex);
                    if (columnName.equalsIgnoreCase(name)) {
                        return true;
                    }
                }
            }
            return false;
        } catch (Exception e) {
            Log.e(TAG, "Error checking if column exists: " + e.getMessage());
            return false;
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
    }

    private boolean isTableExists(SQLiteDatabase db, String tableName) {
        Cursor cursor = null;
        try {
            cursor = db.rawQuery(
                    "SELECT name FROM sqlite_master WHERE type='table' AND name=?",
                    new String[]{tableName}
            );
            return cursor != null && cursor.getCount() > 0;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public void verifyDatabaseIntegrity() {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            // Kiểm tra từng bảng và tạo nếu chưa tồn tại
            if (!isTableExists(db, DatabaseConstants.TABLE_USERS)) {
                db.execSQL(DatabaseConstants.CREATE_TABLE_USERS);
            }
            if (!isTableExists(db, DatabaseConstants.TABLE_HABIT_TYPES)) {
                db.execSQL(DatabaseConstants.CREATE_TABLE_HABIT_TYPES);
            }
            if (!isTableExists(db, DatabaseConstants.TABLE_HABITS)) {
                db.execSQL(DatabaseConstants.CREATE_TABLE_HABITS);
            }
            if (!isTableExists(db, DatabaseConstants.TABLE_DAILY_STATUS)) {
                db.execSQL(DatabaseConstants.CREATE_TABLE_DAILY_STATUS);
            }
            if (!isTableExists(db, DatabaseConstants.TABLE_NOTES)) {
                db.execSQL(DatabaseConstants.CREATE_TABLE_NOTES);
            }
            if (!isTableExists(db, DatabaseConstants.TABLE_NOTIFICATIONS)) {
                db.execSQL(DatabaseConstants.CREATE_TABLE_NOTIFICATIONS);
            }

            Log.d(TAG, "Database integrity verification completed");
        } catch (Exception e) {
            Log.e(TAG, "Error during database integrity verification: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public synchronized void close() {
        if (database != null) {
            database.close();
            database = null;
        }
        super.close();
    }

    public SQLiteDatabase getDatabase() {
        if (database == null || !database.isOpen()) {
            database = this.getWritableDatabase();
        }
        return database;
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        // Enable foreign key constraints
        if (!db.isReadOnly()) {
            db.execSQL("PRAGMA foreign_keys=ON");
        }
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        // Enable WAL mode for better concurrency
        db.setForeignKeyConstraintsEnabled(true);
        db.enableWriteAheadLogging();
    }
}