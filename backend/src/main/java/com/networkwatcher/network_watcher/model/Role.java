package com.networkwatcher.network_watcher.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

// JPA entity for user roles and permissions
@Entity
@Table(name = "roles")
@Data // Lombok: auto-generates getters, setters, toString, equals, hashCode
@NoArgsConstructor // Lombok: generates no-args constructor for JPA
public class Role {
    // Primary key with auto-increment
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Role name (must be unique)
    @Column(unique = true, nullable = false, length = 20)
    private String name;

    // Human-readable description of role permissions
    @Column(length = 200)
    private String description;

    // Predefined role types for the application
    public enum RoleName {
        ROLE_ADMIN,    // Full system access
        ROLE_ANALYST,  // Can view and analyze data, create reports
        ROLE_OPERATOR, // Can run scans and manage devices
        ROLE_VIEWER    // Read-only access to dashboards
    }
}
