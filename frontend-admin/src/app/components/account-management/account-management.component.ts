import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AdminService } from '../../services/admin.service';
import { Account } from '../../models/account.model';

@Component({
  selector: 'app-account-management',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './account-management.component.html',
  styleUrls: ['./account-management.component.css']
})
export class AccountManagementComponent implements OnInit {
  accounts: Account[] = [];
  isLoading = false;
  errorMessage = '';

  constructor(private adminService: AdminService) {}

  ngOnInit(): void {
    this.loadAccounts();
  }

  loadAccounts(): void {
    this.isLoading = true;
    this.errorMessage = '';

    this.adminService.getAllAccounts().subscribe({
      next: (response) => {
        if (response.success) {
          this.accounts = response.data;
        }
        this.isLoading = false;
      },
      error: (error) => {
        this.errorMessage = error.error?.message || 'Failed to load accounts';
        this.isLoading = false;
      }
    });
  }

  getAccountTypeColor(type: string): string {
    switch (type) {
      case 'CHECKING': return '#007bff';
      case 'SAVINGS': return '#28a745';
      case 'CREDIT': return '#dc3545';
      case 'DEBIT': return '#ffc107';
      default: return '#6c757d';
    }
  }
}
