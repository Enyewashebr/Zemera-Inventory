export type OrderSource = 'STOCK' | 'KITCHEN';

export interface OrderItem {
  id: number;
  source: OrderSource;

  productId?: number;        // for STOCK
  productName: string;

  availableStock?: number;   // STOCK only
  unit: string;
  quantity: number;
  unitPrice: number;
}
