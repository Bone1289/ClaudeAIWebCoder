import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, catchError, throwError } from 'rxjs';
import { map } from 'rxjs/operators';
import { User } from '../models/user.model';
import { ApiResponse } from '../models/api-response.model';

@Injectable({
  providedIn: 'root'
})
export class ApiService {
  private apiUrl = '/api';

  constructor(private http: HttpClient) { }

  /**
   * Get a personalized hello message from the backend
   * @param name - The name to include in the greeting
   * @returns Observable<string> - The greeting message
   */
  getHello(name: string): Observable<string> {
    const params = new HttpParams().set('name', name);

    return this.http.get(`${this.apiUrl}/hello`, {
      params,
      responseType: 'text'
    }).pipe(
      catchError(this.handleError)
    );
  }

  /**
   * Check the health status of the backend
   * @returns Observable<string> - The health status message
   */
  getHealth(): Observable<string> {
    return this.http.get(`${this.apiUrl}/health`, {
      responseType: 'text'
    }).pipe(
      catchError(this.handleError)
    );
  }

  // ========== User CRUD Operations ==========

  /**
   * Get all users
   * @returns Observable<ApiResponse<User[]>> - List of all users
   */
  getAllUsers(): Observable<ApiResponse<User[]>> {
    return this.http.get<ApiResponse<User[]>>(`${this.apiUrl}/users`).pipe(
      catchError(this.handleError)
    );
  }

  /**
   * Get user by ID
   * @param id - User ID
   * @returns Observable<ApiResponse<User>> - User object
   */
  getUserById(id: number): Observable<ApiResponse<User>> {
    return this.http.get<ApiResponse<User>>(`${this.apiUrl}/users/${id}`).pipe(
      catchError(this.handleError)
    );
  }

  /**
   * Create a new user
   * @param user - User object to create
   * @returns Observable<ApiResponse<User>> - Created user
   */
  createUser(user: User): Observable<ApiResponse<User>> {
    return this.http.post<ApiResponse<User>>(`${this.apiUrl}/users`, user).pipe(
      catchError(this.handleError)
    );
  }

  /**
   * Update an existing user
   * @param id - User ID
   * @param user - Updated user object
   * @returns Observable<ApiResponse<User>> - Updated user
   */
  updateUser(id: number, user: User): Observable<ApiResponse<User>> {
    return this.http.put<ApiResponse<User>>(`${this.apiUrl}/users/${id}`, user).pipe(
      catchError(this.handleError)
    );
  }

  /**
   * Delete a user
   * @param id - User ID
   * @returns Observable<ApiResponse<void>> - Deletion response
   */
  deleteUser(id: number): Observable<ApiResponse<void>> {
    return this.http.delete<ApiResponse<void>>(`${this.apiUrl}/users/${id}`).pipe(
      catchError(this.handleError)
    );
  }

  /**
   * Handle HTTP errors
   * @param error - The error object
   * @returns Observable<never> - An observable that errors
   */
  private handleError(error: any): Observable<never> {
    let errorMessage = 'An unknown error occurred';

    if (error.error instanceof ErrorEvent) {
      // Client-side error
      errorMessage = `Error: ${error.error.message}`;
    } else {
      // Server-side error
      errorMessage = `Error Code: ${error.status}\nMessage: ${error.message}`;
    }

    console.error(errorMessage);
    return throwError(() => new Error(errorMessage));
  }
}
