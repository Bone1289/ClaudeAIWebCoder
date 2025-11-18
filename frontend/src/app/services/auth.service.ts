import { Injectable, Injector } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';
import { tap, map } from 'rxjs/operators';
import { LoginRequest, SignUpRequest, LoginResponse, UserResponse } from '../models/auth.model';
import { NotificationService } from './notification.service';
import { GrpcClientService } from '../grpc/grpc-client.service';

// gRPC request/response types
interface GrpcSignUpRequest {
  email: string;
  username: string;
  password: string;
  first_name: string;
  last_name: string;
}

interface GrpcSignUpResponse {
  success: boolean;
  message: string;
  user?: {
    id: string;
    email: string;
    username: string;
    first_name: string;
    last_name: string;
    role: string;
    status: string;
    created_at: string;
  };
}

interface GrpcLoginRequest {
  email_or_username: string;
  password: string;
}

interface GrpcLoginResponse {
  success: boolean;
  message: string;
  token: string;
  user?: {
    id: string;
    email: string;
    username: string;
    first_name: string;
    last_name: string;
    role: string;
    status: string;
    created_at: string;
  };
}

interface GrpcGetMeResponse {
  success: boolean;
  message: string;
  user?: {
    id: string;
    email: string;
    username: string;
    first_name: string;
    last_name: string;
    role: string;
    status: string;
    created_at: string;
  };
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly TOKEN_KEY = 'auth_token';
  private readonly USER_KEY = 'current_user';
  private readonly SERVICE_NAME = 'com.example.demo.grpc.AuthService';

  private currentUserSubject = new BehaviorSubject<UserResponse | null>(this.getCurrentUserFromStorage());
  public currentUser$ = this.currentUserSubject.asObservable();
  private notificationService?: NotificationService;

  constructor(
    private grpcClient: GrpcClientService,
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
  signUp(request: SignUpRequest): Observable<{success: boolean, message: string, data: UserResponse | null}> {
    const grpcRequest: GrpcSignUpRequest = {
      email: request.email,
      username: request.username,
      password: request.password,
      first_name: request.firstName,
      last_name: request.lastName
    };

    return this.grpcClient.call<GrpcSignUpRequest, GrpcSignUpResponse>(
      this.SERVICE_NAME,
      'SignUp',
      grpcRequest,
      false // No auth required for signup
    ).pipe(
      map(response => ({
        success: response.success,
        message: response.message,
        data: response.user ? this.mapGrpcUserToResponse(response.user) : null
      }))
    );
  }

  /**
   * Login with email/username and password
   */
  login(request: LoginRequest): Observable<{success: boolean, message: string, data: LoginResponse | null}> {
    const grpcRequest: GrpcLoginRequest = {
      email_or_username: request.emailOrUsername,
      password: request.password
    };

    return this.grpcClient.call<GrpcLoginRequest, GrpcLoginResponse>(
      this.SERVICE_NAME,
      'Login',
      grpcRequest,
      false // No auth required for login
    ).pipe(
      tap(response => {
        if (response.success && response.user) {
          // Store token and user
          this.setToken(response.token);
          const user = this.mapGrpcUserToResponse(response.user);
          this.setCurrentUser(user);
          this.currentUserSubject.next(user);

          // Connect to gRPC streaming for real-time notifications
          setTimeout(() => {
            this.getNotificationService().reconnectStream();
          }, 100);
        }
      }),
      map(response => ({
        success: response.success,
        message: response.message,
        data: response.user ? {
          token: response.token,
          user: this.mapGrpcUserToResponse(response.user)
        } : null
      }))
    );
  }

  /**
   * Logout the current user
   */
  logout(): void {
    // Disconnect gRPC stream before clearing auth data
    this.getNotificationService().disconnectStream();

    localStorage.removeItem(this.TOKEN_KEY);
    localStorage.removeItem(this.USER_KEY);
    this.currentUserSubject.next(null);
  }

  /**
   * Get current authenticated user from API
   */
  getCurrentUser(): Observable<{success: boolean, message: string, data: UserResponse | null}> {
    return this.grpcClient.call<{}, GrpcGetMeResponse>(
      this.SERVICE_NAME,
      'GetMe',
      {}, // Empty request
      true // Auth required
    ).pipe(
      tap(response => {
        if (response.success && response.user) {
          const user = this.mapGrpcUserToResponse(response.user);
          this.setCurrentUser(user);
          this.currentUserSubject.next(user);
        }
      }),
      map(response => ({
        success: response.success,
        message: response.message,
        data: response.user ? this.mapGrpcUserToResponse(response.user) : null
      }))
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

  /**
   * Map gRPC user response to Angular UserResponse model
   */
  private mapGrpcUserToResponse(grpcUser: any): UserResponse {
    return {
      id: grpcUser.id,
      email: grpcUser.email,
      username: grpcUser.username,
      firstName: grpcUser.first_name,
      lastName: grpcUser.last_name,
      role: grpcUser.role,
      status: grpcUser.status,
      createdAt: grpcUser.created_at
    };
  }
}
