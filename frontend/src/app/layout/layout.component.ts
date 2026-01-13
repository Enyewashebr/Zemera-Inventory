import { Component, OnInit, OnDestroy } from '@angular/core';
import { RouterOutlet, RouterLink, RouterLinkActive } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { Router } from '@angular/router'; // <- import Router

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

  constructor(public auth: AuthService, private router: Router) {} // <- inject Router

  private baseNavItems = [
    { path: '/dashboard', label: 'Dashboard' },
    { path: '/products', label: 'Products' },
    { path: '/purchases', label: 'Purchases' },
    { path: '/orders', label: 'Orders' },
    { path: '/stocks', label: 'Stocks' },
    { path: '/reports', label: 'Reports' }
  ];

  get navItems() {
    // add users menu only for super managers
    if (this.auth.isAdmin()) {
      return [...this.baseNavItems, { path: '/users', label: 'Users' }, { path: '/branches', label: 'Branches' }, { path: '/create-user', label: 'Add User' }];
    }
    return this.baseNavItems;
  }

  // ==========================
  // LOGOUT
  // ==========================
  logout(): void {
    this.auth.logout();
    this.router.navigate(['/login']);
  }

  // ==========================
  // DATETIME
  // ==========================
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
