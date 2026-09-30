package com.portfolio.erp.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.Set;

/**
 * User aggregate. Rich model: state changes go through intention-revealing
 * methods instead of exposing raw mutation.
 */
public class User {

    private final Long id;
    private final String username;
    private final Instant createdAt;
    private final Instant updatedAt;

    private String email;
    private String firstName;
    private String lastName;
    private String passwordHash;
    private boolean enabled;
    private Set<Role> roles;

    public User(Long id,
                String username,
                String email,
                String firstName,
                String lastName,
                String passwordHash,
                boolean enabled,
                Set<Role> roles,
                Instant createdAt,
                Instant updatedAt) {
        this.id = id;
        this.username = Objects.requireNonNull(username, "username");
        this.email = Objects.requireNonNull(email, "email");
        this.firstName = Objects.requireNonNull(firstName, "firstName");
        this.lastName = Objects.requireNonNull(lastName, "lastName");
        this.passwordHash = Objects.requireNonNull(passwordHash, "passwordHash");
        this.enabled = enabled;
        this.roles = roles == null ? Set.of() : Set.copyOf(roles);
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static User newUser(String username, String email, String firstName, String lastName,
                               String passwordHash, Set<Role> roles) {
        return new User(null, username, email, firstName, lastName, passwordHash, true, roles, null, null);
    }

    public void updateProfile(String email, String firstName, String lastName) {
        this.email = Objects.requireNonNull(email, "email");
        this.firstName = Objects.requireNonNull(firstName, "firstName");
        this.lastName = Objects.requireNonNull(lastName, "lastName");
    }

    public void changePasswordHash(String passwordHash) {
        this.passwordHash = Objects.requireNonNull(passwordHash, "passwordHash");
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void assignRoles(Set<Role> roles) {
        this.roles = Set.copyOf(roles);
    }

    public boolean hasRole(String roleName) {
        return roles.stream().anyMatch(role -> role.name().equalsIgnoreCase(roleName));
    }

    public String fullName() {
        return firstName + " " + lastName;
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public Set<Role> getRoles() {
        return roles;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
