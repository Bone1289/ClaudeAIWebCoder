import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router, Params } from '@angular/router';
import { BankingService } from '../../../services/banking.service';
import { CategoryService } from '../../../services/category.service';
import { Account, Category, CategoryType, TransactionRequest, TransferRequest } from '../../../models/banking.model';
import { ApiResponse } from '../../../models/api-response.model';

@Component({
  selector: 'app-transaction-forms',
  templateUrl: './transaction-forms.component.html',
  styleUrls: ['./transaction-forms.component.css']
})
export class TransactionFormsComponent implements OnInit {
  account: Account | null = null;
  accounts: Account[] = [];
  accountId: number | null = null;

  activeTab: 'deposit' | 'withdraw' | 'transfer' = 'deposit';

  // Form models
  depositForm = {
    amount: 0,
    description: '',
    categoryId: undefined as number | undefined
  };

  withdrawForm = {
    amount: 0,
    description: '',
    categoryId: undefined as number | undefined
  };

  transferForm = {
    toAccountId: 0,
    amount: 0,
    description: ''
  };

  // Transaction categories loaded from API
  incomeCategories: Category[] = [];
  expenseCategories: Category[] = [];
  allCategories: Category[] = [];

  loading = false;
  error: string | null = null;
  success: string | null = null;

  constructor(
    private bankingService: BankingService,
    private categoryService: CategoryService,
    private route: ActivatedRoute,
    private router: Router
  ) { }

  ngOnInit(): void {
    this.route.params.subscribe((params: Params) => {
      this.accountId = +params['id'];
      if (this.accountId) {
        this.loadAccount();
        this.loadAccounts();
      }
    });

    // Load categories
    this.loadCategories();

    // Subscribe to category updates
    this.categoryService.categories$.subscribe((categories: Category[]) => {
      this.allCategories = categories;
      this.incomeCategories = categories.filter((cat: Category) => cat.type === CategoryType.INCOME && cat.active);
      this.expenseCategories = categories.filter((cat: Category) => cat.type === CategoryType.EXPENSE && cat.active);
    });
  }

  loadCategories(): void {
    this.categoryService.loadCategories().subscribe({
      error: (error: any) => {
        console.error('Failed to load categories:', error);
      }
    });
  }

  loadAccount(): void {
    if (!this.accountId) return;

    this.bankingService.getAccountById(this.accountId).subscribe({
      next: (response: ApiResponse<Account>) => {
        this.account = response.data;
      },
      error: (error: any) => {
        this.error = 'Failed to load account: ' + error.message;
      }
    });
  }

  loadAccounts(): void {
    this.bankingService.getAllAccounts().subscribe({
      next: (response: ApiResponse<Account[]>) => {
        this.accounts = (response.data || []).filter((acc: Account) => acc.id !== this.accountId);
      },
      error: () => {
        // Silently fail - not critical
      }
    });
  }

  setTab(tab: 'deposit' | 'withdraw' | 'transfer'): void {
    this.activeTab = tab;
    this.clearMessages();
  }

  onDeposit(): void {
    if (!this.accountId) return;

    this.loading = true;
    this.clearMessages();

    const request: TransactionRequest = {
      amount: this.depositForm.amount,
      description: this.depositForm.description,
      categoryId: this.depositForm.categoryId
    };

    this.bankingService.deposit(this.accountId, request).subscribe({
      next: (response: ApiResponse<Account>) => {
        this.account = response.data;
        this.success = 'Deposit successful!';
        this.resetDepositForm();
        this.loading = false;
      },
      error: (error: any) => {
        this.error = error.message;
        this.loading = false;
      }
    });
  }

  onWithdraw(): void {
    if (!this.accountId) return;

    this.loading = true;
    this.clearMessages();

    const request: TransactionRequest = {
      amount: this.withdrawForm.amount,
      description: this.withdrawForm.description,
      categoryId: this.withdrawForm.categoryId
    };

    this.bankingService.withdraw(this.accountId, request).subscribe({
      next: (response: ApiResponse<Account>) => {
        this.account = response.data;
        this.success = 'Withdrawal successful!';
        this.resetWithdrawForm();
        this.loading = false;
      },
      error: (error: any) => {
        this.error = error.message;
        this.loading = false;
      }
    });
  }

  onTransfer(): void {
    if (!this.accountId) return;

    this.loading = true;
    this.clearMessages();

    const request: TransferRequest = {
      toAccountId: this.transferForm.toAccountId,
      amount: this.transferForm.amount,
      description: this.transferForm.description
    };

    this.bankingService.transfer(this.accountId, request).subscribe({
      next: (response: ApiResponse<void>) => {
        this.success = 'Transfer successful!';
        this.resetTransferForm();
        this.loadAccount(); // Reload account to get updated balance
        this.loading = false;
      },
      error: (error: any) => {
        this.error = error.message;
        this.loading = false;
      }
    });
  }

  resetDepositForm(): void {
    this.depositForm = {
      amount: 0,
      description: '',
      categoryId: undefined
    };
  }

  resetWithdrawForm(): void {
    this.withdrawForm = {
      amount: 0,
      description: '',
      categoryId: undefined
    };
  }

  resetTransferForm(): void {
    this.transferForm = {
      toAccountId: 0,
      amount: 0,
      description: ''
    };
  }

  clearMessages(): void {
    this.error = null;
    this.success = null;
  }

  goBack(): void {
    this.router.navigate(['/banking/dashboard']);
  }
}
