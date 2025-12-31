import { Routes } from '@angular/router';
import { LoginComponent } from './auth/login.component';
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
import { UserFormComponent } from './user/user-form/user-form.component';
import { SuperDashboardComponent } from './dashboard/super-dashboard/super-dashboard.component';
import { BranchDashboardComponent } from './dashboard/branch-dashboard/branch-dashboard.component';

export const routes: Routes = [
  { path: 'login', component: LoginComponent },

  {
    path: '',
    component: LayoutComponent,
    canActivate: [AuthGuard],
    children: [
      // role-based dashboards
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
      { path: 'dashboard', component: DashboardComponent },
      { path: 'dashboard/super', component: SuperDashboardComponent },
      { path: 'dashboard/branch', component: BranchDashboardComponent },

      // regular menu items
      { path: 'products', component: ProductsComponent },
      { path: 'purchases', component: PurchasesComponent },
      { path: 'orders', component: OrdersComponent },
      { path: 'inventory', component: InventoryComponent },
      { path: 'reports', component: ReportsComponent },
      { path: 'settings', component: SettingsComponent },

      // SUPER_MANAGER only
      { path: 'users', component: UserComponent, canActivate: [AuthGuard], data: { roles: ['SUPER_MANAGER'] } },
      { path: 'create-user', component: UserFormComponent, canActivate: [AuthGuard], data: { roles: ['SUPER_MANAGER'] } }
    ]
  },

  { path: '**', redirectTo: '/login' }
];
