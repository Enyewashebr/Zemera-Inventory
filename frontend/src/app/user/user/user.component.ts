import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient, HttpClientModule } from '@angular/common/http';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-users',
  standalone: true,
  imports: [CommonModule, HttpClientModule, FormsModule],
  templateUrl: './user.component.html',
  styleUrls: ['./user.component.css']
})
export class UserComponent implements OnInit {

  users: any[] = [];
  branches: any[] = [];

  editingUserId: number | null = null;

  user = {
    fullName: '',
    username: '',
    email: '',
    phone: '',
    role: 'BRANCH_MANAGER',
    name: ''
  };

  constructor(private http: HttpClient) {}

  ngOnInit(): void {
    this.loadUsers();
    this.loadBranches();
  }

  loadUsers() {
    this.http.get<any[]>('http://localhost:8080/api/users')
      .subscribe(res => this.users = res);
  }

  loadBranches() {
    this.http.get<any[]>('http://localhost:8080/api/branches')
      .subscribe(res => this.branches = res);
  }

  editUser(id: number) {
    const u = this.users.find(x => x.id === id);
    if (!u) return;

    this.editingUserId = id;
    this.user = { ...u };
  }

  cancelEdit() {
    this.editingUserId = null;
    this.user = {
      fullName: '',
      username: '',
      email: '',
      phone: '',
      role: 'BRANCH_MANAGER',
      name: ''
    };
  }

  save() {
    if (this.editingUserId === null) return;

    this.http.put(
      `http://localhost:8080/api/users/${this.editingUserId}`,
      this.user
    ).subscribe({
      next: () => {
        this.loadUsers();
        this.cancelEdit();
      },
      error: err => console.error('Update failed', err)
    });
  }

  deleteUser(id: number) {
    if (!confirm('Delete this user?')) return;

    this.http.delete(`http://localhost:8080/api/users/${id}`)
      .subscribe({
        next: () => this.users = this.users.filter(u => u.id !== id),
        error: err => console.error('Delete failed', err)
      });
  }
}
