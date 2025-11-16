export enum AccountStatus {
  ACTIVE = 'ACTIVE',
  FROZEN = 'FROZEN',
  CLOSED = 'CLOSED'
}

export enum TransactionType {
  DEPOSIT = 'DEPOSIT',
  WITHDRAWAL = 'WITHDRAWAL',
  TRANSFER_IN = 'TRANSFER_IN',
  TRANSFER_OUT = 'TRANSFER_OUT'
}

export enum CategoryType {
  INCOME = 'INCOME',
  EXPENSE = 'EXPENSE',
  OTHER = 'OTHER'
}

export interface Category {
  id: number;
  name: string;
  description: string;
  type: CategoryType;
  color: string;
  active: boolean;
  createdAt: string;
}

export interface Account {
  id: number;
  accountNumber: string;
  customerId: number;
  accountType: string;
  balance: number;
  status: AccountStatus;
  createdAt: string;
}

export interface Transaction {
  id: number;
  accountId: number;
  type: TransactionType;
  categoryId: number;
  amount: number;
  balanceAfter: number;
  description: string;
  relatedAccountId?: number;
  createdAt: string;
}

export interface AccountStatement {
  account: Account;
  startDate: string;
  endDate: string;
  openingBalance: number;
  closingBalance: number;
  totalDeposits: number;
  totalWithdrawals: number;
  netChange: number;
  transactionCount: number;
  transactions: Transaction[];
}

export interface CategorySummary {
  category: Category;
  amount: number;
  count: number;
  percentage: number;
}

export interface CategoryReport {
  accountId: number;
  transactionType: TransactionType;
  categories: CategorySummary[];
  totalAmount: number;
  totalTransactions: number;
}

export interface TransactionRequest {
  amount: number;
  description: string;
  categoryId?: number;
}

export interface TransferRequest {
  toAccountId: number;
  amount: number;
  description: string;
}

export interface CreateAccountRequest {
  customerId: number;
  accountType: string;
}
