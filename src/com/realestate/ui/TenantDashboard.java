package com.realestate.ui;

import com.realestate.model.*;
import com.realestate.service.AgreementService;
import com.realestate.service.MessageService;
import com.realestate.service.PropertyService;
import com.realestate.ui.components.BadgeRenderer;
import com.realestate.ui.components.StatCard;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

@SuppressWarnings("serial")
public class TenantDashboard extends JPanel {
    private final User currentUser;
    private final PropertyService propertyService = PropertyService.getInstance();
    private final AgreementService agreementService = AgreementService.getInstance();
    private final MessageService messageService = MessageService.getInstance();

    private StatCard availablePropsCard;
    private StatCard myApplicationsCard;
    private StatCard activeLeasesCard;
    private StatCard unreadMessagesCard;

    private JTextField searchField;
    private JTextField maxRentField;
    private JTable propertyTable;
    private DefaultTableModel propertyModel;

    private JTable agreementTable;
    private DefaultTableModel agreementModel;

    private JTable messageTable;
    private DefaultTableModel messageModel;
    private JTextArea chatHistoryArea;
    private JTextArea replyArea;
    private JLabel chatRecipientLabel;
    private String selectedManagerId;
    private String selectedManagerName;
    private String selectedPropertyId;
    private String selectedPropertyTitle;

    public TenantDashboard(User user) {
        this.currentUser = user;
        setLayout(new BorderLayout());
        setBackground(new Color(240, 240, 240));
        setBorder(new EmptyBorder(10, 15, 10, 15));

        JPanel topPanel = new JPanel(new BorderLayout(0, 10));
        topPanel.setOpaque(false);

        JLabel titleLabel = new JLabel("Tenant Dashboard - " + user.getName());
        titleLabel.setFont(Theme.FONT_HEADER);
        titleLabel.setForeground(Color.BLACK);
        topPanel.add(titleLabel, BorderLayout.NORTH);

        JPanel statsRow = new JPanel(new GridLayout(1, 4, 10, 0));
        statsRow.setOpaque(false);

        availablePropsCard = new StatCard("Available Properties", "0", Theme.PRIMARY);
        myApplicationsCard = new StatCard("My Applications", "0", Theme.WARNING);
        activeLeasesCard = new StatCard("Active Leases", "0", Theme.SUCCESS);
        unreadMessagesCard = new StatCard("Unread Messages", "0", Theme.INFO);

        statsRow.add(availablePropsCard);
        statsRow.add(myApplicationsCard);
        statsRow.add(activeLeasesCard);
        statsRow.add(unreadMessagesCard);
        topPanel.add(statsRow, BorderLayout.SOUTH);

        add(topPanel, BorderLayout.NORTH);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(Theme.FONT_BOLD);
        tabbedPane.setBackground(Color.WHITE);

        tabbedPane.addTab("Search Properties", createPropertySearchPanel());
        tabbedPane.addTab("My Agreements", createRentalAgreementsPanel());
        tabbedPane.addTab("Manager Messages", createMessagesPanel());

        add(tabbedPane, BorderLayout.CENTER);

        refreshAllData();
    }

    private JPanel createPropertySearchPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel toolbar = new JPanel(new BorderLayout());
        toolbar.setOpaque(false);

        JPanel searchControls = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        searchControls.setOpaque(false);

        JLabel l1 = new JLabel("Search:");
        l1.setForeground(Color.BLACK);
        searchField = Theme.createTextField(12);

        JLabel l2 = new JLabel("Max Rent (Rs.):");
        l2.setForeground(Color.BLACK);
        maxRentField = Theme.createTextField(7);

        JButton searchBtn = Theme.createPrimaryButton("Search");
        searchBtn.addActionListener(e -> performPropertySearch());

        JButton resetBtn = Theme.createSecondaryButton("Clear");
        resetBtn.addActionListener(e -> {
            searchField.setText("");
            maxRentField.setText("");
            populatePropertyTable(propertyService.getApprovedProperties());
        });

        searchControls.add(l1);
        searchControls.add(searchField);
        searchControls.add(l2);
        searchControls.add(maxRentField);
        searchControls.add(searchBtn);
        searchControls.add(resetBtn);

        JPanel actionBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        actionBtns.setOpaque(false);

        JButton applyBtn = Theme.createSuccessButton("Apply for Rental");
        applyBtn.addActionListener(e -> applyForSelectedProperty());

        JButton inquireBtn = Theme.createSecondaryButton("Contact Manager");
        inquireBtn.addActionListener(e -> contactPropertyManager());

        JButton detailsBtn = Theme.createSecondaryButton("Details");
        detailsBtn.addActionListener(e -> viewPropertyDetails());

        actionBtns.add(detailsBtn);
        actionBtns.add(inquireBtn);
        actionBtns.add(applyBtn);

        toolbar.add(searchControls, BorderLayout.WEST);
        toolbar.add(actionBtns, BorderLayout.EAST);
        panel.add(toolbar, BorderLayout.NORTH);

