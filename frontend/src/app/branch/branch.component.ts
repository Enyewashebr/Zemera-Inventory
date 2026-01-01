import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { BranchService } from '../services/branch.service';

@Component({
  selector: 'app-branch',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './branch.component.html',
  styleUrls: ['./branch.component.css']
})
export class BranchComponent {

  branches: any[] = [];

  // ✅ USED BY TEMPLATE
  newBranch = {
    name: '',
    phone: ''
  };

  // ✅ USED BY TEMPLATE
  isSaving = false;

  successMessage = '';
  errorMessage = '';

  constructor(private branchService: BranchService) {
    this.loadBranches();
  }

  loadBranches() {
    this.branchService.getAll().subscribe({
      next: (data) => this.branches = data,
      error: () => this.errorMessage = 'Failed to load branches'
    });
  }

  saveBranch() {
    if (!this.newBranch.name || !this.newBranch.phone) {
      this.errorMessage = 'All fields are required';
      return;
    }

    this.isSaving = true;

    this.branchService.create(this.newBranch).subscribe({
      next: () => {
        this.successMessage = 'Branch created successfully';
        this.errorMessage = '';
        this.isSaving = false;

        // reset form
        this.newBranch = { name: '', phone: '' };
        this.loadBranches();
      },
      error: () => {
        this.errorMessage = 'Failed to save branch';
        this.isSaving = false;
      }
    });
  }
}
