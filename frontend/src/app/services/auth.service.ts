import { Injectable, Injector } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, tap } from 'rxjs';
import { LoginRequest, SignUpRequest, LoginResponse, UserResponse } from '../models/auth.model';
import { ApiResponse } from '../models/api-response.model';
import { NotificationService } from './notification.service';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly API_URL = '/api/auth';
  private readonly TOKEN_KEY = 'auth_token';
  private readonly USER_KEY = 'current_user';

  private currentUserSubject = new BehaviorSubject<UserResponse | null>(this.getCurrentUserFromStorage());
  public currentUser$ = this.currentUserSubject.asObservable();
  private notificationService?: NotificationService;

  constructor(
    private http: HttpClient,
    private injector: Injector
  ) {}

  /**
   * Get NotificationService (lazy injection to avoid circular dependency)
   */
  private getNotificationService(): NotificationService {
    if (!this.notificationService) {
      this.notificationService = this.injector.get(NotificationService);
    }
    return this.notificationService;
  }

  /**
   * Sign up a new user
   */
  signUp(request: SignUpRequest): Observable<ApiResponse<UserResponse>> {
    return this.http.post<ApiResponse<UserResponse>>(`${this.API_URL}/signup`, request);
  }

  /**
   * Login with email/username and password
   */
  login(request: LoginRequest): Observable<ApiResponse<LoginResponse>> {
    return this.http.post<ApiResponse<LoginResponse>>(`${this.API_URL}/login`, request)
      .pipe(
        tap(response => {
          if (response.success && response.data) {
            // Store token and user
            this.setToken(response.data.token);
            this.setCurrentUser(response.data.user);
            this.currentUserSubject.next(response.data.user);

            // Connect to SSE for real-time notifications
            setTimeout(() => {
              this.getNotificationService().reconnectSSE();
            }, 100);
          }
        })
      );
  }

  /**
   * Logout the current user
   */
  logout(): void {
    // Disconnect SSE before clearing auth data
    this.getNotificationService().disconnectSSE();

    localStorage.removeItem(this.TOKEN_KEY);
    localStorage.removeItem(this.USER_KEY);
    this.currentUserSubject.next(null);
  }

  /**
   * Get current authenticated user from API
   */
  getCurrentUser(): Observable<ApiResponse<UserResponse>> {
    return this.http.get<ApiResponse<UserResponse>>(`${this.API_URL}/me`)
      .pipe(
        tap(response => {
          if (response.success && response.data) {
            this.setCurrentUser(response.data);
            this.currentUserSubject.next(response.data);
          }
        })
      );
  }

  /**
   * Check if user is authenticated
   */
  isAuthenticated(): boolean {
    const token = this.getToken();
    return !!token && !this.isTokenExpired(token);
  }

  /**
   * Get JWT token from localStorage
   */
  getToken(): string | null {
    return localStorage.getItem(this.TOKEN_KEY);
  }

  /**
   * Store JWT token in localStorage
   */
  private setToken(token: string): void {
    localStorage.setItem(this.TOKEN_KEY, token);
  }

  /**
   * Get current user from BehaviorSubject
   */
  getCurrentUserValue(): UserResponse | null {
    return this.currentUserSubject.value;
  }

  /**
   * Get current user from localStorage
   */
  private getCurrentUserFromStorage(): UserResponse | null {
    const userJson = localStorage.getItem(this.USER_KEY);
    return userJson ? JSON.parse(userJson) : null;
  }

  /**
   * Store current user in localStorage
   */
  private setCurrentUser(user: UserResponse): void {
    localStorage.setItem(this.USER_KEY, JSON.stringify(user));
  }

  /**
   * Check if JWT token is expired
   * JWT format: header.payload.signature
   * Payload contains: { exp: timestamp, ... }
   */
  private isTokenExpired(token: string): boolean {
    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      const expiry = payload.exp;
      return expiry ? (Math.floor(Date.now() / 1000)) >= expiry : false;
    } catch (e) {
      return true;
    }
  }
}
