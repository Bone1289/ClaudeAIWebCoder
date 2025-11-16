package com.example.demo.adapter.in.web.category;

import com.example.demo.adapter.in.web.category.dto.CategoryRequest;
import com.example.demo.adapter.in.web.category.dto.CategoryResponse;
import com.example.demo.adapter.in.web.dto.ApiResponse;
import com.example.demo.application.ports.in.ManageCategoryUseCase;
import com.example.demo.domain.TransactionCategory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * REST Controller for managing transaction categories
 */
@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final ManageCategoryUseCase manageCategoryUseCase;

    public CategoryController(ManageCategoryUseCase manageCategoryUseCase) {
        this.manageCategoryUseCase = manageCategoryUseCase;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CategoryResponse>> createCategory(@RequestBody CategoryRequest request) {
        try {
            TransactionCategory.CategoryType type = TransactionCategory.CategoryType.valueOf(request.getType());

            TransactionCategory category = manageCategoryUseCase.createCategory(
                request.getName(),
                request.getDescription(),
                type,
                request.getColor()
            );

            return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Category created successfully", CategoryResponse.fromDomain(category)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getAllCategories(
            @RequestParam(required = false) Boolean activeOnly,
            @RequestParam(required = false) String type) {

        List<TransactionCategory> categories;

        if (type != null) {
            TransactionCategory.CategoryType categoryType = TransactionCategory.CategoryType.valueOf(type);
            if (activeOnly != null && activeOnly) {
                categories = manageCategoryUseCase.getActiveCategoriesByType(categoryType);
            } else {
                categories = manageCategoryUseCase.getCategoriesByType(categoryType);
            }
        } else {
            if (activeOnly != null && activeOnly) {
                categories = manageCategoryUseCase.getActiveCategories();
            } else {
                categories = manageCategoryUseCase.getAllCategories();
            }
        }

        List<CategoryResponse> responses = categories.stream()
            .map(CategoryResponse::fromDomain)
            .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success("Categories retrieved successfully", responses));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CategoryResponse>> getCategoryById(@PathVariable Long id) {
        return manageCategoryUseCase.getCategoryById(id)
            .map(category -> ResponseEntity.ok(
                ApiResponse.success("Category found", CategoryResponse.fromDomain(category))))
            .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("Category not found")));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CategoryResponse>> updateCategory(
            @PathVariable Long id,
            @RequestBody CategoryRequest request) {
        try {
            TransactionCategory category = manageCategoryUseCase.updateCategory(
                id,
                request.getName(),
                request.getDescription(),
                request.getColor()
            );

            return ResponseEntity.ok(
                ApiResponse.success("Category updated successfully", CategoryResponse.fromDomain(category)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage()));
        }
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<ApiResponse<CategoryResponse>> deactivateCategory(@PathVariable Long id) {
        try {
            TransactionCategory category = manageCategoryUseCase.deactivateCategory(id);
            return ResponseEntity.ok(
                ApiResponse.success("Category deactivated successfully", CategoryResponse.fromDomain(category)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage()));
        }
    }

    @PatchMapping("/{id}/activate")
    public ResponseEntity<ApiResponse<CategoryResponse>> activateCategory(@PathVariable Long id) {
        try {
            TransactionCategory category = manageCategoryUseCase.activateCategory(id);
            return ResponseEntity.ok(
                ApiResponse.success("Category activated successfully", CategoryResponse.fromDomain(category)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCategory(@PathVariable Long id) {
        try {
            manageCategoryUseCase.deleteCategory(id);
            return ResponseEntity.ok(ApiResponse.success("Category deleted successfully", null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage()));
        }
    }
}
