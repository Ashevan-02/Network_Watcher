package com.networkwatcher.network_watcher.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

// JPA entity for database table "alerts"
@Entity
@Table(name = "alerts")
@Data // Lombok: auto-generates getters, setters, toString, equals, hashCode
@NoArgsConstructor // Lombok: generates no-args constructor for JPA
public class Alert {
    // Primary key with auto-increment
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Foreign key relationship to Device table (many alerts can belong to one device)
    @ManyToOne
    @JoinColumn(name = "device_id")
    private Device device;

    // Store enum as string in database (not ordinal numbers)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AlertType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AlertSeverity severity;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(length = 1000)
    private String description;

    // Default status is OPEN for new alerts
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AlertStatus status = AlertStatus.OPEN;

    // Timestamp set once when alert is created (never updated)
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // Timestamp when alert was acknowledged by user
    @Column(name = "acknowledged_at")
    private LocalDateTime acknowledgedAt;

    // Username who acknowledged the alert
    @Column(name = "acknowledged_by")
    private String acknowledgedBy;

    // JPA lifecycle callback - runs before saving to database
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    // Types of alerts the system can generate
    public enum AlertType {
        VULNERABILITY,      // Security vulnerability found
        SUSPICIOUS_TRAFFIC, // Unusual network activity
        DEVICE_OFFLINE,     // Device stopped responding
        UNAUTHORIZED_DEVICE,// Unknown device joined network
        HIGH_BANDWIDTH      // Device using excessive bandwidth
    }

    // Alert priority levels
    public enum AlertSeverity {
        LOW,
        MEDIUM,
        HIGH,
        CRITICAL
    }

    // Alert workflow states
    public enum AlertStatus {
        OPEN,         // New alert, needs attention
        ACKNOWLEDGED, // User has seen and is handling
        DISMISSED,    // False positive or resolved
        ESCALATED     // Requires higher-level attention
    }
}
