import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule],
  template: `<p>Redirecting...</p>`
})
export class DashboardComponent implements OnInit {

  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    const role = this.authService.getUserRole();

    if (role === 'SUPER_MANAGER') {
      this.router.navigate(['/dashboard/super']);
    } else if (role === 'BRANCH_MANAGER') {
      this.router.navigate(['/dashboard/branch']);
    } else {
      this.router.navigate(['/login']);
    }
  }
}
