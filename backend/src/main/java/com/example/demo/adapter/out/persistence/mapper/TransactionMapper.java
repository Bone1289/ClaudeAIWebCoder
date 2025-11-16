package com.example.demo.adapter.out.persistence.mapper;

import com.example.demo.adapter.out.persistence.entity.TransactionCategoryJpaEntity;
import com.example.demo.adapter.out.persistence.entity.TransactionJpaEntity;
import com.example.demo.domain.Transaction;
import org.mapstruct.*;

/**
 * MapStruct mapper for Transaction domain ↔ TransactionJpaEntity
 * This is part of the persistence adapter layer
 * MapStruct generates the implementation at compile time (zero runtime overhead)
 * Now handles category entity references instead of enums
 */
@Mapper(componentModel = "spring")
public interface TransactionMapper {

    /**
     * Map JPA Entity to Domain
     */
    @Mapping(target = "type", source = "type")
    @Mapping(target = "categoryId", source = "category.id")
    Transaction toDomain(TransactionJpaEntity entity);

    /**
     * Map Domain to JPA Entity
     * Note: category relationship must be set separately in the repository
     */
    @Mapping(target = "type", source = "type")
    @Mapping(target = "category", ignore = true)  // Set separately in repository
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
}
