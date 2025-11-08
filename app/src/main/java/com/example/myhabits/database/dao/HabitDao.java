package com.example.myhabits.database.dao;

import com.example.myhabits.models.Habit;
import java.util.List;

public interface HabitDao {
    long insert(Habit habit);
    Habit getById(long id);
    List<Habit> getByUserId(long userId); // Phương thức cũ, chỉ lấy thói quen chưa kết thúc
    List<Habit> getByUserId(long userId, boolean includeArchived); // Phương thức mới, có thể lấy tất cả thói quen
    List<Habit> getByType(long typeId);
    int update(Habit habit);
    boolean delete(long id);
    void deleteAll();
}