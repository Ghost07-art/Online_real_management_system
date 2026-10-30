package com.realestate.ui;

import com.realestate.model.Role;
import com.realestate.model.User;
import com.realestate.repository.DataStore;
import com.realestate.service.AuthService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

@SuppressWarnings("serial")
public class MainFrame extends JFrame {
    private final CardLayout cardLayout;
    private final JPanel contentCards;

    private final JPanel topNavBar;
    private final JLabel userBadgeLabel;

    private LoginView loginView;
    private AdminDashboard adminDashboard;
    private PropertyManagerDashboard managerDashboard;
    private TenantDashboard tenantDashboard;

    public MainFrame() {
        setTitle("Online Real Estate Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 720);
        setMinimumSize(new Dimension(900, 600));
        setLocationRelativeTo(null);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                DataStore.getInstance().saveToDisk();
            }
        });

        setLayout(new BorderLayout());

        topNavBar = new JPanel(new BorderLayout());
        topNavBar.setBackground(new Color(24, 76, 120));
        topNavBar.setPreferredSize(new Dimension(0, 48));
        topNavBar.setBorder(new EmptyBorder(8, 15, 8, 15));

        JLabel brandName = new JLabel("Online Real Estate Management System");
        brandName.setFont(new Font("Arial", Font.BOLD, 15));
        brandName.setForeground(Color.WHITE);

        JPanel userActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        userActions.setOpaque(false);

        userBadgeLabel = new JLabel("");
        userBadgeLabel.setFont(Theme.FONT_BOLD);
        userBadgeLabel.setForeground(Color.WHITE);

        JButton logoutBtn = new JButton("Logout");
        logoutBtn.setFont(Theme.FONT_BOLD);
        logoutBtn.setBackground(new Color(220, 220, 220));
        logoutBtn.setForeground(Color.BLACK);
        logoutBtn.setFocusPainted(false);
        logoutBtn.addActionListener(e -> logout());

        userActions.add(userBadgeLabel);
        userActions.add(logoutBtn);

        topNavBar.add(brandName, BorderLayout.WEST);
        topNavBar.add(userActions, BorderLayout.EAST);
        topNavBar.setVisible(false);

        add(topNavBar, BorderLayout.NORTH);

        cardLayout = new CardLayout();
        contentCards = new JPanel(cardLayout);

        loginView = new LoginView(this::handleSuccessfulLogin);
        contentCards.add(loginView, "LOGIN");

        add(contentCards, BorderLayout.CENTER);

        showLogin();
    }

    public void showLogin() {
        topNavBar.setVisible(false);
        cardLayout.show(contentCards, "LOGIN");
    }

    private void handleSuccessfulLogin(User user) {
        topNavBar.setVisible(true);
        userBadgeLabel.setText(user.getName() + " (" + user.getRole().getDisplayName() + ")");

        if (user.getRole() == Role.ADMIN) {
            if (adminDashboard == null) {
                adminDashboard = new AdminDashboard();
                contentCards.add(adminDashboard, "ADMIN");
            } else {
                adminDashboard.refreshAllData();
            }
            cardLayout.show(contentCards, "ADMIN");
        } else if (user.getRole() == Role.PROPERTY_MANAGER) {
            managerDashboard = new PropertyManagerDashboard(user);
            contentCards.add(managerDashboard, "PROPERTY_MANAGER");
            cardLayout.show(contentCards, "PROPERTY_MANAGER");
        } else if (user.getRole() == Role.TENANT) {
            tenantDashboard = new TenantDashboard(user);
            contentCards.add(tenantDashboard, "TENANT");
            cardLayout.show(contentCards, "TENANT");
        }
    }

    private void logout() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Do you want to logout?",
                "Logout", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            AuthService.getInstance().logout();
            showLogin();
        }
    }
}
