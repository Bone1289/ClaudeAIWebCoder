import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { BankingService } from './banking.service';
import { Account, CreateAccountRequest, UpdateAccountRequest, TransactionRequest } from '../models/banking.model';
import { ApiResponse } from '../models/api-response.model';

describe('BankingService', () => {
  let service: BankingService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [BankingService]
    });
    service = TestBed.inject(BankingService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify(); // Verify that no unmatched requests are outstanding
  });

  describe('Account Operations', () => {
    it('should be created', () => {
      expect(service).toBeTruthy();
    });

    it('should create an account', () => {
      const request: CreateAccountRequest = {
        firstName: 'John',
        lastName: 'Doe',
        nationality: 'United States',
        accountType: 'CHECKING'
      };

      const mockResponse: ApiResponse<Account> = {
        success: true,
        message: 'Account created successfully',
        data: {
          id: '123e4567-e89b-12d3-a456-426614174000',
          accountNumber: 'ACC001',
          firstName: 'John',
          lastName: 'Doe',
          nationality: 'United States',
          accountType: 'CHECKING',
          balance: 0,
          status: 'ACTIVE',
          createdAt: new Date(),
          updatedAt: new Date()
        }
      };

      service.createAccount(request).subscribe(response => {
        expect(response.success).toBe(true);
        expect(response.data.firstName).toBe('John');
        expect(response.data.lastName).toBe('Doe');
        expect(response.data.accountType).toBe('CHECKING');
      });

      const req = httpMock.expectOne('/api/banking/accounts');
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual(request);
      req.flush(mockResponse);
    });

    it('should get all accounts', () => {
      const mockResponse: ApiResponse<Account[]> = {
        success: true,
        message: 'Accounts retrieved successfully',
        data: [
          {
            id: '123e4567-e89b-12d3-a456-426614174000',
            accountNumber: 'ACC001',
            firstName: 'John',
            lastName: 'Doe',
            nationality: 'United States',
            accountType: 'CHECKING',
            balance: 1000,
            status: 'ACTIVE',
            createdAt: new Date(),
            updatedAt: new Date()
          },
          {
            id: '123e4567-e89b-12d3-a456-426614174001',
            accountNumber: 'ACC002',
            firstName: 'Jane',
            lastName: 'Smith',
            nationality: 'United Kingdom',
            accountType: 'SAVINGS',
            balance: 5000,
            status: 'ACTIVE',
            createdAt: new Date(),
            updatedAt: new Date()
          }
        ]
      };

      service.getAllAccounts().subscribe(response => {
        expect(response.success).toBe(true);
        expect(response.data.length).toBe(2);
        expect(response.data[0].firstName).toBe('John');
        expect(response.data[1].firstName).toBe('Jane');
      });

      const req = httpMock.expectOne('/api/banking/accounts');
      expect(req.request.method).toBe('GET');
      req.flush(mockResponse);
    });

    it('should get account by ID', () => {
      const accountId = '123e4567-e89b-12d3-a456-426614174000';
      const mockResponse: ApiResponse<Account> = {
        success: true,
        message: 'Account retrieved successfully',
        data: {
          id: accountId,
          accountNumber: 'ACC001',
          firstName: 'John',
          lastName: 'Doe',
          nationality: 'United States',
          accountType: 'CHECKING',
          balance: 1000,
          status: 'ACTIVE',
          createdAt: new Date(),
          updatedAt: new Date()
        }
      };

      service.getAccountById(accountId).subscribe(response => {
        expect(response.success).toBe(true);
        expect(response.data.id).toBe(accountId);
        expect(response.data.firstName).toBe('John');
      });

      const req = httpMock.expectOne(`/api/banking/accounts/${accountId}`);
      expect(req.request.method).toBe('GET');
      req.flush(mockResponse);
    });

    it('should update account', () => {
      const accountId = '123e4567-e89b-12d3-a456-426614174000';
      const updateRequest: UpdateAccountRequest = {
        accountType: 'SAVINGS'
      };

      const mockResponse: ApiResponse<Account> = {
        success: true,
        message: 'Account updated successfully',
        data: {
          id: accountId,
          accountNumber: 'ACC001',
          firstName: 'John',
          lastName: 'Doe',
          nationality: 'United States',
          accountType: 'SAVINGS',
          balance: 1000,
          status: 'ACTIVE',
          createdAt: new Date(),
          updatedAt: new Date()
        }
      };

      service.updateAccount(accountId, updateRequest).subscribe(response => {
        expect(response.success).toBe(true);
        expect(response.data.accountType).toBe('SAVINGS');
      });

      const req = httpMock.expectOne(`/api/banking/accounts/${accountId}`);
      expect(req.request.method).toBe('PUT');
      expect(req.request.body).toEqual(updateRequest);
      req.flush(mockResponse);
    });

    it('should delete account', () => {
      const accountId = '123e4567-e89b-12d3-a456-426614174000';
      const mockResponse: ApiResponse<void> = {
        success: true,
        message: 'Account deleted successfully',
        data: null
      };

      service.deleteAccount(accountId).subscribe(response => {
        expect(response.success).toBe(true);
        expect(response.message).toBe('Account deleted successfully');
      });

      const req = httpMock.expectOne(`/api/banking/accounts/${accountId}`);
      expect(req.request.method).toBe('DELETE');
      req.flush(mockResponse);
    });
  });

  describe('Transaction Operations', () => {
    it('should deposit money', () => {
      const accountId = '123e4567-e89b-12d3-a456-426614174000';
      const depositRequest: TransactionRequest = {
        amount: 500
      };

      const mockResponse: ApiResponse<Account> = {
        success: true,
        message: 'Deposit successful',
        data: {
          id: accountId,
          accountNumber: 'ACC001',
          firstName: 'John',
          lastName: 'Doe',
          nationality: 'United States',
          accountType: 'CHECKING',
          balance: 1500,
          status: 'ACTIVE',
          createdAt: new Date(),
          updatedAt: new Date()
        }
      };

      service.deposit(accountId, depositRequest).subscribe(response => {
        expect(response.success).toBe(true);
        expect(response.data.balance).toBe(1500);
      });

      const req = httpMock.expectOne(`/api/banking/accounts/${accountId}/deposit`);
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual(depositRequest);
      req.flush(mockResponse);
    });

    it('should withdraw money', () => {
      const accountId = '123e4567-e89b-12d3-a456-426614174000';
      const withdrawRequest: TransactionRequest = {
        amount: 300
      };

      const mockResponse: ApiResponse<Account> = {
        success: true,
        message: 'Withdrawal successful',
        data: {
          id: accountId,
          accountNumber: 'ACC001',
          firstName: 'John',
          lastName: 'Doe',
          nationality: 'United States',
          accountType: 'CHECKING',
          balance: 700,
          status: 'ACTIVE',
          createdAt: new Date(),
          updatedAt: new Date()
        }
      };

      service.withdraw(accountId, withdrawRequest).subscribe(response => {
        expect(response.success).toBe(true);
        expect(response.data.balance).toBe(700);
      });

      const req = httpMock.expectOne(`/api/banking/accounts/${accountId}/withdraw`);
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual(withdrawRequest);
      req.flush(mockResponse);
    });
  });

  describe('Error Handling', () => {
    it('should handle HTTP errors', () => {
      const request: CreateAccountRequest = {
        firstName: 'John',
        lastName: 'Doe',
        nationality: 'United States',
        accountType: 'INVALID'
      };

      service.createAccount(request).subscribe({
        next: () => fail('should have failed with 400 error'),
        error: (error) => {
          expect(error).toBeTruthy();
        }
      });

      const req = httpMock.expectOne('/api/banking/accounts');
      req.flush('Invalid account type', { status: 400, statusText: 'Bad Request' });
    });

    it('should handle network errors', () => {
      service.getAllAccounts().subscribe({
        next: () => fail('should have failed with network error'),
        error: (error) => {
          expect(error).toBeTruthy();
        }
      });

      const req = httpMock.expectOne('/api/banking/accounts');
      req.error(new ProgressEvent('Network error'));
    });
  });
});
