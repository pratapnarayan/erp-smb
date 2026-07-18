package com.erp.smb.user.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "user_profiles", schema = "users")
public class UserProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "username is required")
    @Size(min = 3, max = 100, message = "username must be between 3 and 100 characters")
    @Pattern(regexp = "^[a-zA-Z0-9._@+\\-]+$", message = "username may only contain letters, digits, and . _ @ + -")
    @Column(nullable = false, unique = true)
    private String username;

    @Size(max = 255, message = "fullName must not exceed 255 characters")
    private String fullName;

    @NotBlank(message = "role is required")
    @Column(nullable = false)
    private String role;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}
