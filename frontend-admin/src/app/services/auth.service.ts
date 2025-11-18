import { Injectable } from '@angular/core';
import { Observable, BehaviorSubject, map, catchError, throwError } from 'rxjs';
import { Apollo } from 'apollo-angular';
import { LoginRequest, User } from '../models/user.model';
import { LOGIN, GET_CURRENT_USER } from '../graphql/graphql.operations';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly TOKEN_KEY = 'admin_auth_token';
  private readonly USER_KEY = 'admin_current_user';
  private currentUserSubject = new BehaviorSubject<User | null>(this.getCurrentUserFromStorage());

  public currentUser$ = this.currentUserSubject.asObservable();

  constructor(private apollo: Apollo) {}

  /**
   * Admin login with username/email and password
   */
  login(request: LoginRequest): Observable<any> {
    return this.apollo.mutate({
      mutation: LOGIN,
      variables: {
        input: {
          username: request.emailOrUsername || request.email,
          password: request.password
        }
      }
    }).pipe(
      map(result => {
        if (result.data) {
          const authData = (result.data as any).login;

          // Verify user is an admin
          if (authData.user.role !== 'ADMIN') {
            throw new Error('Access denied. Admin privileges required.');
          }

          this.setToken(authData.token);
          this.setCurrentUser(authData.user);
          this.currentUserSubject.next(authData.user);
        }
        return result.data;
      }),
      catchError(this.handleError)
    );
  }

  /**
   * Get current authenticated admin user
   */
  getCurrentUser(): Observable<User> {
    return this.apollo.query({
      query: GET_CURRENT_USER,
      fetchPolicy: 'network-only'
    }).pipe(
      map(result => {
        const user = (result.data as any).me;
        if (user) {
          if (user.role !== 'ADMIN') {
            throw new Error('Access denied. Admin privileges required.');
          }
          this.setCurrentUser(user);
          this.currentUserSubject.next(user);
        }
        return user;
      }),
      catchError(this.handleError)
    );
  }

  /**
   * Logout the current admin
   */
  logout(): void {
    localStorage.removeItem(this.TOKEN_KEY);
    localStorage.removeItem(this.USER_KEY);
    this.currentUserSubject.next(null);
  }

  /**
   * Get JWT token from localStorage
   */
  getToken(): string | null {
    return localStorage.getItem(this.TOKEN_KEY);
  }

  /**
   * Check if admin is authenticated
   */
  isAuthenticated(): boolean {
    const token = this.getToken();
    if (!token) return false;

    try {
      const payload = this.decodeToken(token);
      const currentTime = Math.floor(Date.now() / 1000);
      return payload.exp > currentTime && this.currentUserSubject.value?.role === 'ADMIN';
    } catch {
      return false;
    }
  }

  /**
   * Get current user value from BehaviorSubject
   */
  getCurrentUserValue(): User | null {
    return this.currentUserSubject.value;
  }

  /**
   * Store JWT token in localStorage
   */
  private setToken(token: string): void {
    localStorage.setItem(this.TOKEN_KEY, token);
  }

  /**
   * Store current user in localStorage
   */
  private setCurrentUser(user: User): void {
    localStorage.setItem(this.USER_KEY, JSON.stringify(user));
  }

  /**
   * Get current user from localStorage
   */
  private getCurrentUserFromStorage(): User | null {
    const userStr = localStorage.getItem(this.USER_KEY);
    if (userStr) {
      try {
        return JSON.parse(userStr);
      } catch {
        return null;
      }
    }
    return null;
  }

  /**
   * Decode JWT token payload
   */
  private decodeToken(token: string): any {
    const payload = token.split('.')[1];
    return JSON.parse(atob(payload));
  }

  /**
   * Handle GraphQL errors
   */
  private handleError(error: any): Observable<never> {
    let errorMessage = 'An unknown error occurred';

    if (error.graphQLErrors && error.graphQLErrors.length > 0) {
      errorMessage = error.graphQLErrors.map((e: any) => e.message).join(', ');
    } else if (error.networkError) {
      errorMessage = `Network error: ${error.networkError.message}`;
    } else if (error.message) {
      errorMessage = error.message;
    }

    console.error('GraphQL Error:', errorMessage);
    return throwError(() => new Error(errorMessage));
  }
}
