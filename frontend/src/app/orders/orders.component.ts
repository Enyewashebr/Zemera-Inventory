import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { StockService } from '../services/stock.service';
import { OrderService } from '../services/order.service';
import { Stock } from '../model/stock.model';
import { StockView } from '../model/stockView.model';
import { OrderItem, OrderSource } from '../model/order.model';

@Component({
  selector: 'app-orders',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './orders.component.html',
  styleUrl: './orders.component.css'
})
export class OrdersComponent implements OnInit {
  waiterName = '';

  stockItems: StockView[] = [];
  items: OrderItem[] = [];

  loading = false;
  error = '';
  ticket: any | null = null;

  constructor(
    private stockService: StockService,
    private orderApi: OrderService
  ) {}

  ngOnInit(): void {
    this.loadStock();
    this.addRow();
  }

  loadStock() {
    this.stockService.getMyStock().subscribe({
      next: data => (this.stockItems = data),
      error: () => (this.error = 'Failed to load stock')
    });
  }

  addRow() {
    this.items.push({
      id: Date.now(),
      source: 'STOCK',
      productName: '',
      unit: '',
      quantity: 1,
      unitPrice: 0
    });
  }

  removeRow(id: number) {
    if (this.items.length === 1) return;
    this.items = this.items.filter(i => i.id !== id);
  }

  onSourceChange(item: OrderItem) {
    item.productName = '';
    item.productId = undefined;
    item.availableStock = undefined;
    item.unit = '';
    item.unitPrice = 0;
    item.quantity = 1;
  }

  onStockProductChange(item: OrderItem) {
    const stock = this.stockItems.find(
      s => s.productName === item.productName
    );

    if (!stock) return;

    item.productId = stock.productId;
    item.availableStock = stock.quantity;
    item.unit = stock.unit;
    item.unitPrice = 0; // price comes from backend/menu later
  }

  itemTotal(item: OrderItem): number {
    return item.quantity * item.unitPrice;
  }

  get total(): number {
    return this.items.reduce((s, i) => s + this.itemTotal(i), 0);
  }

  submitOrder() {
    this.error = '';
    this.ticket = null;

    if (!this.waiterName.trim()) {
      this.error = 'Waiter name is required';
      return;
    }

    for (const i of this.items) {
      if (!i.productName) {
        this.error = 'All rows must have a product';
        return;
      }

      if (i.quantity <= 0) {
        this.error = 'Quantity must be greater than zero';
        return;
      }

      if (
        i.source === 'STOCK' &&
        i.availableStock !== undefined &&
        i.quantity > i.availableStock
      ) {
        this.error = `Insufficient stock for ${i.productName}`;
        return;
      }
    }

    this.loading = true;

    this.orderApi.createOrder({
  waiterName: this.waiterName,
  items: this.items.map(i => ({
    productId: i.productId,
    productName: i.productName,
    quantity: i.quantity,
    unit: i.unit,
    unitPrice: i.unitPrice
  }))
})
.subscribe({
      next: (res: any) => {
        this.ticket = res;
        this.loading = false;
        this.loadStock(); // refresh after deduction
        this.reset();
      },
      error: (err: any) => {
        this.error = err.error?.message || 'Order failed';
        this.loading = false;
      }
    });
  }
  reset() {
    this.items = [];
    this.addRow();
  }
  confirmPrint() {
  window.print();
  this.ticket = null; // close modal after printing
}

cancelPrint() {
  this.ticket = null; // close modal without printing
}
  
}
