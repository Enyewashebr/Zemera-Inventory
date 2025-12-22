import { Routes } from '@angular/router';
import { DashboardComponent } from './dashboard/dashboard.component';
import { LayoutComponent } from './layout/layout.component';
import { ProductsComponent } from './products/products.component';

export const routes: Routes = [
  {
    path: '',
    component: LayoutComponent,
    children: [
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
      { path: 'dashboard', component: DashboardComponent },
      { path: 'products', component: ProductsComponent },
      // Temporary placeholders until dedicated components are created
      { path: 'purchases', component: DashboardComponent },
      { path: 'orders', component: DashboardComponent },
      { path: 'inventory', component: DashboardComponent },
      { path: 'reports', component: DashboardComponent },
      { path: 'settings', component: DashboardComponent }
    ]
  }
];

