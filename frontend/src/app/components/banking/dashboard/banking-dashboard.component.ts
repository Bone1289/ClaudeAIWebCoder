import { Component, OnInit } from '@angular/core';
import { BankingService } from '../../../services/banking.service';
import { Account, CreateAccountRequest, UpdateAccountRequest } from '../../../models/banking.model';
import { ApiResponse } from '../../../models/api-response.model';

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
  successMessage: string | null = null;

  // Modal states
  showAddModal = false;
  showEditModal = false;
  showDeleteModal = false;

  // Form data
  newAccount: CreateAccountRequest = {
    customerId: 1, // Default customer ID
    accountType: 'CHECKING'
  };
  editingAccount: Account | null = null;
  editAccountType: string = '';
  accountToDelete: Account | null = null;

  accountTypes = ['CHECKING', 'SAVINGS', 'CREDIT'];

  constructor(private bankingService: BankingService) { }

  ngOnInit(): void {
    this.loadAccounts();
  }

  loadAccounts(): void {
    this.loading = true;
    this.error = null;

    this.bankingService.getAllAccounts().subscribe({
      next: (response: ApiResponse<Account[]>) => {
        this.accounts = response.data || [];
        this.loading = false;
      },
      error: (error: any) => {
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

  // ========== Add Account ==========

  openAddModal(): void {
    this.newAccount = {
      customerId: 1,
      accountType: 'CHECKING'
    };
    this.showAddModal = true;
    this.error = null;
    this.successMessage = null;
  }

  closeAddModal(): void {
    this.showAddModal = false;
  }

  createAccount(): void {
    this.loading = true;
    this.error = null;

    this.bankingService.createAccount(this.newAccount).subscribe({
      next: (response: ApiResponse<Account>) => {
        this.loading = false;
        this.showAddModal = false;
        this.successMessage = 'Account created successfully!';
        this.loadAccounts();
        setTimeout(() => this.successMessage = null, 3000);
      },
      error: (error: any) => {
        this.error = error.message;
        this.loading = false;
      }
    });
  }

  // ========== Edit Account ==========

  openEditModal(account: Account): void {
    this.editingAccount = account;
    this.editAccountType = account.accountType;
    this.showEditModal = true;
    this.error = null;
    this.successMessage = null;
  }

  closeEditModal(): void {
    this.showEditModal = false;
    this.editingAccount = null;
  }

  updateAccount(): void {
    if (!this.editingAccount) return;

    this.loading = true;
    this.error = null;

    const request: UpdateAccountRequest = {
      accountType: this.editAccountType
    };

    this.bankingService.updateAccount(this.editingAccount.id, request).subscribe({
      next: (response: ApiResponse<Account>) => {
        this.loading = false;
        this.showEditModal = false;
        this.successMessage = 'Account updated successfully!';
        this.loadAccounts();
        setTimeout(() => this.successMessage = null, 3000);
      },
      error: (error: any) => {
        this.error = error.message;
        this.loading = false;
      }
    });
  }

  // ========== Delete Account ==========

  openDeleteModal(account: Account): void {
    this.accountToDelete = account;
    this.showDeleteModal = true;
    this.error = null;
    this.successMessage = null;
  }

  closeDeleteModal(): void {
    this.showDeleteModal = false;
    this.accountToDelete = null;
  }

  deleteAccount(): void {
    if (!this.accountToDelete) return;

    this.loading = true;
    this.error = null;

    this.bankingService.deleteAccount(this.accountToDelete.id).subscribe({
      next: (response: ApiResponse<void>) => {
        this.loading = false;
        this.showDeleteModal = false;
        this.successMessage = 'Account deleted successfully!';
        this.loadAccounts();
        setTimeout(() => this.successMessage = null, 3000);
      },
      error: (error: any) => {
        this.error = error.message;
        this.loading = false;
      }
    });
  }
}
