package com.example.demo.adapter.out.persistence.mapper;

import com.example.demo.adapter.out.persistence.entity.UserJpaEntity;
import com.example.demo.domain.User;
import org.mapstruct.*;

/**
 * MapStruct mapper for User domain ↔ UserJpaEntity
 * This is part of the persistence adapter layer
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
            entity.getName(),
            entity.getEmail(),
            entity.getRole()
        );
    }

    /**
     * Map Domain to JPA Entity
     */
    UserJpaEntity toEntity(User domain);
}
