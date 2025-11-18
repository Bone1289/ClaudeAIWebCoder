import { Injectable } from '@angular/core';
import { Observable, map, catchError, throwError } from 'rxjs';
import { Apollo } from 'apollo-angular';
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
import {
  CREATE_ACCOUNT,
  GET_ACCOUNTS,
  GET_ACCOUNT,
  UPDATE_ACCOUNT,
  DELETE_ACCOUNT,
  DEPOSIT,
  WITHDRAW,
  TRANSFER,
  GET_TRANSACTION_HISTORY,
  GET_ALL_TRANSACTIONS,
  GET_ACCOUNT_STATEMENT,
  GET_CATEGORY_REPORT
} from '../graphql/graphql.operations';

@Injectable({
  providedIn: 'root'
})
export class BankingService {
  constructor(private apollo: Apollo) { }

  // ========== Account Operations ==========

  /**
   * Create a new account
   */
  createAccount(request: CreateAccountRequest): Observable<Account> {
    return this.apollo.mutate({
      mutation: CREATE_ACCOUNT,
      variables: {
        input: {
          firstName: request.firstName,
          lastName: request.lastName,
          nationality: request.nationality,
          accountType: request.accountType
        }
      }
    }).pipe(
      map(result => (result.data as any).createAccount),
      catchError(this.handleError)
    );
  }

  /**
   * Get all accounts
   */
  getAllAccounts(): Observable<Account[]> {
    return this.apollo.query({
      query: GET_ACCOUNTS,
      fetchPolicy: 'network-only'
    }).pipe(
      map(result => (result.data as any).accounts),
      catchError(this.handleError)
    );
  }

  /**
   * Get account by ID
   */
  getAccountById(id: string): Observable<Account> {
    return this.apollo.query({
      query: GET_ACCOUNT,
      variables: { id },
      fetchPolicy: 'network-only'
    }).pipe(
      map(result => (result.data as any).account),
      catchError(this.handleError)
    );
  }

  /**
   * Update an account
   */
  updateAccount(id: string, request: UpdateAccountRequest): Observable<Account> {
    return this.apollo.mutate({
      mutation: UPDATE_ACCOUNT,
      variables: {
        id,
        input: {
          accountType: request.accountType
        }
      }
    }).pipe(
      map(result => (result.data as any).updateAccount),
      catchError(this.handleError)
    );
  }

  /**
   * Delete an account
   */
  deleteAccount(id: string): Observable<boolean> {
    return this.apollo.mutate({
      mutation: DELETE_ACCOUNT,
      variables: { id }
    }).pipe(
      map(result => (result.data as any).deleteAccount),
      catchError(this.handleError)
    );
  }

  // ========== Transaction Operations ==========

  /**
   * Deposit money into an account
   */
  deposit(accountId: string, request: TransactionRequest): Observable<Transaction> {
    return this.apollo.mutate({
      mutation: DEPOSIT,
      variables: {
        accountId,
        input: {
          amount: request.amount,
          description: request.description,
          categoryId: request.categoryId
        }
      }
    }).pipe(
      map(result => (result.data as any).deposit),
      catchError(this.handleError)
    );
  }

  /**
   * Withdraw money from an account
   */
  withdraw(accountId: string, request: TransactionRequest): Observable<Transaction> {
    return this.apollo.mutate({
      mutation: WITHDRAW,
      variables: {
        accountId,
        input: {
          amount: request.amount,
          description: request.description,
          categoryId: request.categoryId
        }
      }
    }).pipe(
      map(result => (result.data as any).withdraw),
      catchError(this.handleError)
    );
  }

  /**
   * Transfer money between accounts
   */
  transfer(fromAccountId: string, request: TransferRequest): Observable<Transaction> {
    return this.apollo.mutate({
      mutation: TRANSFER,
      variables: {
        fromAccountId,
        input: {
          toAccountId: request.toAccountId,
          amount: request.amount,
          description: request.description
        }
      }
    }).pipe(
      map(result => (result.data as any).transfer),
      catchError(this.handleError)
    );
  }

  /**
   * Get transaction history for an account
   */
  getTransactionHistory(accountId: string): Observable<Transaction[]> {
    return this.apollo.query({
      query: GET_TRANSACTION_HISTORY,
      variables: { accountId },
      fetchPolicy: 'network-only'
    }).pipe(
      map(result => (result.data as any).transactionHistory),
      catchError(this.handleError)
    );
  }

  /**
   * Get all transactions
   */
  getAllTransactions(): Observable<Transaction[]> {
    return this.apollo.query({
      query: GET_ALL_TRANSACTIONS,
      fetchPolicy: 'network-only'
    }).pipe(
      map(result => (result.data as any).transactions),
      catchError(this.handleError)
    );
  }

  // ========== Statement and Report Operations ==========

  /**
   * Generate account statement for a date range
   */
  getAccountStatement(accountId: string, startDate: string, endDate: string): Observable<AccountStatement> {
    return this.apollo.query({
      query: GET_ACCOUNT_STATEMENT,
      variables: {
        accountId,
        startDate,
        endDate
      },
      fetchPolicy: 'network-only'
    }).pipe(
      map(result => (result.data as any).accountStatement),
      catchError(this.handleError)
    );
  }

  /**
   * Generate category report for an account
   */
  getCategoryReport(accountId: string, type: string): Observable<CategoryReport[]> {
    return this.apollo.query({
      query: GET_CATEGORY_REPORT,
      variables: {
        accountId,
        type
      },
      fetchPolicy: 'network-only'
    }).pipe(
      map(result => (result.data as any).categoryReport),
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
