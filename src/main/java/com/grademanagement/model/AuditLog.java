package com.grademanagement.model;

import java.time.LocalDateTime;

// Audit trail entry for tracking system operations
public class AuditLog {
    private final String id;
    private final String operation;
    private final String userId;
    private final String entityType;
    private final String entityId;
    private final LocalDateTime timestamp;
    private final String details;
    private final boolean success;

    public AuditLog(String operation, String userId, String entityType,
                    String entityId, String details, boolean success) {
        this.id = generateId();
        this.operation = operation;
        this.userId = userId;
        this.entityType = entityType;
        this.entityId = entityId;
        this.timestamp = LocalDateTime.now();
        this.details = details;
        this.success = success;
    }

    // Generate unique audit log ID
    private String generateId() {
        return "AUDIT_" + System.currentTimeMillis() + "_" +
                Thread.currentThread().getId();
    }

    // Getters
    public String getId() {
        return id;
    }

    public String getOperation() {
        return operation;
    }

    public String getUserId() {
        return userId;
    }

    public String getEntityType() {
        return entityType;
    }

    public String getEntityId() {
        return entityId;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public String getDetails() {
        return details;
    }

    public boolean isSuccess() {
        return success;
    }

    // Format timestamp for display
    public String getFormattedTimestamp() {
        return timestamp.toString().replace('T', ' ');
    }

    @Override
    public String toString() {
        return String.format("[%s] %s: %s %s (User: %s) - %s %s",
                getFormattedTimestamp(), operation, entityType, entityId,
                userId, details, success ? "✓" : "✗");
    }

    // Convert to CSV format
    public String toCSV() {
        return String.join(",",
                id,
                timestamp.toString(),
                operation,
                userId,
                entityType,
                entityId,
                details,
                String.valueOf(success)
        );
    }

    // Parse from CSV
    public static AuditLog fromCSV(String csvLine) {
        String[] parts = csvLine.split(",", -1);
        if (parts.length != 8) {
            throw new IllegalArgumentException("Invalid CSV format for AuditLog");
        }

        return new AuditLog(
                parts[2],  // operation
                parts[3],  // userId
                parts[4],  // entityType
                parts[5],  // entityId
                parts[6],  // details
                Boolean.parseBoolean(parts[7])  // success
        );
    }
}
