import { CommonModule } from '@angular/common';
import { Component, OnDestroy, OnInit } from '@angular/core';
import { Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';

@Component({
  selector: 'app-layout',
  standalone: true,
  imports: [CommonModule, RouterOutlet, RouterLink, RouterLinkActive],
  templateUrl: './layout.component.html',
  styleUrl: './layout.component.css'
})
export class LayoutComponent implements OnInit, OnDestroy {
  title = 'Zemera Inventory';
  currentDateTime = new Date();
  private timer?: ReturnType<typeof setInterval>;
  sidebarOpen = false;

  navItems = [
    { label: 'Dashboard', path: '/dashboard' },
    { label: 'Products', path: '/products' },
    { label: 'Purchases', path: '/purchases' },
    { label: 'Orders', path: '/orders' },
    { label: 'Inventory', path: '/inventory' },
    { label: 'Reports', path: '/reports' },
    { label: 'Settings', path: '/settings' }
  ];

  constructor(private router: Router) {}

  toggleSidebar(): void {
    this.sidebarOpen = !this.sidebarOpen;
  }

  closeSidebar(): void {
    this.sidebarOpen = false;
  }

  ngOnInit(): void {
    this.timer = setInterval(() => {
      this.currentDateTime = new Date();
    }, 1000);

    if (this.router.url === '/' || this.router.url === '') {
      this.router.navigate(['/dashboard']);
    }
  }

  ngOnDestroy(): void {
    if (this.timer) {
      clearInterval(this.timer);
    }
  }

  get formattedDate(): string {
    return this.currentDateTime.toLocaleDateString('en-US', {
      weekday: 'long',
      year: 'numeric',
      month: 'short',
      day: 'numeric'
    });
  }

  get formattedTime(): string {
    return this.currentDateTime.toLocaleTimeString('en-US', {
      hour: '2-digit',
      minute: '2-digit',
      second: '2-digit'
    });
  }
}





