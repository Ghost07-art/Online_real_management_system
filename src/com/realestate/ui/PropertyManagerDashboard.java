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
public class PropertyManagerDashboard extends JPanel {
    private final User currentUser;
    private final PropertyService propertyService = PropertyService.getInstance();
    private final AgreementService agreementService = AgreementService.getInstance();
    private final MessageService messageService = MessageService.getInstance();
    private final UserService userService = UserService.getInstance();

    private StatCard myPropsCard;
    private StatCard pendingApprovalCard;
    private StatCard activeLeasesCard;
    private StatCard unreadMessagesCard;

    private JTable propertyTable;
    private DefaultTableModel propertyModel;
    private JTable agreementTable;
    private DefaultTableModel agreementModel;
    private JTable messageTable;
    private DefaultTableModel messageModel;

    private JTextArea chatHistoryArea;
    private JTextArea replyArea;
    private JLabel chatRecipientLabel;
    private String selectedTenantId;
    private String selectedTenantName;
    private String selectedPropertyId;
    private String selectedPropertyTitle;

    public PropertyManagerDashboard(User user) {
        this.currentUser = user;
        setLayout(new BorderLayout());
        setBackground(new Color(240, 240, 240));
        setBorder(new EmptyBorder(10, 15, 10, 15));

        JPanel topPanel = new JPanel(new BorderLayout(0, 10));
        topPanel.setOpaque(false);

        JLabel titleLabel = new JLabel("Property Manager Dashboard - " + user.getName());
        titleLabel.setFont(Theme.FONT_HEADER);
        titleLabel.setForeground(Color.BLACK);
        topPanel.add(titleLabel, BorderLayout.NORTH);

        JPanel statsRow = new JPanel(new GridLayout(1, 4, 10, 0));
        statsRow.setOpaque(false);

        myPropsCard = new StatCard("My Properties", "0", Theme.PRIMARY);
        pendingApprovalCard = new StatCard("Pending Approval", "0", Theme.WARNING);
        activeLeasesCard = new StatCard("Rented Properties", "0", Theme.SUCCESS);
        unreadMessagesCard = new StatCard("Unread Messages", "0", Theme.INFO);

        statsRow.add(myPropsCard);
        statsRow.add(pendingApprovalCard);
        statsRow.add(activeLeasesCard);
        statsRow.add(unreadMessagesCard);
        topPanel.add(statsRow, BorderLayout.SOUTH);

        add(topPanel, BorderLayout.NORTH);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(Theme.FONT_BOLD);
        tabbedPane.setBackground(Color.WHITE);

        tabbedPane.addTab("Properties", createPropertyListingsPanel());
        tabbedPane.addTab("Rental Agreements", createRentalAgreementPanel());
        tabbedPane.addTab("Tenant Messages", createTenantCommunicationPanel());

        add(tabbedPane, BorderLayout.CENTER);

        refreshAllData();
    }

    private JPanel createPropertyListingsPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        toolbar.setOpaque(false);

        JButton addBtn = Theme.createPrimaryButton("Add Property");
        addBtn.addActionListener(e -> showCreatePropertyDialog());

        JButton editBtn = Theme.createSecondaryButton("Edit");
        editBtn.addActionListener(e -> editSelectedProperty());

        JButton deleteBtn = Theme.createDangerButton("Delete");
        deleteBtn.addActionListener(e -> deleteSelectedProperty());

        toolbar.add(addBtn);
        toolbar.add(editBtn);
        toolbar.add(deleteBtn);
        panel.add(toolbar, BorderLayout.NORTH);

        String[] cols = {"Property ID", "Title", "Address", "Rent (Rs.)", "Deposit (Rs.)", "Status", "Admin Feedback"};
        propertyModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        propertyTable = new JTable(propertyModel);
        Theme.styleTable(propertyTable);
        propertyTable.getColumnModel().getColumn(5).setCellRenderer(new BadgeRenderer());

