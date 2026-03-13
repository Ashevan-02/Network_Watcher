package com.networkwatcher.network_watcher.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

// Non-persistent model for real-time packet capture data
// This is NOT a JPA entity - used for temporary packet analysis
@Data // Lombok: auto-generates getters, setters, toString, equals, hashCode
@NoArgsConstructor // Lombok: generates no-args constructor
@AllArgsConstructor // Lombok: generates constructor with all fields
public class CapturedPacket {
    // Source IP address of the packet
    private String sourceIp;
    
    // Destination IP address of the packet
    private String destinationIp;
    
    // Network protocol (TCP, UDP, ICMP, etc.)
    private String protocol;
    
    // Packet size in bytes
    private int length;
    
    // Additional packet information (port numbers, flags, etc.)
    private String info;
    
    // When the packet was captured
    private LocalDateTime timestamp;
}
