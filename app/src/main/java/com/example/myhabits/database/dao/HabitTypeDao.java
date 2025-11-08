package com.example.myhabits.database.dao;

import com.example.myhabits.models.HabitType;
import java.util.List;

public interface HabitTypeDao {
    long insert(HabitType type);
    HabitType getById(long id);
    List<HabitType> getAll();
    int update(HabitType type);
    boolean delete(long id);
    void deleteAll();
}