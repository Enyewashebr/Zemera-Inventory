import { CommonModule } from '@angular/common';
import { Component, OnDestroy, OnInit } from '@angular/core';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.css'
})
export class DashboardComponent implements OnInit, OnDestroy {
  stats = [
    { label: 'Products', value: '1,240', hint: '+8 added this week' },
    { label: 'Purchases', value: '42', hint: 'Awaiting 5 confirmations' },
    { label: 'Orders', value: '68', hint: '12 tickets pending' },
    { label: 'Inventory', value: '96%', hint: 'Healthy stock coverage' },
    { label: 'Reports', value: '7', hint: 'Downloads ready today' },
    { label: 'Manager', value: 'Signed in', hint: 'Full access' }
  ];

  dashboardMetrics = [
    { id: 'sales', label: 'Total Sales (Today)', value: 18250, unit: 'ETB', path: '/orders' },
    { id: 'purchases', label: 'Total Purchases (Today)', value: 9420, unit: 'ETB', path: '/purchases' },
    { id: 'profit', label: 'Estimated Profit (Today)', value: 8830, unit: 'ETB', path: '/reports' },
    { id: 'orders', label: 'Total Orders (Today)', value: 68, unit: 'orders', path: '/orders' }
  ];

  lowStockAlerts = [
    { sku: 'SKU-1421', name: 'Thermal Paper Rolls', level: 12, unit: 'pcs' },
    { sku: 'SKU-1033', name: 'POS Receipt Printer', level: 3, unit: 'units' },
    { sku: 'SKU-2210', name: 'Barcode Labels 4x6', level: 18, unit: 'pcs' },
    { sku: 'SKU-1899', name: 'Cash Drawer', level: 2, unit: 'units' }
  ];

  private refreshTimer?: ReturnType<typeof setInterval>;

  ngOnInit(): void {
    this.refreshTimer = setInterval(() => this.refreshDashboardData(), 7000);
  }

  ngOnDestroy(): void {
    if (this.refreshTimer) {
      clearInterval(this.refreshTimer);
    }
  }

  refreshDashboardData(): void {
    this.dashboardMetrics = this.dashboardMetrics.map((metric) => {
      if (metric.id === 'sales') {
        return { ...metric, value: metric.value + this.randomDelta(150, 400) };
      }
      if (metric.id === 'purchases') {
        return { ...metric, value: metric.value + this.randomDelta(80, 220) };
      }
      if (metric.id === 'profit') {
        return { ...metric, value: this.calculateProfit() };
      }
      if (metric.id === 'orders') {
        return { ...metric, value: metric.value + this.randomDelta(1, 4) };
      }
      return metric;
    });
  }

  private calculateProfit(): number {
    const sales = this.dashboardMetrics.find((m) => m.id === 'sales')?.value ?? 0;
    const purchases = this.dashboardMetrics.find((m) => m.id === 'purchases')?.value ?? 0;
    const margin = 0.38;
    return Math.max(0, Math.round((sales - purchases) * margin));
  }

  private randomDelta(min: number, max: number): number {
    return Math.floor(Math.random() * (max - min + 1)) + min;
  }
}





