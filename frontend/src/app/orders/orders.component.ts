import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { InventoryService } from '../services/inventory.service';
import { SalesService } from '../services/sales.service';

interface OrderItem {
  id: number;
  product: string;
  availableStock: number;
  unit: string;
  quantity: number;
  unitPrice: number;
}

interface FinalizedOrder {
  id: number;
  waiter: string;
  createdAt: Date;
  items: OrderItem[];
  total: number;
}

@Component({
  selector: 'app-orders',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './orders.component.html',
  styleUrl: './orders.component.css'
})
export class OrdersComponent {
  products = [
    { name: 'Dashen Beer', category: 'Beer', unit: 'pcs', price: 60, stock: 24 },
    { name: 'Harar Beer', category: 'Beer', unit: 'pcs', price: 55, stock: 18 },
    { name: 'Water 0.5L', category: 'Water', unit: 'pcs', price: 20, stock: 30 },
    { name: 'Water 1L', category: 'Water', unit: 'pcs', price: 25, stock: 12 },
    { name: 'Areke Dagusa', category: 'Traditional Drinks', unit: 'shot', price: 30, stock: 50 },
    { name: 'Tej Glass', category: 'Traditional Drinks', unit: 'glass', price: 40, stock: 40 },
    { name: 'Tibs (kg)', category: 'Foods for Sale', unit: 'kg', price: 1200, stock: 6.5 },
    { name: 'Shiro (plate)', category: 'Foods for Sale', unit: 'plate', price: 80, stock: 20 }
  ];

  header = {
    waiterName: '',
    createdAt: new Date()
  };

  items: OrderItem[] = [
    {
      id: 1,
      product: '',
      availableStock: 0,
      unit: '',
      quantity: 1,
      unitPrice: 0
    }
  ];
  private nextItemId = 2;

  formErrors: Record<string, string> = {};
  orderError = '';
  lastOrder: FinalizedOrder | null = null;
  successMessage = '';

  constructor(
    private inventoryService: InventoryService,
    private salesService: SalesService
  ) {}

  get subtotal(): number {
    return this.items.reduce((sum, item) => sum + this.itemTotal(item), 0);
  }

  get totalItems(): number {
    return this.items.reduce((sum, item) => sum + (item.quantity || 0), 0);
  }

  get totalAmount(): number {
    return this.subtotal;
  }

  onProductChange(item: OrderItem): void {
    const product = this.products.find((p) => p.name === item.product);
    if (!product) {
      item.availableStock = 0;
      item.unit = '';
      item.unitPrice = 0;
      return;
    }
    item.availableStock = product.stock;
    item.unit = product.unit;
    item.unitPrice = product.price;
    if (!item.quantity || item.quantity <= 0) {
      item.quantity = 1;
    }
  }

  itemTotal(item: OrderItem): number {
    return (item.quantity || 0) * (item.unitPrice || 0);
  }

  addRow(): void {
    this.items = [
      ...this.items,
      {
        id: this.nextItemId++,
        product: '',
        availableStock: 0,
        unit: '',
        quantity: 1,
        unitPrice: 0
      }
    ];
  }

  removeRow(id: number): void {
    if (this.items.length === 1) return;
    this.items = this.items.filter((i) => i.id !== id);
  }

  createOrder(): void {
    this.formErrors = {};
    this.orderError = '';
    this.successMessage = '';

    if (!this.header.waiterName.trim()) {
      this.formErrors['waiterName'] = 'Waiter name is required.';
    }

    if (this.items.length === 0) {
      this.orderError = 'At least one item is required.';
    }

    for (const item of this.items) {
      if (!item.product) {
        this.orderError = 'Each row must have a product.';
        break;
      }
      if (!item.quantity || item.quantity <= 0) {
        this.orderError = 'Quantity must be greater than zero.';
        break;
      }
      if (item.quantity > item.availableStock) {
        this.orderError = `Quantity for ${item.product} cannot exceed available stock (${item.availableStock}).`;
        break;
      }
    }

    if (this.totalAmount <= 0) {
      this.orderError = 'Total amount must be greater than zero.';
    }

    if (this.formErrors['waiterName'] || this.orderError) {
      return;
    }

    // Deduct stock using shared InventoryService and capture sales rows.
    const now = new Date();
    const salesRows = this.items.map((item) => {
      this.inventoryService.decreaseStock(item.product, item.quantity);

      return {
        item: item.product,
        qty: item.quantity,
        unit: item.unit,
        unitPrice: item.unitPrice,
        totalPrice: this.itemTotal(item),
        waiter: this.header.waiterName.trim(),
        timestamp: now
      };
    });

    this.salesService.addSales(salesRows);

    const finalized: FinalizedOrder = {
      id: Date.now(),
      waiter: this.header.waiterName.trim(),
      createdAt: now,
      items: this.items.map((i) => ({ ...i })),
      total: this.totalAmount
    };

    this.lastOrder = finalized;
    this.successMessage = 'Order created and stock updated.';

    this.resetItems();
  }

  printTicket(): void {
    // Simple print trigger; in a real app this would open a dedicated ticket view.
    window.print();
  }

  cancelOrder(): void {
    this.resetItems();
    this.header.waiterName = '';
    this.successMessage = '';
    this.orderError = '';
  }

  private resetItems(): void {
    this.items = [
      {
        id: 1,
        product: '',
        availableStock: 0,
        unit: '',
        quantity: 1,
        unitPrice: 0
      }
    ];
    this.nextItemId = 2;
  }
}


