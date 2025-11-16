package com.example.demo.adapter.out.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * JPA Entity for Transaction persistence
 * Uses standard JPA annotations - database agnostic
 * Now uses foreign key reference to TransactionCategoryJpaEntity
 */
@Entity
@Table(name = "transactions", indexes = {
    @Index(name = "idx_account_id", columnList = "account_id"),
    @Index(name = "idx_created_at", columnList = "created_at"),
    @Index(name = "idx_category_id", columnList = "category_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "BINARY(16)")
    private UUID id;

    @Column(name = "account_id", nullable = false, columnDefinition = "BINARY(16)")
    private UUID accountId;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private TransactionType type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private TransactionCategoryJpaEntity category;

    @Column(name = "amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(name = "balance_after", nullable = false, precision = 15, scale = 2)
    private BigDecimal balanceAfter;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "related_account_id", columnDefinition = "BINARY(16)")
    private UUID relatedAccountId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public enum TransactionType {
        DEPOSIT, WITHDRAWAL, TRANSFER_IN, TRANSFER_OUT
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
