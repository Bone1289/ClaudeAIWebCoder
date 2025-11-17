import { Component, OnInit } from '@angular/core';
import { BankingService } from '../../../services/banking.service';
import { Account, CreateAccountRequest, UpdateAccountRequest } from '../../../models/banking.model';
import { ApiResponse } from '../../../models/api-response.model';

@Component({
  selector: 'app-banking-dashboard',
  templateUrl: './banking-dashboard.component.html',
  styleUrls: ['./banking-dashboard.component.css']
})
export class BankingDashboardComponent implements OnInit {
  accounts: Account[] = [];
  selectedAccount: Account | null = null;
  loading = false;
  error: string | null = null;
  successMessage: string | null = null;

  // Modal states
  showAddModal = false;
  showEditModal = false;
  showDeleteModal = false;

  // Form data
  newAccount: CreateAccountRequest = {
    firstName: '',
    lastName: '',
    nationality: '',
    accountType: 'CHECKING'
  };
  editingAccount: Account | null = null;
  editAccountType: string = '';
  accountToDelete: Account | null = null;

  accountTypes = ['CHECKING', 'SAVINGS', 'CREDIT'];

  countries = [
    { code: 'US', name: 'United States', flag: '🇺🇸' },
    { code: 'GB', name: 'United Kingdom', flag: '🇬🇧' },
    { code: 'CA', name: 'Canada', flag: '🇨🇦' },
    { code: 'AU', name: 'Australia', flag: '🇦🇺' },
    { code: 'DE', name: 'Germany', flag: '🇩🇪' },
    { code: 'FR', name: 'France', flag: '🇫🇷' },
    { code: 'IT', name: 'Italy', flag: '🇮🇹' },
    { code: 'ES', name: 'Spain', flag: '🇪🇸' },
    { code: 'NL', name: 'Netherlands', flag: '🇳🇱' },
    { code: 'BE', name: 'Belgium', flag: '🇧🇪' },
    { code: 'CH', name: 'Switzerland', flag: '🇨🇭' },
    { code: 'AT', name: 'Austria', flag: '🇦🇹' },
    { code: 'SE', name: 'Sweden', flag: '🇸🇪' },
    { code: 'NO', name: 'Norway', flag: '🇳🇴' },
    { code: 'DK', name: 'Denmark', flag: '🇩🇰' },
    { code: 'FI', name: 'Finland', flag: '🇫🇮' },
    { code: 'PL', name: 'Poland', flag: '🇵🇱' },
    { code: 'PT', name: 'Portugal', flag: '🇵🇹' },
    { code: 'GR', name: 'Greece', flag: '🇬🇷' },
    { code: 'IE', name: 'Ireland', flag: '🇮🇪' },
    { code: 'JP', name: 'Japan', flag: '🇯🇵' },
    { code: 'CN', name: 'China', flag: '🇨🇳' },
    { code: 'KR', name: 'South Korea', flag: '🇰🇷' },
    { code: 'IN', name: 'India', flag: '🇮🇳' },
    { code: 'BR', name: 'Brazil', flag: '🇧🇷' },
    { code: 'MX', name: 'Mexico', flag: '🇲🇽' },
    { code: 'AR', name: 'Argentina', flag: '🇦🇷' },
    { code: 'CL', name: 'Chile', flag: '🇨🇱' },
    { code: 'ZA', name: 'South Africa', flag: '🇿🇦' },
    { code: 'EG', name: 'Egypt', flag: '🇪🇬' },
    { code: 'NZ', name: 'New Zealand', flag: '🇳🇿' },
    { code: 'SG', name: 'Singapore', flag: '🇸🇬' },
    { code: 'MY', name: 'Malaysia', flag: '🇲🇾' },
    { code: 'TH', name: 'Thailand', flag: '🇹🇭' },
    { code: 'PH', name: 'Philippines', flag: '🇵🇭' },
    { code: 'ID', name: 'Indonesia', flag: '🇮🇩' },
    { code: 'VN', name: 'Vietnam', flag: '🇻🇳' },
    { code: 'AE', name: 'United Arab Emirates', flag: '🇦🇪' },
    { code: 'SA', name: 'Saudi Arabia', flag: '🇸🇦' },
    { code: 'IL', name: 'Israel', flag: '🇮🇱' },
    { code: 'TR', name: 'Turkey', flag: '🇹🇷' },
    { code: 'RU', name: 'Russia', flag: '🇷🇺' },
    { code: 'UA', name: 'Ukraine', flag: '🇺🇦' },
    { code: 'RO', name: 'Romania', flag: '🇷🇴' },
    { code: 'CZ', name: 'Czech Republic', flag: '🇨🇿' },
    { code: 'HU', name: 'Hungary', flag: '🇭🇺' },
    { code: 'SK', name: 'Slovakia', flag: '🇸🇰' },
    { code: 'BG', name: 'Bulgaria', flag: '🇧🇬' },
    { code: 'HR', name: 'Croatia', flag: '🇭🇷' },
    { code: 'SI', name: 'Slovenia', flag: '🇸🇮' }
  ];

