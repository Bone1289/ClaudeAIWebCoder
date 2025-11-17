import { Component, OnInit } from '@angular/core';
import { CategoryService } from '../../../services/category.service';
import { Category, CategoryType } from '../../../models/banking.model';
import { ApiResponse } from '../../../models/api-response.model';

interface CategoryForm {
  name: string;
  description: string;
  type: CategoryType;
  color: string;
}

@Component({
  selector: 'app-category-management',
  templateUrl: './category-management.component.html',
  styleUrls: ['./category-management.component.css']
})
export class CategoryManagementComponent implements OnInit {
  categories: Category[] = [];
  incomeCategories: Category[] = [];
  expenseCategories: Category[] = [];

  loading = false;
  error: string | null = null;
  successMessage: string | null = null;

  // Modal states
  showAddModal = false;
  showEditModal = false;
  showDeleteModal = false;

  // Form data
  categoryForm: CategoryForm = {
    name: '',
    description: '',
    type: CategoryType.EXPENSE,
    color: '#3498db'
  };

  editingCategory: Category | null = null;
  categoryToDelete: Category | null = null;

  // Available colors
  availableColors = [
    { name: 'Red', value: '#e74c3c' },
    { name: 'Blue', value: '#3498db' },
    { name: 'Green', value: '#2ecc71' },
    { name: 'Orange', value: '#f39c12' },
    { name: 'Purple', value: '#9b59b6' },
    { name: 'Teal', value: '#1abc9c' },
    { name: 'Dark Orange', value: '#e67e22' },
    { name: 'Dark Blue', value: '#2980b9' },
    { name: 'Dark Purple', value: '#8e44ad' },
    { name: 'Dark Red', value: '#c0392b' },
    { name: 'Yellow', value: '#f1c40f' },
    { name: 'Gray', value: '#7f8c8d' }
  ];

  CategoryType = CategoryType;

  constructor(private categoryService: CategoryService) { }

  ngOnInit(): void {
    this.loadCategories();
  }

  loadCategories(): void {
    this.loading = true;
    this.error = null;

    this.categoryService.loadCategories(false).subscribe({
      next: (response: ApiResponse<Category[]>) => {
        this.categories = response.data || [];
        this.filterCategories();
        this.loading = false;
      },
      error: (error: any) => {
        this.error = 'Failed to load categories: ' + error.message;
        this.loading = false;
      }
    });
  }

  filterCategories(): void {
    this.incomeCategories = this.categories.filter((cat: Category) => cat.type === CategoryType.INCOME);
    this.expenseCategories = this.categories.filter((cat: Category) => cat.type === CategoryType.EXPENSE);
  }

  // ========== Add Category ==========

  openAddModal(): void {
    this.categoryForm = {
      name: '',
      description: '',
      type: CategoryType.EXPENSE,
      color: '#3498db'
    };
    this.showAddModal = true;
    this.error = null;
    this.successMessage = null;
  }

  closeAddModal(): void {
    this.showAddModal = false;
  }

  createCategory(): void {
    this.loading = true;
    this.error = null;

    this.categoryService.createCategory(this.categoryForm).subscribe({
      next: (response: ApiResponse<Category>) => {
        this.loading = false;
        this.showAddModal = false;
        this.successMessage = `Category "${this.categoryForm.name}" created successfully!`;
        this.loadCategories();
        setTimeout(() => this.successMessage = null, 3000);
      },
      error: (error: any) => {
        this.error = error.message;
        this.loading = false;
      }
    });
  }

  // ========== Edit Category ==========

  openEditModal(category: Category): void {
    this.editingCategory = category;
    this.categoryForm = {
      name: category.name,
      description: category.description,
      type: category.type,
      color: category.color
    };
    this.showEditModal = true;
    this.error = null;
    this.successMessage = null;
  }

  closeEditModal(): void {
    this.showEditModal = false;
    this.editingCategory = null;
  }

  updateCategory(): void {
    if (!this.editingCategory) return;

    this.loading = true;
    this.error = null;

    this.categoryService.updateCategory(this.editingCategory.id, this.categoryForm).subscribe({
      next: (response: ApiResponse<Category>) => {
        this.loading = false;
        this.showEditModal = false;
        this.successMessage = 'Category updated successfully!';
        this.loadCategories();
        setTimeout(() => this.successMessage = null, 3000);
      },
      error: (error: any) => {
        this.error = error.message;
        this.loading = false;
      }
    });
  }

  // ========== Delete Category ==========

  openDeleteModal(category: Category): void {
    this.categoryToDelete = category;
    this.showDeleteModal = true;
    this.error = null;
    this.successMessage = null;
  }

  closeDeleteModal(): void {
    this.showDeleteModal = false;
    this.categoryToDelete = null;
  }

  deleteCategory(): void {
    if (!this.categoryToDelete) return;

    this.loading = true;
    this.error = null;

    this.categoryService.deleteCategory(this.categoryToDelete.id).subscribe({
      next: (response: ApiResponse<void>) => {
        this.loading = false;
        this.showDeleteModal = false;
        this.successMessage = 'Category deleted successfully!';
        this.loadCategories();
        setTimeout(() => this.successMessage = null, 3000);
      },
      error: (error: any) => {
        this.error = error.message;
        this.loading = false;
      }
    });
  }

  // ========== Toggle Active Status ==========

  toggleCategoryStatus(category: Category): void {
    if (category.active) {
      // Deactivate
      this.categoryService.deactivateCategory(category.id).subscribe({
        next: () => {
          this.successMessage = `Category "${category.name}" deactivated!`;
          this.loadCategories();
          setTimeout(() => this.successMessage = null, 3000);
        },
        error: (error: any) => {
          this.error = error.message;
        }
      });
    }
  }

  getActiveCount(type: CategoryType): number {
    return this.categories.filter((cat: Category) => cat.type === type && cat.active).length;
  }

  getInactiveCount(type: CategoryType): number {
    return this.categories.filter((cat: Category) => cat.type === type && !cat.active).length;
  }
}
