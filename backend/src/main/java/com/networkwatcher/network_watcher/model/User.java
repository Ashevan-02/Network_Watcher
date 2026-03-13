package com.networkwatcher.network_watcher.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

// JPA entity for user authentication and authorization
@Entity
@Table(name = "users")
@Data // Lombok: auto-generates getters, setters, toString, equals, hashCode
@NoArgsConstructor // Lombok: generates no-args constructor for JPA
public class User {
    // Primary key with auto-increment
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Unique username for login (cannot be duplicated)
    @Column(unique = true, nullable = false, length = 50)
    private String username;

    // Encrypted password (never store plain text)
    @Column(nullable = false)
    private String password;

    // User's email address
    @Column(length = 100)
    private String email;

    // Whether user account is active
    @Column(nullable = false)
    private Boolean enabled = true;

    // Many-to-many relationship with Role table
    // One user can have multiple roles, one role can belong to multiple users
    @ManyToMany(fetch = FetchType.EAGER) // Load roles immediately with user
    @JoinTable(
        name = "user_roles",                    // Junction table name
        joinColumns = @JoinColumn(name = "user_id"),        // Foreign key to users table
        inverseJoinColumns = @JoinColumn(name = "role_id")  // Foreign key to roles table
    )
    private Set<Role> roles = new HashSet<>();

    // When user account was created (immutable)
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // When user last successfully logged in
    @Column(name = "last_login")
    private LocalDateTime lastLogin;

    // JPA lifecycle callback - sets creation timestamp
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
