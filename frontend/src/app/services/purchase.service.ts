import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

/* ======================
   PURCHASE MODEL
====================== */
export interface Purchase {
  id?: number;

  productId: number;
  productName?: string;

  quantity: number;
  unitPrice: number;
  totalCost: number;

  purchaseDate: string;        // ✅ renamed
  branchId?: number;           // ✅ optional (from token)

  status?: 'PENDING' | 'APPROVED' | 'DECLINED';  // ✅ fixed
  approvedBy?: string;
  approvedAt?: string;
}

/* ======================
   SERVICE
====================== */
@Injectable({
  providedIn: 'root'
})
export class PurchaseService {

  private baseUrl = 'http://localhost:8080/api/purchase';

  constructor(private http: HttpClient) {}

  /* ======================
     BRANCH MANAGER
  ====================== */

  create(purchase: Purchase): Observable<Purchase> {
    return this.http.post<Purchase>(
      `${this.baseUrl}/create`,
      purchase
    );
  }

  getByBranch(branchId: number): Observable<Purchase[]> {
    return this.http.get<Purchase[]>(
      `${this.baseUrl}/branch/${branchId}`
    );
  }

  getAll(): Observable<Purchase[]> {
    return this.http.get<Purchase[]>(
      `${this.baseUrl}/getAll`
    );
  }

  /* ======================
     SUPER MANAGER
  ====================== */

  approve(purchaseId: number): Observable<void> {
    return this.http.put<void>(
      `${this.baseUrl}/${purchaseId}/approve`,
      {}
    );
  }

  decline(purchaseId: number, comment?: string): Observable<void> {
    return this.http.put<void>(
      `${this.baseUrl}/${purchaseId}/decline`,
      { comment }
    );
  }
}
