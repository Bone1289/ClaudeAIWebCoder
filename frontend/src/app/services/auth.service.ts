import { Injectable, Injector } from '@angular/core';
import { BehaviorSubject, Observable, map } from 'rxjs';
import { Apollo } from 'apollo-angular';
import { LoginRequest, SignUpRequest, UserResponse } from '../models/auth.model';
import { NotificationService } from './notification.service';
import { SIGN_UP, LOGIN, GET_CURRENT_USER, LOGOUT } from '../graphql/graphql.operations';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly TOKEN_KEY = 'auth_token';
  private readonly USER_KEY = 'current_user';

  private currentUserSubject = new BehaviorSubject<UserResponse | null>(this.getCurrentUserFromStorage());
  public currentUser$ = this.currentUserSubject.asObservable();
  private notificationService?: NotificationService;

  constructor(
    private apollo: Apollo,
    private injector: Injector
  ) {}

  /**
   * Safely extract data from GraphQL result with null checking
   */
  private extractData<T>(result: any, key: string): T {
    // If result or result.data is null/undefined, return the data anyway
    // Apollo's error handling will catch any actual errors
    if (!result || !result.data) {
      return null as any;
    }
    return result.data[key];
  }

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
  signUp(request: SignUpRequest): Observable<any> {
    return this.apollo.mutate({
      mutation: SIGN_UP,
      variables: {
        input: {
          email: request.email,
          username: request.username,
          password: request.password,
          firstName: request.firstName,
          lastName: request.lastName
        }
      }
    }).pipe(
      map(result => {
        const authData = this.extractData<any>(result, 'signUp');
        // Store token and user
        this.setToken(authData.token);
        this.setCurrentUser(authData.user);
        this.currentUserSubject.next(authData.user);
        return authData;
      })
    );
  }

  /**
   * Login with email/username and password
   */
  login(request: LoginRequest): Observable<any> {
    return this.apollo.mutate({
      mutation: LOGIN,
      variables: {
        input: {
          username: request.emailOrUsername,
          password: request.password
        }
      }
    }).pipe(
      map(result => {
        const authData = this.extractData<any>(result, 'login');
        // Store token and user
        this.setToken(authData.token);
        this.setCurrentUser(authData.user);
        this.currentUserSubject.next(authData.user);

        // Connect to SSE for real-time notifications
        setTimeout(() => {
          this.getNotificationService().reconnectSSE();
        }, 100);
        return authData;
      })
    );
  }

  /**
   * Logout the current user
   */
  logout(): Observable<any> {
    // Disconnect SSE before clearing auth data (handle errors gracefully)
    try {
      this.getNotificationService().disconnectSSE();
    } catch (error) {
      console.warn('Failed to disconnect SSE during logout:', error);
    }

    return this.apollo.mutate({
      mutation: LOGOUT
    }).pipe(
      map(result => {
        // Clear auth data regardless of mutation result
        localStorage.removeItem(this.TOKEN_KEY);
        localStorage.removeItem(this.USER_KEY);
        this.currentUserSubject.next(null);
        return result.data || true;
      })
    );
  }

  /**
   * Get current authenticated user from API
   */
  getCurrentUser(): Observable<UserResponse> {
    return this.apollo.query({
      query: GET_CURRENT_USER,
      fetchPolicy: 'network-only'
    }).pipe(
      map(result => {
        const user = this.extractData<UserResponse>(result, 'me');
        this.setCurrentUser(user);
        this.currentUserSubject.next(user);
        return user;
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
