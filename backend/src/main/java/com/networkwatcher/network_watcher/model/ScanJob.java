package com.networkwatcher.network_watcher.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

// JPA entity for tracking scan execution and results
@Entity
@Table(name = "scan_jobs")
@Data // Lombok: auto-generates getters, setters, toString, equals, hashCode
@NoArgsConstructor // Lombok: generates no-args constructor for JPA
public class ScanJob {
    // Primary key with auto-increment
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Foreign key to NetworkScope (which network range to scan)
    @ManyToOne
    @JoinColumn(name = "network_scope_id")
    private NetworkScope networkScope;

    // Type of scan to perform
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ScanType scanType;

    // Current status of the scan job
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ScanStatus status = ScanStatus.QUEUED;

    // When scan execution began
    @Column(name = "started_at")
    private LocalDateTime startedAt;

    // When scan finished (success or failure)
    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    // Number of devices discovered during scan
    @Column(name = "devices_found")
    private Integer devicesFound = 0;

    // Error details if scan failed
    @Column(length = 1000)
    private String errorMessage;

    // When scan job was created (immutable)
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // JPA lifecycle callback - sets creation timestamp
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    // Types of scans the system can perform
    public enum ScanType {
        DISCOVERY,     // Basic ping sweep to find active devices
        PORT_SCAN,     // Check for open ports on devices
        VULNERABILITY, // Security vulnerability assessment
        FULL          // Complete scan (discovery + ports + vulnerabilities)
    }

    // Scan execution states
    public enum ScanStatus {
        QUEUED,    // Waiting to start
        RUNNING,   // Currently executing
        SUCCESS,   // Completed successfully
        FAILED,    // Completed with errors
        CANCELLED  // Stopped by user
    }
}
