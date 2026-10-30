package com.realestate.service;

import com.realestate.model.Role;
import com.realestate.model.User;
import com.realestate.model.UserStatus;
import com.realestate.repository.DataStore;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class UserService {
    private static UserService instance;
    private final DataStore dataStore;

    private UserService() {
        this.dataStore = DataStore.getInstance();
    }

    public static synchronized UserService getInstance() {
        if (instance == null) {
            instance = new UserService();
        }
        return instance;
    }

    public synchronized List<User> getAllUsers() {
        return new ArrayList<>(dataStore.getUsers());
    }

    public synchronized List<User> getUsersByRole(Role role) {
        return dataStore.getUsers().stream()
                .filter(u -> u.getRole() == role)
                .collect(Collectors.toList());
    }

    public synchronized List<User> searchUsers(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllUsers();
        }
        String lower = keyword.trim().toLowerCase();
        return dataStore.getUsers().stream()
                .filter(u -> u.getName().toLowerCase().contains(lower) ||
                        u.getEmail().toLowerCase().contains(lower) ||
                        u.getRole().name().toLowerCase().contains(lower))
                .collect(Collectors.toList());
    }

    public synchronized String createUser(String name, String email, String password, Role role) throws Exception {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("User name cannot be empty.");
        }
        if (email == null || !email.contains("@")) {
            throw new IllegalArgumentException("A valid email address is required.");
        }
        if (password == null || password.trim().isEmpty()) {
            password = "password123";
        }

        for (User u : dataStore.getUsers()) {
            if (u.getEmail().equalsIgnoreCase(email.trim())) {
                throw new IllegalArgumentException("A user with email '" + email + "' already exists.");
            }
        }

        String id = "U-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        User newUser = new User(id, name.trim(), email.trim(), password.trim(), role);
        dataStore.getUsers().add(newUser);
        dataStore.saveToDisk();

        return "User account '" + name + "' (" + role.getDisplayName() + ") created successfully! (ID: " + id + ")";
    }

    public synchronized String updateUser(String id, String name, String email, Role role, UserStatus status) throws Exception {
        User user = getUserById(id);
        if (user == null) {
            throw new IllegalArgumentException("User not found with ID: " + id);
        }

        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name cannot be empty.");
        }
        if (email == null || !email.contains("@")) {
            throw new IllegalArgumentException("A valid email address is required.");
        }

        for (User u : dataStore.getUsers()) {
            if (!u.getId().equals(id) && u.getEmail().equalsIgnoreCase(email.trim())) {
                throw new IllegalArgumentException("Email is already taken by another account.");
            }
        }

        user.setName(name.trim());
        user.setEmail(email.trim());
        user.setRole(role);
        user.setStatus(status);
        dataStore.saveToDisk();

        return "User account '" + name + "' updated successfully!";
    }

    public synchronized String toggleUserStatus(String id) throws Exception {
        User user = getUserById(id);
        if (user == null) {
            throw new IllegalArgumentException("User not found.");
        }
        if (user.getRole() == Role.ADMIN && user.getStatus() == UserStatus.ACTIVE) {
            long adminCount = dataStore.getUsers().stream()
                    .filter(u -> u.getRole() == Role.ADMIN && u.getStatus() == UserStatus.ACTIVE)
                    .count();
            if (adminCount <= 1) {
                throw new IllegalStateException("Cannot deactivate the sole active Administrator account.");
            }
        }

        if (user.getStatus() == UserStatus.ACTIVE) {
            user.setStatus(UserStatus.INACTIVE);
        } else {
            user.setStatus(UserStatus.ACTIVE);
        }
        dataStore.saveToDisk();

        return "User status updated to: " + user.getStatus().getLabel();
    }

    public synchronized String deleteUser(String id) throws Exception {
        User user = getUserById(id);
        if (user == null) {
            throw new IllegalArgumentException("User not found.");
        }
        if (user.getRole() == Role.ADMIN) {
            long adminCount = dataStore.getUsers().stream().filter(u -> u.getRole() == Role.ADMIN).count();
            if (adminCount <= 1) {
                throw new IllegalStateException("Cannot delete the only Administrator account.");
            }
        }

        dataStore.getUsers().remove(user);
        dataStore.saveToDisk();
        return "User '" + user.getName() + "' deleted successfully!";
    }

    public synchronized User getUserById(String id) {
        return dataStore.getUsers().stream()
                .filter(u -> u.getId().equals(id))
                .findFirst()
                .orElse(null);
    }
}
