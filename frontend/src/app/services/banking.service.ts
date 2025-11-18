import { Injectable } from '@angular/core';
import { Observable, throwError } from 'rxjs';
import { map, catchError } from 'rxjs/operators';
import { GrpcClientService } from '../grpc/grpc-client.service';
import { ApiResponse } from '../models/api-response.model';
import {
  Account,
  Transaction,
  AccountStatement,
  CategoryReport,
  TransactionRequest,
  TransferRequest,
  CreateAccountRequest,
  UpdateAccountRequest,
  TransactionType
} from '../models/banking.model';

@Injectable({
  providedIn: 'root'
})
export class BankingService {
  private readonly SERVICE_NAME = 'com.example.demo.grpc.BankingService';

  constructor(private grpcClient: GrpcClientService) { }

  // ========== Account Operations ==========

  /**
   * Create a new account
   * @param request - Account creation request
   * @returns Observable<ApiResponse<Account>> - Created account
   */
  createAccount(request: CreateAccountRequest): Observable<ApiResponse<Account>> {
    const grpcRequest = {
      first_name: request.firstName,
      last_name: request.lastName,
      nationality: request.nationality,
      account_type: request.accountType
    };

    return this.grpcClient.call<any, any>(
      this.SERVICE_NAME,
      'CreateAccount',
      grpcRequest
    ).pipe(
      map(response => ({
        success: response.success,
        message: response.message,
        data: response.account ? this.mapGrpcAccountToModel(response.account) : null
      })),
      catchError(this.handleError)
    );
  }

  /**
   * Get all accounts
   * @returns Observable<ApiResponse<Account[]>> - List of all accounts
   */
  getAllAccounts(): Observable<ApiResponse<Account[]>> {
    return this.grpcClient.call<{}, any>(
      this.SERVICE_NAME,
      'GetAllAccounts',
      {}
    ).pipe(
      map(response => ({
        success: response.success,
        message: response.message,
        data: response.accounts ? response.accounts.map((a: any) => this.mapGrpcAccountToModel(a)) : []
      })),
      catchError(this.handleError)
    );
  }

  /**
   * Get account by ID
   * @param id - Account ID
   * @returns Observable<ApiResponse<Account>> - Account object
   */
  getAccountById(id: string): Observable<ApiResponse<Account>> {
    return this.grpcClient.call<any, any>(
      this.SERVICE_NAME,
      'GetAccount',
      { id }
    ).pipe(
      map(response => ({
        success: response.success,
        message: response.message,
        data: response.account ? this.mapGrpcAccountToModel(response.account) : null
      })),
      catchError(this.handleError)
    );
  }

  /**
   * Get accounts by customer ID
   * @param customerId - Customer ID
   * @returns Observable<ApiResponse<Account[]>> - List of customer accounts
   */
  getAccountsByCustomerId(customerId: string): Observable<ApiResponse<Account[]>> {
    // gRPC service returns all accounts for current user automatically
    return this.getAllAccounts();
  }

  /**
   * Update an account
   * @param id - Account ID
   * @param request - Update account request
   * @returns Observable<ApiResponse<Account>> - Updated account
   */
  updateAccount(id: string, request: UpdateAccountRequest): Observable<ApiResponse<Account>> {
    const grpcRequest = {
      id: id,
      account_type: request.accountType
    };

    return this.grpcClient.call<any, any>(
      this.SERVICE_NAME,
      'UpdateAccount',
      grpcRequest
    ).pipe(
      map(response => ({
        success: response.success,
        message: response.message,
        data: response.account ? this.mapGrpcAccountToModel(response.account) : null
      })),
      catchError(this.handleError)
    );
  }

  /**
   * Delete an account
   * @param id - Account ID
   * @returns Observable<ApiResponse<void>> - Delete confirmation
   */
  deleteAccount(id: string): Observable<ApiResponse<void>> {
    return this.grpcClient.call<any, any>(
      this.SERVICE_NAME,
      'DeleteAccount',
      { id }
    ).pipe(
      map(response => ({
        success: response.success,
        message: response.message,
        data: undefined
      })),
      catchError(this.handleError)
    );
  }

