package com.realestate.service;

import com.realestate.model.Message;
import com.realestate.model.Role;
import com.realestate.model.User;
import com.realestate.repository.DataStore;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class MessageService {
    private static MessageService instance;
    private final DataStore dataStore;

    private MessageService() {
        this.dataStore = DataStore.getInstance();
    }

    public static synchronized MessageService getInstance() {
        if (instance == null) {
            instance = new MessageService();
        }
        return instance;
    }

    public synchronized String sendMessage(User sender, String receiverId, String receiverName,
                                          String propertyId, String propertyTitle, String content) throws Exception {
        if (sender == null) {
            throw new IllegalArgumentException("Sender cannot be null.");
        }
        if (receiverId == null || receiverId.trim().isEmpty()) {
            throw new IllegalArgumentException("Recipient must be selected.");
        }
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("Message content cannot be empty.");
        }

        String id = "MSG-" + (300 + dataStore.getMessages().size() + 1);
        Message message = new Message(
                id,
                sender.getId(),
                sender.getName(),
                sender.getRole(),
                receiverId,
                receiverName,
                propertyId != null ? propertyId : "N/A",
                propertyTitle != null ? propertyTitle : "General Inquiry",
                content.trim()
        );

        dataStore.getMessages().add(message);
        dataStore.saveToDisk();

        return "Message successfully delivered to " + receiverName + "! (ID: " + id + ")";
    }

    public synchronized List<Message> getInbox(String userId) {
        return dataStore.getMessages().stream()
                .filter(m -> m.getReceiverId().equals(userId))
                .sorted((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()))
                .collect(Collectors.toList());
    }

    public synchronized List<Message> getSent(String userId) {
        return dataStore.getMessages().stream()
                .filter(m -> m.getSenderId().equals(userId))
                .sorted((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()))
                .collect(Collectors.toList());
    }

    public synchronized List<Message> getConversation(String user1Id, String user2Id) {
        return dataStore.getMessages().stream()
                .filter(m -> (m.getSenderId().equals(user1Id) && m.getReceiverId().equals(user2Id)) ||
                        (m.getSenderId().equals(user2Id) && m.getReceiverId().equals(user1Id)))
                .sorted((a, b) -> a.getTimestamp().compareTo(b.getTimestamp()))
                .collect(Collectors.toList());
    }

    public synchronized int getUnreadCount(String userId) {
        return (int) dataStore.getMessages().stream()
                .filter(m -> m.getReceiverId().equals(userId) && !m.isRead())
                .count();
    }

    public synchronized void markAsRead(String messageId) {
        for (Message m : dataStore.getMessages()) {
            if (m.getId().equals(messageId)) {
                m.setRead(true);
                dataStore.saveToDisk();
                break;
            }
        }
    }
}
