package com.realestate.ui;

import com.realestate.model.Role;
import com.realestate.model.User;
import com.realestate.service.AuthService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.function.Consumer;

@SuppressWarnings("serial")
public class LoginView extends JPanel {
    private final Consumer<User> onLoginSuccess;
    private final JTextField emailField;
    private final JPasswordField passwordField;

    public LoginView(Consumer<User> onLoginSuccess) {
        this.onLoginSuccess = onLoginSuccess;
        setLayout(new GridBagLayout());
        setBackground(new Color(235, 235, 235));

        JPanel box = new JPanel();
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
        box.setPreferredSize(new Dimension(420, 430));
        box.setBackground(Color.WHITE);
        box.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BORDER, 1),
                new EmptyBorder(25, 30, 25, 30)
        ));

        JLabel titleLabel = new JLabel("Real Estate Management System");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel.setForeground(Color.BLACK);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subLabel = new JLabel("User Login");
        subLabel.setFont(new Font("Arial", Font.BOLD, 13));
        subLabel.setForeground(new Color(60, 60, 60));
        subLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        box.add(titleLabel);
        box.add(Box.createRigidArea(new Dimension(0, 5)));
        box.add(subLabel);
        box.add(Box.createRigidArea(new Dimension(0, 20)));

        JLabel emailLbl = new JLabel("Email:");
        emailLbl.setFont(Theme.FONT_BOLD);
        emailLbl.setForeground(Color.BLACK);
        emailLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        emailField = Theme.createTextField(20);
        emailField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        emailField.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel passLbl = new JLabel("Password:");
        passLbl.setFont(Theme.FONT_BOLD);
        passLbl.setForeground(Color.BLACK);
        passLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        passwordField = Theme.createPasswordField(20);
        passwordField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        passwordField.setAlignmentX(Component.LEFT_ALIGNMENT);

        box.add(emailLbl);
        box.add(Box.createRigidArea(new Dimension(0, 4)));
        box.add(emailField);
        box.add(Box.createRigidArea(new Dimension(0, 10)));
        box.add(passLbl);
        box.add(Box.createRigidArea(new Dimension(0, 4)));
        box.add(passwordField);
        box.add(Box.createRigidArea(new Dimension(0, 18)));

        JButton loginBtn = Theme.createPrimaryButton("Login");
        loginBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        loginBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        loginBtn.addActionListener(e -> performLogin());

        JButton regBtn = Theme.createSecondaryButton("Register New Account");
        regBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        regBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        regBtn.addActionListener(e -> showRegisterDialog());

        box.add(loginBtn);
        box.add(Box.createRigidArea(new Dimension(0, 8)));
        box.add(regBtn);
        box.add(Box.createRigidArea(new Dimension(0, 20)));

        JLabel demoLbl = new JLabel("Quick Demo Login:");
        demoLbl.setFont(Theme.FONT_BOLD);
        demoLbl.setForeground(Color.BLACK);
        demoLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        box.add(demoLbl);
        box.add(Box.createRigidArea(new Dimension(0, 6)));

        JPanel demoPanel = new JPanel(new GridLayout(1, 3, 6, 0));
        demoPanel.setBackground(Color.WHITE);
        demoPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        demoPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton adminBtn = Theme.createSecondaryButton("Admin");
        adminBtn.addActionListener(e -> quickLogin("admin@realestate.com", "admin"));

        JButton mgrBtn = Theme.createSecondaryButton("Manager");
        mgrBtn.addActionListener(e -> quickLogin("amit@gmail.com", "amit"));

        JButton tenantBtn = Theme.createSecondaryButton("Tenant");
        tenantBtn.addActionListener(e -> quickLogin("rahul@gmail.com", "rahul"));

        demoPanel.add(adminBtn);
        demoPanel.add(mgrBtn);
        demoPanel.add(tenantBtn);
        box.add(demoPanel);

        add(box);
    }

    private void performLogin() {
        String email = emailField.getText();
        String pass = new String(passwordField.getPassword());
        try {
            User user = AuthService.getInstance().login(email, pass);
            onLoginSuccess.accept(user);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Login Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void quickLogin(String email, String pass) {
        emailField.setText(email);
        passwordField.setText(pass);
        performLogin();
    }

    private void showRegisterDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Register User", true);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(380, 320);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridLayout(5, 2, 8, 10));
        panel.setBorder(new EmptyBorder(20, 20, 15, 20));
        panel.setBackground(Color.WHITE);

        JTextField nameF = Theme.createTextField(15);
        JTextField emailF = Theme.createTextField(15);
        JPasswordField passF = Theme.createPasswordField(15);
        JComboBox<Role> roleCombo = new JComboBox<>(new Role[]{Role.PROPERTY_MANAGER, Role.TENANT});

        JLabel l1 = new JLabel("Name:");
        l1.setForeground(Color.BLACK);
        JLabel l2 = new JLabel("Email:");
        l2.setForeground(Color.BLACK);
        JLabel l3 = new JLabel("Password:");
        l3.setForeground(Color.BLACK);
        JLabel l4 = new JLabel("Role:");
        l4.setForeground(Color.BLACK);

        panel.add(l1);
        panel.add(nameF);
        panel.add(l2);
        panel.add(emailF);
        panel.add(l3);
        panel.add(passF);
        panel.add(l4);
        panel.add(roleCombo);

        JPanel btnBar = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnBar.setBackground(new Color(240, 240, 240));

        JButton cancelBtn = Theme.createSecondaryButton("Cancel");
        cancelBtn.addActionListener(e -> dialog.dispose());

        JButton submitBtn = Theme.createPrimaryButton("Register");
        submitBtn.addActionListener(e -> {
            try {
                User created = AuthService.getInstance().register(
                        nameF.getText(),
                        emailF.getText(),
                        new String(passF.getPassword()),
                        (Role) roleCombo.getSelectedItem()
                );
                JOptionPane.showMessageDialog(dialog, "Account created successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
                emailField.setText(created.getEmail());
                passwordField.setText(created.getPassword());
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnBar.add(cancelBtn);
        btnBar.add(submitBtn);

        dialog.add(panel, BorderLayout.CENTER);
        dialog.add(btnBar, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }
}
