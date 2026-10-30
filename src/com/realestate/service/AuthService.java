package com.realestate.service;

import com.realestate.model.Role;
import com.realestate.model.User;
import com.realestate.model.UserStatus;
import com.realestate.repository.DataStore;

import java.util.UUID;

public class AuthService {
    private static AuthService instance;
    private final DataStore dataStore;
    private User currentUser;

    private AuthService() {
        this.dataStore = DataStore.getInstance();
    }

    public static synchronized AuthService getInstance() {
        if (instance == null) {
            instance = new AuthService();
        }
        return instance;
    }

    public synchronized User login(String email, String password) throws Exception {
        if (email == null || email.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Email and password cannot be empty.");
        }

        for (User u : dataStore.getUsers()) {
            if (u.getEmail().equalsIgnoreCase(email.trim())) {
                if (u.getStatus() == UserStatus.INACTIVE) {
                    throw new IllegalStateException("Your account has been deactivated by the Administrator.");
                }
                if (u.getPassword().equals(password.trim())) {
                    this.currentUser = u;
                    return u;
                } else {
                    throw new IllegalArgumentException("Invalid password. Please check your credentials.");
                }
            }
        }
        throw new IllegalArgumentException("No account found with email: " + email);
    }

    public synchronized User register(String name, String email, String password, Role role) throws Exception {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name cannot be empty.");
        }
        if (email == null || !email.contains("@")) {
            throw new IllegalArgumentException("Please enter a valid email address.");
        }
        if (password == null || password.length() < 3) {
            throw new IllegalArgumentException("Password must be at least 3 characters.");
        }
        if (role == null) {
            throw new IllegalArgumentException("Please select a valid role.");
        }

        for (User u : dataStore.getUsers()) {
            if (u.getEmail().equalsIgnoreCase(email.trim())) {
                throw new IllegalArgumentException("An account with this email already exists.");
            }
        }

        String id = "U-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        User newUser = new User(id, name.trim(), email.trim(), password.trim(), role);
        dataStore.getUsers().add(newUser);
        dataStore.saveToDisk();
        return newUser;
    }

    public synchronized void logout() {
        this.currentUser = null;
    }

    public synchronized User getCurrentUser() {
        return currentUser;
    }

    public synchronized void setCurrentUser(User user) {
        this.currentUser = user;
    }

    public synchronized boolean isLoggedIn() {
        return currentUser != null;
    }
}
