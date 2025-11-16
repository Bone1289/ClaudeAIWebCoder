import { Component, OnInit } from '@angular/core';
import { ApiService } from '../../services/api.service';
import { User } from '../../models/user.model';
import { ApiResponse } from '../../models/api-response.model';

@Component({
  selector: 'app-users',
  templateUrl: './users.component.html',
  styleUrls: ['./users.component.css']
})
export class UsersComponent implements OnInit {
  users: User[] = [];
  loading: boolean = false;
  error: string = '';
  successMessage: string = '';

  // Form fields
  isEditing: boolean = false;
  currentUser: User = this.getEmptyUser();

  constructor(private apiService: ApiService) {}

  ngOnInit(): void {
    this.loadUsers();
  }

  /**
   * Load all users from the API
   */
  loadUsers(): void {
    this.loading = true;
    this.error = '';

    this.apiService.getAllUsers().subscribe({
      next: (response: ApiResponse<User[]>) => {
        this.users = response.data;
        this.loading = false;
      },
      error: (err: any) => {
        this.error = 'Failed to load users: ' + err.message;
        this.loading = false;
      }
    });
  }

  /**
   * Create or update user
   */
  saveUser(): void {
    if (!this.validateUser()) {
      return;
    }

    this.loading = true;
    this.error = '';
    this.successMessage = '';

    if (this.isEditing && this.currentUser.id) {
      // Update existing user
      this.apiService.updateUser(this.currentUser.id, this.currentUser).subscribe({
        next: (response: ApiResponse<User>) => {
          this.successMessage = response.message;
          this.loadUsers();
          this.resetForm();
          this.loading = false;
        },
        error: (err: any) => {
          this.error = 'Failed to update user: ' + err.message;
          this.loading = false;
        }
      });
    } else {
      // Create new user
      this.apiService.createUser(this.currentUser).subscribe({
        next: (response: ApiResponse<User>) => {
          this.successMessage = response.message;
          this.loadUsers();
          this.resetForm();
          this.loading = false;
        },
        error: (err: any) => {
          this.error = 'Failed to create user: ' + err.message;
          this.loading = false;
        }
      });
    }
  }

  /**
   * Edit user
   */
  editUser(user: User): void {
    this.isEditing = true;
    this.currentUser = { ...user };
    this.error = '';
    this.successMessage = '';
  }

  /**
   * Delete user
   */
  deleteUser(id: number | undefined): void {
    if (!id) return;

    if (!confirm('Are you sure you want to delete this user?')) {
      return;
    }

    this.loading = true;
    this.error = '';
    this.successMessage = '';

    this.apiService.deleteUser(id).subscribe({
      next: (response: ApiResponse<void>) => {
        this.successMessage = response.message;
        this.loadUsers();
        this.loading = false;
      },
      error: (err: any) => {
        this.error = 'Failed to delete user: ' + err.message;
        this.loading = false;
      }
    });
  }

  /**
   * Reset form
   */
  resetForm(): void {
    this.currentUser = this.getEmptyUser();
    this.isEditing = false;
    this.error = '';
  }

  /**
   * Validate user input
   */
  private validateUser(): boolean {
    if (!this.currentUser.name || this.currentUser.name.trim() === '') {
      this.error = 'Name is required';
      return false;
    }

    if (!this.currentUser.email || this.currentUser.email.trim() === '') {
      this.error = 'Email is required';
      return false;
    }

    if (!this.currentUser.role || this.currentUser.role.trim() === '') {
      this.error = 'Role is required';
      return false;
    }

    return true;
  }

  /**
   * Get empty user object
   */
  private getEmptyUser(): User {
    return {
      name: '',
      email: '',
      role: 'USER'
    };
  }
}
