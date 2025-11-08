package com.example.myhabits.database.dao;

import com.example.myhabits.models.User;

import java.util.List;

public interface UserDao {
    long insert(User user);
    User getById(long id);
    int update(User user);
    boolean delete(long id);
    List<User> getAll();
    User getFirstUser();
}
