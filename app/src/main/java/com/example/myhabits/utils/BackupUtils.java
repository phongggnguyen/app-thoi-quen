package com.example.myhabits.utils;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import com.example.myhabits.database.data.DBManager;
import com.example.myhabits.database.data.DatabaseConstants;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class BackupUtils {
    private static final String TAG = "BackupUtils";
    private static final String BACKUP_FOLDER = "MyHabits";
    private static final String BACKUP_PREFIX = "myhabits_backup_";
    private static final String BACKUP_SUFFIX = ".db";
    private static final String BACKUP_PATTERN = "yyyyMMdd_HHmmss";

    public static File getBackupFolder() {
        File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File backupFolder = new File(downloadsDir, BACKUP_FOLDER);
        if (!backupFolder.exists()) {
            backupFolder.mkdirs();
        }
        return backupFolder;
    }

    public static String createBackup(Context context) {
        try {
            String timestamp = new SimpleDateFormat(BACKUP_PATTERN, Locale.getDefault())
                    .format(new Date());
            String backupFileName = BACKUP_PREFIX + timestamp + BACKUP_SUFFIX;
            File backupFile = new File(getBackupFolder(), backupFileName);
            File currentDB = context.getDatabasePath(DatabaseConstants.DATABASE_NAME);
            if (currentDB.exists()) {
                copyFile(currentDB, backupFile);
                Log.d(TAG, "Backup created successfully at: " + backupFile.getAbsolutePath());
                return backupFile.getAbsolutePath();
            } else {
                Log.e(TAG, "Database file not found");
                return null;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error creating backup: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public static boolean restoreBackup(Context context, Uri uri) {
        if (uri == null) return false;

        FileOutputStream outputStream = null;
        InputStream inputStream = null;

        try {
            // Đóng kết nối database hiện tại
            DBManager.getInstance(context).closeDatabase();

            // Tạo input stream từ uri được chọn
            inputStream = context.getContentResolver().openInputStream(uri);
            if (inputStream == null) {
                Log.e(TAG, "Failed to open input stream from Uri");
                return false;
            }

            // Lấy file database hiện tại
            File currentDB = context.getDatabasePath(DatabaseConstants.DATABASE_NAME);

            // Tạo file tạm để kiểm tra tính hợp lệ của file backup
            File tempDB = new File(currentDB.getParent(), "temp_" + DatabaseConstants.DATABASE_NAME);

            // Copy vào file tạm trước
            outputStream = new FileOutputStream(tempDB);
            byte[] buffer = new byte[8192];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
            outputStream.flush();
            outputStream.close();
            inputStream.close();

            // Kiểm tra file tạm có phải là SQLite database hợp lệ không
            try {
                SQLiteDatabase checkDB = SQLiteDatabase.openDatabase(
                        tempDB.getPath(), null, SQLiteDatabase.OPEN_READONLY);
                checkDB.close();
            } catch (SQLiteException e) {
                Log.e(TAG, "Invalid database file");
                tempDB.delete();
                throw new Exception("File khôi phục không phải là database hợp lệ");
            }

            // Nếu ok thì copy file tạm vào file chính
            if (currentDB.exists()) {
                currentDB.delete();
            }
            tempDB.renameTo(currentDB);

            // Mở lại kết nối database
            DBManager.getInstance(context).openDatabase();

            Log.d(TAG, "Database restored successfully");
            return true;

        } catch (Exception e) {
            Log.e(TAG, "Error restoring backup: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (outputStream != null) outputStream.close();
                if (inputStream != null) inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void copyFile(File src, File dst) throws IOException {
        try (FileInputStream in = new FileInputStream(src);
             FileOutputStream out = new FileOutputStream(dst)) {
            byte[] buffer = new byte[8192];
            int length;
            while ((length = in.read(buffer)) > 0) {
                out.write(buffer, 0, length);
            }
            out.flush();
        }
    }

    public static boolean isValidBackupFile(String fileName) {
        return fileName != null &&
                fileName.startsWith(BACKUP_PREFIX) &&
                fileName.endsWith(BACKUP_SUFFIX) &&
                fileName.matches(BACKUP_PREFIX + "\\d{8}_\\d{6}\\" + BACKUP_SUFFIX);
    }
}