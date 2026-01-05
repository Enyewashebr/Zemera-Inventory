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
  branches: { id: number; name: string }[] = [];

  editingUserId: number | null = null;
  user = {
    fullName: '',
    username: '',
    email: '',
    phone: '',
    role: 'BRANCH_MANAGER',
    branchId: 0,
    branchName: ''
  };

  currentUserRole: string = 'BRANCH_MANAGER'; // <-- simulate logged-in user's role

  constructor(private http: HttpClient) {}

  ngOnInit(): void {
    this.loadBranches();
    this.loadUsers();
  }

  loadBranches() {
    this.http.get<{ id: number; name: string }[]>('http://localhost:8080/api/branches')
      .subscribe(data => {
        if (this.currentUserRole === 'SUPER_MANAGER') {
          // SUPER_MANAGER sees all branches
          this.branches = data;
        } else {
          // other roles may filter branches if needed
          this.branches = data; // or filter based on branchId
        }
      });
  }

  loadUsers() {
    this.http.get<any[]>('http://localhost:8080/api/users')
      .subscribe(data => this.users = data);
  }

  editUser(id: number) {
    const u = this.users.find(x => x.id === id);
    if (!u) return;

    this.editingUserId = id;
    this.user = {
      fullName: u.fullName,
      username: u.username,
      email: u.email,
      phone: u.phone,
      role: u.role,
      branchId: u.branchId,
      branchName: u.branch_name || u.name || '' // map backend property
    };
  }

  cancelEdit() {
    this.editingUserId = null;
    this.user = {
      fullName: '',
      username: '',
      email: '',
      phone: '',
      role: 'BRANCH_MANAGER',
      branchId: 0,
      branchName: ''
    };
  }

  save() {
    if (this.editingUserId === null) return;

    // Map form to backend payload
    const payload = {
      fullName: this.user.fullName,
      username: this.user.username,
      email: this.user.email,
      phone: this.user.phone,
      role: this.user.role,
      branch_name: this.user.branchName, // <-- backend expects 'name' for branch_name
      branch_id: this.user.branchId
    };

    this.http.put(`http://localhost:8080/api/users/${this.editingUserId}`, payload)
      .subscribe({
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

  getBranchName(branchId: number) {
    const branch = this.branches.find(b => b.id === branchId);
    return branch ? branch.name : '-';
  }
}
