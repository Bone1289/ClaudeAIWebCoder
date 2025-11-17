import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AdminService } from '../../services/admin.service';
import { User } from '../../models/user.model';

@Component({
  selector: 'app-user-management',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './user-management.component.html',
  styleUrls: ['./user-management.component.css']
})
export class UserManagementComponent implements OnInit {
  users: User[] = [];
  isLoading = false;
  errorMessage = '';

  constructor(private adminService: AdminService) {}

  ngOnInit(): void {
    this.loadUsers();
  }

  loadUsers(): void {
    this.isLoading = true;
    this.errorMessage = '';

    this.adminService.getAllUsers().subscribe({
      next: (response) => {
        if (response.success) {
          this.users = response.data;
        }
        this.isLoading = false;
      },
      error: (error) => {
        this.errorMessage = error.error?.message || 'Failed to load users';
        this.isLoading = false;
      }
    });
  }

  suspendUser(user: User): void {
    if (!confirm(`Are you sure you want to suspend ${user.username}?`)) {
      return;
    }

    this.adminService.suspendUser(user.id).subscribe({
      next: () => {
        this.loadUsers();
      },
      error: (error) => {
        alert('Failed to suspend user: ' + (error.error?.message || 'Unknown error'));
      }
    });
  }

  activateUser(user: User): void {
    this.adminService.activateUser(user.id).subscribe({
      next: () => {
        this.loadUsers();
      },
      error: (error) => {
        alert('Failed to activate user: ' + (error.error?.message || 'Unknown error'));
      }
    });
  }

  lockUser(user: User): void {
    if (!confirm(`Are you sure you want to lock ${user.username}?`)) {
      return;
    }

    this.adminService.lockUser(user.id).subscribe({
      next: () => {
        this.loadUsers();
      },
      error: (error) => {
        alert('Failed to lock user: ' + (error.error?.message || 'Unknown error'));
      }
    });
  }

  deleteUser(user: User): void {
    if (!confirm(`Are you sure you want to DELETE ${user.username}? This action cannot be undone.`)) {
      return;
    }

    this.adminService.deleteUser(user.id).subscribe({
      next: () => {
        this.loadUsers();
      },
      error: (error) => {
        alert('Failed to delete user: ' + (error.error?.message || 'Unknown error'));
      }
    });
  }

  getStatusColor(status: string): string {
    switch (status) {
      case 'ACTIVE': return '#28a745';
      case 'SUSPENDED': return '#ffc107';
      case 'LOCKED': return '#dc3545';
      default: return '#6c757d';
    }
  }
}
