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
  accountId: string | null = null;

  activeTab: 'deposit' | 'withdraw' | 'transfer' = 'deposit';

  // Form models
  depositForm = {
    amount: 0,
    description: '',
    categoryId: undefined as string | undefined
  };

  withdrawForm = {
    amount: 0,
    description: '',
    categoryId: undefined as string | undefined
  };

  transferForm = {
    toAccountId: '',
    amount: 0,
    description: ''
  };

  // Transaction categories loaded from API
  incomeCategories: Category[] = [];
  expenseCategories: Category[] = [];
  allCategories: Category[] = [];

  // Autocomplete state
  depositCategoryInput: string = '';
  withdrawCategoryInput: string = '';
  showDepositSuggestions: boolean = false;
  showWithdrawSuggestions: boolean = false;
  filteredIncomeCategories: Category[] = [];
  filteredExpenseCategories: Category[] = [];

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
      this.accountId = params['id'];
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
      toAccountId: '',
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

  // Autocomplete methods for Deposit Category
  onDepositCategoryInput(): void {
    const input = this.depositCategoryInput.toLowerCase().trim();

    if (!input) {
      this.filteredIncomeCategories = this.incomeCategories;
      this.showDepositSuggestions = false;
      this.depositForm.categoryId = undefined;
      return;
    }

    this.filteredIncomeCategories = this.incomeCategories.filter((cat: Category) =>
      cat.name.toLowerCase().includes(input)
    );
    this.showDepositSuggestions = true;

    // Check if exact match exists
    const exactMatch = this.incomeCategories.find((cat: Category) =>
      cat.name.toLowerCase() === input
    );
    if (exactMatch) {
      this.depositForm.categoryId = exactMatch.id;
    } else {
      this.depositForm.categoryId = undefined;
    }
  }

  selectDepositCategory(category: Category): void {
    this.depositCategoryInput = category.name;
    this.depositForm.categoryId = category.id;
    this.showDepositSuggestions = false;
  }

  createDepositCategory(): void {
    const input = this.depositCategoryInput.trim();
    if (!input) return;

    // Check if category already exists
    const exists = this.incomeCategories.find((cat: Category) =>
      cat.name.toLowerCase() === input.toLowerCase()
    );

    if (exists) {
      this.selectDepositCategory(exists);
      return;
    }

    // Create new category
    const newCategory = {
      name: input,
      description: `${input} - Income category`,
      type: CategoryType.INCOME,
      color: this.generateRandomColor()
    };

    this.categoryService.createCategory(newCategory).subscribe({
      next: (response: ApiResponse<Category>) => {
        if (response.success && response.data) {
          this.selectDepositCategory(response.data);
          this.success = `Category "${input}" created successfully!`;
          setTimeout(() => this.success = null, 3000);
        }
      },
      error: (error: any) => {
        this.error = `Failed to create category: ${error.message}`;
      }
    });
  }

  // Autocomplete methods for Withdraw Category
  onWithdrawCategoryInput(): void {
    const input = this.withdrawCategoryInput.toLowerCase().trim();

    if (!input) {
      this.filteredExpenseCategories = this.expenseCategories;
      this.showWithdrawSuggestions = false;
      this.withdrawForm.categoryId = undefined;
      return;
    }

    this.filteredExpenseCategories = this.expenseCategories.filter((cat: Category) =>
      cat.name.toLowerCase().includes(input)
    );
    this.showWithdrawSuggestions = true;

    // Check if exact match exists
    const exactMatch = this.expenseCategories.find((cat: Category) =>
      cat.name.toLowerCase() === input
    );
    if (exactMatch) {
      this.withdrawForm.categoryId = exactMatch.id;
    } else {
      this.withdrawForm.categoryId = undefined;
    }
  }

  selectWithdrawCategory(category: Category): void {
    this.withdrawCategoryInput = category.name;
    this.withdrawForm.categoryId = category.id;
    this.showWithdrawSuggestions = false;
  }

  createWithdrawCategory(): void {
    const input = this.withdrawCategoryInput.trim();
    if (!input) return;

    // Check if category already exists
    const exists = this.expenseCategories.find((cat: Category) =>
      cat.name.toLowerCase() === input.toLowerCase()
    );

    if (exists) {
      this.selectWithdrawCategory(exists);
      return;
    }

    // Create new category
    const newCategory = {
      name: input,
      description: `${input} - Expense category`,
      type: CategoryType.EXPENSE,
      color: this.generateRandomColor()
    };

    this.categoryService.createCategory(newCategory).subscribe({
      next: (response: ApiResponse<Category>) => {
        if (response.success && response.data) {
          this.selectWithdrawCategory(response.data);
          this.success = `Category "${input}" created successfully!`;
          setTimeout(() => this.success = null, 3000);
        }
      },
      error: (error: any) => {
        this.error = `Failed to create category: ${error.message}`;
      }
    });
  }

  // Helper method to generate random colors for new categories
  private generateRandomColor(): string {
    const colors = [
      '#e74c3c', '#3498db', '#2ecc71', '#f39c12', '#9b59b6',
      '#1abc9c', '#e67e22', '#34495e', '#16a085', '#27ae60',
      '#2980b9', '#8e44ad', '#c0392b', '#d35400', '#7f8c8d'
    ];
    return colors[Math.floor(Math.random() * colors.length)];
  }
}
