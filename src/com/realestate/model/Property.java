package com.realestate.model;

import java.io.Serializable;
import java.time.LocalDateTime;

public class Property implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String title;
    private String address;
    private String description;
    private double rentAmount;
    private double depositAmount;
    private PropertyStatus status;
    private String managerId;
    private String managerName;
    private String adminFeedback;
    private LocalDateTime createdAt;

    public Property() {
        this.status = PropertyStatus.PENDING_APPROVAL;
        this.createdAt = LocalDateTime.now();
    }

    public Property(String id, String title, String address, String description,
                    double rentAmount, double depositAmount, String managerId, String managerName) {
        this.id = id;
        this.title = title;
        this.address = address;
        this.description = description;
        this.rentAmount = rentAmount;
        this.depositAmount = depositAmount;
        this.managerId = managerId;
        this.managerName = managerName;
        this.status = PropertyStatus.PENDING_APPROVAL;
        this.createdAt = LocalDateTime.now();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getRentAmount() {
        return rentAmount;
    }

    public void setRentAmount(double rentAmount) {
        this.rentAmount = rentAmount;
    }

    public double getDepositAmount() {
        return depositAmount;
    }

    public void setDepositAmount(double depositAmount) {
        this.depositAmount = depositAmount;
    }

    public PropertyStatus getStatus() {
        return status;
    }

    public void setStatus(PropertyStatus status) {
        this.status = status;
    }

    public String getManagerId() {
        return managerId;
    }

    public void setManagerId(String managerId) {
        this.managerId = managerId;
    }

    public String getManagerName() {
        return managerName;
    }

    public void setManagerName(String managerName) {
        this.managerName = managerName;
    }

    public String getAdminFeedback() {
        return adminFeedback;
    }

    public void setAdminFeedback(String adminFeedback) {
        this.adminFeedback = adminFeedback;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return title + " - " + address + " (Rs. " + (long) rentAmount + "/mo)";
    }
}
