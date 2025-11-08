package com.example.myhabits.models;

public class HabitType {
    private long id;
    private String name;
    private String color;
    private int habitCount;

    public HabitType() {}

    // Getters and Setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public int getHabitCount() {
        return habitCount;
    }

    public void setHabitCount(int habitCount) {
        this.habitCount = habitCount;
    }
}