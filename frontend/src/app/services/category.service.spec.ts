import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { CategoryService, CategoryRequest } from './category.service';
import { Category, CategoryType } from '../models/banking.model';
import { ApiResponse } from '../models/api-response.model';

describe('CategoryService', () => {
  let service: CategoryService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [CategoryService]
    });
    service = TestBed.inject(CategoryService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  describe('loadCategories', () => {
    it('should load all categories', () => {
      const mockCategories: Category[] = [
        {
          id: '1',
          name: 'SALARY',
          description: 'Monthly salary',
          type: CategoryType.INCOME,
          color: '#2ecc71',
          active: true,
          createdAt: new Date()
        },
        {
          id: '2',
          name: 'GROCERIES',
          description: 'Food and supplies',
          type: CategoryType.EXPENSE,
          color: '#e74c3c',
          active: true,
          createdAt: new Date()
        }
      ];

      const mockResponse: ApiResponse<Category[]> = {
        success: true,
        message: 'Categories retrieved successfully',
        data: mockCategories
      };

      service.loadCategories().subscribe(response => {
        expect(response.success).toBe(true);
        expect(response.data.length).toBe(2);
        expect(response.data[0].name).toBe('SALARY');
        expect(response.data[1].name).toBe('GROCERIES');
      });

      const req = httpMock.expectOne(req => req.url.includes('/api/categories'));
      expect(req.request.method).toBe('GET');
      expect(req.request.params.get('activeOnly')).toBe('true');
      req.flush(mockResponse);
    });

    it('should update categories cache when loaded', (done) => {
      const mockCategories: Category[] = [
        {
          id: '1',
          name: 'SALARY',
          description: 'Monthly salary',
          type: CategoryType.INCOME,
          color: '#2ecc71',
          active: true,
          createdAt: new Date()
        }
      ];

      const mockResponse: ApiResponse<Category[]> = {
        success: true,
        message: 'Categories retrieved successfully',
        data: mockCategories
      };

      service.loadCategories().subscribe(() => {
        // Check that the cache was updated
        service.categories$.subscribe(categories => {
          expect(categories.length).toBe(1);
          expect(categories[0].name).toBe('SALARY');
          done();
        });
      });

      const req = httpMock.expectOne(req => req.url.includes('/api/categories'));
      req.flush(mockResponse);
    });

    it('should load categories with activeOnly parameter', () => {
      const mockResponse: ApiResponse<Category[]> = {
        success: true,
        message: 'Categories retrieved successfully',
        data: []
      };

      service.loadCategories(false).subscribe();

      const req = httpMock.expectOne(req => req.url.includes('/api/categories'));
      expect(req.request.params.get('activeOnly')).toBeNull();
      req.flush(mockResponse);
    });
  });

  describe('getCategoriesByType', () => {
    it('should get income categories', () => {
      const mockCategories: Category[] = [
        {
          id: '1',
          name: 'SALARY',
          description: 'Monthly salary',
          type: CategoryType.INCOME,
          color: '#2ecc71',
          active: true,
          createdAt: new Date()
        }
      ];

      const mockResponse: ApiResponse<Category[]> = {
        success: true,
        message: 'Categories retrieved successfully',
        data: mockCategories
      };

      service.getCategoriesByType(CategoryType.INCOME).subscribe(response => {
        expect(response.success).toBe(true);
        expect(response.data.length).toBe(1);
        expect(response.data[0].type).toBe(CategoryType.INCOME);
      });

      const req = httpMock.expectOne(req => req.url.includes('/api/categories'));
      expect(req.request.params.get('type')).toBe(CategoryType.INCOME);
      expect(req.request.params.get('activeOnly')).toBe('true');
      req.flush(mockResponse);
    });

    it('should get expense categories', () => {
      const mockResponse: ApiResponse<Category[]> = {
        success: true,
        message: 'Categories retrieved successfully',
        data: []
      };

      service.getCategoriesByType(CategoryType.EXPENSE, false).subscribe();

      const req = httpMock.expectOne(req => req.url.includes('/api/categories'));
      expect(req.request.params.get('type')).toBe(CategoryType.EXPENSE);
      expect(req.request.params.get('activeOnly')).toBeNull();
      req.flush(mockResponse);
    });
  });

  describe('getCategoryById', () => {
    it('should get category by ID', () => {
      const categoryId = '123';
      const mockCategory: Category = {
        id: categoryId,
        name: 'SALARY',
        description: 'Monthly salary',
        type: CategoryType.INCOME,
        color: '#2ecc71',
        active: true,
        createdAt: new Date()
      };

      const mockResponse: ApiResponse<Category> = {
        success: true,
        message: 'Category retrieved successfully',
        data: mockCategory
      };

      service.getCategoryById(categoryId).subscribe(response => {
        expect(response.success).toBe(true);
        expect(response.data.id).toBe(categoryId);
        expect(response.data.name).toBe('SALARY');
      });

      const req = httpMock.expectOne(`/api/categories/${categoryId}`);
      expect(req.request.method).toBe('GET');
      req.flush(mockResponse);
    });
  });

  describe('createCategory', () => {
    it('should create a new category', () => {
      const categoryRequest: CategoryRequest = {
        name: 'BONUS',
        description: 'Annual bonus',
        type: CategoryType.INCOME,
        color: '#1abc9c'
      };

      const mockCategory: Category = {
        id: '123',
        name: 'BONUS',
        description: 'Annual bonus',
        type: CategoryType.INCOME,
        color: '#1abc9c',
        active: true,
        createdAt: new Date()
      };

      const mockResponse: ApiResponse<Category> = {
        success: true,
        message: 'Category created successfully',
        data: mockCategory
      };

      service.createCategory(categoryRequest).subscribe(response => {
        expect(response.success).toBe(true);
        expect(response.data.name).toBe('BONUS');
      });

      const req = httpMock.expectOne('/api/categories');
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual(categoryRequest);
      req.flush(mockResponse);

      // Verify that loadCategories was called to refresh cache
      const refreshReq = httpMock.expectOne(req => req.url.includes('/api/categories'));
      refreshReq.flush({ success: true, message: 'OK', data: [] });
    });
  });

  describe('updateCategory', () => {
    it('should update a category', () => {
      const categoryId = '123';
      const updateRequest: Partial<CategoryRequest> = {
        name: 'UPDATED_NAME',
        description: 'Updated description'
      };

      const mockCategory: Category = {
        id: categoryId,
        name: 'UPDATED_NAME',
        description: 'Updated description',
        type: CategoryType.INCOME,
        color: '#2ecc71',
        active: true,
        createdAt: new Date()
      };

      const mockResponse: ApiResponse<Category> = {
        success: true,
        message: 'Category updated successfully',
        data: mockCategory
      };

      service.updateCategory(categoryId, updateRequest).subscribe(response => {
        expect(response.success).toBe(true);
        expect(response.data.name).toBe('UPDATED_NAME');
      });

      const req = httpMock.expectOne(`/api/categories/${categoryId}`);
      expect(req.request.method).toBe('PUT');
      expect(req.request.body).toEqual(updateRequest);
      req.flush(mockResponse);

      // Verify that loadCategories was called to refresh cache
      const refreshReq = httpMock.expectOne(req => req.url.includes('/api/categories'));
      refreshReq.flush({ success: true, message: 'OK', data: [] });
    });
  });

  describe('deactivateCategory', () => {
    it('should deactivate a category', () => {
      const categoryId = '123';
      const mockCategory: Category = {
        id: categoryId,
        name: 'SALARY',
        description: 'Monthly salary',
        type: CategoryType.INCOME,
        color: '#2ecc71',
        active: false,
        createdAt: new Date()
      };

      const mockResponse: ApiResponse<Category> = {
        success: true,
        message: 'Category deactivated successfully',
        data: mockCategory
      };

      service.deactivateCategory(categoryId).subscribe(response => {
        expect(response.success).toBe(true);
        expect(response.data.active).toBe(false);
      });

      const req = httpMock.expectOne(`/api/categories/${categoryId}/deactivate`);
      expect(req.request.method).toBe('PATCH');
      req.flush(mockResponse);

      // Verify that loadCategories was called to refresh cache
      const refreshReq = httpMock.expectOne(req => req.url.includes('/api/categories'));
      refreshReq.flush({ success: true, message: 'OK', data: [] });
    });
  });

  describe('deleteCategory', () => {
    it('should delete a category', () => {
      const categoryId = '123';
      const mockResponse: ApiResponse<void> = {
        success: true,
        message: 'Category deleted successfully',
        data: null
      };

      service.deleteCategory(categoryId).subscribe(response => {
        expect(response.success).toBe(true);
      });

      const req = httpMock.expectOne(`/api/categories/${categoryId}`);
      expect(req.request.method).toBe('DELETE');
      req.flush(mockResponse);

      // Verify that loadCategories was called to refresh cache
      const refreshReq = httpMock.expectOne(req => req.url.includes('/api/categories'));
      refreshReq.flush({ success: true, message: 'OK', data: [] });
    });
  });

  describe('Cache Methods', () => {
    it('should get category from cache', () => {
      // First populate the cache
      const mockCategories: Category[] = [
        {
          id: '1',
          name: 'SALARY',
          description: 'Monthly salary',
          type: CategoryType.INCOME,
          color: '#2ecc71',
          active: true,
          createdAt: new Date()
        }
      ];

      const mockResponse: ApiResponse<Category[]> = {
        success: true,
        message: 'OK',
        data: mockCategories
      };

      service.loadCategories().subscribe(() => {
        const category = service.getCategoryFromCache('1');
        expect(category).toBeDefined();
        expect(category?.name).toBe('SALARY');
      });

      const req = httpMock.expectOne(req => req.url.includes('/api/categories'));
      req.flush(mockResponse);
    });

    it('should get income categories from cache', () => {
      const mockCategories: Category[] = [
        {
          id: '1',
          name: 'SALARY',
          description: 'Monthly salary',
          type: CategoryType.INCOME,
          color: '#2ecc71',
          active: true,
          createdAt: new Date()
        },
        {
          id: '2',
          name: 'GROCERIES',
          description: 'Food',
          type: CategoryType.EXPENSE,
          color: '#e74c3c',
          active: true,
          createdAt: new Date()
        }
      ];

      const mockResponse: ApiResponse<Category[]> = {
        success: true,
        message: 'OK',
        data: mockCategories
      };

      service.loadCategories().subscribe(() => {
        const incomeCategories = service.getIncomeCategoriesFromCache();
        expect(incomeCategories.length).toBe(1);
        expect(incomeCategories[0].type).toBe(CategoryType.INCOME);
      });

      const req = httpMock.expectOne(req => req.url.includes('/api/categories'));
      req.flush(mockResponse);
    });

    it('should get expense categories from cache', () => {
      const mockCategories: Category[] = [
        {
          id: '1',
          name: 'SALARY',
          description: 'Monthly salary',
          type: CategoryType.INCOME,
          color: '#2ecc71',
          active: true,
          createdAt: new Date()
        },
        {
          id: '2',
          name: 'GROCERIES',
          description: 'Food',
          type: CategoryType.EXPENSE,
          color: '#e74c3c',
          active: true,
          createdAt: new Date()
        }
      ];

      const mockResponse: ApiResponse<Category[]> = {
        success: true,
        message: 'OK',
        data: mockCategories
      };

      service.loadCategories().subscribe(() => {
        const expenseCategories = service.getExpenseCategoriesFromCache();
        expect(expenseCategories.length).toBe(1);
        expect(expenseCategories[0].type).toBe(CategoryType.EXPENSE);
      });

      const req = httpMock.expectOne(req => req.url.includes('/api/categories'));
      req.flush(mockResponse);
    });
  });
});
