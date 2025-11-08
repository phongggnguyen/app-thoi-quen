package com.example.myhabits.database.data;

import java.util.ArrayList;
import java.util.List;

public class DataChangeManager {
    private static DataChangeManager instance;
    private List<DataChangeListener> listeners = new ArrayList<>();

    private DataChangeManager() {}

    public static synchronized DataChangeManager getInstance() {
        if (instance == null) {
            instance = new DataChangeManager();
        }
        return instance;
    }

    public void addListener(DataChangeListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void removeListener(DataChangeListener listener) {
        listeners.remove(listener);
    }

    public void notifyDataChanged() {
        for (DataChangeListener listener : listeners) {
            listener.onDataChanged();
        }
    }
}
