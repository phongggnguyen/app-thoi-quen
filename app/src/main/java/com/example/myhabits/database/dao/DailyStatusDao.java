package com.example.myhabits.database.dao;

import com.example.myhabits.models.DailyStatus;
import java.util.List;

public interface DailyStatusDao {
    long insert(DailyStatus status);
    DailyStatus getById(long id);
    int update(DailyStatus status);
    boolean delete(long id);
    List<DailyStatus> getByHabitId(long habitId);
    DailyStatus getByHabitIdAndDate(long habitId, String date);
    void markMissedDays(long habitId);
}