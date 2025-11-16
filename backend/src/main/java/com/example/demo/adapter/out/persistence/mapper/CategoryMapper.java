package com.example.demo.adapter.out.persistence.mapper;

import com.example.demo.adapter.out.persistence.entity.TransactionCategoryJpaEntity;
import com.example.demo.domain.TransactionCategory;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper for TransactionCategory domain ↔ JPA entity conversion
 */
@Mapper(componentModel = "spring")
public interface CategoryMapper {

    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "description", source = "description")
    @Mapping(target = "type", source = "type")
    @Mapping(target = "color", source = "color")
    @Mapping(target = "active", source = "active")
    @Mapping(target = "createdAt", source = "createdAt")
    TransactionCategoryJpaEntity toEntity(TransactionCategory domain);

    default TransactionCategory toDomain(TransactionCategoryJpaEntity entity) {
        if (entity == null) {
            return null;
        }

        return TransactionCategory.reconstitute(
            entity.getId(),
            entity.getName(),
            entity.getDescription(),
            mapCategoryType(entity.getType()),
            entity.getColor(),
            entity.isActive(),
            entity.getCreatedAt()
        );
    }

    default TransactionCategory.CategoryType mapCategoryType(TransactionCategoryJpaEntity.CategoryType entityType) {
        if (entityType == null) {
            return null;
        }
        return TransactionCategory.CategoryType.valueOf(entityType.name());
    }

    default TransactionCategoryJpaEntity.CategoryType mapCategoryType(TransactionCategory.CategoryType domainType) {
        if (domainType == null) {
            return null;
        }
        return TransactionCategoryJpaEntity.CategoryType.valueOf(domainType.name());
    }
}
