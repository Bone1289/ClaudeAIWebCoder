package com.example.demo.adapter.in.graphql.dto;

import com.example.demo.domain.TransactionCategory;

import java.time.LocalDateTime;
import java.util.UUID;

public record TransactionCategoryDTO(
        UUID id,
        String name,
        String description,
        TransactionCategory.CategoryType type,
        String color,
        boolean active,
        LocalDateTime createdAt
) {
    public static TransactionCategoryDTO fromDomain(TransactionCategory category) {
        return new TransactionCategoryDTO(
                category.getId(),
                category.getName(),
                category.getDescription(),
                category.getType(),
                category.getColor(),
                category.isActive(),
                category.getCreatedAt()
        );
    }
}
