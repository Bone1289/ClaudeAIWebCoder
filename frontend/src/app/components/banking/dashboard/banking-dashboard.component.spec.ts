import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { FormsModule } from '@angular/forms';
import { of, throwError } from 'rxjs';

import { BankingDashboardComponent } from './banking-dashboard.component';
import { BankingService } from '../../../services/banking.service';
import { Account, CreateAccountRequest } from '../../../models/banking.model';
import { ApiResponse } from '../../../models/api-response.model';

describe('BankingDashboardComponent', () => {
  let component: BankingDashboardComponent;
  let fixture: ComponentFixture<BankingDashboardComponent>;
  let bankingService: jasmine.SpyObj<BankingService>;

  const mockAccount: Account = {
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
  };

  const mockAccounts: Account[] = [
    mockAccount,
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
  ];

  beforeEach(async () => {
    const bankingServiceSpy = jasmine.createSpyObj('BankingService', [
      'getAllAccounts',
      'createAccount',
      'updateAccount',
      'deleteAccount'
    ]);

    await TestBed.configureTestingModule({
      declarations: [BankingDashboardComponent],
      imports: [HttpClientTestingModule, FormsModule],
      providers: [
        { provide: BankingService, useValue: bankingServiceSpy }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(BankingDashboardComponent);
    component = fixture.componentInstance;
    bankingService = TestBed.inject(BankingService) as jasmine.SpyObj<BankingService>;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  describe('ngOnInit', () => {
    it('should load accounts on initialization', () => {
      const mockResponse: ApiResponse<Account[]> = {
        success: true,
        message: 'Accounts loaded',
        data: mockAccounts
      };

      bankingService.getAllAccounts.and.returnValue(of(mockResponse));

      component.ngOnInit();

      expect(bankingService.getAllAccounts).toHaveBeenCalled();
      expect(component.accounts.length).toBe(2);
      expect(component.loading).toBe(false);
    });

    it('should handle error when loading accounts fails', () => {
      const errorResponse = {
        message: 'Failed to load accounts'
      };

      bankingService.getAllAccounts.and.returnValue(throwError(() => errorResponse));

      component.ngOnInit();

      expect(component.error).toBe('Failed to load accounts: Failed to load accounts');
      expect(component.loading).toBe(false);
    });
  });

  describe('loadAccounts', () => {
    it('should load all accounts successfully', () => {
      const mockResponse: ApiResponse<Account[]> = {
        success: true,
        message: 'Accounts loaded',
        data: mockAccounts
      };

      bankingService.getAllAccounts.and.returnValue(of(mockResponse));

      component.loadAccounts();

      expect(component.accounts).toEqual(mockAccounts);
      expect(component.loading).toBe(false);
      expect(component.error).toBeNull();
    });

    it('should handle empty account list', () => {
      const mockResponse: ApiResponse<Account[]> = {
        success: true,
        message: 'No accounts',
        data: []
      };

      bankingService.getAllAccounts.and.returnValue(of(mockResponse));

      component.loadAccounts();

      expect(component.accounts).toEqual([]);
      expect(component.loading).toBe(false);
    });
  });

  describe('Modal Operations', () => {
    it('should open add modal and reset form', () => {
      component.openAddModal();

      expect(component.showAddModal).toBe(true);
      expect(component.newAccount.firstName).toBe('');
      expect(component.newAccount.lastName).toBe('');
      expect(component.newAccount.nationality).toBe('');
      expect(component.newAccount.accountType).toBe('CHECKING');
      expect(component.filteredCountries).toEqual([]);
      expect(component.showCountryDropdown).toBe(false);
    });

    it('should close add modal', () => {
      component.showAddModal = true;
      component.closeAddModal();

      expect(component.showAddModal).toBe(false);
    });

    it('should open edit modal with account data', () => {
      component.openEditModal(mockAccount);

      expect(component.showEditModal).toBe(true);
      expect(component.editingAccount).toEqual(mockAccount);
      expect(component.editAccountType).toBe('CHECKING');
    });

    it('should close edit modal and clear data', () => {
      component.editingAccount = mockAccount;
      component.showEditModal = true;

      component.closeEditModal();

      expect(component.showEditModal).toBe(false);
      expect(component.editingAccount).toBeNull();
    });

    it('should open delete modal with account', () => {
      component.openDeleteModal(mockAccount);

      expect(component.showDeleteModal).toBe(true);
      expect(component.accountToDelete).toEqual(mockAccount);
    });

    it('should close delete modal and clear data', () => {
      component.accountToDelete = mockAccount;
      component.showDeleteModal = true;

      component.closeDeleteModal();

      expect(component.showDeleteModal).toBe(false);
      expect(component.accountToDelete).toBeNull();
    });
  });

  describe('createAccount', () => {
    it('should create account successfully', (done) => {
      const mockResponse: ApiResponse<Account> = {
        success: true,
        message: 'Account created successfully',
        data: mockAccount
      };

      const loadResponse: ApiResponse<Account[]> = {
        success: true,
        message: 'OK',
        data: mockAccounts
      };

      bankingService.createAccount.and.returnValue(of(mockResponse));
      bankingService.getAllAccounts.and.returnValue(of(loadResponse));

      component.newAccount = {
        firstName: 'John',
        lastName: 'Doe',
        nationality: 'United States',
        accountType: 'CHECKING'
      };

      component.createAccount();

      setTimeout(() => {
        expect(bankingService.createAccount).toHaveBeenCalled();
        expect(component.showAddModal).toBe(false);
        expect(component.successMessage).toBe('Account created successfully!');
        expect(component.loading).toBe(false);
        done();
      }, 100);
    });

    it('should handle error when creating account fails', () => {
      const errorResponse = {
        message: 'Invalid account data'
      };

      bankingService.createAccount.and.returnValue(throwError(() => errorResponse));

      component.newAccount = {
        firstName: 'John',
        lastName: 'Doe',
        nationality: 'United States',
        accountType: 'CHECKING'
      };

      component.createAccount();

      expect(component.error).toBe('Invalid account data');
      expect(component.loading).toBe(false);
    });
  });

  describe('updateAccount', () => {
    it('should update account successfully', (done) => {
      const updatedAccount = { ...mockAccount, accountType: 'SAVINGS' };
      const mockResponse: ApiResponse<Account> = {
        success: true,
        message: 'Account updated successfully',
        data: updatedAccount
      };

      const loadResponse: ApiResponse<Account[]> = {
        success: true,
        message: 'OK',
        data: mockAccounts
      };

      bankingService.updateAccount.and.returnValue(of(mockResponse));
      bankingService.getAllAccounts.and.returnValue(of(loadResponse));

      component.editingAccount = mockAccount;
      component.editAccountType = 'SAVINGS';

      component.updateAccount();

      setTimeout(() => {
        expect(bankingService.updateAccount).toHaveBeenCalledWith(
          mockAccount.id,
          { accountType: 'SAVINGS' }
        );
        expect(component.showEditModal).toBe(false);
        expect(component.successMessage).toBe('Account updated successfully!');
        done();
      }, 100);
    });

    it('should not update if no account is selected', () => {
      component.editingAccount = null;

      component.updateAccount();

      expect(bankingService.updateAccount).not.toHaveBeenCalled();
    });
  });

  describe('deleteAccount', () => {
    it('should delete account successfully', (done) => {
      const mockResponse: ApiResponse<void> = {
        success: true,
        message: 'Account deleted successfully',
        data: null
      };

      const loadResponse: ApiResponse<Account[]> = {
        success: true,
        message: 'OK',
        data: []
      };

      bankingService.deleteAccount.and.returnValue(of(mockResponse));
      bankingService.getAllAccounts.and.returnValue(of(loadResponse));

      component.accountToDelete = mockAccount;

      component.deleteAccount();

      setTimeout(() => {
        expect(bankingService.deleteAccount).toHaveBeenCalledWith(mockAccount.id);
        expect(component.showDeleteModal).toBe(false);
        expect(component.successMessage).toBe('Account deleted successfully!');
        done();
      }, 100);
    });

    it('should not delete if no account is selected', () => {
      component.accountToDelete = null;

      component.deleteAccount();

      expect(bankingService.deleteAccount).not.toHaveBeenCalled();
    });
  });

  describe('Helper Methods', () => {
    it('should select an account', () => {
      component.selectAccount(mockAccount);

      expect(component.selectedAccount).toEqual(mockAccount);
    });

    it('should calculate total balance', () => {
      component.accounts = mockAccounts;

      const total = component.getTotalBalance();

      expect(total).toBe(6000); // 1000 + 5000
    });

    it('should count active accounts', () => {
      component.accounts = mockAccounts;

      const count = component.getActiveAccountsCount();

      expect(count).toBe(2);
    });

    it('should get country flag for valid nationality', () => {
      const flag = component.getCountryFlag('United States');

      expect(flag).toBe('🇺🇸');
    });

    it('should return default flag for unknown nationality', () => {
      const flag = component.getCountryFlag('Unknown Country');

      expect(flag).toBe('🌍');
    });
  });

  describe('Nationality Autocomplete', () => {
    it('should filter countries on input', () => {
      const event = {
        target: { value: 'united' }
      };

      component.onNationalityInput(event);

      expect(component.filteredCountries.length).toBeGreaterThan(0);
      expect(component.showCountryDropdown).toBe(true);
    });

    it('should show all countries when input is empty', () => {
      const event = {
        target: { value: '' }
      };

      component.onNationalityInput(event);

      expect(component.filteredCountries.length).toBe(component.countries.length);
      expect(component.showCountryDropdown).toBe(true);
    });

    it('should filter by country code', () => {
      const event = {
        target: { value: 'US' }
      };

      component.onNationalityInput(event);

      const hasUS = component.filteredCountries.some(c => c.code === 'US');
      expect(hasUS).toBe(true);
    });

    it('should show all countries on focus', () => {
      component.onNationalityFocus();

      expect(component.filteredCountries).toEqual(component.countries);
      expect(component.showCountryDropdown).toBe(true);
    });

    it('should select country', () => {
      const country = { code: 'US', name: 'United States', flag: '🇺🇸' };

      component.selectCountry(country);

      expect(component.newAccount.nationality).toBe('United States');
      expect(component.showCountryDropdown).toBe(false);
    });

    it('should hide dropdown with delay', (done) => {
      component.showCountryDropdown = true;

      component.hideCountryDropdown();

      setTimeout(() => {
        expect(component.showCountryDropdown).toBe(false);
        done();
      }, 250);
    });
  });
});
