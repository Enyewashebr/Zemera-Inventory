import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { SalesService } from '../services/sales.service';
import { AuthService } from '../services/auth.service';
import { HttpClient, HttpClientModule } from '@angular/common/http';
import * as XLSX from 'xlsx';
import { saveAs } from 'file-saver';
import jsPDF from 'jspdf';

//import jsPDF from 'jspdf';
import autoTable from 'jspdf-autotable';

export interface Report {
  item?: string;
  qty?: number;
  unit?: string;
  unitPrice?: number;
  totalPrice?: number;
  waiter?: string;
  timestamp?: string;
  sales?: number;
  purchases?: number;
  profit?: number;
}
interface PurchaseRow {
  item?: string;
  quantity: number;
  unitPrice: number;
  totalCost: number;
}


interface SalesRow {
  item: string;
  qty: number;
  unit: string;
  unitPrice: number;
  totalPrice: number;
  waiter: string;
  timestamp: string;
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
  

  selectedTime: 'daily' | 'monthly' | 'yearly' = 'daily';
  selectedReport: 'sales' | 'purchases' | 'profit' = 'sales';
  filterDate = new Date().toISOString().substring(0, 10);
  filterMonth = new Date().toISOString().substring(0, 7);
  filterYear = new Date().getFullYear();

  salesRows: SalesRow[] = [];
purchaseRows: PurchaseRow[] = [];

  branches: Branch[] = [];
  selectedBranchId?: number;
  isSuperManager = false;

  
  totalSales = 0;
  totalPurchases = 0;
  profit = 0;

  loading = false;
  error = '';

  constructor(
    private reportService: SalesService,
    private authService: AuthService,
    private http: HttpClient
  ) {}

  ngOnInit(): void {
    const user = this.authService.getUser();
    this.isSuperManager = user?.role === 'SUPER_MANAGER';

    if (this.isSuperManager) this.loadBranches();
    else this.selectedBranchId = user?.branchId;

    this.loadReport();
  }

  loadBranches() {
    this.http.get<Branch[]>('http://localhost:8080/api/branches')
      .subscribe(data => this.branches = data);
  }

  loadReport(): void {
    this.loading = true;
    this.error = '';

    const value =
      this.selectedTime === 'daily' ? this.filterDate :
      this.selectedTime === 'monthly' ? this.filterMonth + '-01' :
      this.filterYear.toString() + '-01-01';

    // Call the appropriate API based on selectedReport
    let request$;

    if (this.selectedReport === 'sales') {
      request$ = this.reportService.getSales(this.selectedTime, value, this.selectedBranchId);
    } else if (this.selectedReport === 'purchases') {
      request$ = this.reportService.getPurchases(this.selectedTime, value, this.selectedBranchId);
    } else {
      request$ = this.reportService.getProfit(this.selectedTime, value, this.selectedBranchId);
    }

    request$.subscribe({
      next: res => {
        this.loading = false;
        this.salesRows = [];
        this.purchaseRows = [];
        this.totalSales = 0;
        this.totalPurchases = 0;
        this.profit = 0;

        if (this.selectedReport === 'sales') {
          this.salesRows = res.rows || [];
          this.totalSales = res.totalSales || 0;
        } else if (this.selectedReport === 'purchases') {
          this.purchaseRows = res.rows || [];
          this.totalPurchases = res.totalPurchases || 0;
        } else if (this.selectedReport === 'profit') {
          this.totalSales = res.totalSales;
this.totalPurchases = res.totalPurchases;
this.profit = res.profit;

        }
      },
      error: err => {
        this.loading = false;
        this.error = 'Failed to load report';
        if (err.status === 401) this.authService.logout();
      }
    });
  }

 download(type: 'pdf' | 'xls'): void {
  if (this.selectedReport === 'sales') {
    if (type === 'xls') this.exportSalesToExcel();
    else this.exportSalesToPDF();
  } else if (this.selectedReport === 'purchases') {
    if (type === 'xls') this.exportPurchaseToExcel();
    else this.exportPurchaseToPDF();
  } else {
    if (type === 'xls') this.exportProfitToExcel();
    else this.exportProfitToPDF();
  }
}

/* ------------------ SALES EXPORT ------------------ */
exportSalesToExcel() {
  const ws = XLSX.utils.json_to_sheet(this.salesRows);
  const wb = XLSX.utils.book_new();
  XLSX.utils.book_append_sheet(wb, ws, "Sales");
  const fileName = `Sales_Report_${this.selectedTime}_${new Date().getTime()}.xlsx`;
  XLSX.writeFile(wb, fileName);
}

exportSalesToPDF() {
  const doc = new jsPDF();

  autoTable(doc, {
    head: [[
      'Item',
      'Qty',
      'Unit',
      'Unit Price',
      'Total Price',
      'Waiter',
      'Timestamp'
    ]],
   body: this.salesRows.map(r => [
      r.item ?? '',
      r.qty ?? 0,
      r.unit ?? '',
      r.unitPrice ?? 0,
      r.totalPrice ?? 0,
      r.waiter ?? '',
      r.timestamp ? new Date(r.timestamp).toLocaleString() : ''
    ]),
  
  });

  doc.save(`Sales_Report_${this.selectedTime}.pdf`);
}


/* ------------------ PURCHASE EXPORT ------------------ */
exportPurchaseToExcel() {
  const ws = XLSX.utils.json_to_sheet(this.purchaseRows);
  const wb = XLSX.utils.book_new();
  XLSX.utils.book_append_sheet(wb, ws, "Purchases");
  XLSX.writeFile(wb, `Purchases_Report_${this.selectedTime}.xlsx`);
}

exportPurchaseToPDF() {
  const doc = new jsPDF();

  autoTable(doc, {
    head: [['Item', 'Quantity', 'Unit Price', 'Total Cost']],
     body: this.purchaseRows.map(r => [
      r.item ?? '',
      r.quantity ?? 0,
      r.unitPrice ?? 0,
      r.totalCost ?? 0
    ]),
  });

  doc.save(`Purchase_Report_${this.selectedTime}.pdf`);
}



/* ------------------ PROFIT EXPORT ------------------ */
exportProfitToExcel() {
  const data = [
    { Metric: "Total Sales", Value: this.totalSales },
    { Metric: "Total Purchases", Value: this.totalPurchases },
    { Metric: "Profit", Value: this.profit },
  ];

  const ws = XLSX.utils.json_to_sheet(data);
  const wb = XLSX.utils.book_new();
  XLSX.utils.book_append_sheet(wb, ws, "Profit");
  XLSX.writeFile(wb, `Profit_Report_${this.selectedTime}.xlsx`);
}

exportProfitToPDF() {
  const doc = new jsPDF();

  autoTable(doc, {
    head: [['Metric', 'Value']],
    body: [
      ['Total Sales', this.totalSales ?? 0],
      ['Total Purchases', this.totalPurchases ?? 0],
      ['Profit', this.profit ?? 0],
    ],
  });

  doc.save(`Profit_Report_${this.selectedTime}.pdf`);
}


}
