package com.example.demo.adapter.in.graphql.dto;

import com.example.demo.domain.TransactionCategory;

public record UpdateCategoryInputDTO(
        String name,
        String description,
        TransactionCategory.CategoryType type,
        String color
) {
}
