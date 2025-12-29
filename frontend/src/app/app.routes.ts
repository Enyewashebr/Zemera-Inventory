import { Routes } from '@angular/router';

import { LoginComponent } from './auth/login/login.component';
import { AuthGuard } from './gaurd/auth.guard';

import { LayoutComponent } from './layout/layout.component';
import { DashboardComponent } from './dashboard/dashboard.component';
import { InventoryComponent } from './inventory/inventory.component';
import { OrdersComponent } from './orders/orders.component';
import { ProductsComponent } from './products/products.component';
import { PurchasesComponent } from './purchases/purchases.component';
import { ReportsComponent } from './reports/reports.component';
import { SettingsComponent } from './settings/settings.component';
import { UserComponent } from './user/user/user.component';
import { AdminGuard } from './gaurd/admin.guard';




export const routes: Routes = [

 {
  path: 'users',
  component: UserComponent,
  canActivate: [AdminGuard]
},

  // 🔓 Public route (NO layout)
  {
    path: 'login',
    component: LoginComponent
  },

  // 🔒 Protected routes (WITH layout)
  {
    path: '',
    component: LayoutComponent,
    canActivate: [AuthGuard],
    children: [
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
      { path: 'dashboard', component: DashboardComponent },
      { path: 'products', component: ProductsComponent },
      { path: 'purchases', component: PurchasesComponent },
      { path: 'orders', component: OrdersComponent },
      { path: 'inventory', component: InventoryComponent },
      { path: 'reports', component: ReportsComponent },
      { path: 'settings', component: SettingsComponent }
    ]
  },

  // fallback
  {
    path: '**',
    redirectTo: 'dashboard'
  }
];
