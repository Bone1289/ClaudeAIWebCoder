import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { User, ApiResponse } from '../models/user.model';
import { Account } from '../models/account.model';
import { GrpcClientService } from '../grpc/grpc-client.service';
import { GRPC_CONFIG } from '../grpc/grpc-client.config';

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

interface GrpcAccountResponse {
  id: string;
  account_number: string;
  first_name: string;
  last_name: string;
  nationality: string;
  account_type: string;
  balance: string;
  status: string;
  created_at: string;
}

@Injectable({
  providedIn: 'root'
})
export class AdminService {
  private readonly SERVICE_NAME = GRPC_CONFIG.ADMIN_SERVICE;

  constructor(private grpcClient: GrpcClientService) {}

  // ========== User Management ==========

  /**
   * Get all users
   */
  getAllUsers(): Observable<ApiResponse<User[]>> {
    return this.grpcClient.call<{}, any>(
      this.SERVICE_NAME,
      'GetAllUsers',
      {}
    ).pipe(
      map(response => ({
        success: response.success,
        message: response.message,
        data: response.users ? response.users.map((u: any) => this.mapGrpcUserToModel(u)) : []
      }))
    );
  }

  /**
   * Get user by ID
   */
  getUserById(id: string): Observable<ApiResponse<User>> {
    return this.grpcClient.call<any, any>(
      this.SERVICE_NAME,
      'GetUser',
      { id }
    ).pipe(
      map(response => ({
        success: response.success,
        message: response.message,
        data: response.user ? this.mapGrpcUserToModel(response.user) : {} as User
      } as ApiResponse<User>))
    );
  }

  /**
   * Suspend user
   */
  suspendUser(id: string): Observable<ApiResponse<User>> {
    return this.grpcClient.call<any, any>(
      this.SERVICE_NAME,
      'SuspendUser',
      { id }
    ).pipe(
      map(response => ({
        success: response.success,
        message: response.message,
        data: response.user ? this.mapGrpcUserToModel(response.user) : {} as User
      } as ApiResponse<User>))
    );
  }

  /**
   * Activate user
   */
  activateUser(id: string): Observable<ApiResponse<User>> {
    return this.grpcClient.call<any, any>(
      this.SERVICE_NAME,
      'ActivateUser',
      { id }
    ).pipe(
      map(response => ({
        success: response.success,
        message: response.message,
        data: response.user ? this.mapGrpcUserToModel(response.user) : {} as User
      } as ApiResponse<User>))
    );
  }

  /**
   * Lock user
   */
  lockUser(id: string): Observable<ApiResponse<User>> {
    return this.grpcClient.call<any, any>(
      this.SERVICE_NAME,
      'LockUser',
      { id }
    ).pipe(
      map(response => ({
        success: response.success,
        message: response.message,
        data: response.user ? this.mapGrpcUserToModel(response.user) : {} as User
      } as ApiResponse<User>))
    );
  }

  /**
   * Delete user
   */
  deleteUser(id: string): Observable<ApiResponse<void>> {
    return this.grpcClient.call<any, any>(
      this.SERVICE_NAME,
      'DeleteUser',
      { id }
    ).pipe(
      map(response => ({
        success: response.success,
        message: response.message,
        data: undefined
      }))
    );
  }

  // ========== Account Management ==========

  /**
   * Get all accounts (admin view)
   */
  getAllAccounts(): Observable<ApiResponse<Account[]>> {
    return this.grpcClient.call<{}, any>(
      this.SERVICE_NAME,
      'GetAllAdminAccounts',
      {}
    ).pipe(
      map(response => ({
        success: response.success,
        message: response.message,
        data: response.accounts ? response.accounts.map((a: any) => this.mapGrpcAccountToModel(a)) : []
      }))
    );
  }

  /**
   * Get account by ID (admin view)
   */
  getAccountById(id: string): Observable<ApiResponse<Account>> {
    return this.grpcClient.call<any, any>(
      this.SERVICE_NAME,
      'GetAdminAccount',
      { id }
    ).pipe(
      map(response => ({
        success: response.success,
        message: response.message,
        data: response.account ? this.mapGrpcAccountToModel(response.account) : {} as Account
      } as ApiResponse<Account>))
    );
  }

  /**
   * Get accounts by user ID
   */
  getAccountsByUserId(userId: string): Observable<ApiResponse<Account[]>> {
    return this.grpcClient.call<any, any>(
      this.SERVICE_NAME,
      'GetUserAccounts',
      { id: userId }
    ).pipe(
      map(response => ({
        success: response.success,
        message: response.message,
        data: response.accounts ? response.accounts.map((a: any) => this.mapGrpcAccountToModel(a)) : []
      }))
    );
  }

  // ========== Mapping Functions ==========

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

  /**
   * Map gRPC account response to Angular model
   */
  private mapGrpcAccountToModel(grpcAccount: GrpcAccountResponse): Account {
    return {
      id: grpcAccount.id,
      userId: '', // Not provided by gRPC response
      accountNumber: grpcAccount.account_number,
      firstName: grpcAccount.first_name,
      lastName: grpcAccount.last_name,
      nationality: grpcAccount.nationality,
      accountType: grpcAccount.account_type,
      balance: parseFloat(grpcAccount.balance),
      createdAt: grpcAccount.created_at,
      lastTransactionDate: null, // Not provided by gRPC response
      transactionCount: 0 // Not provided by gRPC response
    };
  }
}
