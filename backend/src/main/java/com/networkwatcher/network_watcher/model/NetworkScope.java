package com.networkwatcher.network_watcher.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

// JPA entity for defining network ranges to scan
@Entity
@Table(name = "network_scopes")
@Data // Lombok: auto-generates getters, setters, toString, equals, hashCode
@NoArgsConstructor // Lombok: generates no-args constructor for JPA
public class NetworkScope {
    // Primary key with auto-increment
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Human-readable name for this network scope
    @Column(nullable = false, length = 100)
    private String name;

    // Network range in CIDR notation (e.g., "192.168.1.0/24")
    @Column(nullable = false, length = 50)
    private String cidr;

    // Comma-separated list of IPs to exclude from scanning
    @Column(length = 500)
    private String exclusions;

    // Whether this scope is active for scanning
    @Column(nullable = false)
    private Boolean enabled = true;

    // Cron expression for scheduled scans (e.g., "0 0 2 * * ?")
    @Column(length = 50)
    private String scanSchedule;

    // When this scope was created (immutable)
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // When this scope was last scanned
    @Column(name = "last_scanned")
    private LocalDateTime lastScanned;

    // JPA lifecycle callback - sets creation timestamp
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
