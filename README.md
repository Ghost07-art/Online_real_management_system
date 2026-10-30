# Online Real Estate Management System

A desktop application built using Java and Swing for managing property listings, rental agreements, tenant applications, and communication. This project was developed as a college project to demonstrate Object-Oriented Programming (OOP) concepts, GUI development, and file-based data persistence in Java.

---

## 📌 Project Overview

In traditional rental management, keeping track of properties, agreements, and tenant requests on paper or spreadsheets is messy and hard to manage. This system provides a unified platform with three distinct user roles:

1. **Administrator**: Oversees the entire system, approves/rejects property listings, manages user accounts, and configures system settings.
2. **Property Manager**: Lists properties, reviews rental applications from tenants, manages rental agreements, and communicates with tenants.
3. **Tenant**: Searches and filters available properties, applies for rentals, views active lease agreements, and sends messages to property managers.

---

## 🚀 Features

### 1. Admin Module
- **User Management**: View all registered users (Admins, Managers, Tenants), create new accounts, toggle status (Active/Inactive), and delete accounts.
- **Property Listing Approval**: Review new property listings submitted by Property Managers. Admin can approve them for public listing or reject them with feedback notes.
- **System Settings**: View and modify system configurations like platform commission rate, currency symbol (Rs.), auto-approval toggle, and listing limits.
- **Admin Dashboard**: Real-time counter cards showing total users, properties, pending approvals, and active agreements.

### 2. Property Manager Module
- **Property Listings**: Add new property listings with title, address, monthly rent (in Rs.), security deposit, and description. Edit or delete existing listings.
- **Rental Agreements**: Review tenant rental applications, approve applications to activate lease agreements (which automatically marks the property as rented), reject applications, or terminate leases.
- **Tenant Communication**: Direct messaging system with tenants regarding maintenance, rent receipts, or questions. Shows unread message indicators.

### 3. Tenant Module
- **Property Search & Exploration**: Search available approved properties by keywords or address, and filter listings by maximum monthly rent budget.
- **Rental Applications**: Submit rental applications by choosing move-in date, lease duration (6, 11, 12, or 24 months), and adding personal notes.
- **My Agreements**: View all active and past rental agreements with full details (start date, end date, rent, deposit, terms).
- **Manager Chat**: Send direct inquiries to property managers for specific properties and receive replies.

---

## 🔄 System Workflow

Here is how the system functions in real-world use:

```
[ Property Manager ] ──> Creates Property Listing ──> Status: Pending Approval
                                                               │
                                                               ▼
[ Administrator ]    ──> Reviews & Approves Listing ──> Status: Approved (Visible to Tenants)
                                                               │
                                                               ▼
[ Tenant ]           ──> Searches Properties & Submits Rental Application
                                                               │
                                                               ▼
[ Property Manager ] ──> Reviews Application & Approves ──> Lease Activated
                                                        ──> Property Marked as Rented
                                                               │
                                                               ▼
[ Manager & Tenant ] ──> Ongoing Communication & Messages
```

---

## 💻 Tech Stack & Concepts Used

- **Language**: Java (JDK 17 / JDK 21 / JDK 27 compatible)
- **GUI Toolkit**: Java Swing & AWT (`JFrame`, `JPanel`, `JTable`, `JTabbedPane`, `JDialog`, `CardLayout`, `GridBagLayout`)
- **Architecture**: Layered architecture (Model - Repository - Service - UI)
- **Data Persistence**: Java Object Serialization (`ObjectInputStream` / `ObjectOutputStream`) saved to `data/realestate_data.dat`
- **Core Java Concepts**:
  - Encapsulation (POJO models with private fields and getters/setters)
  - Inheritance & Polymorphism (Custom renderers extending `DefaultTableCellRenderer`)
  - Java Collections Framework (`ArrayList`, `LinkedHashMap`, Java Streams)
  - Exception Handling & Input Validation

---

## 📂 Project Structure

```
JAVA Project/
├── src/
│   └── com/
│       └── realestate/
│           ├── Main.java                # Application entry point
│           ├── model/                   # Data entities & Enums
│           │   ├── Role.java
│           │   ├── UserStatus.java
│           │   ├── PropertyStatus.java
│           │   ├── AgreementStatus.java
│           │   ├── User.java
│           │   ├── Property.java
│           │   ├── RentalAgreement.java
│           │   ├── Message.java
│           │   └── SystemSetting.java
│           ├── repository/              # Data access & file storage
│           │   └── DataStore.java       # Singleton data storage & auto-seeding
│           ├── service/                 # Business logic
│           │   ├── AuthService.java
│           │   ├── UserService.java
│           │   ├── PropertyService.java
│           │   ├── AgreementService.java
│           │   ├── MessageService.java
│           │   └── SettingService.java
│           └── ui/                      # Swing GUI components
│               ├── Theme.java           # Colors, fonts, buttons, and table styling
│               ├── LoginView.java       # Login & registration view
│               ├── MainFrame.java       # Main application frame
│               ├── AdminDashboard.java
│               ├── PropertyManagerDashboard.java
│               ├── TenantDashboard.java
│               └── components/
│                   ├── StatCard.java
│                   └── BadgeRenderer.java
├── data/                                # Directory for persistent data file
├── run.bat                              # Batch script to compile and run on Windows
├── .gitignore                           # Git ignore file
└── README.md                            # Project documentation
```

---

## ⚙️ How to Run

### Prerequisites
- Java Development Kit (JDK 17 or higher) installed.
- Ensure `javac` and `java` are added to your system PATH.

### Steps to Run

1. **Clone the repository**:
   ```bash
   git clone https://github.com/Ghost07-art/Online_real_management_system.git
   cd Online_real_management_system
   ```

2. **Run using the batch file (Windows)**:
   Simply double-click `run.bat` or run in terminal:
   ```cmd
   run.bat
   ```

3. **Or Compile and Run manually via Command Line**:
   ```bash
   # Create bin directory
   mkdir bin

   # Compile all java files
   javac -encoding UTF-8 -d bin src/com/realestate/model/*.java src/com/realestate/repository/*.java src/com/realestate/service/*.java src/com/realestate/ui/components/*.java src/com/realestate/ui/*.java src/com/realestate/*.java

   # Run application
   java -cp bin com.realestate.Main
   ```

---

## 🔑 Pre-Configured Demo Accounts

For easy testing and presentation, demo accounts are preloaded. You can also use the **Quick Demo Login** buttons on the sign-in screen:

| Role | Email | Password | Name |
| :--- | :--- | :--- | :--- |
| **Admin** | `admin@realestate.com` | `admin` | Rajesh Kumar |
| **Property Manager** | `amit@gmail.com` | `amit` | Amit Sharma |
| **Property Manager** | `pooja@gmail.com` | `pooja` | Pooja Verma |
| **Tenant** | `rahul@gmail.com` | `rahul` | Rahul Verma |
| **Tenant** | `priya@gmail.com` | `priya` | Priya Patel |

*(You can also create a new account anytime using the "Register New Account" button).*

---

## 🔮 Future Enhancements
- Integration with MySQL / PostgreSQL database using JDBC.
- Online payment gateway simulation for rent payments.
- Upload and view property photos.
- PDF agreement generation and download.
