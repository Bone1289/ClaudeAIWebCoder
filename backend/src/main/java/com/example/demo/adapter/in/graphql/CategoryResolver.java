package com.example.demo.adapter.in.graphql;

import com.example.demo.adapter.in.graphql.dto.CreateCategoryInputDTO;
import com.example.demo.adapter.in.graphql.dto.TransactionCategoryDTO;
import com.example.demo.adapter.in.graphql.dto.UpdateCategoryInputDTO;
import com.example.demo.application.ports.in.*;
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

    private final CreateCategoryUseCase createCategoryUseCase;
    private final GetCategoryUseCase getCategoryUseCase;
    private final UpdateCategoryUseCase updateCategoryUseCase;
    private final DeleteCategoryUseCase deleteCategoryUseCase;

    public CategoryResolver(CreateCategoryUseCase createCategoryUseCase,
                           GetCategoryUseCase getCategoryUseCase,
                           UpdateCategoryUseCase updateCategoryUseCase,
                           DeleteCategoryUseCase deleteCategoryUseCase) {
        this.createCategoryUseCase = createCategoryUseCase;
        this.getCategoryUseCase = getCategoryUseCase;
        this.updateCategoryUseCase = updateCategoryUseCase;
        this.deleteCategoryUseCase = deleteCategoryUseCase;
    }

    // ==================== Queries ====================

    @QueryMapping
    @PreAuthorize("isAuthenticated()")
    public List<TransactionCategoryDTO> categories(@Argument Boolean activeOnly,
                                                   @Argument TransactionCategory.CategoryType type) {
        boolean active = activeOnly != null && activeOnly;

        if (type != null) {
            return getCategoryUseCase.getCategoriesByType(type, active).stream()
                    .map(TransactionCategoryDTO::fromDomain)
                    .collect(Collectors.toList());
        } else {
            return getCategoryUseCase.getAllCategories(active).stream()
                    .map(TransactionCategoryDTO::fromDomain)
                    .collect(Collectors.toList());
        }
    }

    @QueryMapping
    @PreAuthorize("isAuthenticated()")
    public TransactionCategoryDTO category(@Argument UUID id) {
        return getCategoryUseCase.getCategoryById(id)
                .map(TransactionCategoryDTO::fromDomain)
                .orElseThrow(() -> new RuntimeException("Category not found"));
    }

    // ==================== Mutations ====================

    @MutationMapping
    @PreAuthorize("isAuthenticated()")
    public TransactionCategoryDTO createCategory(@Argument CreateCategoryInputDTO input) {
        TransactionCategory category = createCategoryUseCase.createCategory(
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
        TransactionCategory category = updateCategoryUseCase.updateCategory(
                id,
                input.name(),
                input.description(),
                input.type(),
                input.color()
        ).orElseThrow(() -> new RuntimeException("Category not found"));
        return TransactionCategoryDTO.fromDomain(category);
    }

    @MutationMapping
    @PreAuthorize("isAuthenticated()")
    public TransactionCategoryDTO deactivateCategory(@Argument UUID id) {
        TransactionCategory category = updateCategoryUseCase.deactivateCategory(id)
                .orElseThrow(() -> new RuntimeException("Category not found"));
        return TransactionCategoryDTO.fromDomain(category);
    }

    @MutationMapping
    @PreAuthorize("isAuthenticated()")
    public TransactionCategoryDTO activateCategory(@Argument UUID id) {
        TransactionCategory category = updateCategoryUseCase.activateCategory(id)
                .orElseThrow(() -> new RuntimeException("Category not found"));
        return TransactionCategoryDTO.fromDomain(category);
    }

    @MutationMapping
    @PreAuthorize("isAuthenticated()")
    public Boolean deleteCategory(@Argument UUID id) {
        return deleteCategoryUseCase.deleteCategory(id);
    }
}
