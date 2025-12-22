import { CommonModule } from '@angular/common';
import { Component, OnDestroy, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterOutlet } from '@angular/router';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, FormsModule, CommonModule],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css'
})
export class AppComponent implements OnInit, OnDestroy {
  title = 'Zemera Inventory';
  currentDateTime = new Date();
  timer?: ReturnType<typeof setInterval>;
  refreshTimer?: ReturnType<typeof setInterval>;

  navItems = [
    'Dashboard',
    'Products',
    'Purchases',
    'Orders',
    'Inventory',
    'Reports',
    'Settings'
  ];

  stats = [
    { label: 'Products', value: '1,240', hint: '+8 added this week' },
    { label: 'Purchases', value: '42', hint: 'Awaiting 5 confirmations' },
    { label: 'Orders', value: '68', hint: '12 tickets pending' },
    { label: 'Inventory', value: '96%', hint: 'Healthy stock coverage' },
    { label: 'Reports', value: '7', hint: 'Downloads ready today' },
    { label: 'Manager', value: 'Signed in', hint: 'Full access' }
  ];

  featureTiles = [
    {
      title: 'Product management',
      description: 'Add, edit, and categorize SKUs with pricing and tax rules.'
    },
    {
      title: 'Purchases',
      description: 'Capture supplier deliveries and match against POs.'
    },
    {
      title: 'Orders & ticket printing',
      description: 'Process sales and print customer tickets on demand.'
    },
    {
      title: 'Inventory view',
      description: 'Track on-hand, reserved, and low-stock alerts in one view.'
    },
    {
      title: 'Reports & downloads',
      description: 'Export daily summaries, stock sheets, and sales insights.'
    },
    {
      title: 'Settings (future)',
      description: 'Configure roles, locations, and automation rules.'
    }
  ];

  dashboardMetrics = [
    { id: 'sales', label: 'Total Sales (Today)', value: 18250, unit: 'USD', path: 'orders' },
    { id: 'purchases', label: 'Total Purchases (Today)', value: 9420, unit: 'USD', path: 'purchases' },
    { id: 'profit', label: 'Estimated Profit (Today)', value: 8830, unit: 'USD', path: 'reports' },
    { id: 'orders', label: 'Total Orders (Today)', value: 68, unit: 'orders', path: 'orders' }
  ];

  lowStockAlerts = [
    { sku: 'SKU-1421', name: 'Thermal Paper Rolls', level: 12, unit: 'pcs' },
    { sku: 'SKU-1033', name: 'POS Receipt Printer', level: 3, unit: 'units' },
    { sku: 'SKU-2210', name: 'Barcode Labels 4x6', level: 18, unit: 'pcs' },
    { sku: 'SKU-1899', name: 'Cash Drawer', level: 2, unit: 'units' }
  ];

  productCategories = ['POS Supplies', 'Hardware', 'Software', 'Services'];
  productUnits = ['pcs', 'units', 'box', 'kg', 'liters'];

  products = [
    {
      id: 1,
      name: 'Thermal Paper Rolls',
      category: 'POS Supplies',
      unit: 'pcs',
      buyingPrice: 0.9,
      sellingPrice: 1.8,
      stock: 120,
      createdAt: new Date()
    },
    {
      id: 2,
      name: 'Barcode Labels 4x6',
      category: 'POS Supplies',
      unit: 'pcs',
      buyingPrice: 5.25,
      sellingPrice: 8.5,
      stock: 320,
      createdAt: new Date()
    },
    {
      id: 3,
      name: 'POS Receipt Printer',
      category: 'Hardware',
      unit: 'units',
      buyingPrice: 135,
      sellingPrice: 210,
      stock: 14,
      createdAt: new Date()
    }
  ];

  productForm = {
    name: '',
    category: '',
    unit: '',
    buyingPrice: 0,
    sellingPrice: 0,
    initialStock: 0
  };

  editingProductId: number | null = null;
  formErrors: Record<string, string> = {};

