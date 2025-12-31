import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-super-dashboard',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './super-dashboard.component.html',
  styleUrl: './super-dashboard.component.css'
})
export class SuperDashboardComponent {

  users = [
    { name: 'Branch Manager 1', role: 'BRANCH_MANAGER' },
    { name: 'Branch Manager 2', role: 'BRANCH_MANAGER' }
  ];

  addUser() {
    alert('Add User clicked');
  }
}