  // Autocomplete state
  filteredCountries: any[] = [];
  showCountryDropdown = false;

  constructor(private bankingService: BankingService) { }

  ngOnInit(): void {
    this.loadAccounts();
  }

  loadAccounts(): void {
    this.loading = true;
    this.error = null;

    this.bankingService.getAllAccounts().subscribe({
      next: (response: ApiResponse<Account[]>) => {
        this.accounts = response.data || [];
        this.loading = false;
      },
      error: (error: any) => {
        this.error = 'Failed to load accounts: ' + error.message;
        this.loading = false;
      }
    });
  }

  selectAccount(account: Account): void {
    this.selectedAccount = account;
  }

  getTotalBalance(): number {
    return this.accounts.reduce((total, account) => total + account.balance, 0);
  }

  getActiveAccountsCount(): number {
    return this.accounts.filter(account => account.status === 'ACTIVE').length;
  }

  getCountryFlag(nationality: string): string {
    const country = this.countries.find(c => c.name === nationality || c.code === nationality);
    return country ? country.flag : '🌍';
  }

  // ========== Autocomplete Methods ==========

  onNationalityInput(event: any): void {
    const value = event.target.value;
    if (value && value.length > 0) {
      this.filteredCountries = this.countries.filter(country =>
        country.name.toLowerCase().includes(value.toLowerCase()) ||
        country.code.toLowerCase().includes(value.toLowerCase())
      );
      this.showCountryDropdown = this.filteredCountries.length > 0;
    } else {
      this.filteredCountries = this.countries;
      this.showCountryDropdown = true;
    }
  }

  onNationalityFocus(): void {
    this.filteredCountries = this.countries;
    this.showCountryDropdown = true;
  }

  selectCountry(country: any): void {
    this.newAccount.nationality = country.name;
    this.showCountryDropdown = false;
  }

  hideCountryDropdown(): void {
    // Delay to allow click event on dropdown item to fire
    setTimeout(() => {
      this.showCountryDropdown = false;
    }, 200);
  }

  // ========== Add Account ==========

  openAddModal(): void {
    this.newAccount = {
      firstName: '',
      lastName: '',
      nationality: '',
      accountType: 'CHECKING'
    };
    this.showAddModal = true;
    this.showCountryDropdown = false;
    this.error = null;
    this.successMessage = null;
  }

  closeAddModal(): void {
    this.showAddModal = false;
  }

  createAccount(): void {
    this.loading = true;
    this.error = null;

    this.bankingService.createAccount(this.newAccount).subscribe({
      next: (response: ApiResponse<Account>) => {
        this.loading = false;
        this.showAddModal = false;
        this.successMessage = 'Account created successfully!';
        this.loadAccounts();
        setTimeout(() => this.successMessage = null, 3000);
      },
      error: (error: any) => {
        this.error = error.message;
        this.loading = false;
      }
    });
  }

  // ========== Edit Account ==========

  openEditModal(account: Account): void {
    this.editingAccount = account;
    this.editAccountType = account.accountType;
    this.showEditModal = true;
    this.error = null;
    this.successMessage = null;
  }

  closeEditModal(): void {
    this.showEditModal = false;
    this.editingAccount = null;
  }

  updateAccount(): void {
    if (!this.editingAccount) return;

    this.loading = true;
    this.error = null;

    const request: UpdateAccountRequest = {
      accountType: this.editAccountType
    };

    this.bankingService.updateAccount(this.editingAccount.id, request).subscribe({
      next: (response: ApiResponse<Account>) => {
        this.loading = false;
        this.showEditModal = false;
        this.successMessage = 'Account updated successfully!';
        this.loadAccounts();
        setTimeout(() => this.successMessage = null, 3000);
      },
      error: (error: any) => {
        this.error = error.message;
        this.loading = false;
      }
    });
  }

  // ========== Delete Account ==========

  openDeleteModal(account: Account): void {
    this.accountToDelete = account;
    this.showDeleteModal = true;
    this.error = null;
    this.successMessage = null;
  }

  closeDeleteModal(): void {
    this.showDeleteModal = false;
    this.accountToDelete = null;
  }

  deleteAccount(): void {
    if (!this.accountToDelete) return;

    this.loading = true;
    this.error = null;

    this.bankingService.deleteAccount(this.accountToDelete.id).subscribe({
      next: (response: ApiResponse<void>) => {
        this.loading = false;
        this.showDeleteModal = false;
        this.successMessage = 'Account deleted successfully!';
        this.loadAccounts();
        setTimeout(() => this.successMessage = null, 3000);
      },
      error: (error: any) => {
        this.error = error.message;
        this.loading = false;
      }
    });
  }
}
