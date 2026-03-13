package com.networkwatcher.network_watcher.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

// JPA entity for network devices discovered during scans
@Entity
@Table(name = "devices")
@Data // Lombok: auto-generates getters, setters, toString, equals, hashCode
@NoArgsConstructor // Lombok: generates no-args constructor for JPA
@AllArgsConstructor // Lombok: generates constructor with all fields
public class Device {

    // Primary key with auto-increment
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Device IP address (unique, required, supports IPv4/IPv6)
    @Column(nullable = false, unique = true, length = 45)
    private String ipAddress;

    // Hardware MAC address (format: XX:XX:XX:XX:XX:XX)
    @Column(length = 17)
    private String macAddress;

    // MAC vendor/manufacturer (from OUI database)
    @Column(length = 100)
    private String macVendor;

    // Device hostname (from DNS/NetBIOS/mDNS)
    @Column(length = 255)
    private String hostname;

    // Where hostname came from: SNMP | NETBIOS | DNS | MDNS | MANUAL
    @Column(length = 20, name = "hostname_source")
    private String hostnameSource;

    // Operating system (from Nmap OS fingerprinting)
    @Column(length = 100)
    private String operatingSystem;

    // Current device status (default: ONLINE)
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private DeviceStatus status = DeviceStatus.ONLINE;

    // When device was first discovered (immutable)
    @Column(name = "first_seen", updatable = false)
    private LocalDateTime firstSeen;

    // When device was last seen responding
    @Column(name = "last_seen")
    private LocalDateTime lastSeen;

    // Whether device has known vulnerabilities
    @Column(name = "is_vulnerable")
    private Boolean isVulnerable = false;

    // JPA lifecycle callback - sets timestamps on first save
    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.firstSeen = now;
        this.lastSeen = now;
    }

    // Device connectivity states
    public enum DeviceStatus {
        ONLINE,   // Device responded to recent scan
        OFFLINE,  // Device not responding to scans
        UNKNOWN   // Device discovered but not fully scanned
    }
}
