package com.example.myhabits.models;

public class HabitNotification {
    private long id;
    private long habitId;
    private String title;
    private String content;
    private String habitStartTime;  // thêm mới
    private String habitEndTime;    // thêm mới
    private String habitTime;  // vẫn giữ để tương thích ngược
    private int dayNumber;
    private long notifyTime;
    private boolean isRead;
    private int type;

    public static final int TYPE_REMINDER = 1;
    public static final int TYPE_COMPLETION = 2;

    // Constructor không tham số
    public HabitNotification() {}

    // Constructor đầy đủ tham số
    public HabitNotification(long habitId, String title, String content,
                             String habitTime, int dayNumber, long notifyTime, int type) {
        this.habitId = habitId;
        this.title = title;
        this.content = content;
        this.habitTime = habitTime;
        this.dayNumber = dayNumber;
        this.notifyTime = notifyTime;
        this.type = type;
        this.isRead = false;
    }

    // Getters & Setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getHabitId() { return habitId; }
    public void setHabitId(long habitId) { this.habitId = habitId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getHabitTime() { return habitTime; }
    public void setHabitTime(String habitTime) { this.habitTime = habitTime; }

    public int getDayNumber() { return dayNumber; }
    public void setDayNumber(int dayNumber) { this.dayNumber = dayNumber; }

    public long getNotifyTime() { return notifyTime; }
    public void setNotifyTime(long notifyTime) { this.notifyTime = notifyTime; }

    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }

    public int getType() { return type; }
    public void setType(int type) { this.type = type; }

    public String getHabitStartTime() {
        return habitStartTime;
    }

    public void setHabitStartTime(String habitStartTime) {
        this.habitStartTime = habitStartTime;
    }

    public String getHabitEndTime() {
        return habitEndTime;
    }

    public void setHabitEndTime(String habitEndTime) {
        this.habitEndTime = habitEndTime;
    }
}