  ngOnInit(): void {
    this.timer = setInterval(() => {
      this.currentDateTime = new Date();
    }, 1000);

    // Simulate live refresh when new orders or purchases arrive.
    this.refreshTimer = setInterval(() => {
      this.refreshDashboardData();
    }, 7000);
  }

  ngOnDestroy(): void {
    if (this.timer) {
      clearInterval(this.timer);
    }
    if (this.refreshTimer) {
      clearInterval(this.refreshTimer);
    }
  }

  get formattedDate(): string {
    return this.currentDateTime.toLocaleDateString('en-US', {
      weekday: 'long',
      year: 'numeric',
      month: 'short',
      day: 'numeric'
    });
  }

  get formattedTime(): string {
    return this.currentDateTime.toLocaleTimeString('en-US', {
      hour: '2-digit',
      minute: '2-digit',
      second: '2-digit'
    });
  }

  refreshDashboardData(): void {
    // Placeholder logic to simulate updated totals when new orders/purchases land.
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

  onMetricClick(path: string): void {
    // Route placeholder; swap with Router navigation when routes are ready.
    window.location.hash = path;
  }

  startEdit(productId: number): void {
    const product = this.products.find((p) => p.id === productId);
    if (!product) return;

    this.editingProductId = productId;
    this.productForm = {
      name: product.name,
      category: product.category,
      unit: product.unit,
      buyingPrice: product.buyingPrice,
      sellingPrice: product.sellingPrice,
      initialStock: product.stock
    };
    this.formErrors = {};
  }

  resetForm(): void {
    this.editingProductId = null;
    this.productForm = {
      name: '',
      category: '',
      unit: '',
      buyingPrice: 0,
      sellingPrice: 0,
      initialStock: 0
    };
    this.formErrors = {};
  }

  submitProduct(): void {
    this.formErrors = {};

    const name = this.productForm.name.trim();
    const category = this.productForm.category;
    const unit = this.productForm.unit;
    const buyingPrice = Number(this.productForm.buyingPrice);
    const sellingPrice = Number(this.productForm.sellingPrice);
    const initialStock = Number(this.productForm.initialStock);

    if (!name) {
      this.formErrors['name'] = 'Product name is required.';
    }
    if (this.isNameTaken(name, this.editingProductId)) {
      this.formErrors['name'] = 'Product name must be unique.';
    }
    if (!category) {
      this.formErrors['category'] = 'Category is required.';
    }
    if (!unit) {
      this.formErrors['unit'] = 'Unit is required.';
    }
    if (Number.isNaN(buyingPrice) || buyingPrice < 0) {
      this.formErrors['buyingPrice'] = 'Buying price must be zero or higher.';
    }
    if (Number.isNaN(sellingPrice) || sellingPrice <= 0) {
      this.formErrors['sellingPrice'] = 'Selling price must be greater than zero.';
    }
    if (this.editingProductId === null) {
      if (Number.isNaN(initialStock) || initialStock < 0) {
        this.formErrors['initialStock'] = 'Initial stock must be zero or higher.';
      }
    }

    if (Object.keys(this.formErrors).length > 0) {
      return;
    }

    if (this.editingProductId !== null) {
      this.products = this.products.map((p) =>
        p.id === this.editingProductId
          ? {
              ...p,
              name,
              category,
              sellingPrice,
              buyingPrice,
              // stock remains unchanged during edit
              unit: p.unit
            }
          : p
      );
    } else {
      const newProduct = {
        id: Date.now(),
        name,
        category,
        unit,
        buyingPrice,
        sellingPrice,
        stock: initialStock,
        createdAt: new Date()
      };
      this.products = [newProduct, ...this.products];
    }

    this.resetForm();
  }

  get editingProduct() {
    if (this.editingProductId === null) {
      return null;
    }
    return this.products.find((p) => p.id === this.editingProductId) ?? null;
  }

  private isNameTaken(name: string, ignoreId: number | null): boolean {
    const normalized = name.toLowerCase();
    return this.products.some(
      (p) => p.name.toLowerCase() === normalized && (ignoreId === null || p.id !== ignoreId)
    );
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
