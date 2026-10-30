package com.realestate.ui;

import com.realestate.model.*;
import com.realestate.service.*;
import com.realestate.ui.components.BadgeRenderer;
import com.realestate.ui.components.StatCard;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

@SuppressWarnings("serial")
public class AdminDashboard extends JPanel {
    private final UserService userService = UserService.getInstance();
    private final PropertyService propertyService = PropertyService.getInstance();
    private final AgreementService agreementService = AgreementService.getInstance();
    private final SettingService settingService = SettingService.getInstance();

    private StatCard totalUsersCard;
    private StatCard totalPropsCard;
    private StatCard pendingPropsCard;
    private StatCard activeAgreementsCard;

    private JTable userTable;
    private DefaultTableModel userModel;
    private JTable propertyTable;
    private DefaultTableModel propertyModel;
    private JTable settingTable;
    private DefaultTableModel settingModel;

    private JComboBox<String> propFilterCombo;

    public AdminDashboard() {
        setLayout(new BorderLayout());
        setBackground(new Color(240, 240, 240));
        setBorder(new EmptyBorder(10, 15, 10, 15));

        JPanel topPanel = new JPanel(new BorderLayout(0, 10));
        topPanel.setOpaque(false);

        JLabel titleLabel = new JLabel("Admin Dashboard");
        titleLabel.setFont(Theme.FONT_HEADER);
        titleLabel.setForeground(Color.BLACK);
        topPanel.add(titleLabel, BorderLayout.NORTH);

        JPanel statsRow = new JPanel(new GridLayout(1, 4, 10, 0));
        statsRow.setOpaque(false);

        totalUsersCard = new StatCard("Total Users", "0", Theme.PRIMARY);
        totalPropsCard = new StatCard("Properties", "0", Theme.INFO);
        pendingPropsCard = new StatCard("Pending Approval", "0", Theme.WARNING);
        activeAgreementsCard = new StatCard("Active Agreements", "0", Theme.SUCCESS);

        statsRow.add(totalUsersCard);
        statsRow.add(totalPropsCard);
        statsRow.add(pendingPropsCard);
        statsRow.add(activeAgreementsCard);
        topPanel.add(statsRow, BorderLayout.SOUTH);

        add(topPanel, BorderLayout.NORTH);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(Theme.FONT_BOLD);
        tabbedPane.setBackground(Color.WHITE);

        tabbedPane.addTab("User Management", createUserManagementPanel());
        tabbedPane.addTab("Property Approval", createPropertyManagementPanel());
        tabbedPane.addTab("System Settings", createSystemSettingsPanel());

        add(tabbedPane, BorderLayout.CENTER);

        refreshAllData();
    }

    private JPanel createUserManagementPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel toolbar = new JPanel(new BorderLayout());
        toolbar.setOpaque(false);

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        searchPanel.setOpaque(false);
        JLabel searchLbl = new JLabel("Search:");
        searchLbl.setForeground(Color.BLACK);
        searchPanel.add(searchLbl);

        JTextField searchField = Theme.createTextField(14);
        JButton searchBtn = Theme.createSecondaryButton("Search");
        searchBtn.addActionListener(e -> {
            List<User> filtered = userService.searchUsers(searchField.getText());
            populateUserTable(filtered);
        });

        JButton clearBtn = Theme.createSecondaryButton("Clear");
        clearBtn.addActionListener(e -> {
            searchField.setText("");
            populateUserTable(userService.getAllUsers());
        });

        searchPanel.add(searchField);
        searchPanel.add(searchBtn);
        searchPanel.add(clearBtn);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        btnPanel.setOpaque(false);

        JButton addBtn = Theme.createPrimaryButton("Add User");
        addBtn.addActionListener(e -> showAddUserDialog());

        JButton toggleBtn = Theme.createSecondaryButton("Active / Inactive");
        toggleBtn.addActionListener(e -> toggleSelectedUserStatus());