        JScrollPane scroll = new JScrollPane(propertyTable);
        scroll.setBorder(BorderFactory.createLineBorder(Theme.BORDER, 1));
        panel.add(scroll, BorderLayout.CENTER);

        return panel;
    }

    private void showCreatePropertyDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Add Property Listing", true);
        dialog.setSize(420, 380);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridLayout(5, 2, 8, 10));
        form.setBorder(new EmptyBorder(15, 15, 10, 15));
        form.setBackground(Color.WHITE);

        JTextField titleField = Theme.createTextField(14);
        JTextField addressField = Theme.createTextField(14);
        JTextField rentField = Theme.createTextField(14);
        JTextField depositField = Theme.createTextField(14);
        JTextField descField = Theme.createTextField(14);

        JLabel l1 = new JLabel("Property Title:");
        l1.setForeground(Color.BLACK);
        JLabel l2 = new JLabel("Address:");
        l2.setForeground(Color.BLACK);
        JLabel l3 = new JLabel("Rent / Month (Rs.):");
        l3.setForeground(Color.BLACK);
        JLabel l4 = new JLabel("Deposit (Rs.):");
        l4.setForeground(Color.BLACK);
        JLabel l5 = new JLabel("Description:");
        l5.setForeground(Color.BLACK);

        form.add(l1);
        form.add(titleField);
        form.add(l2);
        form.add(addressField);
        form.add(l3);
        form.add(rentField);
        form.add(l4);
        form.add(depositField);
        form.add(l5);
        form.add(descField);

        JPanel btnBar = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnBar.setBackground(new Color(240, 240, 240));

        JButton cancelBtn = Theme.createSecondaryButton("Cancel");
        cancelBtn.addActionListener(e -> dialog.dispose());

        JButton submitBtn = Theme.createPrimaryButton("Submit");
        submitBtn.addActionListener(e -> {
            try {
                double rent = Double.parseDouble(rentField.getText().trim());
                double deposit = depositField.getText().trim().isEmpty() ? (rent * 2) : Double.parseDouble(depositField.getText().trim());

                String confirmation = propertyService.createProperty(
                        titleField.getText(),
                        addressField.getText(),
                        descField.getText(),
                        rent,
                        deposit,
                        currentUser
                );
                dialog.dispose();
                refreshAllData();
                JOptionPane.showMessageDialog(this, confirmation, "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (NumberFormatException nfe) {
                JOptionPane.showMessageDialog(dialog, "Please enter numeric values for rent.", "Error", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnBar.add(cancelBtn);
        btnBar.add(submitBtn);

        dialog.add(form, BorderLayout.CENTER);
        dialog.add(btnBar, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void editSelectedProperty() {
        int row = propertyTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a property first.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String propId = (String) propertyModel.getValueAt(row, 0);
        Property p = propertyService.getPropertyById(propId);
        if (p == null) return;

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Edit Property - " + propId, true);
        dialog.setSize(420, 380);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridLayout(5, 2, 8, 10));
        form.setBorder(new EmptyBorder(15, 15, 10, 15));
        form.setBackground(Color.WHITE);

        JTextField titleField = Theme.createTextField(14);
        titleField.setText(p.getTitle());
        JTextField addressField = Theme.createTextField(14);
        addressField.setText(p.getAddress());
        JTextField rentField = Theme.createTextField(14);
        rentField.setText(String.valueOf((long) p.getRentAmount()));
        JTextField depositField = Theme.createTextField(14);
        depositField.setText(String.valueOf((long) p.getDepositAmount()));
        JTextField descField = Theme.createTextField(14);
        descField.setText(p.getDescription());

        JLabel l1 = new JLabel("Title:");
        l1.setForeground(Color.BLACK);
        JLabel l2 = new JLabel("Address:");
        l2.setForeground(Color.BLACK);
        JLabel l3 = new JLabel("Rent (Rs.):");
        l3.setForeground(Color.BLACK);
        JLabel l4 = new JLabel("Deposit (Rs.):");
        l4.setForeground(Color.BLACK);
        JLabel l5 = new JLabel("Description:");
        l5.setForeground(Color.BLACK);

        form.add(l1);
        form.add(titleField);
        form.add(l2);
        form.add(addressField);
        form.add(l3);
        form.add(rentField);
        form.add(l4);
        form.add(depositField);
        form.add(l5);
        form.add(descField);

        JPanel btnBar = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnBar.setBackground(new Color(240, 240, 240));

        JButton cancelBtn = Theme.createSecondaryButton("Cancel");
        cancelBtn.addActionListener(e -> dialog.dispose());

        JButton updateBtn = Theme.createPrimaryButton("Update");
        updateBtn.addActionListener(e -> {
            try {
                double rent = Double.parseDouble(rentField.getText().trim());
                double deposit = Double.parseDouble(depositField.getText().trim());
                String confirmation = propertyService.updateProperty(
                        propId,
                        titleField.getText(),
                        addressField.getText(),
                        descField.getText(),
                        rent,
                        deposit
                );
                dialog.dispose();
                refreshAllData();
                JOptionPane.showMessageDialog(this, confirmation, "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnBar.add(cancelBtn);
        btnBar.add(updateBtn);

        dialog.add(form, BorderLayout.CENTER);
        dialog.add(btnBar, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void deleteSelectedProperty() {
        int row = propertyTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a property first.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String propId = (String) propertyModel.getValueAt(row, 0);
        int confirm = JOptionPane.showConfirmDialog(this, "Delete this property?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                String confirmation = propertyService.deleteProperty(propId);
                refreshAllData();
                JOptionPane.showMessageDialog(this, confirmation, "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private JPanel createRentalAgreementPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        toolbar.setOpaque(false);

        JButton approveBtn = Theme.createSuccessButton("Approve Agreement");
        approveBtn.addActionListener(e -> approveSelectedAgreement());

        JButton rejectBtn = Theme.createDangerButton("Reject");
        rejectBtn.addActionListener(e -> rejectSelectedAgreement());

        JButton terminateBtn = Theme.createSecondaryButton("Terminate");
        terminateBtn.addActionListener(e -> terminateSelectedAgreement());

        JButton viewTermsBtn = Theme.createSecondaryButton("Terms");
        viewTermsBtn.addActionListener(e -> viewAgreementTerms());

        toolbar.add(viewTermsBtn);
        toolbar.add(approveBtn);
        toolbar.add(rejectBtn);
        toolbar.add(terminateBtn);
        panel.add(toolbar, BorderLayout.NORTH);

        String[] cols = {"Agreement ID", "Property Title", "Tenant Name", "Tenant Email", "Start Date", "End Date", "Rent (Rs.)", "Status"};
        agreementModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        agreementTable = new JTable(agreementModel);
        Theme.styleTable(agreementTable);
        agreementTable.getColumnModel().getColumn(7).setCellRenderer(new BadgeRenderer());

        JScrollPane scroll = new JScrollPane(agreementTable);
        scroll.setBorder(BorderFactory.createLineBorder(Theme.BORDER, 1));
        panel.add(scroll, BorderLayout.CENTER);

        return panel;
    }

    private void approveSelectedAgreement() {
        int row = agreementTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select an agreement application first.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String agrId = (String) agreementModel.getValueAt(row, 0);
        try {
            String statusUpdate = agreementService.approveAgreement(agrId);
            refreshAllData();
            JOptionPane.showMessageDialog(this, statusUpdate, "Agreement Status", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void rejectSelectedAgreement() {
        int row = agreementTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select an agreement first.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String agrId = (String) agreementModel.getValueAt(row, 0);
        String reason = JOptionPane.showInputDialog(this, "Enter reason for rejection:", "Reject Application", JOptionPane.QUESTION_MESSAGE);
        if (reason != null) {
            try {
                String statusUpdate = agreementService.rejectAgreement(agrId, reason);
                refreshAllData();
                JOptionPane.showMessageDialog(this, statusUpdate, "Agreement Status", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void terminateSelectedAgreement() {
        int row = agreementTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select an agreement to terminate.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String agrId = (String) agreementModel.getValueAt(row, 0);
        int confirm = JOptionPane.showConfirmDialog(this, "Terminate this active rental lease?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                String statusUpdate = agreementService.terminateAgreement(agrId);
                refreshAllData();
                JOptionPane.showMessageDialog(this, statusUpdate, "Agreement Status", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void viewAgreementTerms() {
        int row = agreementTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select an agreement first.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String agrId = (String) agreementModel.getValueAt(row, 0);
        RentalAgreement a = agreementService.getAgreementById(agrId);
        if (a == null) return;

        JTextArea terms = new JTextArea(a.getTerms());
        terms.setEditable(false);
        terms.setLineWrap(true);
        terms.setWrapStyleWord(true);
        terms.setForeground(Color.BLACK);
        terms.setBackground(Color.WHITE);
        JScrollPane scroll = new JScrollPane(terms);
        scroll.setPreferredSize(new Dimension(360, 100));

        JPanel p = new JPanel(new BorderLayout(5, 5));
        p.setBackground(Color.WHITE);

        JLabel info = new JLabel("<html><b>ID:</b> " + a.getId() + "<br>" +
                "<b>Property:</b> " + a.getPropertyTitle() + " (" + a.getPropertyAddress() + ")<br>" +
                "<b>Tenant:</b> " + a.getTenantName() + " (" + a.getTenantEmail() + ")<br>" +
                "<b>Period:</b> " + a.getStartDate() + " to " + a.getEndDate() + "<br>" +
                "<b>Rent:</b> Rs. " + (long) a.getMonthlyRent() + " | <b>Deposit:</b> Rs. " + (long) a.getSecurityDeposit() + "<br>" +
                "<b>Status:</b> " + a.getStatus().getLabel() + "<br><br><b>Terms:</b></html>");
        info.setForeground(Color.BLACK);
        p.add(info, BorderLayout.NORTH);
        p.add(scroll, BorderLayout.CENTER);

        JOptionPane.showMessageDialog(this, p, "Agreement Details", JOptionPane.PLAIN_MESSAGE);
    }

    private JPanel createTenantCommunicationPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 0));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel left = new JPanel(new BorderLayout(0, 5));
        left.setBackground(Color.WHITE);
        left.setPreferredSize(new Dimension(360, 350));

        JPanel leftToolbar = new JPanel(new BorderLayout());
        leftToolbar.setOpaque(false);
        JLabel leftTitle = new JLabel("Tenant Messages");
        leftTitle.setFont(Theme.FONT_BOLD);
        leftTitle.setForeground(Color.BLACK);

        JButton newMsgBtn = Theme.createPrimaryButton("New Message");
        newMsgBtn.addActionListener(e -> showComposeDialog());

        leftToolbar.add(leftTitle, BorderLayout.WEST);
        leftToolbar.add(newMsgBtn, BorderLayout.EAST);
        left.add(leftToolbar, BorderLayout.NORTH);

        String[] cols = {"ID", "From", "Property", "Status"};
        messageModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        messageTable = new JTable(messageModel);
        Theme.styleTable(messageTable);
        messageTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                loadSelectedConversation();
            }
        });

        JScrollPane scrollMsg = new JScrollPane(messageTable);
        scrollMsg.setBorder(BorderFactory.createLineBorder(Theme.BORDER, 1));
        left.add(scrollMsg, BorderLayout.CENTER);

        JPanel right = new JPanel(new BorderLayout(0, 5));
        right.setBackground(Color.WHITE);
        right.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BORDER, 1),
                new EmptyBorder(8, 8, 8, 8)
        ));

        chatRecipientLabel = new JLabel("Select a message to view.");
        chatRecipientLabel.setFont(Theme.FONT_BOLD);
        chatRecipientLabel.setForeground(Color.BLACK);
        right.add(chatRecipientLabel, BorderLayout.NORTH);

        chatHistoryArea = new JTextArea();
        chatHistoryArea.setFont(Theme.FONT_REGULAR);
        chatHistoryArea.setEditable(false);
        chatHistoryArea.setLineWrap(true);
        chatHistoryArea.setWrapStyleWord(true);
        chatHistoryArea.setForeground(Color.BLACK);
        chatHistoryArea.setBackground(new Color(248, 248, 248));
        JScrollPane chatScroll = new JScrollPane(chatHistoryArea);
        right.add(chatScroll, BorderLayout.CENTER);

        JPanel composePanel = new JPanel(new BorderLayout(5, 5));
        composePanel.setOpaque(false);

        replyArea = new JTextArea(2, 20);
        replyArea.setFont(Theme.FONT_REGULAR);
        replyArea.setLineWrap(true);
        replyArea.setWrapStyleWord(true);
        replyArea.setForeground(Color.BLACK);
        replyArea.setBackground(Color.WHITE);
        replyArea.setBorder(BorderFactory.createLineBorder(Theme.BORDER, 1));
        JScrollPane replyScroll = new JScrollPane(replyArea);

        JButton sendBtn = Theme.createPrimaryButton("Send");
        sendBtn.addActionListener(e -> sendReplyMessage());

        composePanel.add(replyScroll, BorderLayout.CENTER);
        composePanel.add(sendBtn, BorderLayout.EAST);
        right.add(composePanel, BorderLayout.SOUTH);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, left, right);
        splitPane.setDividerLocation(360);
        panel.add(splitPane, BorderLayout.CENTER);

        return panel;
    }

    private void loadSelectedConversation() {
        int row = messageTable.getSelectedRow();
        if (row < 0) return;

        String msgId = (String) messageModel.getValueAt(row, 0);
        List<Message> all = messageService.getInbox(currentUser.getId());
        Message selected = null;
        for (Message m : all) {
            if (m.getId().equals(msgId)) {
                selected = m;
                break;
            }
        }
        if (selected == null) return;

        messageService.markAsRead(msgId);
        selectedTenantId = selected.getSenderId();
        selectedTenantName = selected.getSenderName();
        selectedPropertyId = selected.getPropertyId();
        selectedPropertyTitle = selected.getPropertyTitle();

        chatRecipientLabel.setText("Chat: " + selectedTenantName + " (" + selectedPropertyTitle + ")");

        StringBuilder sb = new StringBuilder();
        List<Message> convo = messageService.getConversation(currentUser.getId(), selectedTenantId);
        for (Message m : convo) {
            boolean isMe = m.getSenderId().equals(currentUser.getId());
            sb.append("[").append(m.getFormattedTimestamp()).append("] ")
              .append(isMe ? "You" : m.getSenderName())
              .append(":\n")
              .append(m.getContent())
              .append("\n\n");
        }
        chatHistoryArea.setText(sb.toString());
        chatHistoryArea.setCaretPosition(chatHistoryArea.getDocument().getLength());

        refreshUnreadStats();
    }

    private void sendReplyMessage() {
        if (selectedTenantId == null) {
            JOptionPane.showMessageDialog(this, "Select a conversation or click 'New Message'.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String text = replyArea.getText();
        if (text == null || text.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Message cannot be empty.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            String confirmation = messageService.sendMessage(
                    currentUser,
                    selectedTenantId,
                    selectedTenantName,
                    selectedPropertyId,
                    selectedPropertyTitle,
                    text
            );
            replyArea.setText("");
            loadSelectedConversation();
            refreshMessagesTable();
            JOptionPane.showMessageDialog(this, confirmation, "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showComposeDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Compose Message", true);
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridLayout(3, 2, 8, 10));
        form.setBorder(new EmptyBorder(15, 15, 10, 15));
        form.setBackground(Color.WHITE);

        List<User> tenants = userService.getUsersByRole(Role.TENANT);
        JComboBox<User> tenantCombo = new JComboBox<>(tenants.toArray(new User[0]));

        List<Property> myProps = propertyService.getPropertiesByManager(currentUser.getId());
        JComboBox<Property> propCombo = new JComboBox<>(myProps.toArray(new Property[0]));

        JTextField contentField = Theme.createTextField(14);

        JLabel l1 = new JLabel("Tenant:");
        l1.setForeground(Color.BLACK);
        JLabel l2 = new JLabel("Property:");
        l2.setForeground(Color.BLACK);
        JLabel l3 = new JLabel("Message:");
        l3.setForeground(Color.BLACK);

        form.add(l1);
        form.add(tenantCombo);
        form.add(l2);
        form.add(propCombo);
        form.add(l3);
        form.add(contentField);

        JPanel btnBar = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnBar.setBackground(new Color(240, 240, 240));

        JButton cancelBtn = Theme.createSecondaryButton("Cancel");
        cancelBtn.addActionListener(e -> dialog.dispose());

        JButton sendBtn = Theme.createPrimaryButton("Send");
        sendBtn.addActionListener(e -> {
            User target = (User) tenantCombo.getSelectedItem();
            Property prop = (Property) propCombo.getSelectedItem();
            if (target == null) {
                JOptionPane.showMessageDialog(dialog, "Select a tenant.", "Warning", JOptionPane.WARNING_MESSAGE);
                return;
            }
            try {
                String confirmation = messageService.sendMessage(
                        currentUser,
                        target.getId(),
                        target.getName(),
                        prop != null ? prop.getId() : null,
                        prop != null ? prop.getTitle() : "Direct Message",
                        contentField.getText()
                );
                dialog.dispose();
                refreshMessagesTable();
                JOptionPane.showMessageDialog(this, confirmation, "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnBar.add(cancelBtn);
        btnBar.add(sendBtn);

        dialog.add(form, BorderLayout.CENTER);
        dialog.add(btnBar, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void refreshMessagesTable() {
        messageModel.setRowCount(0);
        List<Message> inbox = messageService.getInbox(currentUser.getId());
        for (Message m : inbox) {
            messageModel.addRow(new Object[]{
                    m.getId(),
                    m.getSenderName(),
                    m.getPropertyTitle(),
                    m.isRead() ? "Read" : "NEW"
            });
        }
    }

    private void refreshUnreadStats() {
        int unread = messageService.getUnreadCount(currentUser.getId());
        unreadMessagesCard.updateValue(String.valueOf(unread));
    }

    public void refreshAllData() {
        List<Property> myProps = propertyService.getPropertiesByManager(currentUser.getId());
        propertyModel.setRowCount(0);
        long pendingCount = 0;
        long rentedCount = 0;
        for (Property p : myProps) {
            if (p.getStatus() == PropertyStatus.PENDING_APPROVAL) pendingCount++;
            if (p.getStatus() == PropertyStatus.RENTED) rentedCount++;

            propertyModel.addRow(new Object[]{
                    p.getId(),
                    p.getTitle(),
                    p.getAddress(),
                    "Rs. " + (long) p.getRentAmount(),
                    "Rs. " + (long) p.getDepositAmount(),
                    p.getStatus().getLabel(),
                    p.getAdminFeedback() != null ? p.getAdminFeedback() : "-"
            });
        }

        List<RentalAgreement> myAgreements = agreementService.getAgreementsByManager(currentUser.getId());
        agreementModel.setRowCount(0);
        for (RentalAgreement a : myAgreements) {
            agreementModel.addRow(new Object[]{
                    a.getId(),
                    a.getPropertyTitle(),
                    a.getTenantName(),
                    a.getTenantEmail(),
                    a.getStartDate(),
                    a.getEndDate(),
                    "Rs. " + (long) a.getMonthlyRent(),
                    a.getStatus().getLabel()
            });
        }

        refreshMessagesTable();
        refreshUnreadStats();

        myPropsCard.updateValue(String.valueOf(myProps.size()));
        pendingApprovalCard.updateValue(String.valueOf(pendingCount));
        activeLeasesCard.updateValue(String.valueOf(rentedCount));
    }
}
