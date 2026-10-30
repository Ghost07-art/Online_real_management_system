package com.realestate.repository;

import com.realestate.model.*;

import java.io.*;
import java.time.LocalDate;
import java.util.*;

public class DataStore implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final String DATA_FILE = "data/realestate_data.dat";
    private static DataStore instance;

    private ArrayList<User> users = new ArrayList<>();
    private ArrayList<Property> properties = new ArrayList<>();
    private ArrayList<RentalAgreement> agreements = new ArrayList<>();
    private ArrayList<Message> messages = new ArrayList<>();
    private LinkedHashMap<String, SystemSetting> settings = new LinkedHashMap<>();

    private DataStore() {
    }

    public static synchronized DataStore getInstance() {
        if (instance == null) {
            instance = loadFromDisk();
            if (instance.users.isEmpty()) {
                instance.seedInitialData();
                instance.saveToDisk();
            }
        }
        return instance;
    }

    public synchronized void saveToDisk() {
        File dir = new File("data");
        if (!dir.exists()) {
            dir.mkdirs();
        }

        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(DATA_FILE))) {
            oos.writeObject(this);
        } catch (IOException e) {
            System.err.println("Error saving data: " + e.getMessage());
        }
    }

    private static DataStore loadFromDisk() {
        File file = new File(DATA_FILE);
        if (file.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
                return (DataStore) ois.readObject();
            } catch (Exception e) {
                System.err.println("Loading fresh data: " + e.getMessage());
            }
        }
        return new DataStore();
    }

    private void seedInitialData() {
        User admin = new User("U-001", "Rajesh Kumar", "admin@realestate.com", "admin", Role.ADMIN);
        User manager1 = new User("U-002", "Amit Sharma", "amit@gmail.com", "amit", Role.PROPERTY_MANAGER);
        User manager2 = new User("U-003", "Pooja Verma", "pooja@gmail.com", "pooja", Role.PROPERTY_MANAGER);
        User tenant1 = new User("U-004", "Rahul Verma", "rahul@gmail.com", "rahul", Role.TENANT);
        User tenant2 = new User("U-005", "Priya Patel", "priya@gmail.com", "priya", Role.TENANT);

        users.add(admin);
        users.add(manager1);
        users.add(manager2);
        users.add(tenant1);
        users.add(tenant2);

        Property p1 = new Property(
                "PROP-101",
                "2 BHK Apartment",
                "Flat 402, Shanti Heights, MG Road, Bengaluru",
                "2 BHK flat with modular kitchen, lift, power backup, and car parking.",
                22000.0,
                50000.0,
                manager1.getId(),
                manager1.getName()
        );
        p1.setStatus(PropertyStatus.APPROVED);

        Property p2 = new Property(
                "PROP-102",
                "1 BHK Flat",
                "Plot 15, Sector 62, Noida",
                "1 BHK semi-furnished flat near metro station with 24 hours water and security.",
                12000.0,
                24000.0,
                manager1.getId(),
                manager1.getName()
        );
        p2.setStatus(PropertyStatus.RENTED);

        Property p3 = new Property(
                "PROP-103",
                "3 BHK House",
                "B-12, Civil Lines, Jaipur",
                "Spacious 3 BHK house with small garden and covered garage.",
                30000.0,
                60000.0,
                manager2.getId(),
                manager2.getName()
        );
        p3.setStatus(PropertyStatus.PENDING_APPROVAL);

        Property p4 = new Property(
                "PROP-104",
                "Studio Apartment",
                "C-804, Green Valley, Andheri West, Mumbai",
                "Furnished studio flat near railway station and market.",
                18000.0,
                36000.0,
                manager2.getId(),
                manager2.getName()
        );
        p4.setStatus(PropertyStatus.APPROVED);

        properties.add(p1);
        properties.add(p2);
        properties.add(p3);
        properties.add(p4);

        RentalAgreement a1 = new RentalAgreement(
                "AGR-201",
                p2.getId(),
                p2.getTitle(),
                p2.getAddress(),
                tenant1.getId(),
                tenant1.getName(),
                tenant1.getEmail(),
                manager1.getId(),
                manager1.getName(),
                LocalDate.now().minusMonths(2),
                LocalDate.now().plusMonths(9),
                12000.0,
                24000.0,
                "11 months agreement. Rent to be paid by 5th of every month."
        );
        a1.setStatus(AgreementStatus.ACTIVE);

        RentalAgreement a2 = new RentalAgreement(
                "AGR-202",
                p1.getId(),
                p1.getTitle(),
                p1.getAddress(),
                tenant2.getId(),
                tenant2.getName(),
                tenant2.getEmail(),
                manager1.getId(),
                manager1.getName(),
                LocalDate.now().plusWeeks(1),
                LocalDate.now().plusMonths(11),
                22000.0,
                50000.0,
                "Application submitted for 11 months lease."
        );
        a2.setStatus(AgreementStatus.PENDING);

        agreements.add(a1);
        agreements.add(a2);

        Message m1 = new Message(
                "MSG-301",
                tenant1.getId(),
                tenant1.getName(),
                tenant1.getRole(),
                manager1.getId(),
                manager1.getName(),
                p2.getId(),
                p2.getTitle(),
                "Namaste Amit ji, has the meter reading been recorded for this month?"
        );
        m1.setRead(true);

        Message m2 = new Message(
                "MSG-302",
                manager1.getId(),
                manager1.getName(),
                manager1.getRole(),
                tenant1.getId(),
                tenant1.getName(),
                p2.getId(),
                p2.getTitle(),
                "Haan Rahul, meter reading note kar li hai. Receipt kal bhej dunga."
        );
        m2.setRead(true);

        Message m3 = new Message(
                "MSG-303",
                tenant2.getId(),
                tenant2.getName(),
                tenant2.getRole(),
                manager1.getId(),
                manager1.getName(),
                p1.getId(),
                p1.getTitle(),
                "Hello, I have submitted the application for MG Road flat. When can I visit for key handover?"
        );
        m3.setRead(false);

        messages.add(m1);
        messages.add(m2);
        messages.add(m3);

        addSetting(new SystemSetting("PLATFORM_NAME", "System Name", "Online Real Estate System", "Platform name", "General"));
        addSetting(new SystemSetting("COMMISSION_RATE", "Commission (%)", "2.0", "Commission rate", "Finance"));
        addSetting(new SystemSetting("CURRENCY", "Currency Symbol", "Rs.", "Currency symbol", "General"));
        addSetting(new SystemSetting("AUTO_APPROVE_LISTINGS", "Auto Approve", "false", "Auto approve property listings", "Approval"));
        addSetting(new SystemSetting("MAX_LISTINGS_PER_MANAGER", "Max Listings", "25", "Maximum properties per manager", "Limits"));
        addSetting(new SystemSetting("SUPPORT_EMAIL", "Support Email", "support@realestate.in", "Contact email", "Contact"));
    }

    private void addSetting(SystemSetting setting) {
        settings.put(setting.getKey(), setting);
    }

    public synchronized List<User> getUsers() {
        return users;
    }

    public synchronized List<Property> getProperties() {
        return properties;
    }

    public synchronized List<RentalAgreement> getAgreements() {
        return agreements;
    }

    public synchronized List<Message> getMessages() {
        return messages;
    }

    public synchronized Map<String, SystemSetting> getSettings() {
        return settings;
    }
}
