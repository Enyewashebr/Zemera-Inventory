import { Component, OnInit, OnDestroy } from '@angular/core';
import { RouterOutlet, RouterLink, RouterLinkActive } from '@angular/router';
import { AuthService } from '../services/auth.service';

@Component({
  selector: 'app-layout',
  standalone: true,
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  templateUrl: './layout.component.html',
  styleUrl: './layout.component.css'
})
export class LayoutComponent implements OnInit, OnDestroy {

  title = 'Cafeteria Management System';
  formattedDate = '';
  formattedTime = '';
  private timer!: number;

  constructor(public auth: AuthService) {}

  private baseNavItems = [
    { path: '/dashboard', label: 'Dashboard' },
    { path: '/products', label: 'Products' },
    { path: '/purchases', label: 'Purchases' },
    { path: '/orders', label: 'Orders' },
    { path: '/inventory', label: 'Inventory' },
    { path: '/reports', label: 'Reports' }
  ];

  get navItems() {
    return this.auth.isAdmin()
      ? [...this.baseNavItems, { path: '/users', label: 'Users' }]
      : this.baseNavItems;
  }

  ngOnInit(): void {
    this.updateDateTime();
    this.timer = window.setInterval(() => this.updateDateTime(), 1000);
  }

  ngOnDestroy(): void {
    clearInterval(this.timer);
  }

  private updateDateTime(): void {
    const now = new Date();
    this.formattedDate = now.toLocaleDateString();
    this.formattedTime = now.toLocaleTimeString();
  }
}
