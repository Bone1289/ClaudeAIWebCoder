import { Component, OnInit } from '@angular/core';
import { BankingService } from '../../../services/banking.service';
import { Account, TransactionCategory } from '../../../models/banking.model';

@Component({
  selector: 'app-banking-dashboard',
  templateUrl: './banking-dashboard.component.html',
  styleUrls: ['./banking-dashboard.component.css']
})
export class BankingDashboardComponent implements OnInit {
  accounts: Account[] = [];
  selectedAccount: Account | null = null;
  loading = false;
  error: string | null = null;

  // Transaction categories for display
  transactionCategories = Object.values(TransactionCategory);

  constructor(private bankingService: BankingService) { }

  ngOnInit(): void {
    this.loadAccounts();
  }

  loadAccounts(): void {
    this.loading = true;
    this.error = null;

    this.bankingService.getAllAccounts().subscribe({
      next: (response) => {
        this.accounts = response.data || [];
        this.loading = false;
      },
      error: (error) => {
        this.error = 'Failed to load accounts: ' + error.message;
        this.loading = false;
      }
    });
  }

  selectAccount(account: Account): void {
    this.selectedAccount = account;
  }

  getTotalBalance(): number {
    return this.accounts.reduce((total, account) => total + account.balance, 0);
  }

  getActiveAccountsCount(): number {
    return this.accounts.filter(account => account.status === 'ACTIVE').length;
  }
}
