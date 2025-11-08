package com.example.myhabits.database.impl;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.myhabits.database.dao.NoteDao;
import com.example.myhabits.database.data.DatabaseConstants;
import com.example.myhabits.database.data.DatabaseHelper;
import com.example.myhabits.models.Note;
import com.example.myhabits.utils.CursorUtils;

import java.util.ArrayList;
import java.util.List;

public class NoteDaoImpl implements NoteDao {
    private SQLiteDatabase database;
    private final DatabaseHelper dbHelper;

    public NoteDaoImpl(Context context) {
        dbHelper = DatabaseHelper.getInstance(context);
        database = dbHelper.getDatabase();
    }

    @Override
    public long insert(Note note) {
        ContentValues values = new ContentValues();
        values.put(DatabaseConstants.COLUMN_NOTE_USER_ID, note.getUserId());
        values.put(DatabaseConstants.COLUMN_NOTE_TITLE, note.getTitle());
        values.put(DatabaseConstants.COLUMN_NOTE_CONTENT, note.getContent());
        values.put(DatabaseConstants.COLUMN_CREATED_AT, note.getCreatedAt());
        values.put(DatabaseConstants.COLUMN_UPDATED_AT, note.getUpdatedAt());

        return database.insert(DatabaseConstants.TABLE_NOTES, null, values);
    }

    @Override
    public Note getById(long id) {
        Note note = null;
        Cursor cursor = database.query(
                DatabaseConstants.TABLE_NOTES,
                null,
                DatabaseConstants.COLUMN_ID + " = ?",
                new String[]{String.valueOf(id)},
                null, null, null
        );

        if (cursor != null && cursor.moveToFirst()) {
            note = cursorToNote(cursor);
            cursor.close();
        }
        return note;
    }

    @Override
    public List<Note> getByUserId(long userId) {
        List<Note> notes = new ArrayList<>();
        Cursor cursor = database.query(
                DatabaseConstants.TABLE_NOTES,
                null,
                DatabaseConstants.COLUMN_NOTE_USER_ID + " = ?",
                new String[]{String.valueOf(userId)},
                null,
                null,
                DatabaseConstants.COLUMN_UPDATED_AT + " DESC"
        );

        if (cursor != null && cursor.moveToFirst()) {
            do {
                notes.add(cursorToNote(cursor));
            } while (cursor.moveToNext());
            cursor.close();
        }
        return notes;
    }

    @Override
    public int update(Note note) {
        ContentValues values = new ContentValues();
        values.put(DatabaseConstants.COLUMN_NOTE_TITLE, note.getTitle());
        values.put(DatabaseConstants.COLUMN_NOTE_CONTENT, note.getContent());
        values.put(DatabaseConstants.COLUMN_UPDATED_AT, System.currentTimeMillis());

        return database.update(
                DatabaseConstants.TABLE_NOTES,
                values,
                DatabaseConstants.COLUMN_ID + " = ?",
                new String[]{String.valueOf(note.getId())}
        );
    }

    @Override
    public boolean delete(long id) {
        return database.delete(
                DatabaseConstants.TABLE_NOTES,
                DatabaseConstants.COLUMN_ID + " = ?",
                new String[]{String.valueOf(id)}
        ) > 0;
    }

    @Override
    public void deleteByUserId(long userId) {
        database.delete(
                DatabaseConstants.TABLE_NOTES,
                DatabaseConstants.COLUMN_NOTE_USER_ID + " = ?",
                new String[]{String.valueOf(userId)}
        );
    }

    private Note cursorToNote(Cursor cursor) {
        Note note = new Note();
        note.setId(CursorUtils.getLongOrDefault(cursor, DatabaseConstants.COLUMN_ID, 0));
        note.setUserId(CursorUtils.getLongOrDefault(cursor, DatabaseConstants.COLUMN_NOTE_USER_ID, 0));
        note.setTitle(CursorUtils.getStringOrNull(cursor, DatabaseConstants.COLUMN_NOTE_TITLE));
        note.setContent(CursorUtils.getStringOrNull(cursor, DatabaseConstants.COLUMN_NOTE_CONTENT));
        note.setCreatedAt(CursorUtils.getLongOrDefault(cursor, DatabaseConstants.COLUMN_CREATED_AT, 0));
        note.setUpdatedAt(CursorUtils.getLongOrDefault(cursor, DatabaseConstants.COLUMN_UPDATED_AT, 0));
        return note;
    }
}