        JButton deleteBtn = Theme.createDangerButton("Delete");
        deleteBtn.addActionListener(e -> deleteSelectedUser());

        btnPanel.add(addBtn);
        btnPanel.add(toggleBtn);
        btnPanel.add(deleteBtn);

        toolbar.add(searchPanel, BorderLayout.WEST);
        toolbar.add(btnPanel, BorderLayout.EAST);
        panel.add(toolbar, BorderLayout.NORTH);

        String[] cols = {"User ID", "Name", "Email", "Role", "Status", "Created Date"};
        userModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        userTable = new JTable(userModel);
        Theme.styleTable(userTable);
        userTable.getColumnModel().getColumn(4).setCellRenderer(new BadgeRenderer());

        JScrollPane scrollPane = new JScrollPane(userTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(Theme.BORDER, 1));
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private void populateUserTable(List<User> list) {
        userModel.setRowCount(0);
        for (User u : list) {
            userModel.addRow(new Object[]{
                    u.getId(),
                    u.getName(),
                    u.getEmail(),
                    u.getRole().getDisplayName(),
                    u.getStatus().getLabel(),
                    u.getCreatedAt() != null ? u.getCreatedAt().toLocalDate().toString() : ""
            });
        }
    }

    private void showAddUserDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Add New User", true);
        dialog.setSize(380, 280);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridLayout(4, 2, 8, 10));
        form.setBorder(new EmptyBorder(15, 15, 10, 15));
        form.setBackground(Color.WHITE);

        JTextField nameField = Theme.createTextField(14);
        JTextField emailField = Theme.createTextField(14);
        JPasswordField passField = Theme.createPasswordField(14);
        JComboBox<Role> roleBox = new JComboBox<>(Role.values());

        JLabel l1 = new JLabel("Name:");
        l1.setForeground(Color.BLACK);
        JLabel l2 = new JLabel("Email:");
        l2.setForeground(Color.BLACK);
        JLabel l3 = new JLabel("Password:");
        l3.setForeground(Color.BLACK);
        JLabel l4 = new JLabel("Role:");
        l4.setForeground(Color.BLACK);

        form.add(l1);
        form.add(nameField);
        form.add(l2);
        form.add(emailField);
        form.add(l3);
        form.add(passField);
        form.add(l4);
        form.add(roleBox);

        JPanel btnBar = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnBar.setBackground(new Color(240, 240, 240));

        JButton cancelBtn = Theme.createSecondaryButton("Cancel");
        cancelBtn.addActionListener(e -> dialog.dispose());

        JButton saveBtn = Theme.createPrimaryButton("Save");
        saveBtn.addActionListener(e -> {
            try {
                String confirmation = userService.createUser(
                        nameField.getText(),
                        emailField.getText(),
                        new String(passField.getPassword()),
                        (Role) roleBox.getSelectedItem()
                );
                dialog.dispose();
                refreshAllData();
                JOptionPane.showMessageDialog(this, confirmation, "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnBar.add(cancelBtn);
        btnBar.add(saveBtn);

        dialog.add(form, BorderLayout.CENTER);
        dialog.add(btnBar, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void toggleSelectedUserStatus() {
        int row = userTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a user from table.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String userId = (String) userModel.getValueAt(row, 0);
        try {
            String msg = userService.toggleUserStatus(userId);
            refreshAllData();
            JOptionPane.showMessageDialog(this, msg, "Information", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteSelectedUser() {
        int row = userTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a user to delete.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String userId = (String) userModel.getValueAt(row, 0);
        String userName = (String) userModel.getValueAt(row, 1);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Delete user " + userName + "?",
                "Confirm", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                String confirmation = userService.deleteUser(userId);
                refreshAllData();
                JOptionPane.showMessageDialog(this, confirmation, "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private JPanel createPropertyManagementPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel toolbar = new JPanel(new BorderLayout());
        toolbar.setOpaque(false);

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        filterPanel.setOpaque(false);
        JLabel filterLbl = new JLabel("Status Filter:");
        filterLbl.setForeground(Color.BLACK);
        filterPanel.add(filterLbl);

        propFilterCombo = new JComboBox<>(new String[]{"All", "Pending Approval", "Approved", "Rejected", "Rented"});
        propFilterCombo.addActionListener(e -> applyPropertyFilter());
        filterPanel.add(propFilterCombo);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        actions.setOpaque(false);

        JButton approveBtn = Theme.createSuccessButton("Approve");
        approveBtn.addActionListener(e -> approveSelectedProperty());

        JButton rejectBtn = Theme.createDangerButton("Reject");
        rejectBtn.addActionListener(e -> rejectSelectedProperty());

        JButton detailsBtn = Theme.createSecondaryButton("Details");
        detailsBtn.addActionListener(e -> viewPropertyDetails());

        actions.add(detailsBtn);
        actions.add(approveBtn);
        actions.add(rejectBtn);

        toolbar.add(filterPanel, BorderLayout.WEST);
        toolbar.add(actions, BorderLayout.EAST);
        panel.add(toolbar, BorderLayout.NORTH);

        String[] cols = {"ID", "Property Title", "Address", "Rent (Rs.)", "Deposit (Rs.)", "Manager", "Status", "Feedback"};
        propertyModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        propertyTable = new JTable(propertyModel);
        Theme.styleTable(propertyTable);
        propertyTable.getColumnModel().getColumn(6).setCellRenderer(new BadgeRenderer());

        JScrollPane scroll = new JScrollPane(propertyTable);
        scroll.setBorder(BorderFactory.createLineBorder(Theme.BORDER, 1));
        panel.add(scroll, BorderLayout.CENTER);

        return panel;
    }

    private void applyPropertyFilter() {
        String filter = (String) propFilterCombo.getSelectedItem();
        List<Property> all = propertyService.getAllProperties();
        propertyModel.setRowCount(0);

        for (Property p : all) {
            boolean matches = true;
            if ("Pending Approval".equals(filter)) matches = (p.getStatus() == PropertyStatus.PENDING_APPROVAL);
            else if ("Approved".equals(filter)) matches = (p.getStatus() == PropertyStatus.APPROVED);
            else if ("Rejected".equals(filter)) matches = (p.getStatus() == PropertyStatus.REJECTED);
            else if ("Rented".equals(filter)) matches = (p.getStatus() == PropertyStatus.RENTED);

            if (matches) {
                propertyModel.addRow(new Object[]{
                        p.getId(),
                        p.getTitle(),
                        p.getAddress(),
                        "Rs. " + (long) p.getRentAmount(),
                        "Rs. " + (long) p.getDepositAmount(),
                        p.getManagerName(),
                        p.getStatus().getLabel(),
                        p.getAdminFeedback() != null ? p.getAdminFeedback() : "-"
                });
            }
        }
    }

    private void approveSelectedProperty() {
        int row = propertyTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a property first.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String propId = (String) propertyModel.getValueAt(row, 0);
        try {
            String statusConfirmation = propertyService.approveProperty(propId);
            refreshAllData();
            JOptionPane.showMessageDialog(this, statusConfirmation, "Status", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void rejectSelectedProperty() {
        int row = propertyTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a property first.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String propId = (String) propertyModel.getValueAt(row, 0);

        String reason = JOptionPane.showInputDialog(this,
                "Enter reason for rejection:",
                "Reject Property", JOptionPane.QUESTION_MESSAGE);

        if (reason != null && !reason.trim().isEmpty()) {
            try {
                String statusConfirmation = propertyService.rejectProperty(propId, reason);
                refreshAllData();
                JOptionPane.showMessageDialog(this, statusConfirmation, "Status", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void viewPropertyDetails() {
        int row = propertyTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a property to view details.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String propId = (String) propertyModel.getValueAt(row, 0);
        Property p = propertyService.getPropertyById(propId);
        if (p == null) return;

        JTextArea descArea = new JTextArea(p.getDescription());
        descArea.setWrapStyleWord(true);
        descArea.setLineWrap(true);
        descArea.setEditable(false);
        descArea.setFont(Theme.FONT_REGULAR);
        descArea.setForeground(Color.BLACK);
        descArea.setBackground(Color.WHITE);
        JScrollPane descScroll = new JScrollPane(descArea);
        descScroll.setPreferredSize(new Dimension(360, 80));

        JPanel pnl = new JPanel(new BorderLayout(5, 8));
        pnl.setBackground(Color.WHITE);

        JLabel info = new JLabel("<html><b>Title:</b> " + p.getTitle() + "<br>" +
                "<b>Address:</b> " + p.getAddress() + "<br>" +
                "<b>Rent:</b> Rs. " + (long) p.getRentAmount() + " | <b>Deposit:</b> Rs. " + (long) p.getDepositAmount() + "<br>" +
                "<b>Manager:</b> " + p.getManagerName() + "<br>" +
                "<b>Status:</b> " + p.getStatus().getLabel() + "<br><br><b>Description:</b></html>");
        info.setForeground(Color.BLACK);
        pnl.add(info, BorderLayout.NORTH);
        pnl.add(descScroll, BorderLayout.CENTER);

        JOptionPane.showMessageDialog(this, pnl, "Property Details", JOptionPane.PLAIN_MESSAGE);
    }

    private JPanel createSystemSettingsPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel toolbar = new JPanel(new BorderLayout());
        toolbar.setOpaque(false);

        JLabel info = new JLabel("System Configuration Settings");
        info.setFont(Theme.FONT_BOLD);
        info.setForeground(Color.BLACK);

        JButton editBtn = Theme.createPrimaryButton("Edit Setting");
        editBtn.addActionListener(e -> editSelectedSetting());

        toolbar.add(info, BorderLayout.WEST);
        toolbar.add(editBtn, BorderLayout.EAST);
        panel.add(toolbar, BorderLayout.NORTH);

        String[] cols = {"Key", "Name", "Value", "Category", "Description"};
        settingModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        settingTable = new JTable(settingModel);
        Theme.styleTable(settingTable);

        JScrollPane scroll = new JScrollPane(settingTable);
        scroll.setBorder(BorderFactory.createLineBorder(Theme.BORDER, 1));
        panel.add(scroll, BorderLayout.CENTER);

        return panel;
    }

    private void editSelectedSetting() {
        int row = settingTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a setting to edit.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String key = (String) settingModel.getValueAt(row, 0);
        String label = (String) settingModel.getValueAt(row, 1);
        String currVal = (String) settingModel.getValueAt(row, 2);

        String newVal = JOptionPane.showInputDialog(this,
                "Enter new value for " + label + ":", currVal);

        if (newVal != null && !newVal.trim().isEmpty()) {
            try {
                String confirmation = settingService.updateSetting(key, newVal);
                refreshAllData();
                JOptionPane.showMessageDialog(this, confirmation, "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public void refreshAllData() {
        totalUsersCard.updateValue(String.valueOf(userService.getAllUsers().size()));
        totalPropsCard.updateValue(String.valueOf(propertyService.getAllProperties().size()));
        pendingPropsCard.updateValue(String.valueOf(propertyService.getPendingProperties().size()));
        long activeCount = agreementService.getAllAgreements().stream().filter(a -> a.getStatus() == AgreementStatus.ACTIVE).count();
        activeAgreementsCard.updateValue(String.valueOf(activeCount));

        populateUserTable(userService.getAllUsers());

        if (propFilterCombo != null) {
            applyPropertyFilter();
        }

        settingModel.setRowCount(0);
        for (SystemSetting s : settingService.getAllSettings()) {
            settingModel.addRow(new Object[]{
                    s.getKey(),
                    s.getLabel(),
                    s.getValue(),
                    s.getCategory(),
                    s.getDescription()
            });
        }
    }
}
