package com.example.demo.adapter.in.grpc.category;

import com.example.demo.application.service.CategoryService;
import com.example.demo.domain.TransactionCategory;
import com.example.demo.grpc.category.*;
import com.example.demo.grpc.common.IdRequest;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * gRPC service adapter for category operations
 * Follows hexagonal architecture pattern - this is an input adapter
 */
@GrpcService
public class CategoryGrpcService extends CategoryServiceGrpc.CategoryServiceImplBase {

    private static final Logger logger = LoggerFactory.getLogger(CategoryGrpcService.class);

    private final CategoryService categoryService;

    public CategoryGrpcService(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @Override
    public void createCategory(CreateCategoryRequest request, StreamObserver<CreateCategoryResponse> responseObserver) {
        try {
            logger.info("gRPC CreateCategory request: {}", request.getName());

            TransactionCategory category = categoryService.createCategory(
                    request.getName(),
                    request.getDescription(),
                    TransactionCategory.CategoryType.valueOf(request.getType()),
                    request.getColor()
            );

            CreateCategoryResponse response = CreateCategoryResponse.newBuilder()
                    .setSuccess(true)
                    .setMessage("Category created successfully")
                    .setCategory(mapToCategoryResponse(category))
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

        } catch (IllegalArgumentException e) {
            logger.error("CreateCategory validation error: {}", e.getMessage());
            responseObserver.onError(io.grpc.Status.INVALID_ARGUMENT
                    .withDescription(e.getMessage())
                    .asRuntimeException());
        } catch (Exception e) {
            logger.error("CreateCategory error", e);
            responseObserver.onError(io.grpc.Status.INTERNAL
                    .withDescription("Failed to create category: " + e.getMessage())
                    .asRuntimeException());
        }
    }

    @Override
    public void getCategory(IdRequest request, StreamObserver<GetCategoryResponse> responseObserver) {
        try {
            logger.info("gRPC GetCategory request for id: {}", request.getId());

            TransactionCategory category = categoryService.getCategoryById(UUID.fromString(request.getId()))
                    .orElseThrow(() -> new IllegalArgumentException("Category not found"));

            GetCategoryResponse response = GetCategoryResponse.newBuilder()
                    .setSuccess(true)
                    .setMessage("Category retrieved successfully")
                    .setCategory(mapToCategoryResponse(category))
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

        } catch (IllegalArgumentException e) {
            logger.error("GetCategory error: {}", e.getMessage());
            responseObserver.onError(io.grpc.Status.NOT_FOUND
                    .withDescription(e.getMessage())
                    .asRuntimeException());
        } catch (Exception e) {
            logger.error("GetCategory error", e);
            responseObserver.onError(io.grpc.Status.INTERNAL
                    .withDescription("Failed to get category: " + e.getMessage())
                    .asRuntimeException());
        }
    }

    @Override
    public void getAllCategories(GetAllCategoriesRequest request,
                                 StreamObserver<GetAllCategoriesResponse> responseObserver) {
        try {
            logger.info("gRPC GetAllCategories request");

            List<TransactionCategory> categories;

            if (request.getActiveOnly()) {
                categories = categoryService.getActiveCategories();
            } else if (!request.getType().isEmpty()) {
                TransactionCategory.CategoryType type = TransactionCategory.CategoryType.valueOf(request.getType());
                categories = categoryService.getCategoriesByType(type);
            } else {
                categories = categoryService.getAllCategories();
            }

            GetAllCategoriesResponse response = GetAllCategoriesResponse.newBuilder()
                    .setSuccess(true)
                    .setMessage("Categories retrieved successfully")
                    .addAllCategories(categories.stream()
                            .map(this::mapToCategoryResponse)
                            .collect(Collectors.toList()))
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

        } catch (Exception e) {
            logger.error("GetAllCategories error", e);
            responseObserver.onError(io.grpc.Status.INTERNAL
                    .withDescription("Failed to get categories: " + e.getMessage())
                    .asRuntimeException());
        }
    }

    @Override
    public void updateCategory(UpdateCategoryRequest request, StreamObserver<UpdateCategoryResponse> responseObserver) {
        try {
            logger.info("gRPC UpdateCategory request for id: {}", request.getId());

            TransactionCategory category = categoryService.updateCategory(
                    UUID.fromString(request.getId()),
                    request.getName(),
                    request.getDescription(),
                    TransactionCategory.CategoryType.valueOf(request.getType()),
                    request.getColor()
            );

            UpdateCategoryResponse response = UpdateCategoryResponse.newBuilder()
                    .setSuccess(true)
                    .setMessage("Category updated successfully")
                    .setCategory(mapToCategoryResponse(category))
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

        } catch (IllegalArgumentException e) {
            logger.error("UpdateCategory validation error: {}", e.getMessage());
            responseObserver.onError(io.grpc.Status.INVALID_ARGUMENT
                    .withDescription(e.getMessage())
                    .asRuntimeException());
        } catch (Exception e) {
            logger.error("UpdateCategory error", e);
            responseObserver.onError(io.grpc.Status.INTERNAL
                    .withDescription("Failed to update category: " + e.getMessage())
                    .asRuntimeException());
        }
    }

    @Override
    public void deleteCategory(IdRequest request, StreamObserver<DeleteCategoryResponse> responseObserver) {
        try {
            logger.info("gRPC DeleteCategory request for id: {}", request.getId());

            categoryService.deleteCategory(UUID.fromString(request.getId()));

            DeleteCategoryResponse response = DeleteCategoryResponse.newBuilder()
                    .setSuccess(true)
                    .setMessage("Category deleted successfully")
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

        } catch (IllegalStateException e) {
            logger.error("DeleteCategory business rule error: {}", e.getMessage());
            responseObserver.onError(io.grpc.Status.FAILED_PRECONDITION
                    .withDescription(e.getMessage())
                    .asRuntimeException());
        } catch (Exception e) {
            logger.error("DeleteCategory error", e);
            responseObserver.onError(io.grpc.Status.INTERNAL
                    .withDescription("Failed to delete category: " + e.getMessage())
                    .asRuntimeException());
        }
    }

    @Override
    public void activateCategory(IdRequest request, StreamObserver<ActivateCategoryResponse> responseObserver) {
        try {
            logger.info("gRPC ActivateCategory request for id: {}", request.getId());

            TransactionCategory category = categoryService.activateCategory(UUID.fromString(request.getId()));

            ActivateCategoryResponse response = ActivateCategoryResponse.newBuilder()
                    .setSuccess(true)
                    .setMessage("Category activated successfully")
                    .setCategory(mapToCategoryResponse(category))
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

        } catch (Exception e) {
            logger.error("ActivateCategory error", e);
            responseObserver.onError(io.grpc.Status.INTERNAL
                    .withDescription("Failed to activate category: " + e.getMessage())
                    .asRuntimeException());
        }
    }

    @Override
    public void deactivateCategory(IdRequest request, StreamObserver<DeactivateCategoryResponse> responseObserver) {
        try {
            logger.info("gRPC DeactivateCategory request for id: {}", request.getId());

            TransactionCategory category = categoryService.deactivateCategory(UUID.fromString(request.getId()));

            DeactivateCategoryResponse response = DeactivateCategoryResponse.newBuilder()
                    .setSuccess(true)
                    .setMessage("Category deactivated successfully")
                    .setCategory(mapToCategoryResponse(category))
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

        } catch (Exception e) {
            logger.error("DeactivateCategory error", e);
            responseObserver.onError(io.grpc.Status.INTERNAL
                    .withDescription("Failed to deactivate category: " + e.getMessage())
                    .asRuntimeException());
        }
    }

    /**
     * Maps domain TransactionCategory to gRPC CategoryResponse
     */
    private CategoryResponse mapToCategoryResponse(TransactionCategory category) {
        return CategoryResponse.newBuilder()
                .setId(category.getId().toString())
                .setName(category.getName())
                .setDescription(category.getDescription())
                .setType(category.getType().name())
                .setColor(category.getColor())
                .setActive(category.isActive())
                .build();
    }
}
