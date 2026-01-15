import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { SalesService } from '../services/sales.service';
import { AuthService } from '../services/auth.service';
import { HttpClient, HttpClientModule } from '@angular/common/http';


  export interface Report {
  // Common fields
  item?: string;        // Item name
  qty?: number;         // Quantity sold/purchased
  unit?: string;        // Unit of measurement
  unitPrice?: number;   // Price per unit
  totalPrice?: number;  // Total price (qty * unitPrice)
  waiter?: string;      // Only for sales
  timestamp?: string;   // Date/time of sale or purchase

  // Optional aggregate/profit fields
  sales?: number;       // Total sales (for profit report)
  purchases?: number;   // Total purchases (for profit report)
  profit?: number;      // Profit (for profit report)
}
interface Branch {
  id: number;
  name: string;
}

@Component({
  selector: 'app-reports',
  standalone: true,
  imports: [CommonModule, FormsModule, HttpClientModule],
  templateUrl: './reports.component.html',
  styleUrls: ['./reports.component.css']
})
export class ReportsComponent implements OnInit {

  // =====================
  // FILTER STATE
  // =====================
  selectedTime: 'daily' | 'monthly' | 'yearly' = 'daily';
  selectedReport: 'sales' | 'purchases' | 'profit' = 'sales';

  filterDate = new Date().toISOString().substring(0, 10);
  filterMonth = new Date().toISOString().substring(0, 7);
  filterYear = new Date().getFullYear();

  // Branch dropdown for super manager
  branches: Branch[] = [];
  selectedBranchId?: number;
  reports: Report[] = [];
  isSuperManager = false;
  salesRows: any[] = [];
  purchaseRows: any[] = [];
  totalSales = 0;
  totalPurchases = 0;
  profit = 0;
  loading = false;
  error = '';
  constructor(
    private reportService: SalesService,
    private authService: AuthService,
    private http:HttpClient
  ) {}

  ngOnInit(): void {
    const user = this.authService.getUser();
  this.isSuperManager = user?.role === 'SUPER_MANAGER';

  if (this.isSuperManager) {
    // Super manager selects branch
    this.loadBranches();
  } else {
    // Branch manager fixed to their branch
    this.selectedBranchId = user?.branchId;
  }

    this.loadReport();
  }

  // =====================
  // LOAD BRANCHES
  // =====================
  loadBranches() {
    this.http.get<Branch[]>('http://localhost:8080/api/branches')
      .subscribe(data => this.branches = data);
  }

  // =====================
  // LOAD REPORT
  // =====================
 loadReport(): void {
  this.loading = true;
  this.error = '';

  // Ensure value is always a valid date string
  const value =
    this.selectedTime === 'daily'
      ? this.filterDate
      : this.selectedTime === 'monthly'
        ? this.filterMonth + '-01'
        : this.filterYear.toString() + '-01-01';

  this.reportService
    .getReport(this.selectedTime, this.selectedReport, value, this.selectedBranchId)
    .subscribe({
      next: res => {
        this.loading = false;
        this.salesRows = [];
        this.purchaseRows = [];

        if (this.selectedReport === 'sales') {
          this.salesRows = res.rows || [];
          this.totalSales = this.salesRows.reduce((sum, r) => sum + (r.totalPrice || 0), 0);
        }

        if (this.selectedReport === 'purchases') {
          this.purchaseRows = res.rows || [];
          this.totalPurchases = this.purchaseRows.reduce(
            (sum, r) => sum + ((r.qty || 0) * (r.unitPrice || 0)),
            0
          );
        }

        if (this.selectedReport === 'profit') {
          this.totalSales = res.sales || 0;
          this.totalPurchases = res.purchases || 0;
          this.profit = res.profit || 0;
        }
      },
      error: err => {
        this.loading = false;
        this.error = 'Failed to load report';
        if (err.status === 401) this.authService.logout();
      }
    });
}


 


  // =====================
  // DOWNLOAD
  // =====================
  download(type: 'pdf' | 'xls'): void {
    alert(`Download ${type.toUpperCase()} report`);
  }
}
