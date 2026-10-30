package com.realestate.service;

import com.realestate.model.SystemSetting;
import com.realestate.repository.DataStore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SettingService {
    private static SettingService instance;
    private final DataStore dataStore;

    private SettingService() {
        this.dataStore = DataStore.getInstance();
    }

    public static synchronized SettingService getInstance() {
        if (instance == null) {
            instance = new SettingService();
        }
        return instance;
    }

    public synchronized List<SystemSetting> getAllSettings() {
        return new ArrayList<>(dataStore.getSettings().values());
    }

    public synchronized String getSettingValue(String key, String defaultValue) {
        SystemSetting s = dataStore.getSettings().get(key);
        return (s != null && s.getValue() != null) ? s.getValue() : defaultValue;
    }

    public synchronized String updateSetting(String key, String newValue) throws Exception {
        SystemSetting s = dataStore.getSettings().get(key);
        if (s == null) {
            throw new IllegalArgumentException("Unknown system setting key: " + key);
        }
        if (newValue == null || newValue.trim().isEmpty()) {
            throw new IllegalArgumentException("Setting value cannot be empty.");
        }

        s.setValue(newValue.trim());
        dataStore.saveToDisk();

        return "System configuration for '" + s.getLabel() + "' updated successfully to: " + newValue.trim();
    }
}
