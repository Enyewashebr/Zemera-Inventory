import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HttpClient, HttpClientModule } from '@angular/common/http';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-user-form',
  standalone: true,
  imports: [CommonModule, FormsModule, HttpClientModule],
  templateUrl: './user-form.component.html',
  styleUrls: ['./user-form.component.css']
})
export class UserFormComponent implements OnInit {

  user = {
    fullName: '',
    phone: '',
    username: '',
    password: '',
    role: 'BRANCH_MANAGER',
    branchId: null as number | null
  };

  
  errorMessage = '';
  loading = false;

  constructor(private http: HttpClient, private router: Router) {}

  branches: { id: number; name: string }[] = [];

ngOnInit() {
  this.http.get<{id:number,name:string}[]>('http://localhost:8080/api/branches')
    .subscribe({
      next: data => this.branches = data,
      error: err => console.error('Failed to load branches', err)
    });
}

  save() {
    this.loading = true;

    this.http.post('http://localhost:8080/api/auth/create', this.user)
      .subscribe({
        next: () => this.router.navigate(['/users']),
        error: err => {
          this.errorMessage = err.error || 'Failed to create user';
          this.loading = false;
        }
      });
  }
}
