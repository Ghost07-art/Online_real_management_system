package com.realestate.service;

import com.realestate.model.*;
import com.realestate.repository.DataStore;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class AgreementService {
    private static AgreementService instance;
    private final DataStore dataStore;

    private AgreementService() {
        this.dataStore = DataStore.getInstance();
    }

    public static synchronized AgreementService getInstance() {
        if (instance == null) {
            instance = new AgreementService();
        }
        return instance;
    }

    public synchronized List<RentalAgreement> getAllAgreements() {
        return new ArrayList<>(dataStore.getAgreements());
    }

    public synchronized List<RentalAgreement> getAgreementsByManager(String managerId) {
        return dataStore.getAgreements().stream()
                .filter(a -> a.getManagerId().equals(managerId))
                .collect(Collectors.toList());
    }

    public synchronized List<RentalAgreement> getAgreementsByTenant(String tenantId) {
        return dataStore.getAgreements().stream()
                .filter(a -> a.getTenantId().equals(tenantId))
                .collect(Collectors.toList());
    }

    public synchronized RentalAgreement getAgreementById(String id) {
        return dataStore.getAgreements().stream()
                .filter(a -> a.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    public synchronized String applyForRental(Property property, User tenant, LocalDate startDate,
                                             int durationMonths, String notes) throws Exception {
        if (property == null) {
            throw new IllegalArgumentException("Property cannot be null.");
        }
        if (property.getStatus() != PropertyStatus.APPROVED) {
            throw new IllegalStateException("This property is currently not available for rental.");
        }
        if (tenant == null) {
            throw new IllegalArgumentException("Tenant information required.");
        }
        if (startDate == null) {
            startDate = LocalDate.now().plusWeeks(1);
        }
        if (durationMonths <= 0) {
            durationMonths = 12;
        }

        for (RentalAgreement a : dataStore.getAgreements()) {
            if (a.getPropertyId().equals(property.getId()) &&
                    a.getTenantId().equals(tenant.getId()) &&
                    a.getStatus() == AgreementStatus.PENDING) {
                throw new IllegalStateException("You already have an active pending application for this property.");
            }
        }

        String id = "AGR-" + (200 + dataStore.getAgreements().size() + 1);
        LocalDate endDate = startDate.plusMonths(durationMonths);
        String terms = notes != null && !notes.trim().isEmpty() ? notes.trim() :
                "Standard residential lease for " + durationMonths + " months. Subject to manager verification.";

        RentalAgreement agreement = new RentalAgreement(
                id,
                property.getId(),
                property.getTitle(),
                property.getAddress(),
                tenant.getId(),
                tenant.getName(),
                tenant.getEmail(),
                property.getManagerId(),
                property.getManagerName(),
                startDate,
                endDate,
                property.getRentAmount(),
                property.getDepositAmount(),
                terms
        );

        dataStore.getAgreements().add(agreement);
        dataStore.saveToDisk();

        return "Rental application submitted successfully! (Agreement Reference: " + id + ")";
    }

    public synchronized String approveAgreement(String agreementId) throws Exception {
        RentalAgreement agreement = getAgreementById(agreementId);
        if (agreement == null) {
            throw new IllegalArgumentException("Agreement not found with ID: " + agreementId);
        }

        agreement.setStatus(AgreementStatus.ACTIVE);

        Property property = PropertyService.getInstance().getPropertyById(agreement.getPropertyId());
        if (property != null) {
            property.setStatus(PropertyStatus.RENTED);
        }

        dataStore.saveToDisk();
        return "Agreement Status Update: ACTIVE. Property '" + agreement.getPropertyTitle() + "' has been marked as rented to " + agreement.getTenantName() + ".";
    }

    public synchronized String rejectAgreement(String agreementId, String reason) throws Exception {
        RentalAgreement agreement = getAgreementById(agreementId);
        if (agreement == null) {
            throw new IllegalArgumentException("Agreement not found with ID: " + agreementId);
        }

        agreement.setStatus(AgreementStatus.TERMINATED);
        agreement.setTerms(agreement.getTerms() + " [Manager Note: " + reason + "]");
        dataStore.saveToDisk();

        return "Agreement Status Update: REJECTED / TERMINATED.";
    }

    public synchronized String terminateAgreement(String agreementId) throws Exception {
        RentalAgreement agreement = getAgreementById(agreementId);
        if (agreement == null) {
            throw new IllegalArgumentException("Agreement not found.");
        }

        agreement.setStatus(AgreementStatus.TERMINATED);

        Property property = PropertyService.getInstance().getPropertyById(agreement.getPropertyId());
        if (property != null && property.getStatus() == PropertyStatus.RENTED) {
            property.setStatus(PropertyStatus.APPROVED);
        }

        dataStore.saveToDisk();
        return "Agreement Status Update: Lease terminated. Property '" + agreement.getPropertyTitle() + "' is now available for new listings.";
    }
}
