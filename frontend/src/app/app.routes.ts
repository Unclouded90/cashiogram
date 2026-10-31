import { Routes } from '@angular/router';

import { Dashboard } from './features/dashboard/dashboard';
import { AccountSettings } from './features/account/account-settings/account-settings';
import { Categories } from './features/categories/categories';

export const routes: Routes = [
  {
    path: '',
    component: Dashboard,
  },
  {
    path: 'settings/account',
    component: AccountSettings,
  },
  {
    path: 'settings/categories',
    component: Categories,
  },
];
