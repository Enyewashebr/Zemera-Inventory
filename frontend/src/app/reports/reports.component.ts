import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-reports',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './reports.component.html',
  styleUrl: './reports.component.css'
})
export class ReportsComponent {
  activeTab: 'sales' | 'purchases' | 'profit' | 'monthly' = 'sales';

  salesFilterDate = new Date().toISOString().substring(0, 10);
  purchaseFilterDate = new Date().toISOString().substring(0, 10);
  monthlyFilterMonth = `${new Date().getFullYear()}-${String(
    new Date().getMonth() + 1
  ).padStart(2, '0')}`;

  // Mock data
  salesRows = [
    { item: 'Thermal Paper Rolls', qty: 40, unit: 'pcs', unitPrice: 1.8, remaining: 80 },
    { item: 'Barcode Labels 4x6', qty: 10, unit: 'pcs', unitPrice: 8.5, remaining: 8 }
  ];

  purchaseRows = [
    { item: 'Thermal Paper Rolls', qty: 100, unit: 'pcs', unitPrice: 0.9 },
    { item: 'Barcode Labels 4x6', qty: 30, unit: 'pcs', unitPrice: 5.25 }
  ];

  get totalSales(): number {
    return this.salesRows.reduce((sum, r) => sum + r.qty * r.unitPrice, 0);
  }

  get totalPurchases(): number {
    return this.purchaseRows.reduce((sum, r) => sum + r.qty * r.unitPrice, 0);
  }

  get profit(): number {
    return this.totalSales - this.totalPurchases;
  }

  download(format: 'pdf' | 'xls'): void {
    // Placeholder for download behavior.
    alert(`Generating ${format.toUpperCase()} for selected period...`);
  }
}


