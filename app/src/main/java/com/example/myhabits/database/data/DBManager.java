package com.example.myhabits.database.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.example.myhabits.database.dao.DailyStatusDao;
import com.example.myhabits.database.dao.HabitDao;
import com.example.myhabits.database.dao.HabitNotificationDao;
import com.example.myhabits.database.dao.HabitTypeDao;
import com.example.myhabits.database.dao.NoteDao;
import com.example.myhabits.database.dao.UserDao;
import com.example.myhabits.database.impl.DailyStatusDaoImpl;
import com.example.myhabits.database.impl.HabitDaoImpl;
import com.example.myhabits.database.impl.HabitNotificationDaoImpl;
import com.example.myhabits.database.impl.HabitTypeDaoImpl;
import com.example.myhabits.database.impl.NoteDaoImpl;
import com.example.myhabits.database.impl.UserDaoImpl;

public class DBManager {
    private static DBManager instance;
    private final Context context;
    private DatabaseHelper dbHelper;
    private UserDao userDao;
    private HabitTypeDao habitTypeDao;
    private HabitDao habitDao;
    private DailyStatusDao dailyStatusDao;
    private NoteDao noteDao;
    private SQLiteDatabase database;
    private HabitNotificationDao habitNotificationDao;

    private DBManager(Context context) {
        this.context = context;
        init();
        checkAndUpgradeDatabase();
    }

    public static synchronized DBManager getInstance(Context context) {
        if (instance == null) {
            instance = new DBManager(context.getApplicationContext());
        }
        return instance;
    }

    private void init() {
        dbHelper = DatabaseHelper.getInstance(context);
        database = dbHelper.getWritableDatabase();
        userDao = new UserDaoImpl(context);
        habitTypeDao = new HabitTypeDaoImpl(context);
        habitDao = new HabitDaoImpl(context);
        dailyStatusDao = new DailyStatusDaoImpl(context);
        noteDao = new NoteDaoImpl(context);
    }

    // Thêm các phương thức transaction
    public void beginTransaction() {
        if (database != null) {
            database.beginTransaction();
        }
    }

    public void setTransactionSuccessful() {
        if (database != null) {
            database.setTransactionSuccessful();
        }
    }

    public void endTransaction() {
        if (database != null) {
            database.endTransaction();
        }
    }



    public UserDao getUserDao() {
        return userDao;
    }

    public HabitTypeDao getHabitTypeDao() {
        return habitTypeDao;
    }

    public HabitDao getHabitDao() {
        return habitDao;
    }

    public DailyStatusDao getDailyStatusDao() {
        return dailyStatusDao;
    }


    public NoteDao getNoteDao() {
        return noteDao;
    }

    public void checkAndUpgradeDatabase() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int currentVersion = db.getVersion();

        if (currentVersion < DatabaseConstants.DATABASE_VERSION) {
            dbHelper.onUpgrade(db, currentVersion, DatabaseConstants.DATABASE_VERSION);
        }
    }

    public void closeDatabase() {
        if (database != null && database.isOpen()) {
            database.close();
        }
        if (dbHelper != null) {
            dbHelper.close();
        }
    }

    public void openDatabase() {
        if (database == null || !database.isOpen()) {
            database = dbHelper.getWritableDatabase();
        }
    }

    public HabitNotificationDao getHabitNotificationDao() {
        if (habitNotificationDao == null) {
            habitNotificationDao = new HabitNotificationDaoImpl(context);
        }
        return habitNotificationDao;
    }

    public SQLiteDatabase getDatabase() {
        if (database == null || !database.isOpen()) {
            database = dbHelper.getWritableDatabase();
        }
        return database;
    }

}