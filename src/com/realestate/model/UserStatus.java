package com.realestate.model;

public enum UserStatus {
    ACTIVE("Active"),
    INACTIVE("Inactive");

    private final String label;

    UserStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    @Override
    public String toString() {
        return label;
    }
}
