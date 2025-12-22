import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface CreateOrderItemPayload {
  productName: string;
  quantity: number;
}

export interface CreateOrderPayload {
  waiterName: string;
  items: CreateOrderItemPayload[];
}

export interface OrderTicketItem {
  productName: string;
  quantity: number;
  unit: string;
  unitPrice: number;
  lineTotal: number;
}

export interface OrderTicketResponse {
  orderId: number;
  waiterName: string;
  createdAt: string;
  currency: string; // "ETB"
  totalAmount: number;
  items: OrderTicketItem[];
}

@Injectable({
  providedIn: 'root'
})
export class OrderApiService {
  // For now hardcode; later you can move to environment.ts if you want.
  private readonly baseUrl = 'http://localhost:8080/api';

  constructor(private http: HttpClient) {}

  createOrder(payload: CreateOrderPayload): Observable<OrderTicketResponse> {
    return this.http.post<OrderTicketResponse>(`${this.baseUrl}/orders`, payload);
  }
}


