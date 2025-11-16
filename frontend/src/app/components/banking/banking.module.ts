import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule, Routes } from '@angular/router';

import { BankingDashboardComponent } from './dashboard/banking-dashboard.component';
import { TransactionFormsComponent } from './transactions/transaction-forms.component';
import { AccountStatementComponent } from './statement/account-statement.component';
import { CategoryReportsComponent } from './reports/category-reports.component';

const routes: Routes = [
  {
    path: 'dashboard',
    component: BankingDashboardComponent
  },
  {
    path: 'transactions/:id',
    component: TransactionFormsComponent
  },
  {
    path: 'statement/:id',
    component: AccountStatementComponent
  },
  {
    path: 'reports/:id',
    component: CategoryReportsComponent
  },
  {
    path: '',
    redirectTo: 'dashboard',
    pathMatch: 'full'
  }
];

@NgModule({
  declarations: [
    BankingDashboardComponent,
    TransactionFormsComponent,
    AccountStatementComponent,
    CategoryReportsComponent
  ],
  imports: [
    CommonModule,
    FormsModule,
    RouterModule.forChild(routes)
  ]
})
export class BankingModule { }
