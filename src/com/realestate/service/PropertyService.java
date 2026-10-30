package com.realestate.service;

import com.realestate.model.Property;
import com.realestate.model.PropertyStatus;
import com.realestate.model.User;
import com.realestate.repository.DataStore;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class PropertyService {
    private static PropertyService instance;
    private final DataStore dataStore;

    private PropertyService() {
        this.dataStore = DataStore.getInstance();
    }

    public static synchronized PropertyService getInstance() {
        if (instance == null) {
            instance = new PropertyService();
        }
        return instance;
    }

    public synchronized List<Property> getAllProperties() {
        return new ArrayList<>(dataStore.getProperties());
    }

    public synchronized List<Property> getApprovedProperties() {
        return dataStore.getProperties().stream()
                .filter(p -> p.getStatus() == PropertyStatus.APPROVED)
                .collect(Collectors.toList());
    }

    public synchronized List<Property> getPendingProperties() {
        return dataStore.getProperties().stream()
                .filter(p -> p.getStatus() == PropertyStatus.PENDING_APPROVAL)
                .collect(Collectors.toList());
    }

    public synchronized List<Property> getPropertiesByManager(String managerId) {
        return dataStore.getProperties().stream()
                .filter(p -> p.getManagerId().equals(managerId))
                .collect(Collectors.toList());
    }

    public synchronized Property getPropertyById(String id) {
        return dataStore.getProperties().stream()
                .filter(p -> p.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    public synchronized String createProperty(String title, String address, String description,
                                              double rentAmount, double depositAmount, User manager) throws Exception {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Property title is required.");
        }
        if (address == null || address.trim().isEmpty()) {
            throw new IllegalArgumentException("Property address is required.");
        }
        if (description == null || description.trim().isEmpty()) {
            throw new IllegalArgumentException("Property description is required.");
        }
        if (rentAmount <= 0) {
            throw new IllegalArgumentException("Rent amount must be greater than zero.");
        }
        if (depositAmount < 0) {
            depositAmount = rentAmount * 2;
        }

        String autoApproveStr = SettingService.getInstance().getSettingValue("AUTO_APPROVE_LISTINGS", "false");
        boolean autoApprove = Boolean.parseBoolean(autoApproveStr);

        String id = "PROP-" + (100 + dataStore.getProperties().size() + 1);
        Property prop = new Property(id, title.trim(), address.trim(), description.trim(),
                rentAmount, depositAmount, manager.getId(), manager.getName());

        if (autoApprove) {
            prop.setStatus(PropertyStatus.APPROVED);
        } else {
            prop.setStatus(PropertyStatus.PENDING_APPROVAL);
        }

        dataStore.getProperties().add(prop);
        dataStore.saveToDisk();

        if (autoApprove) {
            return "Property '" + title + "' listed successfully and automatically approved! (ID: " + id + ")";
        } else {
            return "Property '" + title + "' listed successfully and submitted for Admin approval! (ID: " + id + ")";
        }
    }

    public synchronized String updateProperty(String id, String title, String address, String description,
                                              double rentAmount, double depositAmount) throws Exception {
        Property prop = getPropertyById(id);
        if (prop == null) {
            throw new IllegalArgumentException("Property not found with ID: " + id);
        }

        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Property title is required.");
        }
        if (address == null || address.trim().isEmpty()) {
            throw new IllegalArgumentException("Property address is required.");
        }
        if (rentAmount <= 0) {
            throw new IllegalArgumentException("Rent amount must be greater than zero.");
        }

        prop.setTitle(title.trim());
        prop.setAddress(address.trim());
        prop.setDescription(description != null ? description.trim() : "");
        prop.setRentAmount(rentAmount);
        prop.setDepositAmount(depositAmount);
        dataStore.saveToDisk();

        return "Property '" + title + "' details updated successfully!";
    }

    public synchronized String deleteProperty(String id) throws Exception {
        Property prop = getPropertyById(id);
        if (prop == null) {
            throw new IllegalArgumentException("Property not found.");
        }
        dataStore.getProperties().remove(prop);
        dataStore.saveToDisk();
        return "Property '" + prop.getTitle() + "' deleted successfully.";
    }

    public synchronized String approveProperty(String id) throws Exception {
        Property prop = getPropertyById(id);
        if (prop == null) {
            throw new IllegalArgumentException("Property not found.");
        }
        prop.setStatus(PropertyStatus.APPROVED);
        prop.setAdminFeedback("Approved for public listing.");
        dataStore.saveToDisk();
        return "Listing Approval Status: APPROVED. Property '" + prop.getTitle() + "' is now visible to tenants.";
    }

    public synchronized String rejectProperty(String id, String reason) throws Exception {
        Property prop = getPropertyById(id);
        if (prop == null) {
            throw new IllegalArgumentException("Property not found.");
        }
        prop.setStatus(PropertyStatus.REJECTED);
        prop.setAdminFeedback(reason != null && !reason.trim().isEmpty() ? reason.trim() : "Does not meet listing standards.");
        dataStore.saveToDisk();
        return "Listing Approval Status: REJECTED. Property manager has been notified.";
    }

    public synchronized List<Property> searchProperties(String keyword, Double maxRent) {
        return dataStore.getProperties().stream()
                .filter(p -> p.getStatus() == PropertyStatus.APPROVED)
                .filter(p -> {
                    if (keyword == null || keyword.trim().isEmpty()) return true;
                    String kw = keyword.trim().toLowerCase();
                    return p.getTitle().toLowerCase().contains(kw) ||
                            p.getAddress().toLowerCase().contains(kw) ||
                            p.getDescription().toLowerCase().contains(kw);
                })
                .filter(p -> {
                    if (maxRent == null || maxRent <= 0) return true;
                    return p.getRentAmount() <= maxRent;
                })
                .collect(Collectors.toList());
    }
}
