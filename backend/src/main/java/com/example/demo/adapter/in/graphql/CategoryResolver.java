package com.example.demo.adapter.in.graphql;

import com.example.demo.adapter.in.graphql.dto.CreateCategoryInputDTO;
import com.example.demo.adapter.in.graphql.dto.TransactionCategoryDTO;
import com.example.demo.adapter.in.graphql.dto.UpdateCategoryInputDTO;
import com.example.demo.application.ports.in.ManageCategoryUseCase;
import com.example.demo.domain.TransactionCategory;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * GraphQL Resolver for Category operations
 */
@Controller
public class CategoryResolver {

    private final ManageCategoryUseCase manageCategoryUseCase;

    public CategoryResolver(ManageCategoryUseCase manageCategoryUseCase) {
        this.manageCategoryUseCase = manageCategoryUseCase;
    }

    // ==================== Queries ====================

    @QueryMapping
    @PreAuthorize("isAuthenticated()")
    public List<TransactionCategoryDTO> categories(@Argument Boolean activeOnly,
                                                   @Argument TransactionCategory.CategoryType type) {
        boolean active = activeOnly != null && activeOnly;

        if (type != null) {
            if (active) {
                return manageCategoryUseCase.getActiveCategoriesByType(type).stream()
                        .map(TransactionCategoryDTO::fromDomain)
                        .collect(Collectors.toList());
            } else {
                return manageCategoryUseCase.getCategoriesByType(type).stream()
                        .map(TransactionCategoryDTO::fromDomain)
                        .collect(Collectors.toList());
            }
        } else {
            if (active) {
                return manageCategoryUseCase.getActiveCategories().stream()
                        .map(TransactionCategoryDTO::fromDomain)
                        .collect(Collectors.toList());
            } else {
                return manageCategoryUseCase.getAllCategories().stream()
                        .map(TransactionCategoryDTO::fromDomain)
                        .collect(Collectors.toList());
            }
        }
    }

    @QueryMapping
    @PreAuthorize("isAuthenticated()")
    public TransactionCategoryDTO category(@Argument UUID id) {
        return manageCategoryUseCase.getCategoryById(id)
                .map(TransactionCategoryDTO::fromDomain)
                .orElseThrow(() -> new RuntimeException("Category not found"));
    }

    // ==================== Mutations ====================

    @MutationMapping
    @PreAuthorize("isAuthenticated()")
    public TransactionCategoryDTO createCategory(@Argument CreateCategoryInputDTO input) {
        TransactionCategory category = manageCategoryUseCase.createCategory(
                input.name(),
                input.description(),
                input.type(),
                input.color()
        );
        return TransactionCategoryDTO.fromDomain(category);
    }

    @MutationMapping
    @PreAuthorize("isAuthenticated()")
    public TransactionCategoryDTO updateCategory(@Argument UUID id, @Argument UpdateCategoryInputDTO input) {
        // Note: ManageCategoryUseCase.updateCategory doesn't take a type parameter
        TransactionCategory category = manageCategoryUseCase.updateCategory(
                id,
                input.name(),
                input.description(),
                input.color()
        );
        return TransactionCategoryDTO.fromDomain(category);
    }

    @MutationMapping
    @PreAuthorize("isAuthenticated()")
    public TransactionCategoryDTO deactivateCategory(@Argument UUID id) {
        TransactionCategory category = manageCategoryUseCase.deactivateCategory(id);
        return TransactionCategoryDTO.fromDomain(category);
    }

    @MutationMapping
    @PreAuthorize("isAuthenticated()")
    public TransactionCategoryDTO activateCategory(@Argument UUID id) {
        TransactionCategory category = manageCategoryUseCase.activateCategory(id);
        return TransactionCategoryDTO.fromDomain(category);
    }

    @MutationMapping
    @PreAuthorize("isAuthenticated()")
    public Boolean deleteCategory(@Argument UUID id) {
        manageCategoryUseCase.deleteCategory(id);
        return true;
    }
}
