import { Injectable } from '@angular/core';
import { Observable, map, catchError, throwError } from 'rxjs';
import { Apollo } from 'apollo-angular';
import { User } from '../models/user.model';
import { Account } from '../models/account.model';
import {
  GET_ALL_USERS,
  GET_USER_BY_ID,
  GET_ALL_ADMIN_ACCOUNTS,
  GET_ADMIN_ACCOUNT_BY_ID,
  GET_ACCOUNTS_BY_USER_ID,
  SUSPEND_USER,
  ACTIVATE_USER,
  LOCK_USER,
  DELETE_USER
} from '../graphql/graphql.operations';

@Injectable({
  providedIn: 'root'
})
export class AdminService {
  constructor(private apollo: Apollo) {}

  // ========== User Management ==========

  /**
   * Get all users
   */
  getAllUsers(): Observable<User[]> {
    return this.apollo.query({
      query: GET_ALL_USERS,
      fetchPolicy: 'network-only'
    }).pipe(
      map(result => (result.data as any).adminUsers),
      catchError(this.handleError)
    );
  }

  /**
   * Get user by ID
   */
  getUserById(id: string): Observable<User> {
    return this.apollo.query({
      query: GET_USER_BY_ID,
      variables: { id },
      fetchPolicy: 'network-only'
    }).pipe(
      map(result => (result.data as any).adminUser),
      catchError(this.handleError)
    );
  }

  /**
   * Suspend a user
   */
  suspendUser(id: string): Observable<User> {
    return this.apollo.mutate({
      mutation: SUSPEND_USER,
      variables: { id }
    }).pipe(
      map(result => (result.data as any).adminSuspendUser),
      catchError(this.handleError)
    );
  }

  /**
   * Activate a user
   */
  activateUser(id: string): Observable<User> {
    return this.apollo.mutate({
      mutation: ACTIVATE_USER,
      variables: { id }
    }).pipe(
      map(result => (result.data as any).adminActivateUser),
      catchError(this.handleError)
    );
  }

  /**
   * Lock a user
   */
  lockUser(id: string): Observable<User> {
    return this.apollo.mutate({
      mutation: LOCK_USER,
      variables: { id }
    }).pipe(
      map(result => (result.data as any).adminLockUser),
      catchError(this.handleError)
    );
  }

  /**
   * Delete a user
   */
  deleteUser(id: string): Observable<boolean> {
    return this.apollo.mutate({
      mutation: DELETE_USER,
      variables: { id }
    }).pipe(
      map(result => (result.data as any).adminDeleteUser),
      catchError(this.handleError)
    );
  }

  // ========== Account Management ==========

  /**
   * Get all accounts
   */
  getAllAccounts(): Observable<Account[]> {
    return this.apollo.query({
      query: GET_ALL_ADMIN_ACCOUNTS,
      fetchPolicy: 'network-only'
    }).pipe(
      map(result => (result.data as any).adminAccounts),
      catchError(this.handleError)
    );
  }

  /**
   * Get account by ID
   */
  getAccountById(id: string): Observable<Account> {
    return this.apollo.query({
      query: GET_ADMIN_ACCOUNT_BY_ID,
      variables: { id },
      fetchPolicy: 'network-only'
    }).pipe(
      map(result => (result.data as any).adminAccount),
      catchError(this.handleError)
    );
  }

  /**
   * Get accounts by user ID
   */
  getAccountsByUserId(userId: string): Observable<Account[]> {
    return this.apollo.query({
      query: GET_ACCOUNTS_BY_USER_ID,
      variables: { userId },
      fetchPolicy: 'network-only'
    }).pipe(
      map(result => (result.data as any).adminUserAccounts),
      catchError(this.handleError)
    );
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
