package com.example.myhabits.database.dao;

import com.example.myhabits.models.HabitNotification;

import java.util.List;

public interface HabitNotificationDao {
    long insert(HabitNotification notification);
    HabitNotification getById(long id);
    List<HabitNotification> getAll();
    List<HabitNotification> getUnread();
    int update(HabitNotification notification);
    boolean delete(long id);
    void deleteAll();
    void markAsRead(long id);
    boolean hasUnreadNotifications();
    int getUnreadCount();
}
