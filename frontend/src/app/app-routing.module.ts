import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

const routes: Routes = [
  {
    path: '',
    redirectTo: 'banking/dashboard',
    pathMatch: 'full'
  },
  {
    path: 'banking',
    loadChildren: () => import('./components/banking/banking.module').then(m => m.BankingModule)
  },
  {
    path: '**',
    redirectTo: 'banking/dashboard'
  }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
