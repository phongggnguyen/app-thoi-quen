package com.example.myhabits.models;

import android.os.Parcel;
import android.os.Parcelable;

public class Habit implements Parcelable {
    private long id;
    private long userId;
    private long typeId;
    private String name;
    private String startDate;
    private String startTime;
    private String endTime;
    private int reminderMinutes;
    private int status;
    private int streakCount;
    private String lastCompletedDate;
    private int targetDays;

    // Constructor
    public Habit() {}

    protected Habit(Parcel in) {
        id = in.readLong();
        userId = in.readLong();
        typeId = in.readLong();
        name = in.readString();
        startDate = in.readString();
        startTime = in.readString();
        endTime = in.readString();
        reminderMinutes = in.readInt();
        status = in.readInt();
        streakCount = in.readInt();
        lastCompletedDate = in.readString();
        targetDays = in.readInt();
    }


    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeLong(userId);
        dest.writeLong(typeId);
        dest.writeString(name);
        dest.writeString(startDate);
        dest.writeString(startTime);
        dest.writeString(endTime);
        dest.writeInt(reminderMinutes);
        dest.writeInt(status);
        dest.writeInt(streakCount);
        dest.writeString(lastCompletedDate);
        dest.writeInt(targetDays);
    }

    public static final Parcelable.Creator<Habit> CREATOR = new Parcelable.Creator<Habit>() {
        @Override
        public Habit createFromParcel(Parcel in) {
            return new Habit(in);
        }

        @Override
        public Habit[] newArray(int size) {
            return new Habit[size];
        }
    };


    public int describeContents() {
        return 0;
    }

    // Getters and Setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getUserId() { return userId; }
    public void setUserId(long userId) { this.userId = userId; }

    public long getTypeId() { return typeId; }
    public void setTypeId(long typeId) { this.typeId = typeId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }

    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }

    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }

    public int getReminderMinutes() { return reminderMinutes; }
    public void setReminderMinutes(int reminderMinutes) { this.reminderMinutes = reminderMinutes; }

    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }

    public int getStreakCount() { return streakCount; }
    public void setStreakCount(int streakCount) { this.streakCount = streakCount; }

    public String getLastCompletedDate() { return lastCompletedDate; }
    public void setLastCompletedDate(String lastCompletedDate) { this.lastCompletedDate = lastCompletedDate; }

    public int getTargetDays() { return targetDays; }
    public void setTargetDays(int targetDays) { this.targetDays = targetDays; }
}