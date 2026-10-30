package com.realestate.model;

public enum AgreementStatus {
    PENDING("Pending Approval"),
    ACTIVE("Active"),
    COMPLETED("Completed"),
    TERMINATED("Terminated");

    private final String label;

    AgreementStatus(String label) {
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
