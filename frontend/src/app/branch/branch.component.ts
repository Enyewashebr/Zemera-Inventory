import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { BranchService } from '../services/branch.service';
import { HttpClient, HttpClientModule } from '@angular/common/http';

@Component({
  selector: 'app-branch',
  standalone: true,
  imports: [CommonModule, FormsModule, HttpClientModule],
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
  editingBranchId: number | null = null;
  branch = {
    name: '',
    phone: ''
  };

 
  // ✅ USED BY TEMPLATE
  isSaving = false;

  successMessage = '';
  errorMessage = '';

  constructor(private branchService: BranchService, private http: HttpClient) {
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



  editBranch(id: number) {
    const b = this.branches.find(x => x.id === id);
    if (!b) return;

    this.editingBranchId = id;
    this.branch = {
      name: b.name,
      phone: b.phone
    };
  }

  cancelEdit() {
    this.editingBranchId = null;
    this.branch = {
      name: '',
      phone: ''
     
    };
  }
   save() {
    if (this.editingBranchId === null) return;

    // Map form to backend payload
    const payload = {
      name: this.branch.name,
      phone: this.branch.phone
    };

    this.http.put(`https://zemera-inventory-1.onrender.com/api/branches/${this.editingBranchId}`, payload)
      .subscribe({
        next: () => {
          this.loadBranches();
          this.cancelEdit();
        },
        error: err => console.error('Update failed', err)
      });
  }

  deleteBranch(id: number) {
    if (!confirm('Delete this user?')) return;

    this.http.delete(`https://zemera-inventory-1.onrender.com/api/branches/${id}`)
      .subscribe({
        next: () => this.branches = this.branches.filter(b => b.id !== id),
        error: err => console.error('Delete failed', err)
      });
  }

}
