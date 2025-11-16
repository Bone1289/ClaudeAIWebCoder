package com.example.demo.adapter.out.persistence.mapper;

import com.example.demo.adapter.out.persistence.entity.TransactionJpaEntity;
import com.example.demo.domain.Transaction;
import org.mapstruct.*;

/**
 * MapStruct mapper for Transaction domain ↔ TransactionJpaEntity
 * This is part of the persistence adapter layer
 * MapStruct generates the implementation at compile time (zero runtime overhead)
 */
@Mapper(componentModel = "spring")
public interface TransactionMapper {

    /**
     * Map JPA Entity to Domain
     */
    @Mapping(target = "type", source = "type")
    @Mapping(target = "category", source = "category")
    Transaction toDomain(TransactionJpaEntity entity);

    /**
     * Map Domain to JPA Entity
     */
    @Mapping(target = "type", source = "type")
    @Mapping(target = "category", source = "category")
    TransactionJpaEntity toEntity(Transaction domain);

    /**
     * Custom mapping for Transaction Type enum
     */
    default Transaction.TransactionType mapType(TransactionJpaEntity.TransactionType entityType) {
        if (entityType == null) return null;
        return Transaction.TransactionType.valueOf(entityType.name());
    }

    default TransactionJpaEntity.TransactionType mapType(Transaction.TransactionType domainType) {
        if (domainType == null) return null;
        return TransactionJpaEntity.TransactionType.valueOf(domainType.name());
    }

    /**
     * Custom mapping for Transaction Category enum
     */
    default Transaction.TransactionCategory mapCategory(TransactionJpaEntity.TransactionCategory entityCategory) {
        if (entityCategory == null) return null;
        return Transaction.TransactionCategory.valueOf(entityCategory.name());
    }

    default TransactionJpaEntity.TransactionCategory mapCategory(Transaction.TransactionCategory domainCategory) {
        if (domainCategory == null) return null;
        return TransactionJpaEntity.TransactionCategory.valueOf(domainCategory.name());
    }
}
