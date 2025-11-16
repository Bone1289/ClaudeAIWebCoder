package com.example.demo.adapter.out.persistence.mapper;

import com.example.demo.adapter.out.persistence.entity.AccountJpaEntity;
import com.example.demo.domain.Account;
import org.mapstruct.*;

/**
 * MapStruct mapper for Account domain ↔ AccountJpaEntity
 * This is part of the persistence adapter layer
 * MapStruct generates the implementation at compile time (zero runtime overhead)
 */
@Mapper(componentModel = "spring")
public interface AccountMapper {

    /**
     * Map JPA Entity to Domain
     * Uses Account.of() factory method due to private constructor
     */
    default Account toDomain(AccountJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        return Account.of(
            entity.getId(),
            entity.getAccountNumber(),
            entity.getCustomerId(),
            entity.getAccountType(),
            entity.getBalance(),
            mapStatus(entity.getStatus()),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    /**
     * Map Domain to JPA Entity
     */
    @Mapping(target = "status", source = "status")
    AccountJpaEntity toEntity(Account domain);

    /**
     * Custom mapping for Account Status enum
     */
    default Account.AccountStatus mapStatus(AccountJpaEntity.AccountStatus entityStatus) {
        if (entityStatus == null) return null;
        return Account.AccountStatus.valueOf(entityStatus.name());
    }

    default AccountJpaEntity.AccountStatus mapStatus(Account.AccountStatus domainStatus) {
        if (domainStatus == null) return null;
        return AccountJpaEntity.AccountStatus.valueOf(domainStatus.name());
    }
}
