import { Injectable } from '@angular/core';

export interface SaleRow {
  id: number;
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
}


