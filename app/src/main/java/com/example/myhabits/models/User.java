package com.example.myhabits.models;

public class User {
    private long id;
    private String name;
    private String avatar;
    private long createdAt;

    public User() {}

    public User(String name, String avatar) {
        this.name = name;
        this.avatar = avatar;
        this.createdAt = System.currentTimeMillis();
    }

    // Getters and Setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }
    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
}
