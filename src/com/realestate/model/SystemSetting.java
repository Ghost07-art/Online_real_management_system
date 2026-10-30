package com.realestate.model;

import java.io.Serializable;
import java.time.LocalDateTime;

public class SystemSetting implements Serializable {
    private static final long serialVersionUID = 1L;

    private String key;
    private String label;
    private String value;
    private String description;
    private String category;
    private LocalDateTime updatedAt;

    public SystemSetting() {
        this.updatedAt = LocalDateTime.now();
    }

    public SystemSetting(String key, String label, String value, String description, String category) {
        this.key = key;
        this.label = label;
        this.value = value;
        this.description = description;
        this.category = category;
        this.updatedAt = LocalDateTime.now();
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
        this.updatedAt = LocalDateTime.now();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
