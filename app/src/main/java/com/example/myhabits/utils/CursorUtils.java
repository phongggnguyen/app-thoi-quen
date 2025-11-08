package com.example.myhabits.utils;

import android.database.Cursor;

public class CursorUtils {
    public static String getStringOrNull(Cursor cursor, String columnName) {
        int index = cursor.getColumnIndex(columnName);
        return index >= 0 && !cursor.isNull(index) ? cursor.getString(index) : null;
    }

    public static long getLongOrDefault(Cursor cursor, String columnName, long defaultValue) {
        int index = cursor.getColumnIndex(columnName);
        return index >= 0 && !cursor.isNull(index) ? cursor.getLong(index) : defaultValue;
    }

}
