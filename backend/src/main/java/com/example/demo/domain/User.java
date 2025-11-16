package com.example.demo.domain;

/**
 * User domain entity
 * This is the core business object representing a user in the system
 */
public class User {
    private final Long id;
    private final String name;
    private final String email;
    private final String role;

    // Private constructor to enforce use of builder
    private User(Long id, String name, String email, String role) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.role = role;
    }

    // Factory method for creating a new user (without ID)
    public static User create(String name, String email, String role) {
        validateUserData(name, email, role);
        return new User(null, name, email, role);
    }

    // Factory method for creating a user with ID (from persistence)
    public static User of(Long id, String name, String email, String role) {
        validateUserData(name, email, role);
        if (id == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
        return new User(id, name, email, role);
    }

    // Method to update user data (creates a new instance with updated values)
    public User update(String name, String email, String role) {
        validateUserData(name, email, role);
        return new User(this.id, name, email, role);
    }

    private static void validateUserData(String name, String email, String role) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("User name cannot be null or empty");
        }
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("User email cannot be null or empty");
        }
        if (role == null || role.trim().isEmpty()) {
            throw new IllegalArgumentException("User role cannot be null or empty");
        }
    }

    // Getters
    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getRole() {
        return role;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", role='" + role + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return id != null && id.equals(user.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
