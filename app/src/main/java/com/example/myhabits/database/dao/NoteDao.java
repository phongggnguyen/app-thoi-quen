package com.example.myhabits.database.dao;

import com.example.myhabits.models.Note;
import java.util.List;

public interface NoteDao {
    long insert(Note note);
    Note getById(long id);
    List<Note> getByUserId(long userId);
    int update(Note note);
    boolean delete(long id);
    void deleteByUserId(long userId);
}