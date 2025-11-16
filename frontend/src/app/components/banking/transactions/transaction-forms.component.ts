import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { BankingService } from '../../../services/banking.service';
import { Account, TransactionCategory, TransactionRequest, TransferRequest } from '../../../models/banking.model';

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
    category: TransactionCategory.OTHER
  };

  withdrawForm = {
    amount: 0,
    description: '',
    category: TransactionCategory.OTHER
  };

  transferForm = {
    toAccountId: 0,
    amount: 0,
    description: ''
  };

  // Transaction categories
  incomeCategories = [
    TransactionCategory.SALARY,
    TransactionCategory.INVESTMENT,
    TransactionCategory.REFUND,
    TransactionCategory.OTHER
  ];

  expenseCategories = [
    TransactionCategory.GROCERIES,
    TransactionCategory.UTILITIES,
    TransactionCategory.RENT,
    TransactionCategory.ENTERTAINMENT,
    TransactionCategory.HEALTHCARE,
    TransactionCategory.TRANSPORTATION,
    TransactionCategory.SHOPPING,
    TransactionCategory.DINING,
    TransactionCategory.OTHER
  ];

  loading = false;
  error: string | null = null;
  success: string | null = null;

  constructor(
    private bankingService: BankingService,
    private route: ActivatedRoute,
    private router: Router
  ) { }

  ngOnInit(): void {
    this.route.params.subscribe(params => {
      this.accountId = +params['id'];
      if (this.accountId) {
        this.loadAccount();
        this.loadAccounts();
      }
    });
  }

  loadAccount(): void {
    if (!this.accountId) return;

    this.bankingService.getAccountById(this.accountId).subscribe({
      next: (response) => {
        this.account = response.data;
      },
      error: (error) => {
        this.error = 'Failed to load account: ' + error.message;
      }
    });
  }

  loadAccounts(): void {
    this.bankingService.getAllAccounts().subscribe({
      next: (response) => {
        this.accounts = (response.data || []).filter(acc => acc.id !== this.accountId);
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
      category: this.depositForm.category
    };

    this.bankingService.deposit(this.accountId, request).subscribe({
      next: (response) => {
        this.account = response.data;
        this.success = 'Deposit successful!';
        this.resetDepositForm();
        this.loading = false;
      },
      error: (error) => {
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
      category: this.withdrawForm.category
    };

    this.bankingService.withdraw(this.accountId, request).subscribe({
      next: (response) => {
        this.account = response.data;
        this.success = 'Withdrawal successful!';
        this.resetWithdrawForm();
        this.loading = false;
      },
      error: (error) => {
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
      next: () => {
        this.success = 'Transfer successful!';
        this.resetTransferForm();
        this.loadAccount(); // Reload account to get updated balance
        this.loading = false;
      },
      error: (error) => {
        this.error = error.message;
        this.loading = false;
      }
    });
  }

  resetDepositForm(): void {
    this.depositForm = {
      amount: 0,
      description: '',
      category: TransactionCategory.OTHER
    };
  }

  resetWithdrawForm(): void {
    this.withdrawForm = {
      amount: 0,
      description: '',
      category: TransactionCategory.OTHER
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
