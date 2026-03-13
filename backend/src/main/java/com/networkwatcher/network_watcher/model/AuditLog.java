package com.networkwatcher.network_watcher.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

// JPA entity for tracking user actions and system events
@Entity
@Table(name = "audit_logs")
@Data // Lombok: auto-generates getters, setters, toString, equals, hashCode
@NoArgsConstructor // Lombok: generates no-args constructor for JPA
public class AuditLog {
    // Primary key with auto-increment
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // User who performed the action
    @Column(nullable = false, length = 50)
    private String username;

    // What action was performed (LOGIN, SCAN_STARTED, DEVICE_DELETED, etc.)
    @Column(nullable = false, length = 100)
    private String action;

    // What resource was affected (device ID, scan job ID, etc.)
    @Column(length = 200)
    private String resource;

    // Additional context about the action
    @Column(length = 1000)
    private String details;

    // IP address where action originated from
    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    // When the action occurred (immutable)
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // JPA lifecycle callback - sets timestamp when saving
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
