import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router, Params } from '@angular/router';
import { BankingService } from '../../../services/banking.service';
import { CategoryReport, TransactionType, Account } from '../../../models/banking.model';
import { ApiResponse } from '../../../models/api-response.model';

@Component({
  selector: 'app-category-reports',
  templateUrl: './category-reports.component.html',
  styleUrls: ['./category-reports.component.css']
})
export class CategoryReportsComponent implements OnInit {
  account: Account | null = null;
  accountId: string | null = null;

  incomeReport: CategoryReport | null = null;
  expenseReport: CategoryReport | null = null;

  selectedType: TransactionType = TransactionType.WITHDRAWAL;
  transactionTypes = TransactionType;

  loading = false;
  error: string | null = null;

  constructor(
    private bankingService: BankingService,
    private route: ActivatedRoute,
    private router: Router
  ) { }

  ngOnInit(): void {
    this.route.params.subscribe((params: Params) => {
      this.accountId = params['id'];
      if (this.accountId) {
        this.loadAccount();
        this.loadReports();
      }
    });
  }

  loadAccount(): void {
    if (!this.accountId) return;

    this.bankingService.getAccountById(this.accountId).subscribe({
      next: (response: ApiResponse<Account>) => {
        this.account = response.data;
      },
      error: () => {
        // Silently fail - not critical
      }
    });
  }

  loadReports(): void {
    if (!this.accountId) return;

    this.loading = true;
    this.error = null;

    // Load both reports
    this.bankingService.getCategoryReport(this.accountId, TransactionType.DEPOSIT).subscribe({
      next: (response: ApiResponse<CategoryReport>) => {
        this.incomeReport = response.data;
      },
      error: (error: any) => {
        this.error = 'Failed to load income report: ' + error.message;
      }
    });

    this.bankingService.getCategoryReport(this.accountId, TransactionType.WITHDRAWAL).subscribe({
      next: (response: ApiResponse<CategoryReport>) => {
        this.expenseReport = response.data;
        this.loading = false;
      },
      error: (error: any) => {
        this.error = 'Failed to load expense report: ' + error.message;
        this.loading = false;
      }
    });
  }

  getActiveReport(): CategoryReport | null {
    return this.selectedType === TransactionType.DEPOSIT ? this.incomeReport : this.expenseReport;
  }

  getChartData(): { category: string; amount: number; percentage: number; color: string }[] {
    const report = this.getActiveReport();
    if (!report) return [];

    return report.categories.map((cat) => ({
      category: cat.category.name,
      amount: cat.amount,
      percentage: cat.percentage,
      color: cat.category.color
    }));
  }

  goBack(): void {
    this.router.navigate(['/banking/dashboard']);
  }
}
