import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { User } from '../../model/user.model';

@Component({
  selector: 'app-user-form',
  templateUrl: './user-form.component.html',
  standalone: true,
  imports: [FormsModule],
})
export class UserFormComponent {

  user: User = {
    username: '',
    password: '',
    role: 'BRANCH_MANAGER',
    branchId: 1,
    active: true
  };

  loading = false;
  errorMessage = '';
  successMessage = '';

  constructor(private http: HttpClient, private router: Router) {}

  save() {
    this.loading = true;
    this.errorMessage = '';
    this.successMessage = '';

    // Call backend API
    this.http.post('http://localhost:8080/api/auth/create', this.user)
      .subscribe({
        next: (res: any) => {
          console.log('User created:', res);
          this.successMessage = `User ${res.username} created successfully!`;
          this.loading = false;
          // Optionally navigate to user list
          this.router.navigate(['/users']);
        },
        error: (err) => {
          console.error('Error creating user:', err);
          this.errorMessage = err.error || 'Failed to create user';
          this.loading = false;
        }
      });
  }
}
