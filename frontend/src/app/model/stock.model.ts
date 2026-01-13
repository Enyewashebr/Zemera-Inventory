export interface Stock {
  id: number;

  branchId: number;
  branchName: string;

  productId: number;
  productName: string;
  unit: string;

  quantity: number;
  updatedAt: string;
}


export interface StockView {
  stockId: number;
   productId: number;
  productName: string;
  category: string;
  subcategory: string;
  unit: string;
  quantity: number;
  lastUpdated: string;
}
