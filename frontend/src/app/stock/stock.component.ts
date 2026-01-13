import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { StockService } from '../services/stock.service';
import { AuthService } from '../services/auth.service';
import { BranchService } from '../services/branch.service';
import { StockView } from '../model/stockView.model';
import { Branch } from '../model/branch.model';

@Component({
  selector: 'app-stock',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './stock.component.html',
  styleUrl: './stock.component.css'
})
export class StockComponent implements OnInit {

  stocks: StockView[] = [];
  branches: Branch[] = [];

  selectedBranchId?: number;
  isBranchManager = false;
  isSuperManager = false;
  loading = false;
  error = '';

  constructor(
    private stockService: StockService,
    private authService: AuthService,
    private branchService: BranchService
  ) {}

  ngOnInit(): void {
    const role = this.authService.getUserRole();
    this.isBranchManager = role === 'BRANCH_MANAGER';
    this.isSuperManager = role === 'SUPER_MANAGER';

    if (this.isBranchManager) this.loadMyStock();
    if (this.isSuperManager) this.loadBranches();
  }

  loadMyStock() {
    this.loading = true;
    this.stockService.getMyStock().subscribe({
      next: data => {
        this.stocks = data;
        this.loading = false;
      },
      error: err => {
        this.loading = false;
        this.error = 'Failed to load stock';
        if (err.status === 401) this.authService.logout();
      }
    });
  }

  loadBranches() {
    this.branchService.getAllBranches()
      .subscribe(data => this.branches = data);
  }

  onBranchChange() {
    if (!this.selectedBranchId) return;

    this.loading = true;
    this.stockService.getByBranch(this.selectedBranchId).subscribe({
      next: data => {
        this.stocks = data;
        this.loading = false;
      },
      error: () => {
        this.loading = false;
        this.error = 'Failed to load branch stock';
      }
    });
  }

  stockStatus(qty: number): 'ok' | 'low' | 'out' {
    if (qty <= 0) return 'out';
    if (qty < 10) return 'low';
    return 'ok';
  }
}
