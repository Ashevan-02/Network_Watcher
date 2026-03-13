package com.networkwatcher.network_watcher.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

// JPA entity for tracking network traffic per device
@Entity
@Table(name = "bandwidth_usage")
@Data // Lombok: auto-generates getters, setters, toString, equals, hashCode
@NoArgsConstructor // Lombok: generates no-args constructor for JPA
@AllArgsConstructor // Lombok: generates constructor with all fields
public class BandwidthUsage {

    // Primary key with auto-increment
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Foreign key to Device table (many bandwidth records per device)
    @ManyToOne
    @JoinColumn(name = "device_id", nullable = false)
    private Device device;

    // Outbound traffic in bytes
    @Column(name = "bytes_sent")
    private Long bytesSent = 0L;

    // Inbound traffic in bytes
    @Column(name = "bytes_received")
    private Long bytesReceived = 0L;

    // Total traffic (calculated field)
    @Column(name = "total_bytes")
    private Long totalBytes = 0L;

    // Outbound packet count
    @Column(name = "packets_sent")
    private Long packetsSent = 0L;

    // Inbound packet count
    @Column(name = "packets_received")
    private Long packetsReceived = 0L;

    // When this measurement was taken
    @Column(name = "recorded_at")
    private LocalDateTime recordedAt;

    // JPA lifecycle callback - runs before first save
    @PrePersist
    protected void onCreate() {
        this.recordedAt = LocalDateTime.now();
        this.totalBytes = this.bytesSent + this.bytesReceived;
    }

    // JPA lifecycle callback - runs before updates
    @PreUpdate
    protected void onUpdate() {
        this.totalBytes = this.bytesSent + this.bytesReceived;
    }
}
