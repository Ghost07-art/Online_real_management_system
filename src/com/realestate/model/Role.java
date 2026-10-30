package com.realestate.model;

public enum Role {
    ADMIN("Administrator"),
    PROPERTY_MANAGER("Property Manager"),
    TENANT("Tenant");

    private final String displayName;

    Role(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
