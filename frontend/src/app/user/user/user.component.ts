import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient, HttpClientModule } from '@angular/common/http';
import { User } from '../../model/user.model';
import { Router } from '@angular/router';





@Component({
  selector: 'app-users',
  standalone: true,
  imports: [CommonModule, HttpClientModule],
  templateUrl: './user.component.html',
  styleUrls: ['./user.component.css']
})
export class UserComponent implements OnInit {

  users: User[] = [];
  branches: any[] = [];
  constructor(private http: HttpClient, private router: Router) { }

 

  ngOnInit(): void {
    this.loadBranches();
    this.loadUsers();
  }

  loadUsers() {
    this.http.get<User[]>('http://localhost:8080/api/users')
      .subscribe(res => this.users = res);
  }

  loadBranches() {
    this.http.get<any[]>('http://localhost:8080/api/branches')
      .subscribe(res => this.branches = res);
  }

  getBranchName(branchId?: number | null): string {
    if (!branchId) return 'All';
    const branch = this.branches.find(b => b.id === branchId);
    return branch ? branch.name : 'Unknown';
  }



  editUser(user: any) {
  // Navigate to the user form with user id
  this.router.navigate(['/users/edit', user.id]);
}

deleteUser(userId: number) {
  if (!confirm('Are you sure you want to delete this user?')) return;

  this.http.delete(`http://localhost:8080/api/users/${userId}`)
    .subscribe({
      next: () => {
        // Remove deleted user from local array
        this.users = this.users.filter(u => u.id !== userId);
      },
      error: err => console.error('Failed to delete user', err)
    });
}

}
