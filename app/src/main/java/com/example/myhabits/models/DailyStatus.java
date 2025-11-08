package com.example.myhabits.models;

import android.os.Parcel;
import android.os.Parcelable;

public class DailyStatus implements Parcelable{
    private long id;
    private long habitId;
    private String date;
    private int status;
    private long checkTime;
    private String note;
    private long noteUpdatedTime;  // Thêm trường mới
    private int dayNumber;

    public DailyStatus() {}

    public DailyStatus(long habitId, String date, int status, int dayNumber) {
        this.habitId = habitId;
        this.date = date;
        this.status = status;
        this.dayNumber = dayNumber;
    }

    protected DailyStatus(Parcel in) {
        id = in.readLong();
        habitId = in.readLong();
        date = in.readString();
        status = in.readInt();
        checkTime = in.readLong();
        note = in.readString();
        noteUpdatedTime = in.readLong();
        dayNumber = in.readInt();
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeLong(habitId);
        dest.writeString(date);
        dest.writeInt(status);
        dest.writeLong(checkTime);
        dest.writeString(note);
        dest.writeLong(noteUpdatedTime);
        dest.writeInt(dayNumber);
    }

    public static final Parcelable.Creator<DailyStatus> CREATOR = new Parcelable.Creator<DailyStatus>() {
        @Override
        public DailyStatus createFromParcel(Parcel in) {
            return new DailyStatus(in);
        }

        @Override
        public DailyStatus[] newArray(int size) {
            return new DailyStatus[size];
        }
    };

    public int describeContents() {
        return 0;
    }

    // Getters and Setters cũ giữ nguyên
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getHabitId() {
        return habitId;
    }

    public void setHabitId(long habitId) {
        this.habitId = habitId;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public long getCheckTime() {
        return checkTime;
    }

    public void setCheckTime(long checkTime) {
        this.checkTime = checkTime;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public int getDayNumber() {
        return dayNumber;
    }

    public void setDayNumber(int dayNumber) {
        this.dayNumber = dayNumber;
    }

    // Thêm getter và setter cho trường mới
    public long getNoteUpdatedTime() {
        return noteUpdatedTime;
    }

    public void setNoteUpdatedTime(long noteUpdatedTime) {
        this.noteUpdatedTime = noteUpdatedTime;
    }
}