  // ========== Transaction Operations ==========

  /**
   * Deposit money into an account
   * @param accountId - Account ID
   * @param request - Transaction request with amount and description
   * @returns Observable<ApiResponse<Account>> - Updated account
   */
  deposit(accountId: string, request: TransactionRequest): Observable<ApiResponse<Account>> {
    const grpcRequest = {
      account_id: accountId,
      amount: request.amount.toString(),
      description: request.description,
      category_id: request.categoryId || ''
    };

    return this.grpcClient.call<any, any>(
      this.SERVICE_NAME,
      'Deposit',
      grpcRequest
    ).pipe(
      map(response => ({
        success: response.success,
        message: response.message,
        data: response.account ? this.mapGrpcAccountToModel(response.account) : null
      })),
      catchError(this.handleError)
    );
  }

  /**
   * Withdraw money from an account
   * @param accountId - Account ID
   * @param request - Transaction request with amount and description
   * @returns Observable<ApiResponse<Account>> - Updated account
   */
  withdraw(accountId: string, request: TransactionRequest): Observable<ApiResponse<Account>> {
    const grpcRequest = {
      account_id: accountId,
      amount: request.amount.toString(),
      description: request.description,
      category_id: request.categoryId || ''
    };

    return this.grpcClient.call<any, any>(
      this.SERVICE_NAME,
      'Withdraw',
      grpcRequest
    ).pipe(
      map(response => ({
        success: response.success,
        message: response.message,
        data: response.account ? this.mapGrpcAccountToModel(response.account) : null
      })),
      catchError(this.handleError)
    );
  }

  /**
   * Transfer money between accounts
   * @param fromAccountId - Source account ID
   * @param request - Transfer request with destination account, amount, and description
   * @returns Observable<ApiResponse<void>> - Transfer confirmation
   */
  transfer(fromAccountId: string, request: TransferRequest): Observable<ApiResponse<void>> {
    const grpcRequest = {
      from_account_id: fromAccountId,
      to_account_id: request.toAccountId,
      amount: request.amount.toString(),
      description: request.description
    };

    return this.grpcClient.call<any, any>(
      this.SERVICE_NAME,
      'Transfer',
      grpcRequest
    ).pipe(
      map(response => ({
        success: response.success,
        message: response.message,
        data: undefined
      })),
      catchError(this.handleError)
    );
  }

  /**
   * Get transaction history for an account
   * @param accountId - Account ID
   * @returns Observable<ApiResponse<Transaction[]>> - List of transactions
   */
  getTransactionHistory(accountId: string): Observable<ApiResponse<Transaction[]>> {
    const grpcRequest = {
      account_id: accountId,
      page: 0,
      size: 100
    };

    return this.grpcClient.call<any, any>(
      this.SERVICE_NAME,
      'GetAccountTransactions',
      grpcRequest
    ).pipe(
      map(response => ({
        success: response.success,
        message: response.message,
        data: response.transactions ? response.transactions.map((t: any) => this.mapGrpcTransactionToModel(t)) : []
      })),
      catchError(this.handleError)
    );
  }

  /**
   * Get all transactions
   * @returns Observable<ApiResponse<Transaction[]>> - List of all transactions
   */
  getAllTransactions(): Observable<ApiResponse<Transaction[]>> {
    const grpcRequest = {
      page: 0,
      size: 100,
      sort_by: 'createdAt',
      sort_direction: 'DESC'
    };

    return this.grpcClient.call<any, any>(
      this.SERVICE_NAME,
      'GetAllTransactions',
      grpcRequest
    ).pipe(
      map(response => ({
        success: response.success,
        message: response.message,
        data: response.transactions ? response.transactions.map((t: any) => this.mapGrpcTransactionToModel(t)) : []
      })),
      catchError(this.handleError)
    );
  }

