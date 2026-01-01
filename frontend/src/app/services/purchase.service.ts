import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface Purchase {
  id?: number;
  productId: number;
  productName?: string;
  quantity: number;
  unitPrice: number;
  totalCost: number;
  date: string;
  branchId: number;

  status?: 'Pending' | 'Approved' | 'Rejected';
  approvedBy?: string;
  approvedAt?: string;
}

@Injectable({
  providedIn: 'root'
})
export class PurchaseService {
  private baseUrl = 'http://localhost:8080/api/purchase';

  constructor(private http: HttpClient) {}

  create(purchase: Purchase): Observable<Purchase> {
    return this.http.post<Purchase>(this.baseUrl, purchase);
  }

  getByBranch(branchId: number): Observable<Purchase[]> {
    return this.http.get<Purchase[]>(`${this.baseUrl}?branchId=${branchId}`);
  }
}
