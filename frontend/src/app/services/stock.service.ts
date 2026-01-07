import { Injectable } from '@angular/core';

export interface InventoryItem {
  name: string;
  category: string;
  unit: string;
  stock: number;
}

@Injectable({
  providedIn: 'root'
})
export class StockService {
  private items: StockItem[] = [
    { name: 'Dashen Beer', category: 'Beer', unit: 'pcs', stock: 24 },
    { name: 'Harar Beer', category: 'Beer', unit: 'pcs', stock: 18 },
    { name: 'Water 0.5L', category: 'Water', unit: 'pcs', stock: 30 },
    { name: 'Water 1L', category: 'Water', unit: 'pcs', stock: 12 },
    { name: 'Areke Dagusa', category: 'Traditional Drinks', unit: 'L', stock: 5 },
    { name: 'Areke Gibto', category: 'Traditional Drinks', unit: 'L', stock: 3 },
    { name: 'Tej Glass', category: 'Traditional Drinks', unit: 'glass', stock: 40 }
  ];

  getAll(): StockItem[] {
    return this.items.map((i) => ({ ...i }));
  }

  /**
   * Decrease stock for an item; returns the updated item or undefined if not found.
   */
  decreaseStock(name: string, quantity: number): StockItem | undefined {
    const item = this.items.find((i) => i.name === name);
    if (!item) {
      return undefined;
    }
    item.stock = Math.max(0, item.stock - quantity);
    return { ...item };
  }

  /**
   * Increase stock for an item; used when undoing/cancelling an order.
   */
  increaseStock(name: string, quantity: number): StockItem | undefined {
    const item = this.items.find((i) => i.name === name);
    if (!item) {
      return undefined;
    }
    item.stock += quantity;
    return { ...item };
  }
}