        String[] cols = {"ID", "Property Title", "Address", "Rent / Month", "Deposit", "Manager", "Status"};
        propertyModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
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

    private void performPropertySearch() {
        String kw = searchField.getText();
        Double maxRent = null;
        if (!maxRentField.getText().trim().isEmpty()) {
            try {
                maxRent = Double.parseDouble(maxRentField.getText().trim());
            } catch (NumberFormatException ignored) {}
        }
        List<Property> results = propertyService.searchProperties(kw, maxRent);
        populatePropertyTable(results);
    }

    private void populatePropertyTable(List<Property> list) {
        propertyModel.setRowCount(0);
        for (Property p : list) {
            propertyModel.addRow(new Object[]{
                    p.getId(),
                    p.getTitle(),
                    p.getAddress(),
                    "Rs. " + (long) p.getRentAmount(),
                    "Rs. " + (long) p.getDepositAmount(),
                    p.getManagerName(),
                    p.getStatus().getLabel()
            });
        }
    }

    private void applyForSelectedProperty() {
        int row = propertyTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a property first.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String propId = (String) propertyModel.getValueAt(row, 0);
        Property p = propertyService.getPropertyById(propId);
        if (p == null) return;

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Apply for " + p.getTitle(), true);
        dialog.setSize(400, 320);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridLayout(4, 2, 8, 10));
        form.setBorder(new EmptyBorder(15, 15, 10, 15));
        form.setBackground(Color.WHITE);

        JTextField startDateField = Theme.createTextField(12);
        startDateField.setText(LocalDate.now().plusWeeks(1).toString());

        JComboBox<Integer> durationCombo = new JComboBox<>(new Integer[]{6, 11, 12, 24});
        durationCombo.setSelectedItem(11);

        JTextField notesField = Theme.createTextField(12);
        notesField.setText("Interested in renting this property.");

        JLabel l1 = new JLabel("Property:");
        l1.setForeground(Color.BLACK);
        JLabel l2 = new JLabel("Move-in Date (YYYY-MM-DD):");
        l2.setForeground(Color.BLACK);
        JLabel l3 = new JLabel("Duration (Months):");
        l3.setForeground(Color.BLACK);
        JLabel l4 = new JLabel("Notes:");
        l4.setForeground(Color.BLACK);

        JLabel propNameLbl = new JLabel(p.getTitle());
        propNameLbl.setFont(Theme.FONT_BOLD);
        propNameLbl.setForeground(Color.BLACK);

        form.add(l1);
        form.add(propNameLbl);
        form.add(l2);
        form.add(startDateField);
        form.add(l3);
        form.add(durationCombo);
        form.add(l4);
        form.add(notesField);

        JPanel btnBar = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnBar.setBackground(new Color(240, 240, 240));

        JButton cancelBtn = Theme.createSecondaryButton("Cancel");
        cancelBtn.addActionListener(e -> dialog.dispose());

