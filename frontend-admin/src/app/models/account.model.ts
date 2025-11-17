export interface Account {
  id: string;
  userId: string;
  accountNumber: string;
  firstName: string;
  lastName: string;
  nationality: string;
  accountType: string;
  balance: number;
  createdAt: string;
  lastTransactionDate: string | null;
  transactionCount: number;
}