  // ========== Statement and Report Operations ==========

  /**
   * Generate account statement for a date range
   * @param accountId - Account ID
   * @param startDate - Start date (ISO string)
   * @param endDate - End date (ISO string)
   * @returns Observable<ApiResponse<AccountStatement>> - Account statement
   */
  getAccountStatement(accountId: string, startDate: string, endDate: string): Observable<ApiResponse<AccountStatement>> {
    const grpcRequest = {
      account_id: accountId,
      start_date: startDate,
      end_date: endDate
    };

    return this.grpcClient.call<any, any>(
      this.SERVICE_NAME,
      'GetAccountStatement',
      grpcRequest
    ).pipe(
      map(response => ({
        success: response.success,
        message: response.message,
        data: response.account ? {
          account: this.mapGrpcAccountToModel(response.account),
          transactions: response.transactions ? response.transactions.map((t: any) => this.mapGrpcTransactionToModel(t)) : [],
          summary: response.summary ? {
            openingBalance: parseFloat(response.summary.opening_balance),
            closingBalance: parseFloat(response.summary.closing_balance),
            totalDeposits: parseFloat(response.summary.total_deposits),
            totalWithdrawals: parseFloat(response.summary.total_withdrawals),
            transactionCount: response.summary.transaction_count
          } : null
        } : null
      })),
      catchError(this.handleError)
    );
  }

  /**
   * Generate category report for an account
   * @param accountId - Account ID
   * @param transactionType - Type of transactions (DEPOSIT or WITHDRAWAL)
   * @returns Observable<ApiResponse<CategoryReport>> - Category report
   */
  getCategoryReport(accountId: string, transactionType: TransactionType): Observable<ApiResponse<CategoryReport>> {
    const grpcRequest = {
      account_id: accountId,
      transaction_type: transactionType
    };

    return this.grpcClient.call<any, any>(
      this.SERVICE_NAME,
      'GetCategoryReport',
      grpcRequest
    ).pipe(
      map(response => ({
        success: response.success,
        message: response.message,
        data: {
          accountId: response.account_id,
          transactionType: response.transaction_type,
          categories: response.categories || [],
          totalAmount: parseFloat(response.total_amount || '0')
        }
      })),
      catchError(this.handleError)
    );
  }

  // ========== Mapping Functions ==========

  /**
   * Map gRPC account response to Angular model
   */
  private mapGrpcAccountToModel(grpcAccount: any): Account {
    return {
      id: grpcAccount.id,
      userId: grpcAccount.user_id,
      accountNumber: grpcAccount.account_number,
      firstName: grpcAccount.first_name,
      lastName: grpcAccount.last_name,
      nationality: grpcAccount.nationality,
      accountType: grpcAccount.account_type,
      balance: parseFloat(grpcAccount.balance),
      status: grpcAccount.status,
      createdAt: grpcAccount.created_at
    };
  }

  /**
   * Map gRPC transaction response to Angular model
   */
  private mapGrpcTransactionToModel(grpcTransaction: any): Transaction {
    return {
      id: grpcTransaction.id,
      accountId: grpcTransaction.account_id,
      amount: parseFloat(grpcTransaction.amount),
      type: grpcTransaction.type,
      description: grpcTransaction.description,
      categoryId: grpcTransaction.category_id,
      categoryName: grpcTransaction.category_name,
      balanceAfter: parseFloat(grpcTransaction.balance_after),
      createdAt: grpcTransaction.created_at
    };
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
    } else if (error.error && error.error.message) {
      // Server-side error with message
      errorMessage = error.error.message;
    } else {
      // Server-side error without message
      errorMessage = `Error Code: ${error.status}\nMessage: ${error.message}`;
    }

    console.error('Banking Service Error:', errorMessage, error);
    return throwError(() => new Error(errorMessage));
  }
}
