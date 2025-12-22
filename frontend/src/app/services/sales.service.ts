import { Injectable } from '@angular/core';

export interface SaleRow {
  id: number;
  orderId: number;
  item: string;
  qty: number;
  unit: string;
  unitPrice: number;
  totalPrice: number;
  waiter: string;
  timestamp: Date;
}

@Injectable({
  providedIn: 'root'
})
export class SalesService {
  private sales: SaleRow[] = [];

  addSales(rows: Omit<SaleRow, 'id'>[]): void {
    const baseId = Date.now();
    rows.forEach((row, index) => {
      this.sales.push({
        ...row,
        id: baseId + index
      });
    });
  }

  getSales(): SaleRow[] {
    return this.sales.map((s) => ({ ...s }));
  }

  removeSalesByOrder(orderId: number): void {
    this.sales = this.sales.filter((s) => s.orderId !== orderId);
  }
}


