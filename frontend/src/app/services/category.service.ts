import { Injectable } from '@angular/core';
import { Observable, BehaviorSubject, map } from 'rxjs';
import { tap } from 'rxjs/operators';
import { Apollo } from 'apollo-angular';
import { Category, CategoryType } from '../models/banking.model';
import {
  GET_CATEGORIES,
  GET_CATEGORY,
  CREATE_CATEGORY,
  UPDATE_CATEGORY,
  DEACTIVATE_CATEGORY,
  DELETE_CATEGORY
} from '../graphql/graphql.operations';

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
  // Cache categories for better performance
  private categoriesCache$ = new BehaviorSubject<Category[]>([]);
  public categories$ = this.categoriesCache$.asObservable();

  constructor(private apollo: Apollo) {
    this.loadCategories();
  }

  /**
   * Safely extract data from GraphQL result with null checking
   */
  private extractData<T>(result: any, key: string): T {
    if (!result || !result.data) {
      throw new Error('GraphQL response is null or undefined');
    }
    const data = result.data[key];
    if (data === undefined || data === null) {
      throw new Error(`GraphQL response missing expected field: ${key}`);
    }
    return data;
  }

  /**
   * Load all active categories
   */
  loadCategories(activeOnly: boolean = true): Observable<Category[]> {
    return this.apollo.query({
      query: GET_CATEGORIES,
      variables: {
        activeOnly,
        type: null
      },
      fetchPolicy: 'network-only'
    }).pipe(
      map(result => this.extractData<Category[]>(result, 'categories')),
      tap((categories: Category[]) => {
        this.categoriesCache$.next(categories);
      })
    );
  }

  /**
   * Get categories by type (INCOME or EXPENSE)
   */
  getCategoriesByType(type: CategoryType, activeOnly: boolean = true): Observable<Category[]> {
    return this.apollo.query({
      query: GET_CATEGORIES,
      variables: {
        activeOnly,
        type
      },
      fetchPolicy: 'network-only'
    }).pipe(
      map(result => this.extractData<Category[]>(result, 'categories'))
    );
  }

  /**
   * Get a specific category by ID
   */
  getCategoryById(id: string): Observable<Category> {
    return this.apollo.query({
      query: GET_CATEGORY,
      variables: { id },
      fetchPolicy: 'network-only'
    }).pipe(
      map(result => this.extractData<Category>(result, 'category'))
    );
  }

  /**
   * Create a new category
   */
  createCategory(request: CategoryRequest): Observable<Category> {
    return this.apollo.mutate({
      mutation: CREATE_CATEGORY,
      variables: {
        input: {
          name: request.name,
          description: request.description,
          type: request.type,
          color: request.color
        }
      }
    }).pipe(
      map(result => this.extractData<Category>(result, 'createCategory')),
      tap(() => this.loadCategories().subscribe()) // Refresh cache
    );
  }

  /**
   * Update an existing category
   */
  updateCategory(id: string, request: Partial<CategoryRequest>): Observable<Category> {
    return this.apollo.mutate({
      mutation: UPDATE_CATEGORY,
      variables: {
        id,
        input: {
          name: request.name,
          description: request.description,
          type: request.type,
          color: request.color
        }
      }
    }).pipe(
      map(result => this.extractData<Category>(result, 'updateCategory')),
      tap(() => this.loadCategories().subscribe()) // Refresh cache
    );
  }

  /**
   * Deactivate a category (soft delete)
   */
  deactivateCategory(id: string): Observable<Category> {
    return this.apollo.mutate({
      mutation: DEACTIVATE_CATEGORY,
      variables: { id }
    }).pipe(
      map(result => this.extractData<Category>(result, 'deactivateCategory')),
      tap(() => this.loadCategories().subscribe()) // Refresh cache
    );
  }

  /**
   * Delete a category
   */
  deleteCategory(id: string): Observable<boolean> {
    return this.apollo.mutate({
      mutation: DELETE_CATEGORY,
      variables: { id }
    }).pipe(
      map(result => this.extractData<boolean>(result, 'deleteCategory')),
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
}
