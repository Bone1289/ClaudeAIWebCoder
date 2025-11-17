import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { User, ApiResponse } from '../models/user.model';
import { Account } from '../models/account.model';

@Injectable({
  providedIn: 'root'
})
export class AdminService {
  private readonly API_URL = 'http://localhost:8080/api/admin';

  constructor(private http: HttpClient) {}

  // User management
  getAllUsers(): Observable<ApiResponse<User[]>> {
    return this.http.get<ApiResponse<User[]>>(`${this.API_URL}/users`);
  }

  getUserById(id: string): Observable<ApiResponse<User>> {
    return this.http.get<ApiResponse<User>>(`${this.API_URL}/users/${id}`);
  }

  suspendUser(id: string): Observable<ApiResponse<User>> {
    return this.http.put<ApiResponse<User>>(`${this.API_URL}/users/${id}/suspend`, {});
  }

  activateUser(id: string): Observable<ApiResponse<User>> {
    return this.http.put<ApiResponse<User>>(`${this.API_URL}/users/${id}/activate`, {});
  }

  lockUser(id: string): Observable<ApiResponse<User>> {
    return this.http.put<ApiResponse<User>>(`${this.API_URL}/users/${id}/lock`, {});
  }

  deleteUser(id: string): Observable<ApiResponse<void>> {
    return this.http.delete<ApiResponse<void>>(`${this.API_URL}/users/${id}`);
  }

  // Account management
  getAllAccounts(): Observable<ApiResponse<Account[]>> {
    return this.http.get<ApiResponse<Account[]>>(`${this.API_URL}/accounts`);
  }

  getAccountById(id: string): Observable<ApiResponse<Account>> {
    return this.http.get<ApiResponse<Account>>(`${this.API_URL}/accounts/${id}`);
  }

  getAccountsByUserId(userId: string): Observable<ApiResponse<Account[]>> {
    return this.http.get<ApiResponse<Account[]>>(`${this.API_URL}/users/${userId}/accounts`);
  }
}
