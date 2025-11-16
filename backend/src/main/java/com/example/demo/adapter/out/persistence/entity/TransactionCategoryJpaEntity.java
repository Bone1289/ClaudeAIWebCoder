package com.example.demo.adapter.out.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * JPA Entity for Transaction Category
 * Database-agnostic using standard JPA
 */
@Entity
@Table(name = "transaction_categories",
       uniqueConstraints = @UniqueConstraint(columnNames = "name"),
       indexes = {
           @Index(name = "idx_category_type", columnList = "type"),
           @Index(name = "idx_category_active", columnList = "active")
       })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionCategoryJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "BINARY(16)")
    private UUID id;

    @Column(name = "name", nullable = false, unique = true, length = 50)
    private String name;

    @Column(name = "description", length = 255)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 10)
    private CategoryType type;

    @Column(name = "color", length = 7)
    private String color;

    @Column(name = "active", nullable = false)
    private boolean active;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public enum CategoryType {
        INCOME,
        EXPENSE
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (active == false) {
            active = true;
        }
    }
}
