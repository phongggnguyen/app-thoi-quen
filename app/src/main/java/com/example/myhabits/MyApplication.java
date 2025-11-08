package com.example.myhabits;

import android.app.Application;

public class MyApplication extends Application {
    private static MyApplication instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        com.jakewharton.threetenabp.AndroidThreeTen.init(this);
    }

    public static MyApplication getInstance() {
        return instance;
    }
}