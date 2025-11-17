package com.example.demo.domain;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * User domain entity for authentication and authorization
 * Represents a user who can own multiple bank accounts
 */
public class User {
    private final UUID id;
    private final String email;
    private final String username;
    private final String password; // BCrypt hashed password
    private final String firstName;
    private final String lastName;
    private final UserRole role;
    private final UserStatus status;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    public enum UserRole {
        USER, ADMIN
    }

    public enum UserStatus {
        ACTIVE, SUSPENDED, LOCKED
    }

    private User(UUID id, String email, String username, String password,
                 String firstName, String lastName, UserRole role, UserStatus status,
                 LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.email = email;
        this.username = username;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.role = role;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * Create a new user (for registration)
     */
    public static User create(String email, String username, String hashedPassword,
                             String firstName, String lastName) {
        validateEmail(email);
        validateUsername(username);
        validatePassword(hashedPassword);
        validateName(firstName, "First name");
        validateName(lastName, "Last name");

        return new User(
            null,
            email.toLowerCase().trim(),
            username.trim(),
            hashedPassword,
            firstName.trim(),
            lastName.trim(),
            UserRole.USER,
            UserStatus.ACTIVE,
            LocalDateTime.now(),
            LocalDateTime.now()
        );
    }

    /**
     * Reconstitute user from persistence
     */
    public static User of(UUID id, String email, String username, String password,
                         String firstName, String lastName, UserRole role, UserStatus status,
                         LocalDateTime createdAt, LocalDateTime updatedAt) {
        if (id == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
        validateEmail(email);
        validateUsername(username);
        validatePassword(password);
        validateName(firstName, "First name");
        validateName(lastName, "Last name");

        return new User(id, email.toLowerCase().trim(), username.trim(), password,
                       firstName.trim(), lastName.trim(), role, status, createdAt, updatedAt);
    }

    /**
     * Update user profile
     */
    public User updateProfile(String firstName, String lastName) {
        validateName(firstName, "First name");
        validateName(lastName, "Last name");

        return new User(id, email, username, password, firstName.trim(), lastName.trim(),
                       role, status, createdAt, LocalDateTime.now());
    }

    /**
     * Update password
     */
    public User updatePassword(String newHashedPassword) {
        validatePassword(newHashedPassword);

        return new User(id, email, username, newHashedPassword, firstName, lastName,
                       role, status, createdAt, LocalDateTime.now());
    }

    /**
     * Suspend user
     */
    public User suspend() {
        if (status == UserStatus.LOCKED) {
            throw new IllegalStateException("Cannot suspend a locked user");
        }
        return new User(id, email, username, password, firstName, lastName,
                       role, UserStatus.SUSPENDED, createdAt, LocalDateTime.now());
    }

    /**
     * Lock user (due to security reasons)
     */
    public User lock() {
        return new User(id, email, username, password, firstName, lastName,
                       role, UserStatus.LOCKED, createdAt, LocalDateTime.now());
    }

    /**
     * Activate user
     */
    public User activate() {
        return new User(id, email, username, password, firstName, lastName,
                       role, UserStatus.ACTIVE, createdAt, LocalDateTime.now());
    }

    /**
     * Promote to admin
     */
    public User promoteToAdmin() {
        if (status != UserStatus.ACTIVE) {
            throw new IllegalStateException("Only active users can be promoted to admin");
        }
        return new User(id, email, username, password, firstName, lastName,
                       UserRole.ADMIN, status, createdAt, LocalDateTime.now());
    }

    /**
     * Check if user can login
     */
    public boolean canLogin() {
        return status == UserStatus.ACTIVE;
    }

    // Validation methods
    private static void validateEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email cannot be null or empty");
        }
        if (!EMAIL_PATTERN.matcher(email.trim()).matches()) {
            throw new IllegalArgumentException("Invalid email format");
        }
    }

    private static void validateUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be null or empty");
        }
        if (username.trim().length() < 3) {
            throw new IllegalArgumentException("Username must be at least 3 characters");
        }
        if (username.trim().length() > 50) {
            throw new IllegalArgumentException("Username must be at most 50 characters");
        }
    }

    private static void validatePassword(String password) {
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty");
        }
    }

    private static void validateName(String name, String fieldName) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " cannot be null or empty");
        }
        if (name.trim().length() > 100) {
            throw new IllegalArgumentException(fieldName + " must be at most 100 characters");
        }
    }

    // Getters
    public UUID getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public UserRole getRole() {
        return role;
    }

    public UserStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
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

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", email='" + email + '\'' +
                ", username='" + username + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", role=" + role +
                ", status=" + status +
                '}';
    }
}
