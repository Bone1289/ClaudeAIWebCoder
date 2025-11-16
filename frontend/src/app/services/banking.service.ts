import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, catchError, throwError } from 'rxjs';
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
  private apiUrl = '/api/banking';

  constructor(private http: HttpClient) { }

  // ========== Account Operations ==========

  /**
   * Create a new account
   * @param request - Account creation request
   * @returns Observable<ApiResponse<Account>> - Created account
   */
  createAccount(request: CreateAccountRequest): Observable<ApiResponse<Account>> {
    return this.http.post<ApiResponse<Account>>(`${this.apiUrl}/accounts`, request).pipe(
      catchError(this.handleError)
    );
  }

  /**
   * Get all accounts
   * @returns Observable<ApiResponse<Account[]>> - List of all accounts
   */
  getAllAccounts(): Observable<ApiResponse<Account[]>> {
    return this.http.get<ApiResponse<Account[]>>(`${this.apiUrl}/accounts`).pipe(
      catchError(this.handleError)
    );
  }

  /**
   * Get account by ID
   * @param id - Account ID
   * @returns Observable<ApiResponse<Account>> - Account object
   */
  getAccountById(id: string): Observable<ApiResponse<Account>> {
    return this.http.get<ApiResponse<Account>>(`${this.apiUrl}/accounts/${id}`).pipe(
      catchError(this.handleError)
    );
  }

  /**
   * Get accounts by customer ID
   * @param customerId - Customer ID
   * @returns Observable<ApiResponse<Account[]>> - List of customer accounts
   */
  getAccountsByCustomerId(customerId: string): Observable<ApiResponse<Account[]>> {
    return this.http.get<ApiResponse<Account[]>>(`${this.apiUrl}/accounts/customer/${customerId}`).pipe(
      catchError(this.handleError)
    );
  }

  /**
   * Update an account
   * @param id - Account ID
   * @param request - Update account request
   * @returns Observable<ApiResponse<Account>> - Updated account
   */
  updateAccount(id: string, request: UpdateAccountRequest): Observable<ApiResponse<Account>> {
    return this.http.put<ApiResponse<Account>>(`${this.apiUrl}/accounts/${id}`, request).pipe(
      catchError(this.handleError)
    );
  }

  /**
   * Delete an account
   * @param id - Account ID
   * @returns Observable<ApiResponse<void>> - Delete confirmation
   */
  deleteAccount(id: string): Observable<ApiResponse<void>> {
    return this.http.delete<ApiResponse<void>>(`${this.apiUrl}/accounts/${id}`).pipe(
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
    return this.http.post<ApiResponse<Account>>(`${this.apiUrl}/accounts/${accountId}/deposit`, request).pipe(
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
    return this.http.post<ApiResponse<Account>>(`${this.apiUrl}/accounts/${accountId}/withdraw`, request).pipe(
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
    return this.http.post<ApiResponse<void>>(`${this.apiUrl}/accounts/${fromAccountId}/transfer`, request).pipe(
      catchError(this.handleError)
    );
  }

  /**
   * Get transaction history for an account
   * @param accountId - Account ID
   * @returns Observable<ApiResponse<Transaction[]>> - List of transactions
   */
  getTransactionHistory(accountId: string): Observable<ApiResponse<Transaction[]>> {
    return this.http.get<ApiResponse<Transaction[]>>(`${this.apiUrl}/accounts/${accountId}/transactions`).pipe(
      catchError(this.handleError)
    );
  }

  /**
   * Get all transactions
   * @returns Observable<ApiResponse<Transaction[]>> - List of all transactions
   */
  getAllTransactions(): Observable<ApiResponse<Transaction[]>> {
    return this.http.get<ApiResponse<Transaction[]>>(`${this.apiUrl}/transactions`).pipe(
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
    const params = new HttpParams()
      .set('startDate', startDate)
      .set('endDate', endDate);

    return this.http.get<ApiResponse<AccountStatement>>(
      `${this.apiUrl}/accounts/${accountId}/statement`,
      { params }
    ).pipe(
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
    const params = new HttpParams().set('type', transactionType);

    return this.http.get<ApiResponse<CategoryReport>>(
      `${this.apiUrl}/accounts/${accountId}/category-report`,
      { params }
    ).pipe(
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
    } else if (error.error && error.error.message) {
      // Server-side error with message
      errorMessage = error.error.message;
    } else {
      // Server-side error without message
      errorMessage = `Error Code: ${error.status}\nMessage: ${error.message}`;
    }

    console.error(errorMessage);
    return throwError(() => new Error(errorMessage));
  }
}
