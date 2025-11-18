import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router, Params } from '@angular/router';
import { BankingService } from '../../../services/banking.service';
import { CategoryReport, TransactionType, Account } from '../../../models/banking.model';


@Component({
  selector: 'app-category-reports',
  templateUrl: './category-reports.component.html',
  styleUrls: ['./category-reports.component.css']
})
export class CategoryReportsComponent implements OnInit {
  account: Account | null = null;
  accountId: string | null = null;

  incomeReport: CategoryReport[] = [];
  expenseReport: CategoryReport[] = [];

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
      next: (account: Account) => {
        this.account = account;
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
      next: (categoryReports: CategoryReport[]) => {
        this.incomeReport = categoryReports;
      },
      error: (error: any) => {
        this.error = 'Failed to load income report: ' + error.message;
      }
    });

    this.bankingService.getCategoryReport(this.accountId, TransactionType.WITHDRAWAL).subscribe({
      next: (categoryReports: CategoryReport[]) => {
        this.expenseReport = categoryReports;
        this.loading = false;
      },
      error: (error: any) => {
        this.error = 'Failed to load expense report: ' + error.message;
        this.loading = false;
      }
    });
  }

  getActiveReport(): CategoryReport[] {
    return this.selectedType === TransactionType.DEPOSIT ? this.incomeReport : this.expenseReport;
  }

  getChartData(): { category: string; amount: number; percentage: number; color: string }[] {
    const report = this.getActiveReport();
    if (!report || report.length === 0) return [];

    return report.map((categoryReport) => ({
      category: categoryReport.categoryName,
      amount: categoryReport.totalAmount,
      percentage: categoryReport.percentage,
      color: '#3498db' // Default color, can be customized
    }));
  }

  goBack(): void {
    this.router.navigate(['/banking/dashboard']);
  }
}
