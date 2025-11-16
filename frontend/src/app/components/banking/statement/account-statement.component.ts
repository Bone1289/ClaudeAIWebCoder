import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router, Params } from '@angular/router';
import { BankingService } from '../../../services/banking.service';
import { AccountStatement } from '../../../models/banking.model';
import { ApiResponse } from '../../../models/api-response.model';

@Component({
  selector: 'app-account-statement',
  templateUrl: './account-statement.component.html',
  styleUrls: ['./account-statement.component.css']
})
export class AccountStatementComponent implements OnInit {
  statement: AccountStatement | null = null;
  accountId: number | null = null;

  // Date range
  startDate: string = '';
  endDate: string = '';

  loading = false;
  error: string | null = null;
  currentDate = new Date();

  constructor(
    private bankingService: BankingService,
    private route: ActivatedRoute,
    private router: Router
  ) { }

  ngOnInit(): void {
    this.route.params.subscribe((params: Params) => {
      this.accountId = +params['id'];
      this.setDefaultDateRange();
    });
  }

  setDefaultDateRange(): void {
    const today = new Date();
    const firstDayOfMonth = new Date(today.getFullYear(), today.getMonth(), 1);

    this.endDate = this.formatDate(today);
    this.startDate = this.formatDate(firstDayOfMonth);
  }

  formatDate(date: Date): string {
    return date.toISOString().split('T')[0];
  }

  loadStatement(): void {
    if (!this.accountId || !this.startDate || !this.endDate) return;

    this.loading = true;
    this.error = null;

    const start = new Date(this.startDate).toISOString();
    const end = new Date(this.endDate + 'T23:59:59').toISOString();

    this.bankingService.getAccountStatement(this.accountId, start, end).subscribe({
      next: (response: ApiResponse<AccountStatement>) => {
        this.statement = response.data;
        this.loading = false;
      },
      error: (error: any) => {
        this.error = 'Failed to load statement: ' + error.message;
        this.loading = false;
      }
    });
  }

  printStatement(): void {
    window.print();
  }

  goBack(): void {
    this.router.navigate(['/banking/dashboard']);
  }
}
