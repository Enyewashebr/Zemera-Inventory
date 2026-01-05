import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { AuthService } from '../services/auth.service';
import { Router } from '@angular/router';
@Component({
  selector: 'app-login',
  standalone: true,
  imports: [FormsModule, CommonModule],
  templateUrl: './login.component.html',
})
export class LoginComponent {
  username: string = '';
  password: string = '';
  loading: boolean = false;
  error: string = '';

  constructor(private auth: AuthService, private router: Router) {}

 login() {
  this.loading = true;
  this.error = '';

  this.auth.login(this.username, this.password).subscribe({
    next: res => {
      this.loading = false;

      if (res.role === 'SUPER_MANAGER') {
        this.router.navigate(['/dashboard/super']);
      } else {
        this.router.navigate(['/dashboard/branch']);
      }
    },
    error: err => {
      this.loading = false;
      this.error = 'Login failed';
      console.error('Login error:', err);
    }
  });
}



}
