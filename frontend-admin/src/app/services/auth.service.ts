import { Injectable } from '@angular/core';
import { Observable, BehaviorSubject } from 'rxjs';
import { tap, map } from 'rxjs/operators';
import { LoginRequest, LoginResponse, User, ApiResponse } from '../models/user.model';
import { GrpcClientService } from '../grpc/grpc-client.service';
import { GRPC_CONFIG } from '../grpc/grpc-client.config';

interface GrpcLoginRequest {
  email_or_username: string;
  password: string;
}

interface GrpcUserResponse {
  id: string;
  username: string;
  email: string;
  first_name: string;
  last_name: string;
  role: string;
  status: string;
  account_non_locked: boolean;
  created_at: string;
  last_login?: string;
}

interface GrpcLoginResponse {
  success: boolean;
  message: string;
  token: string;
  user: GrpcUserResponse;
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly SERVICE_NAME = GRPC_CONFIG.AUTH_SERVICE;
  private readonly TOKEN_KEY = GRPC_CONFIG.TOKEN_KEY;
  private currentUserSubject = new BehaviorSubject<User | null>(this.getCurrentUserFromStorage());

  public currentUser$ = this.currentUserSubject.asObservable();

  constructor(private grpcClient: GrpcClientService) {}

  login(request: LoginRequest): Observable<ApiResponse<LoginResponse>> {
    const grpcRequest: GrpcLoginRequest = {
      email_or_username: request.emailOrUsername,
      password: request.password
    };

    return this.grpcClient.call<GrpcLoginRequest, GrpcLoginResponse>(
      this.SERVICE_NAME,
      'Login',
      grpcRequest,
      false
    ).pipe(
      tap(response => {
        if (response.success && response.user) {
          // Verify user is an admin
          if (response.user.role !== 'ADMIN') {
            throw new Error('Access denied. Admin privileges required.');
          }
          this.setToken(response.token);
          const user = this.mapGrpcUserToModel(response.user);
          this.setCurrentUser(user);
          this.currentUserSubject.next(user);
        }
      }),
      map(response => ({
        success: response.success,
        message: response.message,
        data: {
          token: response.token,
          type: 'Bearer',
          user: this.mapGrpcUserToModel(response.user)
        }
      }))
    );
  }

  logout(): void {
    localStorage.removeItem(this.TOKEN_KEY);
    localStorage.removeItem('admin_current_user');
    this.currentUserSubject.next(null);
  }

  getToken(): string | null {
    return localStorage.getItem(this.TOKEN_KEY);
  }

  isAuthenticated(): boolean {
    const token = this.getToken();
    if (!token) return false;

    // Check if token is expired
    try {
      const payload = this.decodeToken(token);
      const currentTime = Math.floor(Date.now() / 1000);
      return payload.exp > currentTime && this.currentUserSubject.value?.role === 'ADMIN';
    } catch {
      return false;
    }
  }

  private setToken(token: string): void {
    localStorage.setItem(this.TOKEN_KEY, token);
  }

  private setCurrentUser(user: User): void {
    localStorage.setItem('admin_current_user', JSON.stringify(user));
  }

  private getCurrentUserFromStorage(): User | null {
    const userStr = localStorage.getItem('admin_current_user');
    if (userStr) {
      try {
        return JSON.parse(userStr);
      } catch {
        return null;
      }
    }
    return null;
  }

  private decodeToken(token: string): any {
    const payload = token.split('.')[1];
    return JSON.parse(atob(payload));
  }

  /**
   * Map gRPC user response to Angular model
   */
  private mapGrpcUserToModel(grpcUser: GrpcUserResponse): User {
    return {
      id: grpcUser.id,
      username: grpcUser.username,
      email: grpcUser.email,
      firstName: grpcUser.first_name,
      lastName: grpcUser.last_name,
      role: grpcUser.role as 'USER' | 'ADMIN',
      status: grpcUser.status as 'ACTIVE' | 'SUSPENDED' | 'LOCKED',
      createdAt: grpcUser.created_at,
      updatedAt: grpcUser.created_at // Using created_at as fallback for updatedAt
    };
  }
}
