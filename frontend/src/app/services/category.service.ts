import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, BehaviorSubject } from 'rxjs';
import { tap } from 'rxjs/operators';
import { ApiResponse } from '../models/api-response.model';
import { Category, CategoryType } from '../models/banking.model';
import { environment } from '../../environments/environment';

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
  private apiUrl = `${environment.apiUrl}/categories`;

  // Cache categories for better performance
  private categoriesCache$ = new BehaviorSubject<Category[]>([]);
  public categories$ = this.categoriesCache$.asObservable();

  constructor(private http: HttpClient) {
    this.loadCategories();
  }

  /**
   * Load all active categories
   */
  loadCategories(activeOnly: boolean = true): Observable<ApiResponse<Category[]>> {
    let params = new HttpParams();
    if (activeOnly) {
      params = params.set('activeOnly', 'true');
    }

    return this.http.get<ApiResponse<Category[]>>(this.apiUrl, { params }).pipe(
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
    let params = new HttpParams();
    params = params.set('type', type);
    if (activeOnly) {
      params = params.set('activeOnly', 'true');
    }

    return this.http.get<ApiResponse<Category[]>>(this.apiUrl, { params });
  }

  /**
   * Get a specific category by ID
   */
  getCategoryById(id: number): Observable<ApiResponse<Category>> {
    return this.http.get<ApiResponse<Category>>(`${this.apiUrl}/${id}`);
  }

  /**
   * Create a new category
   */
  createCategory(request: CategoryRequest): Observable<ApiResponse<Category>> {
    return this.http.post<ApiResponse<Category>>(this.apiUrl, request).pipe(
      tap(() => this.loadCategories()) // Refresh cache
    );
  }

  /**
   * Update an existing category
   */
  updateCategory(id: number, request: Partial<CategoryRequest>): Observable<ApiResponse<Category>> {
    return this.http.put<ApiResponse<Category>>(`${this.apiUrl}/${id}`, request).pipe(
      tap(() => this.loadCategories()) // Refresh cache
    );
  }

  /**
   * Deactivate a category (soft delete)
   */
  deactivateCategory(id: number): Observable<ApiResponse<Category>> {
    return this.http.patch<ApiResponse<Category>>(`${this.apiUrl}/${id}/deactivate`, {}).pipe(
      tap(() => this.loadCategories()) // Refresh cache
    );
  }

  /**
   * Delete a category
   */
  deleteCategory(id: number): Observable<ApiResponse<void>> {
    return this.http.delete<ApiResponse<void>>(`${this.apiUrl}/${id}`).pipe(
      tap(() => this.loadCategories()) // Refresh cache
    );
  }

  /**
   * Get category from cache by ID
   */
  getCategoryFromCache(id: number): Category | undefined {
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
}