        JButton submitBtn = Theme.createSuccessButton("Submit Application");
        submitBtn.addActionListener(e -> {
            try {
                LocalDate start = LocalDate.parse(startDateField.getText().trim());
                int months = (Integer) durationCombo.getSelectedItem();
                String confirmation = agreementService.applyForRental(p, currentUser, start, months, notesField.getText());
                dialog.dispose();
                refreshAllData();
                JOptionPane.showMessageDialog(this, confirmation, "Success", JOptionPane.INFORMATION_MESSAGE);
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

    private void contactPropertyManager() {
        int row = propertyTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a property first.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String propId = (String) propertyModel.getValueAt(row, 0);
        Property p = propertyService.getPropertyById(propId);
        if (p == null) return;

        String msg = JOptionPane.showInputDialog(this,
                "Send message to manager " + p.getManagerName() + ":",
                "Contact Manager", JOptionPane.QUESTION_MESSAGE);

        if (msg != null && !msg.trim().isEmpty()) {
            try {
                String confirmation = messageService.sendMessage(
                        currentUser,
                        p.getManagerId(),
                        p.getManagerName(),
                        p.getId(),
                        p.getTitle(),
                        msg
                );
                refreshAllData();
                JOptionPane.showMessageDialog(this, confirmation, "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void viewPropertyDetails() {
        int row = propertyTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a property first.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String propId = (String) propertyModel.getValueAt(row, 0);
        Property p = propertyService.getPropertyById(propId);
        if (p == null) return;

        JTextArea desc = new JTextArea(p.getDescription());
        desc.setEditable(false);
        desc.setLineWrap(true);
        desc.setWrapStyleWord(true);
        desc.setForeground(Color.BLACK);
        desc.setBackground(Color.WHITE);
        JScrollPane scroll = new JScrollPane(desc);
        scroll.setPreferredSize(new Dimension(360, 90));

        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBackground(Color.WHITE);

        JLabel info = new JLabel("<html><b>Title:</b> " + p.getTitle() + "<br>" +
                "<b>Address:</b> " + p.getAddress() + "<br>" +
                "<b>Rent:</b> Rs. " + (long) p.getRentAmount() + " / month | <b>Deposit:</b> Rs. " + (long) p.getDepositAmount() + "<br>" +
                "<b>Manager:</b> " + p.getManagerName() + "<br><br><b>Description:</b></html>");
        info.setForeground(Color.BLACK);
        panel.add(info, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);

        JOptionPane.showMessageDialog(this, panel, "Property Details", JOptionPane.PLAIN_MESSAGE);
    }

    private JPanel createRentalAgreementsPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        toolbar.setOpaque(false);

        JButton viewTermsBtn = Theme.createPrimaryButton("View Agreement Terms");
        viewTermsBtn.addActionListener(e -> viewSelectedAgreementDetails());
        toolbar.add(viewTermsBtn);
        panel.add(toolbar, BorderLayout.NORTH);

        String[] cols = {"Agreement ID", "Property", "Address", "Manager", "Start Date", "End Date", "Rent (Rs.)", "Status"};
        agreementModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
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

    private void viewSelectedAgreementDetails() {
        int row = agreementTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select an agreement to view details.", "Warning", JOptionPane.WARNING_MESSAGE);
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
        scroll.setPreferredSize(new Dimension(360, 90));

        JPanel p = new JPanel(new BorderLayout(5, 5));
        p.setBackground(Color.WHITE);

        JLabel info = new JLabel("<html><b>Agreement ID:</b> " + a.getId() + "<br>" +
                "<b>Property:</b> " + a.getPropertyTitle() + " (" + a.getPropertyAddress() + ")<br>" +
                "<b>Manager:</b> " + a.getManagerName() + "<br>" +
                "<b>Period:</b> " + a.getStartDate() + " to " + a.getEndDate() + "<br>" +
                "<b>Rent:</b> Rs. " + (long) a.getMonthlyRent() + " | <b>Deposit:</b> Rs. " + (long) a.getSecurityDeposit() + "<br>" +
                "<b>Status:</b> " + a.getStatus().getLabel() + "<br><br><b>Terms:</b></html>");
        info.setForeground(Color.BLACK);
        p.add(info, BorderLayout.NORTH);
        p.add(scroll, BorderLayout.CENTER);

        JOptionPane.showMessageDialog(this, p, "Agreement Details", JOptionPane.PLAIN_MESSAGE);
    }

    private JPanel createMessagesPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 0));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel left = new JPanel(new BorderLayout(0, 5));
        left.setBackground(Color.WHITE);
        left.setPreferredSize(new Dimension(360, 350));

        JLabel leftTitle = new JLabel("Messages");
        leftTitle.setFont(Theme.FONT_BOLD);
        leftTitle.setForeground(Color.BLACK);
        left.add(leftTitle, BorderLayout.NORTH);

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

        JButton sendBtn = Theme.createPrimaryButton("Send Reply");
        sendBtn.addActionListener(e -> sendReplyMessage());

        composePanel.add(replyScroll, BorderLayout.CENTER);
        composePanel.add(sendBtn, BorderLayout.EAST);
        right.add(composePanel, BorderLayout.SOUTH);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, left, right);
        split.setDividerLocation(360);
        panel.add(split, BorderLayout.CENTER);

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
        selectedManagerId = selected.getSenderId();
        selectedManagerName = selected.getSenderName();
        selectedPropertyId = selected.getPropertyId();
        selectedPropertyTitle = selected.getPropertyTitle();

        chatRecipientLabel.setText("Chat: " + selectedManagerName + " (" + selectedPropertyTitle + ")");

        StringBuilder sb = new StringBuilder();
        List<Message> convo = messageService.getConversation(currentUser.getId(), selectedManagerId);
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
        if (selectedManagerId == null) {
            JOptionPane.showMessageDialog(this, "Select a conversation first.", "Warning", JOptionPane.WARNING_MESSAGE);
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
                    selectedManagerId,
                    selectedManagerName,
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
        List<Property> approved = propertyService.getApprovedProperties();
        populatePropertyTable(approved);
        availablePropsCard.updateValue(String.valueOf(approved.size()));

        List<RentalAgreement> myAgreements = agreementService.getAgreementsByTenant(currentUser.getId());
        agreementModel.setRowCount(0);
        long pending = 0;
        long active = 0;
        for (RentalAgreement a : myAgreements) {
            if (a.getStatus() == AgreementStatus.PENDING) pending++;
            if (a.getStatus() == AgreementStatus.ACTIVE) active++;

            agreementModel.addRow(new Object[]{
                    a.getId(),
                    a.getPropertyTitle(),
                    a.getPropertyAddress(),
                    a.getManagerName(),
                    a.getStartDate(),
                    a.getEndDate(),
                    "Rs. " + (long) a.getMonthlyRent(),
                    a.getStatus().getLabel()
            });
        }
        myApplicationsCard.updateValue(String.valueOf(pending));
        activeLeasesCard.updateValue(String.valueOf(active));

        refreshMessagesTable();
        refreshUnreadStats();
    }
}
