package com.example.demo.adapter.out.persistence.mapper;

import com.example.demo.adapter.out.persistence.entity.UserJpaEntity;
import com.example.demo.domain.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper for User domain ↔ UserJpaEntity
 * This is part of the persistence adapter layer
 * MapStruct generates the implementation at compile time (zero runtime overhead)
 */
@Mapper(componentModel = "spring")
public interface UserMapper {

    /**
     * Map JPA Entity to Domain
     * Uses User.of() factory method due to private constructor
     */
    default User toDomain(UserJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        return User.of(
            entity.getId(),
            entity.getEmail(),
            entity.getUsername(),
            entity.getPassword(),
            entity.getFirstName(),
            entity.getLastName(),
            mapRole(entity.getRole()),
            mapStatus(entity.getStatus()),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    /**
     * Map Domain to JPA Entity
     */
    @Mapping(target = "role", source = "role")
    @Mapping(target = "status", source = "status")
    UserJpaEntity toEntity(User domain);

    /**
     * Custom mapping for User Role enum
     */
    default User.UserRole mapRole(UserJpaEntity.UserRole entityRole) {
        if (entityRole == null) return null;
        return User.UserRole.valueOf(entityRole.name());
    }

    default UserJpaEntity.UserRole mapRole(User.UserRole domainRole) {
        if (domainRole == null) return null;
        return UserJpaEntity.UserRole.valueOf(domainRole.name());
    }

    /**
     * Custom mapping for User Status enum
     */
    default User.UserStatus mapStatus(UserJpaEntity.UserStatus entityStatus) {
        if (entityStatus == null) return null;
        return User.UserStatus.valueOf(entityStatus.name());
    }

    default UserJpaEntity.UserStatus mapStatus(User.UserStatus domainStatus) {
        if (domainStatus == null) return null;
        return UserJpaEntity.UserStatus.valueOf(domainStatus.name());
    }
}
