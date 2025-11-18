import { Injectable } from '@angular/core';
import { Observable, BehaviorSubject } from 'rxjs';
import { tap, map } from 'rxjs/operators';
import { GrpcClientService } from '../grpc/grpc-client.service';
import { ApiResponse } from '../models/api-response.model';
import { Category, CategoryType } from '../models/banking.model';

export interface CategoryRequest {
  name: string;
  description: string;
  type: CategoryType;
  color?: string;
}

@Injectable({
  providedIn: 'root'
})
export class CategoryService {
  private readonly SERVICE_NAME = 'com.example.demo.grpc.CategoryService';

  // Cache categories for better performance
  private categoriesCache$ = new BehaviorSubject<Category[]>([]);
  public categories$ = this.categoriesCache$.asObservable();

  constructor(private grpcClient: GrpcClientService) {
    this.loadCategories().subscribe();
  }

  /**
   * Load all active categories
   */
  loadCategories(activeOnly: boolean = true): Observable<ApiResponse<Category[]>> {
    const grpcRequest = {
      active_only: activeOnly,
      type: '' // Empty means all types
    };

    return this.grpcClient.call<any, any>(
      this.SERVICE_NAME,
      'GetAllCategories',
      grpcRequest
    ).pipe(
      map(response => ({
        success: response.success,
        message: response.message,
        data: response.categories ? response.categories.map((c: any) => this.mapGrpcCategoryToModel(c)) : []
      })),
      tap((response: ApiResponse<Category[]>) => {
        if (response.success && response.data) {
          this.categoriesCache$.next(response.data);
        }
      })
    );
  }

  /**
   * Get categories by type (INCOME, EXPENSE, OTHER)
   */
  getCategoriesByType(type: CategoryType, activeOnly: boolean = true): Observable<ApiResponse<Category[]>> {
    const grpcRequest = {
      active_only: activeOnly,
      type: type
    };

    return this.grpcClient.call<any, any>(
      this.SERVICE_NAME,
      'GetAllCategories',
      grpcRequest
    ).pipe(
      map(response => ({
        success: response.success,
        message: response.message,
        data: response.categories ? response.categories.map((c: any) => this.mapGrpcCategoryToModel(c)) : []
      }))
    );
  }

  /**
   * Get a specific category by ID
   */
  getCategoryById(id: string): Observable<ApiResponse<Category>> {
    return this.grpcClient.call<any, any>(
      this.SERVICE_NAME,
      'GetCategory',
      { id }
    ).pipe(
      map(response => ({
        success: response.success,
        message: response.message,
        data: response.category ? this.mapGrpcCategoryToModel(response.category) : {} as Category
      } as ApiResponse<Category>))
    );
  }

  /**
   * Create a new category
   */
  createCategory(request: CategoryRequest): Observable<ApiResponse<Category>> {
    const grpcRequest = {
      name: request.name,
      description: request.description,
      type: request.type,
      color: request.color || '#000000'
    };

    return this.grpcClient.call<any, any>(
      this.SERVICE_NAME,
      'CreateCategory',
      grpcRequest
    ).pipe(
      map(response => ({
        success: response.success,
        message: response.message,
        data: response.category ? this.mapGrpcCategoryToModel(response.category) : {} as Category
      } as ApiResponse<Category>)),
      tap(() => this.loadCategories().subscribe()) // Refresh cache
    );
  }

  /**
   * Update an existing category
   */
  updateCategory(id: string, request: Partial<CategoryRequest>): Observable<ApiResponse<Category>> {
    // Get existing category from cache to fill missing fields
    const existingCategory = this.getCategoryFromCache(id);

    const grpcRequest = {
      id: id,
      name: request.name || existingCategory?.name || '',
      description: request.description || existingCategory?.description || '',
      type: request.type || existingCategory?.type || CategoryType.OTHER,
      color: request.color || existingCategory?.color || '#000000'
    };

    return this.grpcClient.call<any, any>(
      this.SERVICE_NAME,
      'UpdateCategory',
      grpcRequest
    ).pipe(
      map(response => ({
        success: response.success,
        message: response.message,
        data: response.category ? this.mapGrpcCategoryToModel(response.category) : {} as Category
      } as ApiResponse<Category>)),
      tap(() => this.loadCategories().subscribe()) // Refresh cache
    );
  }

  /**
   * Deactivate a category (soft delete)
   */
  deactivateCategory(id: string): Observable<ApiResponse<Category>> {
    return this.grpcClient.call<any, any>(
      this.SERVICE_NAME,
      'DeactivateCategory',
      { id }
    ).pipe(
      map(response => ({
        success: response.success,
        message: response.message,
        data: response.category ? this.mapGrpcCategoryToModel(response.category) : {} as Category
      } as ApiResponse<Category>)),
      tap(() => this.loadCategories().subscribe()) // Refresh cache
    );
  }

  /**
   * Delete a category
   */
  deleteCategory(id: string): Observable<ApiResponse<void>> {
    return this.grpcClient.call<any, any>(
      this.SERVICE_NAME,
      'DeleteCategory',
      { id }
    ).pipe(
      map(response => ({
        success: response.success,
        message: response.message,
        data: undefined
      })),
      tap(() => this.loadCategories().subscribe()) // Refresh cache
    );
  }

  /**
   * Get category from cache by ID
   */
  getCategoryFromCache(id: string): Category | undefined {
    return this.categoriesCache$.value.find((cat: Category) => cat.id === id);
  }

  /**
   * Get all categories from cache
   */
  getCategoriesFromCache(): Category[] {
    return this.categoriesCache$.value;
  }

  /**
   * Get income categories from cache
   */
  getIncomeCategoriesFromCache(): Category[] {
    return this.categoriesCache$.value.filter((cat: Category) => cat.type === CategoryType.INCOME && cat.active);
  }

  /**
   * Get expense categories from cache
   */
  getExpenseCategoriesFromCache(): Category[] {
    return this.categoriesCache$.value.filter((cat: Category) => cat.type === CategoryType.EXPENSE && cat.active);
  }

  /**
   * Map gRPC category response to Angular model
   */
  private mapGrpcCategoryToModel(grpcCategory: any): Category {
    return {
      id: grpcCategory.id,
      name: grpcCategory.name,
      description: grpcCategory.description,
      type: grpcCategory.type as CategoryType,
      color: grpcCategory.color,
      active: grpcCategory.active,
      createdAt: grpcCategory.created_at || new Date().toISOString()
    };
  }
}
