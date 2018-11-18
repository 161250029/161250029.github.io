import { NgModule }             from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { LoginComponent }   from './login/login.component';
import { AccountSummaryComponent }   from './account-summary/account-summary.component';
// import { AccountSummaryComponent }      from './heroes/heroes.component';

const routes: Routes = [
  // { path: 'account-summary', component: AccountSummaryComponent },
  { path: 'login', component: LoginComponent },
  { path: 'account-summary', component: AccountSummaryComponent },
  { path: '', redirectTo: '/login', pathMatch: 'full' },
  { path: '**', redirectTo: '/account-summary' }
];

@NgModule({
  imports: [ RouterModule.forRoot(routes) ],
  exports: [ RouterModule ]
})
export class AppRoutingModule {}
